package com.ssafy.ozz.monolith.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.Collections;

/**
 * Gateway가 담당하던 FastAPI 경로 변환만 남긴 외부 시스템 어댑터.
 * Java 도메인 모듈은 모두 프로세스 내부에서 호출된다.
 */
@RestController
public class AiProxyController {

    private static final String PUBLIC_PREFIX = "/api/ai";
    private final RestClient restClient;

    public AiProxyController(
            RestClient.Builder builder,
            @Value("${fastapi.url}") String fastApiUrl
    ) {
        this.restClient = builder.baseUrl(fastApiUrl).build();
    }

    @RequestMapping(PUBLIC_PREFIX + "/**")
    public ResponseEntity<byte[]> proxy(HttpServletRequest request) throws IOException {
        String path = request.getRequestURI().substring(PUBLIC_PREFIX.length());
        String target = "/api/v1" + path;
        if (request.getQueryString() != null) {
            target += "?" + request.getQueryString();
        }

        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        RestClient.RequestBodySpec outgoing = restClient.method(method)
                .uri(target)
                .headers(headers -> copyRequestHeaders(request, headers));

        byte[] body = request.getInputStream().readAllBytes();
        if (body.length > 0) {
            outgoing.body(body);
        }

        return outgoing.exchange((sent, received) -> {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.putAll(received.getHeaders());
            responseHeaders.remove(HttpHeaders.TRANSFER_ENCODING);
            responseHeaders.remove(HttpHeaders.CONTENT_LENGTH);
            return ResponseEntity.status(received.getStatusCode())
                    .headers(responseHeaders)
                    .body(received.getBody().readAllBytes());
        });
    }

    private static void copyRequestHeaders(HttpServletRequest request, HttpHeaders target) {
        Collections.list(request.getHeaderNames()).forEach(name -> {
            if (!HttpHeaders.HOST.equalsIgnoreCase(name)
                    && !HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(name)) {
                target.put(name, Collections.list(request.getHeaders(name)));
            }
        });
    }
}
