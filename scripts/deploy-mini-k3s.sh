#!/usr/bin/env bash
set -euo pipefail

TAG="${TAG:-dev}"
MINI_PC="${MINI_PC:-mini-pc}"
MINI_PC_PORT="${MINI_PC_PORT:-22}"
REMOTE_KUBECONFIG="${REMOTE_KUBECONFIG:-\$HOME/.kube/config}"
SECRET_FILE="${SECRET_FILE:-}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
REMOTE_DIR="/tmp/ozz-deploy-${TAG}"
IMAGES=(
  ozz/backend
  ozz/ai
  ozz/frontend
)

"${ROOT_DIR}/scripts/build-images.sh" "${TAG}"

ARCHIVE="$(mktemp -t ozz-images.XXXXXX.tar)"
K8S_ARCHIVE="$(mktemp -t ozz-k8s-base.XXXXXX.tar.gz)"
K8S_BUNDLE_DIR="$(mktemp -d -t ozz-k8s-base.XXXXXX)"
trap 'rm -f "${ARCHIVE}" "${K8S_ARCHIVE}"; rm -rf "${K8S_BUNDLE_DIR}"' EXIT

docker save -o "${ARCHIVE}" "${IMAGES[@]/%/:${TAG}}"
cp -R "${ROOT_DIR}/k8s" "${K8S_BUNDLE_DIR}/k8s"
sed -i "s/newTag: dev/newTag: ${TAG}/g" "${K8S_BUNDLE_DIR}/k8s/base/kustomization.yaml"
tar -czf "${K8S_ARCHIVE}" -C "${K8S_BUNDLE_DIR}" k8s

ssh -p "${MINI_PC_PORT}" "${MINI_PC}" "rm -rf '${REMOTE_DIR}' && mkdir -p '${REMOTE_DIR}'"
scp -P "${MINI_PC_PORT}" "${ARCHIVE}" "${MINI_PC}:${REMOTE_DIR}/ozz-images.tar"
scp -P "${MINI_PC_PORT}" "${K8S_ARCHIVE}" "${MINI_PC}:${REMOTE_DIR}/ozz-k8s-base.tar.gz"
if [[ -n "${SECRET_FILE}" ]]; then
  scp -P "${MINI_PC_PORT}" "${SECRET_FILE}" "${MINI_PC}:${REMOTE_DIR}/ozz-secret.yaml"
fi

ssh -p "${MINI_PC_PORT}" "${MINI_PC}" "cat > '${REMOTE_DIR}/import-images.yaml' <<'YAML'
apiVersion: batch/v1
kind: Job
metadata:
  name: ozz-image-import
  namespace: kube-system
spec:
  backoffLimit: 0
  template:
    spec:
      restartPolicy: Never
      hostPID: true
      containers:
        - name: importer
          image: rancher/k3s:v1.33.3-k3s1
          imagePullPolicy: IfNotPresent
          securityContext:
            privileged: true
          command:
            - /bin/sh
            - -c
            - |
              set -eux
              ctr --address /run/k3s/containerd/containerd.sock --namespace k8s.io images import /host-deploy/ozz-images.tar
          volumeMounts:
            - name: k3s-run
              mountPath: /run/k3s
            - name: host-deploy
              mountPath: /host-deploy
      volumes:
        - name: k3s-run
          hostPath:
            path: /run/k3s
        - name: host-deploy
          hostPath:
            path: ${REMOTE_DIR}
YAML
export KUBECONFIG=${REMOTE_KUBECONFIG}
kubectl delete job -n kube-system ozz-image-import --ignore-not-found=true
kubectl apply -f '${REMOTE_DIR}/import-images.yaml'
kubectl wait --for=condition=complete job/ozz-image-import -n kube-system --timeout=300s
rm -rf '${REMOTE_DIR}/k8s'
mkdir -p '${REMOTE_DIR}/k8s'
tar -xzf '${REMOTE_DIR}/ozz-k8s-base.tar.gz' -C '${REMOTE_DIR}/k8s'
kubectl apply -f '${REMOTE_DIR}/k8s/k8s/base/namespace.yaml'
if [[ -f '${REMOTE_DIR}/ozz-secret.yaml' ]]; then
  kubectl apply -f '${REMOTE_DIR}/ozz-secret.yaml'
fi
kubectl apply -k '${REMOTE_DIR}/k8s/k8s/base'
kubectl -n ozz rollout status deploy/ozz-backend --timeout=240s
kubectl -n ozz rollout status deploy/ozz-frontend --timeout=240s
kubectl -n ozz get pods,svc,ingress"

echo "Deployed OZZ to k3s. Point ozz.local to the mini-pc IP or edit k8s/base/configmap.yaml and ingress.yaml."
