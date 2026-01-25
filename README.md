# X-Ray API Architecture Documentation

## Overview

The X-Ray API system provides transparency into multi-step decision processes, capturing not just *what* happened, but *why* decisions were made. It enables debugging, analysis, and optimization of complex pipelines by maintaining a complete audit trail of runs, steps, and candidates.

The system is built using Spring Boot with a modular architecture consisting of:
- **Data Layer**: JPA entities and repositories
- **Service Layer**: Business logic and orchestration
- **API Layer**: REST controllers and DTOs
- **Application Layer**: Main Spring Boot application

---

## Project Structure

The project follows a multi-module Maven structure:

```
founding.engineer/
├── equal-db/              # Database module (entities, repositories, services, DTOs)
├── equal-api/             # API module (controllers, converters, exception handlers)
└── founding-engineer-app/ # Application module (main Spring Boot app)
```

### Module Responsibilities

- **equal-db**: Contains all domain logic, data models, repositories, services, and DTOs. This module is independent and can be used by other modules.
- **equal-api**: Contains REST controllers, request/response converters, and global exception handling. Depends on `equal-db`.
- **founding-engineer-app**: Main Spring Boot application that wires everything together. Depends on `equal-api`.

---

## Data Model

### Entity Hierarchy

The system uses a hierarchical data model: **Run → Steps → Candidates**

```
XRayRun (Root Entity)
├── XRayStep (Child Entity)
    └── XRayCandidate (Child Entity)
```

### Core Entities

#### XRayRun

The root entity representing a complete pipeline execution.

**Fields:**
- `runId` (UUID, Primary Key) - Auto-generated unique identifier
- `pipelineType` (String, Required) - Type of pipeline (e.g., "competitor_selection", "listing_optimization")
- `pipelineId` (String, Required) - Unique identifier for this pipeline instance
- `startedAt` (LocalDateTime, Required) - Automatically set via `@PrePersist` callback
- `completedAt` (LocalDateTime, Optional) - Set when run completes or fails
- `status` (RunStatus Enum, Required) - `IN_PROGRESS`, `COMPLETED`, or `FAILED`
- `input` (JSONB) - Initial input to the pipeline
- `output` (JSONB) - Final output of the pipeline
- `steps` (OneToMany relationship) - List of steps in this run

**Database Indexes:**
- `idx_pipeline_type` on `pipeline_type`
- `idx_pipeline_id` on `pipeline_id`
- `idx_status` on `status`
- `idx_started_at` on `started_at`

**Key Features:**
- `@PrePersist` callback automatically sets `startedAt` to current timestamp if null
- Default status is `IN_PROGRESS`
- Cascading delete: Deleting a run deletes all associated steps and candidates

#### XRayStep

Represents a single step within a pipeline execution.

**Fields:**
- `stepId` (UUID, Primary Key) - Auto-generated unique identifier
- `run` (ManyToOne relationship to XRayRun) - Parent run
- `runId` (UUID, Read-only) - Foreign key to parent run
- `stepName` (String, Required) - Name of the step (e.g., "keyword_generation", "filtering")
- `stepType` (String, Required) - Type of step (e.g., "llm", "api", "filter", "ranking")
- `order` (Integer, Required) - Sequence order in the pipeline
- `startedAt` (LocalDateTime, Required) - When the step started
- `completedAt` (LocalDateTime, Optional) - When the step completed
- `status` (StepStatus Enum, Required) - `SUCCESS`, `FAILED`, or `SKIPPED`
- `input` (JSONB) - Input data for this step
- `output` (JSONB) - Output data from this step
- `reasoning` (String, Optional) - Explanation of why decisions were made
- `metadata` (JSONB) - Additional context information
- `candidates` (OneToMany relationship) - List of candidates evaluated in this step

**Database Indexes:**
- `idx_step_run_id` on `run_id`
- `idx_step_type` on `step_type`
- `idx_step_name` on `step_name`
- `idx_step_order` on `order`

