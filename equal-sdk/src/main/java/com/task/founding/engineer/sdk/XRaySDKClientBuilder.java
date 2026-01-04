package com.task.founding.engineer.sdk;

import com.task.founding.engineer.sdk.client.XRayClient;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Fluent builder for {@link com.task.founding.engineer.sdk.XRaySDK}. Use of the builder is preferred over
 * using constructors of the client class.
 * <p>
 * Usage example:
 * <pre>
 * XRaySDK sdk = XRaySDKClientBuilder.standard()
 *     .withBaseUrl("http://localhost:8080")
 *     .withGracefulDegradation(true)
 *     .withAsync(true)
 *     .build();
 * </pre>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class XRaySDKClientBuilder {

    private String baseUrl;
    private Boolean enableGracefulDegradation = true;
    private Boolean enableAsync = true;

    /**
     * Create new instance of builder with all defaults set.
     *
     * @return a new builder instance
     */
    public static XRaySDKClientBuilder standard() {
        return new XRaySDKClientBuilder();
    }

    /**
     * Create a default client instance with default settings.
     * <p>
     * Note: This requires the base URL to be set via environment variable or system property.
     * For custom configuration, use {@link #standard()} instead.
     *
     * @return a default configured XRaySDK client instance
     * @throws IllegalArgumentException if base URL is not configured
     */
    public static XRaySDK defaultClient() {
        String baseUrl = System.getProperty("xray.sdk.baseUrl", 
                System.getenv("XRAY_SDK_BASE_URL"));
        if (Objects.isNull(baseUrl) || baseUrl.isEmpty()) {
            throw new IllegalArgumentException(
                    "Base URL is required. Set it via XRAY_SDK_BASE_URL environment variable " +
                    "or xray.sdk.baseUrl system property, or use standard().withBaseUrl()");
        }
        return standard()
                .withBaseUrl(baseUrl)
                .withGracefulDegradation(true)
                .withAsync(true)
                .build();
    }

    /**
     * Set the base URL for the X-Ray API.
     *
     * @param baseUrl the base URL (e.g., "http://localhost:8080")
     * @return this builder instance
     */
    public XRaySDKClientBuilder withBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    /**
     * Enable or disable graceful degradation.
     * <p>
     * When enabled, the SDK will continue operation even if API calls fail,
     * logging warnings instead of throwing exceptions.
     *
     * @param enableGracefulDegradation true to enable graceful degradation
     * @return this builder instance
     */
    public XRaySDKClientBuilder withGracefulDegradation(boolean enableGracefulDegradation) {
        this.enableGracefulDegradation = enableGracefulDegradation;
        return this;
    }

    /**
     * Enable or disable async operations.
     * <p>
     * When enabled, SDK methods return immediately without waiting for API responses.
     * When disabled, SDK methods block until API calls complete.
     *
     * @param enableAsync true to enable async operations
     * @return this builder instance
     */
    public XRaySDKClientBuilder withAsync(boolean enableAsync) {
        this.enableAsync = enableAsync;
        return this;
    }

    /**
     * Build the XRaySDK client instance using the current builder configuration.
     *
     * @return a fully configured XRaySDK client instance
     * @throws IllegalArgumentException if baseUrl is not set
     */
    public XRaySDK build() {
        if (Objects.isNull(baseUrl) || baseUrl.isEmpty()) {
            throw new IllegalArgumentException("Base URL is required. Use withBaseUrl() to set it.");
        }
        XRayClient client = new XRayClient(baseUrl, enableGracefulDegradation);
        return new XRaySDKClient(client, enableAsync);
    }
}
