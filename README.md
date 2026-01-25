# X-Ray SDK

The X-Ray SDK is a Java library for instrumenting multi-step algorithmic systems to provide transparency into decision processes. It captures not just *what* happened, but *why* decisions were made, enabling debugging, analysis, and optimization of complex pipelines.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Core Concepts](#core-concepts)
- [API Reference](#api-reference)
- [Usage Examples](#usage-examples)
- [Configuration](#configuration)
- [Error Handling](#error-handling)
- [Architecture](#architecture)
- [Best Practices](#best-practices)

---

## Overview

The X-Ray SDK provides a simple, non-intrusive way to instrument your algorithmic pipelines. It maintains a thread-local context that tracks the current run and step, allowing you to add instrumentation with minimal code changes.

### Key Benefits

- **Transparency**: See exactly what happened at each step of your pipeline
- **Debugging**: Quickly identify where and why decisions went wrong
- **Analysis**: Query across pipelines to find patterns and optimize
- **Non-blocking**: Async operations don't slow down your pipeline
- **Graceful Degradation**: Continues operation even if the API is unavailable

---

## Features

### Core Features

- **Thread-local Context**: Automatically tracks current run and step across async operations
- **Async Operations**: Non-blocking API calls that don't impact pipeline performance
- **Graceful Degradation**: Continues execution even if the X-Ray API is unavailable
- **Fluent API**: Simple, intuitive interface for instrumentation
- **Flexible Data Model**: Supports any pipeline type with JSON-based data storage

### Advanced Features

- **Batch Operations**: Efficiently add multiple candidates in a single call
- **Metadata Support**: Attach custom metadata at any level (run, step, candidate)
- **Context Management**: Automatic context tracking and cleanup
- **Error Resilience**: Comprehensive error handling with logging

---

## Installation

### Maven

Add the SDK as a dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.task</groupId>
    <artifactId>equal-sdk</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### Gradle

```gradle
implementation 'com.task:equal-sdk:0.0.1-SNAPSHOT'
```

### Requirements

- Java 21 or higher
- Access to the X-Ray API server
- Spring WebFlux (included as dependency)

---

## Quick Start

### 1. Create an SDK Instance

```java
import com.task.founding.engineer.sdk.XRaySDK;
import com.task.founding.engineer.sdk.XRaySDKClientBuilder;

// Create SDK instance
XRaySDK sdk = XRaySDKClientBuilder.standard()
    .withBaseUrl("http://localhost:8080")
    .withGracefulDegradation(true)
    .withAsync(true)
    .build();
```

### 2. Instrument Your Pipeline

```java
// Start a run
StartRunRequest runRequest = StartRunRequest.builder()
    .pipelineType("competitor_selection")
    .pipelineId("run-123")
    .input(Map.of("query", "phone case"))
    .build();
StartRunResult runResult = sdk.startRun(runRequest);

// Start a step
StartStepRequest stepRequest = StartStepRequest.builder()
    .stepName("keyword_generation")
    .stepType("llm")
    .order(1)
    .input(Map.of("query", "phone case"))
    .build();
StartStepResult stepResult = sdk.startStep(stepRequest);

// Add candidates
for (Candidate candidate : candidates) {
    AddCandidateRequest candidateRequest = AddCandidateRequest.builder()
        .candidateData(candidate.getData())
        .score(candidate.getScore())
        .selected(candidate.isSelected())
        .rejectionReason(candidate.getRejectionReason())
        .build();
    sdk.addCandidate(candidateRequest);
}

// End the step
EndStepRequest endStepRequest = EndStepRequest.builder()
    .output(selectedCandidates)
    .reasoning("Selected top 5 candidates based on relevance score")
    .build();
sdk.endStep(endStepRequest);

// End the run
EndRunRequest endRunRequest = EndRunRequest.builder()
    .output(finalResult)
    .build();
sdk.endRun(endRunRequest);
```

---

## Core Concepts

### Run

A **Run** represents a complete execution of a pipeline. It contains:
- Pipeline type and ID
- Input and output data
- Status (IN_PROGRESS, COMPLETED, FAILED)
- All steps executed during the run

### Step

A **Step** represents a single operation within a pipeline. Steps can be:
- **LLM calls**: Language model interactions
- **API calls**: External service calls
- **Filtering**: Candidate filtering operations
- **Ranking**: Candidate ranking operations
- **Selection**: Final selection operations

Each step contains:
- Step name and type
- Input and output data
- Reasoning (why decisions were made)
- All candidates evaluated

### Candidate

A **Candidate** represents an option evaluated within a step. Candidates contain:
- Data (domain-specific)
- Score (if applicable)
- Selection status (selected/rejected)
- Rejection reason (if rejected)

### Context

The SDK uses **thread-local context** to automatically track:
- Current run ID
- Current step ID
- Metadata

This allows you to call SDK methods without explicitly passing IDs, making instrumentation simple and clean.

---

## API Reference

### XRaySDK Interface

The main interface for interacting with the X-Ray system.

#### Run Operations

##### `startRun(StartRunRequest request)`

Starts a new X-Ray run.

**Request:**
```java
StartRunRequest.builder()
    .pipelineType("competitor_selection")  // Required
    .pipelineId("run-123")                  // Required
    .input(Map.of(...))                     // Required
    .build()
```

**Response:**
```java
StartRunResult.builder()
    .runId(UUID)  // May be null in async mode
    .build()
```

**Example:**
```java
StartRunRequest request = StartRunRequest.builder()
    .pipelineType("competitor_selection")
    .pipelineId("run-" + System.currentTimeMillis())
    .input(Map.of("query", "phone case", "category", "electronics"))
    .build();

StartRunResult result = sdk.startRun(request);
// In sync mode, result.getRunId() contains the created run ID
// In async mode, use sdk.getCurrentRunId() after the operation completes
```

##### `endRun(EndRunRequest request)`

Completes the current run.

**Request:**
```java
EndRunRequest.builder()
    .output(Map.of(...))  // Optional
    .build()
```

**Response:**
```java
EndRunResult.builder().build()
```

**Example:**
```java
EndRunRequest request = EndRunRequest.builder()
    .output(Map.of("selectedCompetitors", selectedList))
    .build();

sdk.endRun(request);
```

##### `failRun(FailRunRequest request)`

Marks the current run as failed.

**Request:**
```java
FailRunRequest.builder().build()
```

**Response:**
```java
FailRunResult.builder().build()
```

**Example:**
```java
try {
    // Pipeline execution
} catch (Exception e) {
    sdk.failRun(FailRunRequest.builder().build());
    throw e;
}
```

#### Step Operations

##### `startStep(StartStepRequest request)`

Starts a new step within the current run.

**Request:**
```java
StartStepRequest.builder()
    .stepName("keyword_generation")     // Required
    .stepType("llm")                     // Required
    .order(1)                            // Required
    .input(Map.of(...))                  // Optional
    .output(Map.of(...))                 // Optional
    .reasoning("...")                    // Optional
    .metadata(Map.of(...))               // Optional
    .build()
```

**Response:**
```java
StartStepResult.builder()
    .stepId(UUID)  // May be null in async mode
    .build()
```

**Example:**
```java
StartStepRequest request = StartStepRequest.builder()
    .stepName("price_filter")
    .stepType("filter")
    .order(2)
    .input(Map.of("candidates", candidates, "priceRange", Map.of("min", 10, "max", 50)))
    .metadata(Map.of("version", "1.0"))
    .build();

StartStepResult result = sdk.startStep(request);
```

##### `endStep(EndStepRequest request)`

Completes the current step.

**Request:**
```java
EndStepRequest.builder()
    .output(Map.of(...))    // Optional
    .reasoning("...")       // Optional
    .build()
```

**Response:**
```java
EndStepResult.builder().build()
```

**Example:**
```java
EndStepRequest request = EndStepRequest.builder()
    .output(filteredCandidates)
    .reasoning("Filtered candidates by price range $10-$50, removed 15 items")
    .build();

sdk.endStep(request);
```

#### Candidate Operations

##### `addCandidate(AddCandidateRequest request)`

Adds a candidate to the current step.

**Request:**
```java
AddCandidateRequest.builder()
    .candidateData(Map.of(...))      // Required
    .score(0.85)                     // Optional
    .selected(true)                  // Optional (default: false)
    .rejectionReason("...")          // Optional
    .metadata(Map.of(...))          // Optional
    .build()
```

**Response:**
```java
AddCandidateResult.builder()
    .candidateId(UUID)  // May be null in async mode
    .build()
```

**Example:**
```java
// Selected candidate
AddCandidateRequest selectedRequest = AddCandidateRequest.builder()
    .candidateData(Map.of("name", "Product A", "price", 29.99))
    .score(0.92)
    .selected(true)
    .metadata(Map.of("source", "api"))
    .build();
sdk.addCandidate(selectedRequest);

// Rejected candidate
AddCandidateRequest rejectedRequest = AddCandidateRequest.builder()
    .candidateData(Map.of("name", "Product B", "price", 5.99))
    .score(0.45)
    .selected(false)
    .rejectionReason("Price below minimum threshold")
    .build();
sdk.addCandidate(rejectedRequest);
```

##### `selectCandidate(SelectCandidateRequest request)`

Marks a candidate as selected. (Note: Currently, selection is done via `addCandidate` with `selected=true`. This method is kept for API consistency.)

**Request:**
```java
SelectCandidateRequest.builder()
    .candidateId(UUID)        // Required
    .reasoning("...")         // Optional
    .build()
```

##### `rejectCandidate(RejectCandidateRequest request)`

Marks a candidate as rejected. (Note: Currently, rejection is done via `addCandidate` with `selected=false`. This method is kept for API consistency.)

**Request:**
```java
RejectCandidateRequest.builder()
    .candidateId(UUID)        // Required
    .reason("...")           // Optional
    .build()
```

#### Metadata Operations

##### `addMetadata(AddMetadataRequest request)`

Adds metadata to the current context.

**Request:**
```java
AddMetadataRequest.builder()
    .key("environment")       // Required
    .value("production")      // Required
    .build()
```

**Response:**
```java
AddMetadataResult.builder().build()
```

**Example:**
```java
sdk.addMetadata(AddMetadataRequest.builder()
    .key("version")
    .value("2.1.0")
    .build());
```

#### Context Operations

##### `getCurrentRunId()`

Returns the current run ID from context, or `null` if no active run.

**Returns:** `UUID` or `null`

**Example:**
```java
UUID runId = sdk.getCurrentRunId();
if (runId != null) {
    // Use runId for logging or other purposes
}
```

##### `getCurrentStepId()`

Returns the current step ID from context, or `null` if no active step.

**Returns:** `UUID` or `null`

---

## Usage Examples

### Minimal Instrumentation

For simple pipelines, you only need to track key decision points:

```java
XRaySDK sdk = XRaySDKClientBuilder.standard()
    .withBaseUrl("http://localhost:8080")
    .build();

// Start run
sdk.startRun(StartRunRequest.builder()
    .pipelineType("competitor_selection")
    .pipelineId("run-123")
    .input(inputData)
    .build());

// Track key steps
sdk.startStep(StartStepRequest.builder()
    .stepName("filtering")
    .stepType("filter")
    .order(1)
    .input(filterInput)
    .build());

// ... filtering logic ...

sdk.endStep(EndStepRequest.builder()
    .output(filteredResults)
    .reasoning("Filtered by price and rating")
    .build());

// End run
sdk.endRun(EndRunRequest.builder()
    .output(finalResult)
    .build());
```

### Full Instrumentation

For detailed tracking, capture all candidates:

```java
// Start step
sdk.startStep(StartStepRequest.builder()
    .stepName("candidate_evaluation")
    .stepType("llm")
    .order(1)
    .input(candidates)
    .build());

// Evaluate each candidate
for (Candidate candidate : candidates) {
    double score = evaluateCandidate(candidate);
    boolean selected = score > 0.7;
    
    AddCandidateRequest request = AddCandidateRequest.builder()
        .candidateData(candidate.getData())
        .score(score)
        .selected(selected)
        .rejectionReason(selected ? null : "Score below threshold")
        .metadata(Map.of("model", "gpt-4", "timestamp", System.currentTimeMillis()))
        .build();
    
    sdk.addCandidate(request);
}

// End step with reasoning
sdk.endStep(EndStepRequest.builder()
    .output(selectedCandidates)
    .reasoning("Selected candidates with score > 0.7, total: " + selectedCandidates.size())
    .build());
```

### Batch Candidate Addition

For steps with many candidates, use batch operations (via the client directly):

```java
// Note: Batch operations are available through XRayClient
// This is more efficient for large candidate sets
List<Map<String, Object>> candidates = ...;
XRayClient client = ...; // Access client if needed
client.addCandidatesBatchAsync(stepId, candidates);
```

### Error Handling

```java
try {
    sdk.startRun(StartRunRequest.builder()
        .pipelineType("competitor_selection")
        .pipelineId("run-123")
        .input(inputData)
        .build());
    
    // Pipeline execution
    processPipeline();
    
    sdk.endRun(EndRunRequest.builder()
        .output(result)
        .build());
        
} catch (Exception e) {
    // Mark run as failed
    sdk.failRun(FailRunRequest.builder().build());
    throw e;
}
```

### Async Operations

When async mode is enabled, operations return immediately:

```java
XRaySDK sdk = XRaySDKClientBuilder.standard()
    .withBaseUrl("http://localhost:8080")
    .withAsync(true)  // Enable async mode
    .build();

// This returns immediately
sdk.startRun(StartRunRequest.builder()
    .pipelineType("competitor_selection")
    .pipelineId("run-123")
    .input(inputData)
    .build());

// Get run ID from context after operation completes
UUID runId = sdk.getCurrentRunId();
```

---

## Configuration

### Builder Options

The `XRaySDKClientBuilder` provides several configuration options:

#### `withBaseUrl(String baseUrl)`

Sets the base URL for the X-Ray API.

**Required:** Yes

**Example:**
```java
.withBaseUrl("http://localhost:8080")
.withBaseUrl("https://api.example.com")
```

#### `withGracefulDegradation(boolean enable)`

Enables or disables graceful degradation.

**Default:** `true`

**When enabled:**
- API failures are logged as warnings
- Execution continues without throwing exceptions
- Operations return empty results on failure

**When disabled:**
- API failures throw exceptions
- May interrupt pipeline execution

**Example:**
```java
.withGracefulDegradation(true)   // Recommended for production
.withGracefulDegradation(false)   // For debugging
```

#### `withAsync(boolean enable)`

Enables or disables async operations.

**Default:** `true`

**When enabled:**
- SDK methods return immediately
- API calls happen asynchronously
- Use `getCurrentRunId()` / `getCurrentStepId()` to get IDs after completion

**When disabled:**
- SDK methods block until API calls complete
- Results contain IDs directly
- Slower but more predictable

**Example:**
```java
.withAsync(true)   // Recommended for performance
.withAsync(false)  // For synchronous workflows
```

### Environment Variables

You can configure the SDK using environment variables:

```bash
export XRAY_SDK_BASE_URL="http://localhost:8080"
```

Then use the default client:

```java
XRaySDK sdk = XRaySDKClientBuilder.defaultClient();
```

### System Properties

Alternatively, use system properties:

```bash
java -Dxray.sdk.baseUrl=http://localhost:8080 YourApp
```

---

## Error Handling

### Graceful Degradation

When graceful degradation is enabled (default), the SDK handles errors gracefully:

- **API Unavailable**: Logs warning, continues execution
- **Network Errors**: Logs warning, continues execution
- **Timeout**: Logs warning, continues execution
- **Invalid Response**: Logs error, continues execution

**Example:**
```java
// Even if API is down, this won't throw an exception
sdk.startRun(StartRunRequest.builder()
    .pipelineType("competitor_selection")
    .pipelineId("run-123")
    .input(inputData)
    .build());

// Pipeline continues normally
processPipeline();
```

### Error Logging

All errors are logged using SLF4J. Ensure you have a logging implementation (e.g., Logback, Log4j2) in your classpath.

**Log Levels:**
- **DEBUG**: Normal operations (run started, step started, etc.)
- **WARN**: API failures when graceful degradation is enabled
- **ERROR**: API failures when graceful degradation is disabled

### Context Validation

The SDK validates context before operations:

- **No Active Run**: `startStep()` logs a warning and returns empty result
- **No Active Step**: `addCandidate()` logs a warning and returns empty result
- **No Active Run**: `endRun()` logs a warning and returns empty result

Always ensure you call `startRun()` before `startStep()`, and `startStep()` before `addCandidate()`.

---

## Architecture

### Component Overview

```
XRaySDK (Interface)
    └── XRaySDKClient (Implementation)
        ├── XRayClient (HTTP Client)
        └── XRayContext (Thread-local Context)
```

### XRaySDKClient

The main implementation of the SDK interface. It:
- Manages context (run ID, step ID)
- Delegates HTTP calls to `XRayClient`
- Handles async/sync modes
- Provides error handling

### XRayClient

Low-level HTTP client that:
- Makes REST API calls to the X-Ray API
- Uses Spring WebFlux for reactive HTTP
- Handles timeouts (5 seconds default)
- Implements graceful degradation

### XRayContext

Thread-local context manager that:
- Stores current run ID and step ID
- Maintains metadata map
- Provides thread-safe access
- Automatically cleans up on run completion

### Request/Response Models

All operations use request/response DTOs:

- **Request Models**: `StartRunRequest`, `StartStepRequest`, `AddCandidateRequest`, etc.
- **Result Models**: `StartRunResult`, `StartStepResult`, `AddCandidateResult`, etc.

All models use Lombok builders for easy construction.

---

## Best Practices

### 1. Use Builder Pattern

Always use builders for request objects:

```java
// Good
StartRunRequest request = StartRunRequest.builder()
    .pipelineType("competitor_selection")
    .pipelineId("run-123")
    .input(inputData)
    .build();

// Avoid
StartRunRequest request = new StartRunRequest();
request.setPipelineType("competitor_selection");
// ...
```

### 2. Enable Graceful Degradation in Production

Always enable graceful degradation in production to prevent API issues from breaking your pipeline:

```java
XRaySDK sdk = XRaySDKClientBuilder.standard()
    .withBaseUrl(apiUrl)
    .withGracefulDegradation(true)  // Always true in production
    .withAsync(true)
    .build();
```

### 3. Use Async Mode for Performance

Async mode provides better performance by not blocking on API calls:

```java
.withAsync(true)  // Recommended
```

### 4. Provide Meaningful Reasoning

Always provide reasoning when ending steps:

```java
sdk.endStep(EndStepRequest.builder()
    .output(results)
    .reasoning("Filtered 100 candidates to 15 based on price range $10-$50 and rating > 4.0")
    .build());
```

### 5. Use Consistent Step Types

Use standardized step types for cross-pipeline queries:

- `"llm"` - Language model operations
- `"api"` - External API calls
- `"filter"` - Filtering operations
- `"ranking"` - Ranking operations
- `"selection"` - Selection operations

### 6. Clean Up on Errors

Always mark runs as failed on exceptions:

```java
try {
    sdk.startRun(...);
    // ... pipeline execution ...
    sdk.endRun(...);
} catch (Exception e) {
    sdk.failRun(FailRunRequest.builder().build());
    throw e;
}
```

### 7. Add Metadata for Context

Use metadata to provide additional context:

```java
sdk.addMetadata(AddMetadataRequest.builder()
    .key("environment")
    .value("production")
    .build());

sdk.addMetadata(AddMetadataRequest.builder()
    .key("version")
    .value("2.1.0")
    .build());
```

### 8. Reuse SDK Instance

Create one SDK instance and reuse it:

```java
// Create once
private static final XRaySDK sdk = XRaySDKClientBuilder.standard()
    .withBaseUrl("http://localhost:8080")
    .build();

// Reuse everywhere
public void processPipeline() {
    sdk.startRun(...);
    // ...
}
```

### 9. Validate Context

Check context before operations if needed:

```java
UUID runId = sdk.getCurrentRunId();
if (runId == null) {
    // Handle missing run context
    return;
}
```

### 10. Use Descriptive Step Names

Use clear, descriptive step names:

```java
// Good
.stepName("price_filter")
.stepName("relevance_ranking")
.stepName("final_selection")

// Avoid
.stepName("step1")
.stepName("filter")
.stepName("process")
```

---

## Thread Safety

The SDK is **thread-safe** for concurrent use:

- Each thread has its own context (via `ThreadLocal`)
- Multiple threads can use the same SDK instance
- Context is automatically isolated per thread
- No synchronization needed

**Example:**
```java
// Same SDK instance, different threads
ExecutorService executor = Executors.newFixedThreadPool(10);
for (int i = 0; i < 10; i++) {
    executor.submit(() -> {
        // Each thread has its own context
        sdk.startRun(...);
        // ...
    });
}
```

---

## Performance Considerations

### Async Mode

Async mode provides the best performance:
- Non-blocking API calls
- Pipeline execution continues immediately
- API calls happen in background

### Batch Operations

For steps with many candidates, consider batch operations (available through `XRayClient` directly).

### Timeout Configuration

Default timeout is 5 seconds. For slow APIs, you may need to adjust this in `XRayClient`.

### Memory Usage

The SDK uses minimal memory:
- Thread-local context is lightweight
- No caching of large objects
- Automatic cleanup on run completion

---

## Troubleshooting

### Issue: Run ID is null in async mode

**Solution:** Use `sdk.getCurrentRunId()` after the operation completes, or wait for the async operation to finish.

### Issue: Step operations fail with "No active run"

**Solution:** Ensure you call `startRun()` before `startStep()`.

### Issue: Candidate operations fail with "No active step"

**Solution:** Ensure you call `startStep()` before `addCandidate()`.

### Issue: API calls are slow

**Solution:** Enable async mode with `withAsync(true)`.

### Issue: Exceptions are thrown even with graceful degradation

**Solution:** Check that graceful degradation is enabled: `withGracefulDegradation(true)`.