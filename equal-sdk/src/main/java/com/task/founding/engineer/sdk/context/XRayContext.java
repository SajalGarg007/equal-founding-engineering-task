package com.task.founding.engineer.sdk.context;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Data
@Builder
public class XRayContext {

    private UUID currentRunId;
    private UUID currentStepId;
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    private static final ThreadLocal<XRayContext> contextHolder = new ThreadLocal<>();

    public static XRayContext get() {
        return contextHolder.get();
    }

    public static void set(XRayContext context) {
        contextHolder.set(context);
    }

    public static void clear() {
        contextHolder.remove();
    }

    public static boolean exists() {
        return Objects.nonNull(contextHolder.get());
    }

    public static XRayContext getOrCreate() {
        XRayContext context = contextHolder.get();
        if (Objects.isNull(context)) {
            context = XRayContext.builder().build();
            contextHolder.set(context);
        }
        return context;
    }
}