**Key Features:**
- Default status is `SUCCESS`
- Steps are ordered by `order` field when retrieved
- Supports cross-pipeline queries via `stepType` and `stepName`

#### XRayCandidate

Represents a candidate evaluated within a step.

**Fields:**
- `candidateId` (UUID, Primary Key) - Auto-generated unique identifier
- `step` (ManyToOne relationship to XRayStep) - Parent step
- `stepId` (UUID, Read-only) - Foreign key to parent step
- `data` (JSONB, Required) - Candidate data (domain-specific)
- `score` (Double, Optional) - Numerical score if applicable
- `selected` (Boolean, Required, Default: false) - Whether this candidate was selected
- `rejectionReason` (String, Optional) - Reason for rejection if not selected
- `metadata` (JSONB, Optional) - Additional metadata

**Database Indexes:**
- `idx_candidate_step_id` on `step_id`
- `idx_candidate_selected` on `selected`
- `idx_candidate_score` on `score`

**Key Features:**
- Default `selected` value is `false`
- Supports filtering by selection status
- Enables analysis of rejection patterns

### Enums

#### RunStatus
- `IN_PROGRESS` - Run is currently executing
- `COMPLETED` - Run finished successfully
- `FAILED` - Run encountered an error

#### StepStatus
- `SUCCESS` - Step completed successfully
- `FAILED` - Step encountered an error
- `SKIPPED` - Step was skipped

### Design Rationale

- **Hierarchical Structure**: Natural representation of multi-step processes
- **Flexible JSON Fields**: Domain-agnostic, can handle any pipeline type
- **Step Type Classification**: Enables cross-pipeline queries (e.g., "all filtering steps")
- **Reasoning Fields**: Captures the "why" behind decisions
- **Pipeline Identification**: `pipelineType` + `pipelineId` supports multiple pipelines
- **Database Optimization**: Strategic indexes for common query patterns

---

## Repository Layer

The repository layer provides data access using Spring Data JPA. All repositories extend `JpaRepository` and provide both standard CRUD operations and custom query methods.

### XRayRunRepository

**Location**: `equal-db/src/main/java/com/task/founding/engineer/repository/XRayRunRepository.java`

**Key Methods:**
- `findByIdWithStepsAndCandidates(UUID runId)` - Custom query to fetch run with all steps and candidates (eager loading)
- `findByPipelineType(String pipelineType)` - Find runs by pipeline type
- `findByPipelineTypeAndStatus(String pipelineType, RunStatus status)` - Find runs by type and status
- `findByStartedAtBetween(LocalDateTime start, LocalDateTime end)` - Find runs within date range
- `findByPipelineTypeAndStartedAtBetween(String pipelineType, LocalDateTime start, LocalDateTime end)` - Combined filter

**Purpose**: Manages `XRayRun` entities and provides query methods for filtering runs.

### XRayStepRepository

**Location**: `equal-db/src/main/java/com/task/founding/engineer/repository/XRayStepRepository.java`

**Key Methods:**
- `findByRunIdOrderByOrderAsc(UUID runId)` - Get all steps for a run, ordered by sequence
- `findByStepType(String stepType)` - Find steps by type across all runs
- `findFilteringSteps(String pipelineType, LocalDateTime startDate, LocalDateTime endDate)` - Custom query for filtering steps with date range
- `findStepsWithHighRejectionRate(String stepType, double rejectionRate)` - Native SQL query to find steps with rejection rate above threshold

**Purpose**: Manages `XRayStep` entities and provides cross-pipeline query capabilities.

**Special Features:**
- Uses native SQL queries for complex calculations (rejection rates)
- Supports filtering by pipeline type and date ranges

### XRayCandidateRepository

**Location**: `equal-db/src/main/java/com/task/founding/engineer/repository/XRayCandidateRepository.java`

