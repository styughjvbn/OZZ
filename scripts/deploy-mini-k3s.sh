#!/usr/bin/env bash
set -euo pipefail

TAG="${TAG:-dev}"
MINI_PC="${MINI_PC:-mini-pc}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
IMAGES=(
  ozz/eureka
  ozz/gateway
  ozz/auth
  ozz/user
  ozz/clothes
  ozz/favorite
  ozz/board
  ozz/file-server
  ozz/ai
  ozz/frontend
)

"${ROOT_DIR}/scripts/build-images.sh" "${TAG}"

ARCHIVE="$(mktemp -t ozz-images.XXXXXX.tar)"
trap 'rm -f "${ARCHIVE}"' EXIT

docker save -o "${ARCHIVE}" "${IMAGES[@]/%/:${TAG}}"
scp "${ARCHIVE}" "${MINI_PC}:/tmp/ozz-images.tar"
ssh "${MINI_PC}" "sudo k3s ctr images import /tmp/ozz-images.tar && rm /tmp/ozz-images.tar"

kubectl apply -k "${ROOT_DIR}/k8s/base"
kubectl -n ozz rollout status deploy/ozz-gateway --timeout=180s
kubectl -n ozz rollout status deploy/ozz-frontend --timeout=180s

echo "Deployed OZZ to k3s. Point ozz.local to the mini-pc IP or edit k8s/base/configmap.yaml and ingress.yaml."
