# Architecture Workflow for X-Ray SDK & API

## Overview

This document outlines the step-by-step architecture workflow for building the X-Ray SDK and API system. The X-Ray system provides transparency into multi-step decision processes, capturing not just *what* happened, but *why* decisions were made.

---

## Phase 1: Data Model Design

### 1.1 Core Entities

```
XRayRun (Root Entity)
├── runId (UUID)
├── pipelineType (String) - e.g., "competitor_selection", "listing_optimization"
├── pipelineId (String) - unique identifier for this pipeline instance
├── startedAt (Timestamp)
├── completedAt (Timestamp)
├── status (Enum: IN_PROGRESS, COMPLETED, FAILED)
├── input (JSON) - initial input to the pipeline
├── output (JSON) - final output
└── steps (List<XRayStep>)

XRayStep
├── stepId (UUID)
├── runId (FK to XRayRun)
├── stepName (String) - e.g., "keyword_generation", "filtering"
├── stepType (String) - e.g., "llm", "api", "filter", "ranking"
├── order (Integer) - sequence in pipeline
├── startedAt (Timestamp)
├── completedAt (Timestamp)
├── status (Enum: SUCCESS, FAILED, SKIPPED)
├── input (JSON) - step input
├── output (JSON) - step output
├── reasoning (String) - why this decision was made
├── candidates (List<XRayCandidate>) - for steps with multiple candidates
└── metadata (JSON) - additional context

XRayCandidate
├── candidateId (UUID)
├── stepId (FK to XRayStep)
├── data (JSON) - candidate data
├── score (Double) - if applicable
├── selected (Boolean) - was this candidate selected?
├── rejectionReason (String) - if rejected, why?
└── metadata (JSON)
```

### 1.2 Design Rationale

- **Hierarchical Structure**: Run → Steps → Candidates allows natural representation of multi-step processes
- **Flexible JSON Fields**: Domain-agnostic, can handle any pipeline type
- **Step Type Classification**: Enables cross-pipeline queries (e.g., "all filtering steps")
- **Reasoning Fields**: Captures the "why" behind decisions
- **Pipeline Identification**: `pipelineType` + `pipelineId` supports multiple pipelines

---

## Phase 2: SDK Design

### 2.1 Core SDK Components

```
XRayContext (Thread-local context)
├── currentRunId
├── currentStepId
└── metadata

XRaySDK (Main entry point)
├── startRun(pipelineType, pipelineId, input)
├── startStep(stepName, stepType, input)
├── addCandidate(candidateData, score, metadata)
├── selectCandidate(candidateId, reasoning)
├── rejectCandidate(candidateId, reason)
├── endStep(output, reasoning)
└── endRun(output)

XRayClient (HTTP client to API)
├── sendStepData(stepData)
├── sendRunData(runData)
└── handleFailure() - graceful degradation
```

### 2.2 SDK Features

- **Thread-local Context**: Maintains state across async operations
- **Non-blocking**: Async API calls, doesn't slow down pipeline
- **Graceful Degradation**: If API is down, queue locally or skip silently
- **Minimal Instrumentation**: Wrap key decision points
- **Full Instrumentation**: Capture all candidates with reasoning

---

## Phase 3: API Design

### 3.1 Endpoints

#### Ingest Endpoints

```
POST /api/v1/runs
  - Create/start a new X-Ray run
  - Body: { pipelineType, pipelineId, input }
  - Returns: { runId }

POST /api/v1/runs/{runId}/steps
  - Add step data to a run
  - Body: { stepName, stepType, input, output, reasoning, candidates[] }
  - Returns: { stepId }

POST /api/v1/steps/{stepId}/candidates
  - Add candidate(s) to a step
  - Body: { candidates[] } where each candidate has: { data, score, selected, rejectionReason, metadata }
  - Returns: { candidateIds[] }
  
POST /api/v1/steps/{stepId}/candidates/batch
  - Add multiple candidates in batch (for performance with large candidate sets)
  - Body: { candidates[] } - array of candidate objects
  - Returns: { count, candidateIds[] }
```

#### Query Endpoints