**Key Methods:**
- `findByStepId(UUID stepId)` - Get all candidates for a step
- `findByStepIdAndSelectedTrue(UUID stepId)` - Get only selected candidates
- `findByStepIdAndSelectedFalse(UUID stepId)` - Get only rejected candidates
- `countByStepId(UUID stepId)` - Count total candidates for a step
- `countByStepIdAndSelectedTrue(UUID stepId)` - Count selected candidates

**Purpose**: Manages `XRayCandidate` entities and provides filtering by selection status.

---

## Service Layer

The service layer contains business logic and orchestrates operations across repositories. All services are interfaces with corresponding implementations.

### RunService

**Interface**: `equal-db/src/main/java/com/task/founding/engineer/service/RunService.java`  
**Implementation**: `equal-db/src/main/java/com/task/founding/engineer/service/impl/RunServiceImpl.java`

**Methods:**
- `createRun(CreateRunRequestDTO request)` - Creates a new run with `IN_PROGRESS` status
- `getRunById(UUID runId)` - Retrieves a run with all steps and candidates
- `getAllRuns(String pipelineType, RunStatus status, LocalDateTime startDate, LocalDateTime endDate)` - Query runs with optional filters
- `completeRun(UUID runId, Object output)` - Marks run as completed and sets output
- `failRun(UUID runId)` - Marks run as failed

**Key Features:**
- Automatically sets `startedAt` timestamp (via entity callback)
- Supports complex filtering combinations (pipeline type, status, date range)
- Date range filtering takes precedence over status filtering
- Throws `RuntimeException` if run not found

### StepService

**Interface**: `equal-db/src/main/java/com/task/founding/engineer/service/StepService.java`  
**Implementation**: `equal-db/src/main/java/com/task/founding/engineer/service/impl/StepServiceImpl.java`

**Methods:**
- `createStep(UUID runId, CreateStepRequestDTO request)` - Creates a step and optionally creates candidates
- `getStepById(UUID stepId)` - Retrieves a step by ID
- `getStepsByRunId(UUID runId)` - Gets all steps for a run, ordered by sequence
- `getStepsByType(String stepType)` - Gets all steps of a specific type across runs
- `completeStep(UUID stepId, Object output, String reasoning)` - Completes a step with output and optional reasoning

**Key Features:**
- Automatically creates candidates if provided in the request
- Uses `CandidateService` for candidate creation (separation of concerns)
- Throws `RuntimeException` if run or step not found
- Sets default step status to `SUCCESS`

### CandidateService

**Interface**: `equal-db/src/main/java/com/task/founding/engineer/service/CandidateService.java`  
**Implementation**: `equal-db/src/main/java/com/task/founding/engineer/service/impl/CandidateServiceImpl.java`

**Methods:**
- `createCandidate(UUID stepId, CreateCandidateRequestDTO request)` - Creates a single candidate
- `createCandidates(UUID stepId, List<CreateCandidateRequestDTO> requests)` - Batch creates multiple candidates
- `getCandidatesByStepId(UUID stepId, Boolean selected)` - Gets candidates with optional selection filter
- `getSelectedCandidates(UUID stepId)` - Gets only selected candidates
- `getRejectedCandidates(UUID stepId)` - Gets only rejected candidates

**Key Features:**
- Defaults `selected` to `false` if not provided
- Supports batch operations for performance
- Throws `RuntimeException` if step not found
- Provides convenient methods for filtered retrieval

### AnalyticsService

**Interface**: `equal-db/src/main/java/com/task/founding/engineer/service/AnalyticsService.java`  
**Implementation**: `equal-db/src/main/java/com/task/founding/engineer/service/impl/AnalyticsServiceImpl.java`

**Methods:**
- `getFilteringStats(String pipelineType, LocalDateTime startDate, LocalDateTime endDate)` - Calculates filtering statistics

**Key Features:**
- Aggregates statistics across filtering steps
- Calculates total input/output candidates, rejection rates (average, min, max)
- Supports filtering by pipeline type and date range
- Returns zero values if no filtering steps found

