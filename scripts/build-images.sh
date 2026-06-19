#!/usr/bin/env bash
set -euo pipefail

TAG="${1:-dev}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "${ROOT_DIR}/backend"
chmod +x ./gradlew
./gradlew buildAll

docker build -t "ozz/eureka:${TAG}" ./eureka
docker build -t "ozz/gateway:${TAG}" ./gateway
docker build -t "ozz/auth:${TAG}" ./auth
docker build -t "ozz/user:${TAG}" ./user
docker build -t "ozz/clothes:${TAG}" ./clothes
docker build -t "ozz/favorite:${TAG}" ./favorite
docker build -t "ozz/board:${TAG}" ./board
docker build -t "ozz/file-server:${TAG}" ./file-server
docker build -t "ozz/ai:${TAG}" ./ai

cd "${ROOT_DIR}"
docker build -f frontend/ozz/Dockerfile -t "ozz/frontend:${TAG}" .

echo "Built OZZ images with tag ${TAG}"
