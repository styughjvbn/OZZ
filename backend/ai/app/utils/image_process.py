import base64
import logging
import os
from io import BytesIO

from PIL import Image

from app.schemas.attributes import ImageMetadata
from app.utils.image_utils import download_img

MODAL_APP_NAME = os.getenv("MODAL_IMAGE_PROCESS_APP", "ozz-image-process")
MODAL_FUNCTION_NAME = os.getenv("MODAL_IMAGE_PROCESS_FUNCTION", "process_image")
IMAGE_PROCESS_MODE = os.getenv("IMAGE_PROCESS_MODE", "modal").lower()


def _image_to_base64(image: Image.Image) -> str:
    output = BytesIO()
    image.save(output, format="PNG")
    return base64.b64encode(output.getvalue()).decode("utf-8")


def _base64_to_image(encoded_image: str) -> Image.Image:
    return Image.open(BytesIO(base64.b64decode(encoded_image))).convert("RGBA")


def _call_modal(payload: dict) -> Image.Image:
    try:
        import modal
    except ImportError as exc:
        raise RuntimeError("modal package is not installed") from exc

    fn = modal.Function.from_name(MODAL_APP_NAME, MODAL_FUNCTION_NAME)
    result = fn.remote(payload)

    if not isinstance(result, dict) or "image" not in result:
        raise RuntimeError("invalid Modal image process response")

    return _base64_to_image(result["image"])


def process(
    image: Image.Image,
    high_category: str,
    low_category: str = None,
    basic_remove_bg: bool = False,
) -> Image.Image:
    if IMAGE_PROCESS_MODE != "modal":
        logging.info("이미지 처리 Modal 비활성화: 원본 이미지 사용")
        return image

    payload = {
        "image": _image_to_base64(image),
        "highCategory": high_category,
        "lowCategory": low_category,
        "isOnlyItem": basic_remove_bg,
    }

    try:
        logging.info("Modal 이미지 처리 요청")
        return _call_modal(payload)
    except Exception:
        logging.exception("Modal 이미지 처리 실패: 원본 이미지 사용")
        return image


def process_url(image_metadata: ImageMetadata) -> Image.Image:
    from app.core.client.clothesMetadata import clothesMetadata

    high_category, low_category = clothesMetadata.low_categoryId_to_low_high_response(
        image_metadata.categoryLowId
    )

    if IMAGE_PROCESS_MODE == "modal":
        payload = {
            "imageUrl": image_metadata.imgUrl,
            "highCategory": high_category.name,
            "lowCategory": low_category.name,
            "isOnlyItem": image_metadata.isOnlyItem,
        }

        try:
            logging.info("Modal URL 이미지 처리 요청")
            return _call_modal(payload)
        except Exception:
            logging.exception("Modal URL 이미지 처리 실패: 원본 이미지 사용")

    return download_img(image_metadata.imgUrl)