**Statistics Calculated:**
- Total filtering steps
- Total input candidates
- Total output candidates (selected)
- Average rejection rate
- Minimum rejection rate
- Maximum rejection rate

---

## Controller Layer

The controller layer exposes REST API endpoints. All controllers use DTOs for request/response and delegate to service layer.

### RunController

**Location**: `equal-api/src/main/java/com/task/founding/engineer/api/controller/RunController.java`  
**Base Path**: `/api/v1/runs`

**Endpoints:**
- `POST /api/v1/runs` - Create a new run
  - Request: `CreateRunRequestDTO`
  - Response: `ApiResponse<IdResponseDTO>`
  
- `GET /api/v1/runs/{runId}` - Get run by ID
  - Response: `ApiResponse<RunResponseDTO>`
  
- `GET /api/v1/runs` - Get all runs with optional filters
  - Query Parameters: `pipelineType`, `status`, `startDate`, `endDate`
  - Response: `ApiResponse<List<RunResponseDTO>>`
  
- `PUT /api/v1/runs/{runId}/complete` - Complete a run
  - Request Body: Optional output object
  - Response: `ApiResponse<Void>`
  
- `PUT /api/v1/runs/{runId}/fail` - Mark run as failed
  - Response: `ApiResponse<Void>`

**Features:**
- Date range filtering using ISO 8601 format
- Validates request DTOs using Jakarta Validation
- Returns standardized `ApiResponse` wrapper

### StepController

**Location**: `equal-api/src/main/java/com/task/founding/engineer/api/controller/StepController.java`  
**Base Path**: `/api/v1`

**Endpoints:**
- `POST /api/v1/runs/{runId}/steps` - Create a step
  - Request: `CreateStepRequestDTO`
  - Response: `ApiResponse<IdResponseDTO>`
  
- `GET /api/v1/steps/{stepId}` - Get step by ID
  - Response: `ApiResponse<StepResponseDTO>`
  
- `GET /api/v1/runs/{runId}/steps` - Get all steps for a run
  - Response: `ApiResponse<List<StepResponseDTO>>`
  
- `GET /api/v1/steps` - Get steps by type
  - Query Parameter: `stepType` (optional)
  - Response: `ApiResponse<List<StepResponseDTO>>`
  - Returns empty list if `stepType` not provided
  
- `PUT /api/v1/steps/{stepId}/complete` - Complete a step
  - Request Body: Optional `{ output, reasoning }`
  - Response: `ApiResponse<Void>`

**Features:**
- Supports creating steps with candidates in a single request
- Extracts `output` and `reasoning` from request body
- Validates required fields (stepName, stepType, order)

### CandidateController

**Location**: `equal-api/src/main/java/com/task/founding/engineer/api/controller/CandidateController.java`  
**Base Path**: `/api/v1/steps/{stepId}/candidates`

**Endpoints:**
- `POST /api/v1/steps/{stepId}/candidates` - Create a single candidate
  - Request: `CreateCandidateRequestDTO`
  - Response: `ApiResponse<IdResponseDTO>`
  
- `POST /api/v1/steps/{stepId}/candidates/batch` - Create multiple candidates
  - Request: `BatchCreateCandidatesRequestDTO`
  - Response: `ApiResponse<IdResponseDTO>` (contains list of IDs)
  
- `GET /api/v1/steps/{stepId}/candidates` - Get candidates for a step
  - Query Parameter: `selected` (true/false, optional)
  - Response: `ApiResponse<List<CandidateResponseDTO>>`
  
- `GET /api/v1/steps/{stepId}/candidates/selected` - Get only selected candidates
  - Response: `ApiResponse<List<CandidateResponseDTO>>`
  
- `GET /api/v1/steps/{stepId}/candidates/rejected` - Get only rejected candidates
  - Response: `ApiResponse<List<CandidateResponseDTO>>`

**Features:**
- Supports both single and batch candidate creation
- Provides convenient endpoints for filtered retrieval
- Validates candidate data structure