```
GET /api/v1/runs/{runId}
  - Get full run with all steps
  - Returns: Complete XRayRun with nested steps and candidates

GET /api/v1/runs
  - Query runs with filters
  - Query params: pipelineType, status, dateRange
  - Returns: List of runs

GET /api/v1/steps
  - Query steps across runs
  - Query params: stepType, stepName, filters (e.g., rejectionRate > 0.9)
  - Returns: List of steps with parent run info

GET /api/v1/steps/{stepId}/candidates
  - Get all candidates for a step
  - Query params: selected (true/false), limit, offset
  - Returns: List of candidates

GET /api/v1/steps/{stepId}/candidates/selected
  - Get only selected candidates for a step
  - Returns: List of selected candidates

GET /api/v1/steps/{stepId}/candidates/rejected
  - Get only rejected candidates for a step
  - Query params: limit, offset (for pagination with large sets)
  - Returns: List of rejected candidates

GET /api/v1/analytics/filtering-stats
  - Custom analytics endpoint
  - Query params: pipelineType, dateRange
  - Returns: Stats about filtering steps (input count, output count, rejection rate)
```

---

## Phase 4: Implementation Phases

### Phase 4.1: Foundation (Day 1 - Morning)

#### 1. Database Schema
- Create JPA entities (`XRayRun`, `XRayStep`, `XRayCandidate`)
- Add indexes: `runId`, `pipelineType`, `stepType`, `stepName`
- Migration scripts

#### 2. Basic API Structure
- REST controllers
- DTOs for request/response
- Service layer
- Repository layer

### Phase 4.2: Core API (Day 1 - Afternoon)

#### 1. Ingest Endpoints
- `POST /api/v1/runs` - create run
- `POST /api/v1/runs/{runId}/steps` - add step data
- `POST /api/v1/steps/{stepId}/candidates` - add candidate(s) to step
- `POST /api/v1/steps/{stepId}/candidates/batch` - batch add candidates (for performance)
- Validation and error handling

#### 2. Query Endpoints
- `GET /api/v1/runs/{runId}` - get run details
- `GET /api/v1/runs` - list runs with filters
- `GET /api/v1/steps/{stepId}/candidates` - get candidates for a step
- `GET /api/v1/steps/{stepId}/candidates/selected` - get selected candidates
- `GET /api/v1/steps/{stepId}/candidates/rejected` - get rejected candidates
- Basic querying

### Phase 4.3: SDK Development (Day 2 - Morning)

#### 1. SDK Core
- `XRaySDK` class with fluent API
- Thread-local context management
- HTTP client for API communication
- Async/non-blocking calls

#### 2. SDK Features
- Graceful degradation (queue locally or skip if API down)
- Candidate tracking
- Reasoning capture

### Phase 4.4: Advanced Querying (Day 2 - Afternoon)

#### 1. Cross-pipeline Queries
- `GET /api/v1/steps` with flexible filters
- Analytics endpoints
- Query examples: "filtering steps with >90% rejection rate"

#### 2. Performance Optimizations
- Pagination
- Lazy loading for candidates
- Summary vs. detailed capture

### Phase 4.5: Example Implementation (Day 2 - Evening)

#### 1. Demo Pipeline
- Implement competitor selection example
- Show minimal vs. full instrumentation
- Demonstrate debugging walkthrough

#### 2. Testing
- Unit tests for SDK
- Integration tests for API
- Example scenarios

---

## Phase 5: Architecture Document Structure

### 5.1 Document Sections

1. **Overview**
   - Problem statement
   - Solution approach

2. **Data Model**
   - Entity diagram
   - Rationale for structure
   - Alternatives considered

3. **SDK Design**
   - API surface
   - Integration patterns
   - Minimal vs. full instrumentation

4. **API Design**
   - Endpoint specifications
   - Request/response examples
   - Query patterns

5. **Debugging Walkthrough**
   - Phone case → laptop stand scenario
   - Step-by-step investigation

6. **Queryability**
   - Cross-pipeline query design
   - Constraints and conventions

7. **Performance & Scale**
   - Handling large candidate sets
   - Trade-offs: completeness vs. performance
   - Sampling strategies

8. **Developer Experience**
   - Integration guide
   - Error handling
   - Graceful degradation

---

## Phase 6: Key Architectural Decisions

### 6.1 Data Model

- **Decision**: Hierarchical JSON structure
- **Rationale**: Flexible for different domains, queryable via JSON fields
- **Alternative**: Strongly typed schemas (too rigid)

### 6.2 Performance

- **Decision**: Summary + sampling for large candidate sets
- **Rationale**: Balance completeness with storage/performance
- **Implementation**: SDK allows `captureMode` (FULL, SUMMARY, SAMPLED)

### 6.3 Queryability

