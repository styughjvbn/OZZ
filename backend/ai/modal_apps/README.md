# OZZ Modal Image Processing

이미지 세그멘테이션 모델은 mini-pc에서 실행하지 않고 Modal GPU 함수로 실행합니다.

## Deploy

```bash
cd backend/ai
pip install modal
modal setup
modal deploy modal_apps/image_process_modal.py
```

## Runtime env

`ozz-ai` 컨테이너에는 다음 값을 주입합니다.

```bash
IMAGE_PROCESS_MODE=modal
MODAL_IMAGE_PROCESS_APP=ozz-image-process
MODAL_IMAGE_PROCESS_FUNCTION=process_image
MODAL_TOKEN_ID=...
MODAL_TOKEN_SECRET=...
```

Modal 호출에 실패하면 `ozz-ai`는 원본 이미지를 사용합니다.