### AnalyticsController

**Location**: `equal-api/src/main/java/com/task/founding/engineer/api/controller/AnalyticsController.java`  
**Base Path**: `/api/v1/analytics`

**Endpoints:**
- `GET /api/v1/analytics/filtering_stats` - Get filtering statistics
  - Query Parameters: `pipelineType` (optional), `startDate` (optional), `endDate` (optional)
  - Response: `ApiResponse<FilteringStatsResponseDTO>`

**Features:**
- Provides aggregated analytics for filtering steps
- Supports filtering by pipeline type and date range
- Returns comprehensive statistics

---

## DTOs and Converters

### Request DTOs

**Location**: `equal-db/src/main/java/com/task/founding/engineer/dto/request/`

- **CreateRunRequestDTO**: `pipelineType`, `pipelineId`, `input`
- **CreateStepRequestDTO**: `stepName`, `stepType`, `order`, `input`, `output`, `reasoning`, `metadata`, `candidates[]`
- **CreateCandidateRequestDTO**: `data`, `score`, `selected`, `rejectionReason`, `metadata`
- **BatchCreateCandidatesRequestDTO**: `candidates[]` (list of `CreateCandidateRequestDTO`)

### Response DTOs

**Location**: `equal-db/src/main/java/com/task/founding/engineer/dto/response/`

- **RunResponseDTO**: Complete run information with nested steps
- **StepResponseDTO**: Step information with nested candidates
- **CandidateResponseDTO**: Candidate information
- **FilteringStatsResponseDTO**: Analytics statistics
- **IdResponseDTO**: Simple ID response (single UUID or list of UUIDs)
- **ApiResponse<T>**: Standardized API response wrapper with `success`, `message`, and `data` fields

### Converters

**Location**: `equal-api/src/main/java/com/task/founding/engineer/api/controller/converter/`

Converters transform entity objects to response DTOs:
- **RunConverter**: `XRayRun` → `RunResponseDTO`
- **StepConverter**: `XRayStep` → `StepResponseDTO`
- **CandidateConverter**: `XRayCandidate` → `CandidateResponseDTO`

**Purpose**: Separates entity structure from API contract, allowing independent evolution of internal models and external API.

---

## Exception Handling

### GlobalExceptionHandler

**Location**: `equal-api/src/main/java/com/task/founding/engineer/api/controller/exception/handler/GlobalExceptionHandler.java`

**Features:**
- Centralized exception handling using `@RestControllerAdvice`
- Converts exceptions to standardized `ApiResponse` format
- Handles validation errors (`MethodArgumentNotValidException`)
- Handles generic exceptions with appropriate HTTP status codes

**Response Format:**
```json
{
  "success": false,
  "message": "Error message",
  "data": null
}
```

---

## Key Architectural Decisions

### 1. Multi-Module Structure

**Decision**: Split into `equal-db`, `equal-api`, and `founding-engineer-app` modules.

**Rationale**:
- Clear separation of concerns
- `equal-db` can be reused independently
- Easier testing and maintenance
- Better dependency management

### 2. JSONB for Flexible Data

**Decision**: Use PostgreSQL JSONB for `input`, `output`, `data`, and `metadata` fields.

**Rationale**:
- Domain-agnostic design
- Supports any pipeline type without schema changes
- PostgreSQL JSONB provides indexing and querying capabilities
- Flexible for future requirements

### 3. Hierarchical Entity Structure

**Decision**: Three-level hierarchy (Run → Step → Candidate).

**Rationale**:
- Natural representation of multi-step processes
- Enables efficient querying at each level
- Supports cascading operations
- Clear ownership and relationships

### 4. Service Layer Abstraction

**Decision**: Interface-based service layer with implementations.

**Rationale**:
- Enables testing with mocks
- Allows future implementation swapping
- Clear contract definition
- Better separation of concerns

### 5. DTO Pattern