- **Decision**: Standardized `stepType` and `stepName` conventions
- **Rationale**: Enables cross-pipeline queries
- **Constraint**: Developers must use consistent naming

### 6.4 SDK Design

- **Decision**: Thread-local context + fluent API
- **Rationale**: Minimal code changes, works with async code
- **Alternative**: Aspect-oriented (too magic, harder to debug)

---

## Phase 7: Project Structure

```
src/
├── main/
│   ├── java/com/task/founding/engineer/
│   │   ├── sdk/
│   │   │   ├── XRaySDK.java
│   │   │   ├── XRayContext.java
│   │   │   ├── XRayClient.java
│   │   │   └── model/
│   │   │       ├── Run.java
│   │   │       ├── Step.java
│   │   │       └── Candidate.java
│   │   ├── api/
│   │   │   ├── controller/
│   │   │   │   ├── RunController.java
│   │   │   │   └── StepController.java
│   │   │   ├── service/
│   │   │   │   ├── RunService.java
│   │   │   │   └── StepService.java
│   │   │   ├── dto/
│   │   │   └── repository/
│   │   │       ├── RunRepository.java
│   │   │       └── StepRepository.java
│   │   ├── model/
│   │   │   ├── XRayRun.java
│   │   │   ├── XRayStep.java
│   │   │   └── XRayCandidate.java
│   │   └── examples/
│   │       └── CompetitorSelectionPipeline.java
│   └── resources/
│       └── application.properties
├── test/
└── ARCHITECTURE.md (root level)
```

---

## Phase 8: Implementation Checklist

- [ ] Database entities and migrations
- [ ] Basic CRUD API endpoints
- [ ] SDK core classes
- [ ] HTTP client integration
- [ ] Graceful degradation logic
- [ ] Query endpoints with filters
- [ ] Example pipeline implementation
- [ ] Unit and integration tests
- [ ] Architecture document
- [ ] README with setup instructions
- [ ] Video walkthrough

---

## Next Steps

1. **Start with Data Model** (Phase 4.1) - Foundation is critical
2. **Build API Endpoints** (Phase 4.2) - Core functionality
3. **Develop SDK** (Phase 4.3) - Developer-facing interface
4. **Add Advanced Features** (Phase 4.4) - Queryability and analytics
5. **Create Example & Documentation** (Phase 4.5) - Demonstrate usage

---

## Key Considerations

### Performance & Scale

- **Large Candidate Sets**: For steps with 5,000 candidates, capture:
  - Summary statistics (input count, output count, rejection rate)
  - Sample of rejected candidates (e.g., first 10, last 10, random 20)
  - Full details only for selected candidates
  - Developer can configure via `captureMode`

### Queryability

- **Cross-pipeline Queries**: Standardize on:
  - `stepType`: "llm", "api", "filter", "ranking", "selection"
  - `stepName`: Descriptive but consistent (e.g., "price_filter", "relevance_filter")
  - Metadata conventions for common patterns

### Developer Experience

- **Minimal Instrumentation**: Just wrap decision points
  ```java
  xray.startStep("filtering", "filter", input);
  // ... filtering logic ...
  xray.endStep(output, "Filtered by price range $10-$50");
  ```

- **Full Instrumentation**: Track all candidates
  ```java
  for (Candidate c : candidates) {
      xray.addCandidate(c, score, metadata);
      if (shouldSelect(c)) {
          xray.selectCandidate(c.getId(), "Matches price and rating criteria");
      } else {
          xray.rejectCandidate(c.getId(), "Price out of range");
      }
  }
  ```

- **Graceful Degradation**: If API is unavailable:
  - Log warning
  - Continue execution
  - Optionally queue for retry
  - Never throw exceptions that break pipeline

---

## Example: Debugging Walkthrough

**Scenario**: Phone case matched against laptop stand

1. **Query the run**: `GET /api/v1/runs/{runId}`
2. **Examine steps in order**:
   - Step 1: Keyword generation → Check if keywords were relevant
   - Step 2: Search results → Check if search returned wrong products
   - Step 3: Filtering → Check if filters were too loose
   - Step 4: LLM evaluation → Check reasoning for why laptop stand was selected
3. **Identify failure point**: Likely in Step 4 - LLM reasoning shows incorrect relevance assessment
4. **Fix**: Adjust LLM prompt or add additional validation

---

## Conclusion

This workflow provides a structured approach to building the X-Ray system. Each phase builds on the previous one, ensuring a solid foundation while maintaining flexibility for future enhancements.

