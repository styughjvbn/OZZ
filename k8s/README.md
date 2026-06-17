# OZZ k3s deployment

이 디렉터리는 dev-pc에서 빌드한 이미지를 mini-pc의 단일 노드 k3s에 올리는 기본 배포 골격입니다.

## 전제

- dev-pc에 `docker`, `kubectl`, `ssh`, `scp`가 설치되어 있어야 합니다.
- `kubectl`은 mini-pc k3s 클러스터를 바라보고 있어야 합니다.
- mini-pc는 SSH alias `mini-pc`로 접속 가능해야 합니다. 다르면 `MINI_PC=user@host`로 지정하세요.
- k3s 기본 Ingress Controller인 Traefik을 사용합니다.

## 배포

1. Secret 값을 수정합니다.

```bash
kubectl apply -f k8s/base/namespace.yaml
cp k8s/base/secret.example.yaml /tmp/ozz-secret.yaml
vi /tmp/ozz-secret.yaml
kubectl apply -f /tmp/ozz-secret.yaml
```

2. 호스트를 수정합니다.

`k8s/base/configmap.yaml`의 `SERVER_HOST`, `FRONT_HTTP_HOST`, `FRONT_HTTPS_HOST`, OAuth redirect URL과 `k8s/base/ingress.yaml`의 `host`를 mini-pc에서 사용할 도메인 또는 hosts 이름으로 맞춥니다.

3. 배포합니다.

```bash
MINI_PC=user@mini-pc TAG=dev ./scripts/deploy-mini-k3s.sh
```

4. 상태를 확인합니다.

```bash
kubectl -n ozz get pods,svc,ingress
kubectl -n ozz logs deploy/ozz-gateway
```

## 초기 데이터

MySQL 초기 데이터가 필요하면 `exec/ozzDB.sql`을 실행 중인 MySQL Pod에 넣어 import합니다.

```bash
kubectl -n ozz exec -i mysql-db-0 -- mysql -uroot -p ozz < exec/ozzDB.sql
```

## 주의할 점

- `k8s/base/secret.example.yaml`은 샘플입니다. 실제 키를 저장소에 커밋하지 마세요.
- 프론트 코드 일부 생성 API 클라이언트에 예전 도메인 또는 `localhost:8000` 기본값이 남아 있습니다. 운영 호스트에서 API 호출이 실패하면 해당 baseUrl을 상대 경로 또는 현재 도메인 기반으로 정리해야 합니다.
- ElasticSearch synonym 파일은 ConfigMap에 최소 예시만 넣어두었습니다. 검색 품질을 그대로 유지하려면 `backend/es/elasticsearch/config/synonym` 내용을 ConfigMap으로 확장하세요.
