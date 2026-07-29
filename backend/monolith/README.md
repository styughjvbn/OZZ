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

For a local full stack, build the jar first and then run:

```shell
./gradlew buildMonolith
docker compose up --build
```

## Runtime boundaries

- Java domain-to-domain calls use `InternalModuleAdapters` and stay in-process.
- Gateway JWT validation, user-header propagation, and CORS run in the monolith.
- FastAPI remains an external process and is exposed through `/api/ai/**`.
- MySQL, Redis, RabbitMQ, and file storage remain infrastructure
  dependencies.

Cross-module contracts are plain Java outbound ports under each module's
`application.port.out` package. The composition root implements those ports in
`InternalModuleAdapters`; domain modules do not depend on Eureka or OpenFeign.
Every domain project is a `java-library` and only `monolith` creates a bootable
jar.

## Kubernetes

`k8s/base` now deploys one `ozz-backend` JVM instead of Eureka, Gateway, and six
service JVMs. Set `INTERNAL_API_TOKEN` in `ozz-secret`; FastAPI uses it for its
callbacks into the clothes module.
