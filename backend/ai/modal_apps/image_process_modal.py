import base64
from io import BytesIO

import modal

app = modal.App("ozz-image-process")

image = (
    modal.Image.debian_slim(python_version="3.10")
    .pip_install(
        "pillow~=10.4.0",
        "requests~=2.32.0",
        "numpy~=1.26.4",
        "rembg~=2.0.57",
        "onnxruntime-gpu~=1.18.1",
        "torch==2.4.0",
        "torchvision==0.19.0",
        "transformers==4.43.3",
    )
)

category_name_to_label = {
    "상의": [1, 2, 3, 28, 29, 30, 31, 32, 34, 36],
    "하의": [7, 8, 9, 11, 20, 21, 22, 33],
    "아우터": [4, 5, 6, 10, 1, 2, 3, 28, 29, 30, 31, 32, 34, 36],
    "원피스": [11, 12, 13, 29, 30, 31, 32, 34],
    "신발": [23, 24],
    "가방": [25],
    "악세서리": {
        "모자": [15],
        "주얼리": [],
        "기타": [14, 19, 20, 23],
    },
}

clothes_category_name_to_label = {
    "신발": [9, 10],
    "가방": [16],
}

models = {}


def _image_from_payload(payload: dict):
    from PIL import Image
    import requests

    if payload.get("image"):
        image_bytes = base64.b64decode(payload["image"])
    elif payload.get("imageUrl"):
        response = requests.get(payload["imageUrl"], timeout=15)
        response.raise_for_status()
        image_bytes = response.content
    else:
        raise ValueError("image or imageUrl is required")

    return Image.open(BytesIO(image_bytes)).convert("RGB")


def _image_to_base64(processed_image) -> str:
    output = BytesIO()
    processed_image.save(output, format="PNG")
    return base64.b64encode(output.getvalue()).decode("utf-8")


def _load_models():
    if models:
        return models

    import torch
    from transformers import AutoModelForSemanticSegmentation, SegformerImageProcessor

    device = "cuda" if torch.cuda.is_available() else "cpu"

    models["fashion_processor"] = SegformerImageProcessor.from_pretrained(
        "sayeed99/segformer-b2-fashion"
    )
    models["fashion_model"] = AutoModelForSemanticSegmentation.from_pretrained(
        "sayeed99/segformer-b2-fashion"
    ).to(device).eval()
    models["clothes_processor"] = SegformerImageProcessor.from_pretrained(
        "sayeed99/segformer_b3_clothes"
    )
    models["clothes_model"] = AutoModelForSemanticSegmentation.from_pretrained(
        "sayeed99/segformer_b3_clothes"
    ).to(device).eval()
    models["device"] = device

    return models


def _target_classes(mapping, high_category: str, low_category: str | None):
    target_classes = mapping[high_category]

    if isinstance(target_classes, dict):
        if low_category not in target_classes:
            raise ValueError(f"Low category {low_category} is not supported")
        target_classes = target_classes[low_category]

    return target_classes


def _segment_image(source_image, processor, model, target_classes: list[int], device: str):
    import numpy as np
    import torch
    import torch.nn as nn

    inputs = processor(images=source_image, return_tensors="pt")
    inputs = {key: value.to(device) for key, value in inputs.items()}

    with torch.no_grad():
        outputs = model(**inputs)

    logits = outputs.logits.cpu()
    upsampled_logits = nn.functional.interpolate(
        logits,
        size=source_image.size[::-1],
        mode="bilinear",
        align_corners=False,
    )
    pred_seg = upsampled_logits.argmax(dim=1)[0].numpy()

    return np.isin(pred_seg, target_classes).astype(np.uint8)


def _apply_mask(source_image, mask):
    import numpy as np
    from PIL import Image

    image_np = np.array(source_image)
    alpha_channel = mask * 255
    image_rgba = np.dstack((image_np, alpha_channel))

    return Image.fromarray(image_rgba)


def _crop_to_object(source_image, mask):
    import numpy as np

    coords = np.column_stack(np.where(mask > 0))

    if coords.size == 0:
        return source_image

    top_left = coords.min(axis=0)
    bottom_right = coords.max(axis=0)

    return source_image.crop((*top_left[::-1], *bottom_right[::-1]))


def _remove_background(source_image):
    from rembg import remove

    return remove(source_image)


def _extract_objects(
    source_image,
    processor,
    model,
    mapping,
    high_category: str,
    low_category: str | None = None,
    device: str = "cpu",
):
    target_classes = _target_classes(mapping, high_category, low_category)
    mask = _segment_image(source_image, processor, model, target_classes, device)

    if mask.sum() == 0:
        return None

    transparent_image = _apply_mask(source_image, mask)

    return _crop_to_object(transparent_image, mask)


@app.function(
    image=image,
    gpu="T4",
    scaledown_window=600,
    timeout=120,
)
def process_image(payload: dict) -> dict:
    loaded_models = _load_models()
    source_image = _image_from_payload(payload)
    high_category = payload["highCategory"]
    low_category = payload.get("lowCategory")
    is_only_item = payload.get("isOnlyItem", False)

    if is_only_item:
        processed_image = _remove_background(source_image)
    elif high_category == "악세서리":
        processed_image = _extract_objects(
            source_image,
            loaded_models["fashion_processor"],
            loaded_models["fashion_model"],
            category_name_to_label,
            "악세서리",
            low_category,
            loaded_models["device"],
        )
    elif high_category in ["신발", "가방"]:
        processed_image = _extract_objects(
            source_image,
            loaded_models["clothes_processor"],
            loaded_models["clothes_model"],
            clothes_category_name_to_label,
            high_category,
            None,
            loaded_models["device"],
        )
    else:
        processed_image = _extract_objects(
            source_image,
            loaded_models["fashion_processor"],
            loaded_models["fashion_model"],
            category_name_to_label,
            high_category,
            None,
            loaded_models["device"],
        )

    if processed_image is None:
        processed_image = _remove_background(source_image)

    return {"image": _image_to_base64(processed_image)}
