package com.task.founding.engineer.sdk.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class XRayClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final boolean enableGracefulDegradation;

    public XRayClient(String baseUrl, boolean enableGracefulDegradation) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
        this.objectMapper = new ObjectMapper();
        this.enableGracefulDegradation = enableGracefulDegradation;
    }

    public CompletableFuture<UUID> createRunAsync(String pipelineType, String pipelineId, Object input) {
        Map<String, Object> requestBody = Map.of(
                "pipelineType", pipelineType,
                "pipelineId", pipelineId,
                "input", input
        );

        return webClient.post()
                .uri("/api/v1/runs")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(5))
                .map(response -> {
                    Map<String, Object> responseData = (Map<String, Object>) response.get("data");
                    Object idObj = responseData.get("id");
                    return UUID.fromString(idObj.toString());
                })
                .doOnError(error -> {
                    if (enableGracefulDegradation) {
                        log.warn("Failed to create run via API, continuing without tracking: {}", error.getMessage());
                    } else {
                        log.error("Failed to create run via API", error);
                    }
                })
                .onErrorResume(error -> {
                    if (enableGracefulDegradation) {
                        return Mono.empty();
                    }
                    return Mono.error(error);
                })
                .toFuture()
                .thenApply(result -> Objects.nonNull(result) ? result : null);
    }

    public CompletableFuture<UUID> createStepAsync(
            UUID runId,
            String stepName,
            String stepType,
            Integer order,
            Object input,
            Object output,
            String reasoning,
            Object metadata) {

        Map<String, Object> requestBody = new java.util.HashMap<>();
        requestBody.put("stepName", stepName);
        requestBody.put("stepType", stepType);
        requestBody.put("order", order);
        requestBody.put("input", input);
        if (Objects.nonNull(output)) {
            requestBody.put("output", output);
        }
        if (Objects.nonNull(reasoning)) {
            requestBody.put("reasoning", reasoning);
        }
        if (Objects.nonNull(metadata)) {
            requestBody.put("metadata", metadata);
        }

        return webClient.post()
                .uri("/api/v1/runs/{runId}/steps", runId)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(5))
                .map(response -> {
                    Map<String, Object> responseData = (Map<String, Object>) response.get("data");
                    Object idObj = responseData.get("id");
                    return UUID.fromString(idObj.toString());
                })
                .doOnError(error -> {
                    if (enableGracefulDegradation) {
                        log.warn("Failed to create step via API, continuing without tracking: {}", error.getMessage());
                    } else {
                        log.error("Failed to create step via API", error);
                    }
                })
                .onErrorResume(error -> {
                    if (enableGracefulDegradation) {
                        return Mono.empty();
                    }
                    return Mono.error(error);
                })
                .toFuture()
                .thenApply(result -> Objects.nonNull(result) ? result : null);
    }

    public CompletableFuture<UUID> addCandidateAsync(
            UUID stepId,
            Object data,
            Double score,
            Boolean selected,
            String rejectionReason,
            Object metadata) {

        Map<String, Object> requestBody = new java.util.HashMap<>();
        requestBody.put("data", data);
        if (Objects.nonNull(score)) {
            requestBody.put("score", score);
        }
        if (Objects.nonNull(selected)) {
            requestBody.put("selected", selected);
        }
        if (Objects.nonNull(rejectionReason)) {
            requestBody.put("rejectionReason", rejectionReason);
        }
        if (Objects.nonNull(metadata)) {
            requestBody.put("metadata", metadata);
        }

        return webClient.post()
                .uri("/api/v1/steps/{stepId}/candidates", stepId)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(5))
                .map(response -> {
                    Map<String, Object> responseData = (Map<String, Object>) response.get("data");
                    Object idObj = responseData.get("id");
                    if (idObj instanceof String) {
                        return UUID.fromString((String) idObj);
                    } else if (idObj instanceof Map) {
                        Map<String, Object> idMap = (Map<String, Object>) idObj;
                        return UUID.fromString(idMap.get("id").toString());
                    }
                    return UUID.fromString(idObj.toString());
                })
                .doOnError(error -> {
                    if (enableGracefulDegradation) {
                        log.warn("Failed to add candidate via API, continuing without tracking: {}", error.getMessage());
                    } else {
                        log.error("Failed to add candidate via API", error);
                    }
                })
                .onErrorResume(error -> {
                    if (enableGracefulDegradation) {
                        return Mono.empty();
                    }
                    return Mono.error(error);
                })
                .toFuture()
                .thenApply(result -> Objects.nonNull(result) ? result : null);
    }

    public CompletableFuture<java.util.List<UUID>> addCandidatesBatchAsync(
            UUID stepId,
            java.util.List<Map<String, Object>> candidates) {

        Map<String, Object> requestBody = Map.of("candidates", candidates);

        return webClient.post()
                .uri("/api/v1/steps/{stepId}/candidates/batch", stepId)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(10))
                .map(response -> {
                    Map<String, Object> data = (Map<String, Object>) response.get("data");
                    Object idObj = data.get("id");
                    @SuppressWarnings("unchecked")
                    java.util.List<Object> idsList;
                    if (idObj instanceof Map) {
                        Map<String, Object> idMap = (Map<String, Object>) idObj;
                        idsList = (java.util.List<Object>) idMap.get("ids");
                    } else {
                        idsList = (java.util.List<Object>) idObj;
                    }
                    return idsList.stream()
                            .map(id -> UUID.fromString(id.toString()))
                            .collect(java.util.stream.Collectors.toList());
                })
                .doOnError(error -> {
                    if (enableGracefulDegradation) {
                        log.warn("Failed to batch add candidates via API, continuing without tracking: {}", error.getMessage());
                    } else {
                        log.error("Failed to batch add candidates via API", error);
                    }
                })
                .onErrorResume(error -> {
                    if (enableGracefulDegradation) {
                        return Mono.empty();
                    }
                    return Mono.error(error);
                })
                .toFuture()
                .thenApply(result -> Objects.nonNull(result) ? result : java.util.Collections.emptyList());
    }

    public CompletableFuture<Void> completeStepAsync(UUID stepId, Object output, String reasoning) {
        Map<String, Object> requestBody = new java.util.HashMap<>();
        if (Objects.nonNull(output)) {
            requestBody.put("output", output);
        }
        if (Objects.nonNull(reasoning)) {
            requestBody.put("reasoning", reasoning);
        }

        return webClient.put()
                .uri("/api/v1/steps/{stepId}/complete", stepId)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(5))
                .doOnError(error -> {
                    if (enableGracefulDegradation) {
                        log.warn("Failed to complete step via API, continuing without tracking: {}", error.getMessage());
                    } else {
                        log.error("Failed to complete step via API", error);
                    }
                })
                .onErrorResume(error -> {
                    if (enableGracefulDegradation) {
                        return Mono.empty();
                    }
                    return Mono.error(error);
                })
                .toFuture()
                .thenApply(result -> null);
    }

    public CompletableFuture<Void> completeRunAsync(UUID runId, Object output) {
        return webClient.put()
                .uri("/api/v1/runs/{runId}/complete", runId)
                .bodyValue(Objects.nonNull(output) ? Map.of("output", output) : Map.of())
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(5))
                .doOnError(error -> {
                    if (enableGracefulDegradation) {
                        log.warn("Failed to complete run via API, continuing without tracking: {}", error.getMessage());
                    } else {
                        log.error("Failed to complete run via API", error);
                    }
                })
                .onErrorResume(error -> {
                    if (enableGracefulDegradation) {
                        return Mono.empty();
                    }
                    return Mono.error(error);
                })
                .toFuture()
                .thenApply(result -> null);
    }

    public CompletableFuture<Void> failRunAsync(UUID runId) {
        return webClient.put()
                .uri("/api/v1/runs/{runId}/fail", runId)
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(5))
                .doOnError(error -> {
                    if (enableGracefulDegradation) {
                        log.warn("Failed to fail run via API, continuing without tracking: {}", error.getMessage());
                    } else {
                        log.error("Failed to fail run via API", error);
                    }
                })
                .onErrorResume(error -> {
                    if (enableGracefulDegradation) {
                        return Mono.empty();
                    }
                    return Mono.error(error);
                })
                .toFuture()
                .thenApply(result -> null);
    }
}

