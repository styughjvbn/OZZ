#!/usr/bin/env bash
set -euo pipefail

TAG="${1:-dev}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "${ROOT_DIR}/backend"
chmod +x ./gradlew
./gradlew buildMonolith

docker build -t "ozz/backend:${TAG}" ./monolith
docker build -t "ozz/ai:${TAG}" ./ai

cd "${ROOT_DIR}"
docker build -f frontend/ozz/Dockerfile -t "ozz/frontend:${TAG}" .

echo "Built OZZ images with tag ${TAG}"