**Decision**: Use DTOs for all API requests/responses.

**Rationale**:
- Decouples API contract from internal entity structure
- Allows API versioning
- Provides validation boundaries
- Enables API evolution without entity changes

### 6. Standardized API Response

**Decision**: Wrap all responses in `ApiResponse<T>`.

**Rationale**:
- Consistent API structure
- Easy to add metadata (pagination, errors)
- Better error handling
- Client-side consistency

### 7. Native SQL for Complex Queries

**Decision**: Use native SQL queries for rejection rate calculations.

**Rationale**:
- JPQL limitations with arithmetic operations
- Better performance for complex calculations
- PostgreSQL-specific optimizations
- Handles division by zero safely

### 8. Automatic Timestamp Management

**Decision**: Use `@PrePersist` callback for `startedAt` timestamps.

**Rationale**:
- Ensures timestamps are always set
- Reduces boilerplate in service layer
- Consistent timestamp handling
- Database-level guarantee

---

## Database Schema

### Tables

1. **xray_runs**
   - Primary Key: `run_id` (UUID)
   - Indexes: `pipeline_type`, `pipeline_id`, `status`, `started_at`

2. **xray_steps**
   - Primary Key: `step_id` (UUID)
   - Foreign Key: `run_id` → `xray_runs.run_id`
   - Indexes: `run_id`, `step_type`, `step_name`, `order`

3. **xray_candidates**
   - Primary Key: `candidate_id` (UUID)
   - Foreign Key: `step_id` → `xray_steps.step_id`
   - Indexes: `step_id`, `selected`, `score`

### Relationships

- **XRayRun** → **XRayStep**: One-to-Many (Cascade: ALL, Orphan Removal: true)
- **XRayStep** → **XRayCandidate**: One-to-Many (Cascade: ALL, Orphan Removal: true)

### Constraints

- `startedAt` is automatically set via `@PrePersist` callback
- Foreign keys enforce referential integrity
- Cascading delete ensures data consistency

---

## Technology Stack

- **Framework**: Spring Boot 4.0.1
- **Database**: PostgreSQL with JSONB support
- **ORM**: Spring Data JPA / Hibernate
- **Build Tool**: Maven
- **Java Version**: 21
- **Validation**: Jakarta Validation
- **Testing**: JUnit 5, Mockito

---

## API Response Format

All API responses follow this structure:

```json
{
  "success": true,
  "message": "Optional success message",
  "data": { ... }
}
```

**Error Response:**
```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

---

## Query Patterns

### Cross-Pipeline Queries

The system supports querying steps across different pipelines using:
- `stepType`: Standardized step types (e.g., "filter", "llm", "api")
- `stepName`: Descriptive step names (e.g., "price_filter", "relevance_filter")
- `pipelineType`: Filter by pipeline type

### Date Range Filtering

All date parameters use ISO 8601 format: `YYYY-MM-DDTHH:mm:ss`

Example: `2024-01-15T10:30:00`

### Filtering Combinations

The API supports various filter combinations:
- Pipeline type only
- Status only
- Date range only
- Pipeline type + status
- Pipeline type + date range
- All filters combined

---

## Testing Strategy

### Unit Tests

- **Service Layer**: Mock repositories, test business logic
- **Controller Layer**: Mock services, test HTTP layer
- **Repository Layer**: Integration tests with test database

### Test Structure

- Service tests: `equal-db/src/test/java/com/task/founding/engineer/service/impl/`
- Controller tests: `equal-api/src/test/java/com/task/founding/engineer/api/controller/`

### Test Coverage

- Success scenarios
- Error scenarios (not found, validation errors)
- Edge cases (empty lists, null values)
- Filter combinations
- Batch operations

---

## Conclusion

This architecture provides a robust, scalable foundation for tracking and analyzing multi-step decision processes. The modular design, flexible data model, and comprehensive API enable debugging, optimization, and cross-pipeline analysis while maintaining clear separation of concerns and testability.
