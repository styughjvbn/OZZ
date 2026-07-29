# OZZ modular monolith

`monolith` is the single Spring Boot runtime for the Java backend. The existing
`auth`, `user`, `file-server`, `clothes`, `favorite`, and `board` projects remain
domain modules and are packaged into one executable jar. `eureka` and `gateway`
are not runtime dependencies.

## Build

```shell
./gradlew buildMonolith
```

The executable jar is created under `monolith/build/libs`.

## Runtime boundaries

- Java domain-to-domain calls use `InternalModuleAdapters` and stay in-process.
- Gateway JWT validation, user-header propagation, and CORS run in the monolith.
- FastAPI remains an external process and is exposed through `/api/ai/**`.
- MySQL, Redis, RabbitMQ, Elasticsearch, and file storage remain infrastructure
  dependencies.

The original Feign interfaces are temporarily retained as migration contracts.
They are not discovered as Feign clients in the monolith. A later step should
move these contracts to explicit module application APIs and remove OpenFeign
from each domain module.

## Kubernetes

`k8s/base` now deploys one `ozz-backend` JVM instead of Eureka, Gateway, and six
service JVMs. Set `INTERNAL_API_TOKEN` in `ozz-secret`; FastAPI uses it for its
callbacks into the clothes module.
