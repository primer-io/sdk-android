# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

### Essential Development Commands
```bash
# Clean and build debug
./gradlew clean assembleDebug

# Run all tests
./gradlew test

# Run static analysis (Detekt)
./gradlew detekt

# Generate coverage reports (Kover)
./gradlew koverMergedHtmlReport

# Publish to local Maven repository
./gradlew publishToMavenLocal
```

### Testing Commands
```bash
# Run single module tests
./gradlew :module-name:test

# Run instrumentation tests with orchestrator
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests "ClassName"
```

### Build Distribution
```bash
# Build and distribute via Fastlane
fastlane distribute

# Upload test build to BrowserStack
fastlane upload_test_build
```

## Architecture Overview

### Modular Structure
This is a **highly modularized Android SDK** (57 modules) following **Clean Architecture** principles:

#### Core Infrastructure
- **`:arch-core`** - Base architecture, DI, networking, validation framework
- **`:logging`** - Centralized logging with structured output
- **`:errors-core`** - Error handling with Result patterns
- **`:payments-core`** - Core payment processing logic

#### UI Modules
- **`:drop-in`** - Complete drop-in UI solution (legacy Views)
- **`:composable`** - Jetpack Compose UI with Clean Architecture
- **`:ui-core`** - Shared UI utilities and theming

#### Payment Methods
Each payment method is a separate module (`:google-pay`, `:paypal`, `:klarna`, `:stripe-ach`, etc.) following the same architectural patterns.

### Key Architectural Patterns

#### Clean Architecture Layers
- **Domain**: Use cases (`BaseInteractor`, `BaseSuspendInteractor`), validation rules
- **Data**: Repositories, network clients, serialization
- **Presentation**: MVVM with ViewModels, View Binding, Jetpack Compose

#### Dependency Injection
- **Custom DI Framework**: Manual DI with `SdkComponent`, `DISdkComponent`
- **Scoped Components**: Different scopes for SDK lifecycle management
- No external DI framework dependencies

#### Error Handling
- **Result Pattern**: Custom `Either` type for success/failure handling
- **Validation Framework**: Composable validation rules (`ValidationRule`, `ValidationRulesChain`)
- **Centralized Errors**: `APIError`, `HttpException` hierarchy

#### Reactive Programming
- **Kotlin Coroutines**: Structured concurrency throughout
- **Flow/StateFlow**: Reactive data streams
- **LiveData**: Legacy UI state management support

### Composable Module Architecture

The `:composable` module demonstrates modern Clean Architecture with Jetpack Compose:

```
/composable/
├── Primer.kt (Public API entry point)
├── /scope/ (Public scoped APIs)
└── /internal/
    ├── /di/ (DI container)
    ├── /data/ (Repositories, mappers)
    ├── /domain/ (Interactors, models)
    └── /presentation/ (ViewModels, screens, navigation)
```

**Key Features:**
- **Scope Pattern**: Type-safe APIs (`PrimerCheckoutScope`, `CardFormScope`)
- **Design System**: Auto-generated tokens from Style Dictionary
- **Navigation**: Event-based navigation with Jetpack Navigation
- **Material 3**: Custom theming with light/dark mode support

### Testing Strategy
- **JUnit 5**: Modern testing framework
- **MockK**: Kotlin-first mocking
- **Test Fixtures**: Shared utilities in `:arch-core:testFixtures`
- **Coverage**: Kover for code coverage reporting

### Code Quality
- **Detekt**: Static analysis with auto-correction enabled
- **Lint**: Custom baselines per module
- **SonarQube**: Code quality metrics integration
- **ProGuard/R8**: Obfuscation rules per module

## SDK-Specific Patterns

### Dual SDK Architecture
The SDK operates in two modes:
- **Drop-in UI** (`:drop-in`) - Complete UI solution with fragments
- **Headless** (`:headless-core`) - Programmatic control with custom UI

Both modes share core business logic but have different presentation layers.

### Decision Handler Pattern
Payment flows use decision handlers for 3DS, redirects, and custom flows:
```kotlin
// Implement PrimerDecisionHandler for custom payment flows
class CustomDecisionHandler : PrimerDecisionHandler {
    override fun handleDecision(decision: PrimerDecision): PrimerDecisionResult
}
```

### Client Token Lifecycle
- Client tokens expire and must be refreshed
- SDK handles token validation and expiry automatically
- Always check token validity before payment operations

### Payment Method Configuration
Payment methods are configured server-side but can be filtered client-side:
```kotlin
PrimerSettings.paymentMethodFilters = listOf("CARD", "GOOGLE_PAY")
```

## Common Development Workflows

### Adding New Payment Methods
1. Create new module following naming pattern (e.g., `:new-payment-method`)
2. Implement `PrimerPaymentMethod` interface
3. Add UI components if needed
4. Register in `:drop-in` and `:composable` if applicable
5. Add to BOM module
6. Update payment method factory

### Working with Validation
The SDK uses a composable validation system:
```kotlin
ValidationRulesChain.builder()
    .addRule(RequiredRule())
    .addRule(LengthRule(min = 4))
    .build()
```

### Network Layer Customization
- All network calls go through `PrimerHttpClient`
- Interceptors can be added for logging, auth, etc.
- Custom serialization framework (no Gson/Moshi dependency)

## Testing Patterns

### Payment Flow Testing
- Use `MockWebServer` for API responses
- Test decision handlers with mock decisions
- Validate payment method configurations
- Test token expiry scenarios

### UI Testing Approach
- Compose tests use `@get:Rule val composeTestRule = createComposeRule()`
- Fragment tests use FragmentScenario
- Integration tests run on BrowserStack

### Test Data Management
- Shared test fixtures in `:arch-core:testFixtures`
- Payment method test data in respective modules
- Mock client tokens for testing

## Security & Compliance

### ProGuard Configuration
Each module has specific ProGuard rules for:
- Payment processor SDKs
- Networking components
- Reflection-based code

### Secrets Management
- Use `secrets.defaults.properties` for non-sensitive defaults
- Local `secrets.properties` for development keys
- Never commit actual API keys or tokens

### PCI Compliance
- Card data never stored in logs or persistent storage
- Sensitive data cleared from memory after use
- Network communications use certificate pinning

## Debugging Common Issues

### DI-Related Problems
- Check `SdkComponent` initialization
- Verify module dependencies are properly registered
- Look for circular dependencies in DI graph

### Payment Flow Failures
- Check client token validity and expiry
- Verify payment method configuration server-side
- Check decision handler implementations
- Review network logs for API call failures

### UI Rendering Issues
- Verify design tokens are properly generated
- Check theme configuration (light/dark mode)
- Review Compose state management
- Validate ViewBinding setup in fragments

### Build Issues
- Run `./gradlew clean` for gradle cache issues
- Check version catalog consistency in `libs.versions.toml`
- Verify BOM version alignment across modules

## Development Guidelines

### Module Creation
When creating new modules, follow existing patterns:
1. Use `build.gradle.kts` with version catalogs
2. Apply common tooling scripts from `/tooling/`
3. Follow package structure: `data`, `domain`, `presentation`
4. Add to BOM module for version management

### Composable Development
For Compose-related work in `:composable`:
- Follow Clean Architecture layers strictly
- Use internal namespace for implementation details
- Leverage existing design tokens from Style Dictionary
- Integrate with existing DI container

### Validation & Input Handling
- Use existing validation framework in `:arch-core`
- Implement `ValidationRule` for new validation logic
- Leverage visual transformations for input formatting
- Follow error propagation patterns

### Payment Method Integration
- Each payment method should be a separate module
- Implement common interfaces from `:payment-methods-core`
- Follow headless-first approach with UI layer on top
- Use existing networking and serialization infrastructure

## Environment Configuration

### API Environment Setup
```kotlin
PrimerSettings.apiEnvironment = PrimerApiEnvironment.SANDBOX // or PRODUCTION
```

### Logging Configuration
```kotlin
PrimerSettings.loggingLevel = PrimerLoggingLevel.DEBUG // for development
```

### Custom HTTP Configuration
```kotlin
PrimerSettings.httpConfig = PrimerHttpConfig.builder()
    .timeout(30_000)
    .retryPolicy(customRetryPolicy)
    .build()
```

## Jira MCP Integration

### Overview
Claude Code has access to Jira via MCP (Model Context Protocol) integration with full read/write capabilities for project management automation.

### Available Capabilities
- **Project & Issue Management**: List projects, search issues, get detailed issue information
- **Issue Operations**: Create, edit, update, transition, and link issues
- **Sprint Management**: Add issues to sprints, view sprint information
- **Advanced Search**: Use JQL (Jira Query Language) for complex queries
- **Comments & Collaboration**: Add comments, view discussions

### Connection Details
- **Atlassian Cloud**: primerapi.atlassian.net
- **User**: Darius Partene (darius@primer.io)
- **Projects**: 17 accessible projects including ACC (Acceptance)
- **Permissions**: Full read/write access with create capabilities

### Common Usage Patterns

#### Creating Tickets from Planning Documents
```kotlin
// Example: Create ticket from planning document with all fields
mcp__atlassian__createJiraIssue(
    cloudId = "fede99c7-8c04-47a5-bda6-cefae2e9b1b9",
    projectKey = "ACC",
    issueTypeName = "Task",
    summary = "[Android SDK] Feature Implementation",
    description = "Structured description with context and files to modify",
    assignee_account_id = "712020:f0d41e97-a9b5-4beb-955c-ff1649fcea27",
    additional_fields = {
        "components": [{"name": "Checkout Android SDK"}],
        "customfield_10517": {"value": "Experience"},
        "customfield_10014": "ACC-4906",  // Epic link
        "customfield_10034": 8,  // Story points
        "customfield_10079": {  // Acceptance criteria
            "type": "doc",
            "version": 1,
            "content": [{
                "type": "bulletList",
                "content": [
                    {"type": "listItem", "content": [{"type": "paragraph", "content": [{"type": "text", "text": "First acceptance criterion"}]}]},
                    {"type": "listItem", "content": [{"type": "paragraph", "content": [{"type": "text", "text": "Second acceptance criterion"}]}]}
                ]
            }]
        }
    }
)

// Then add to sprint
mcp__atlassian__editJiraIssue(
    issueIdOrKey = "ACC-XXXX",
    fields = {"customfield_10020": 1877}  // Sprint ID
)
```

#### Required Fields for ACC Project
- **Components**: Must specify component (e.g., "Checkout Android SDK")
- **Acceptance Team**: Required custom field (customfield_10517)
  - Common values: "Experience", "Platform", "Security"
- **Assignee**: Use account ID format for assignment

#### Complete Field Mapping
- **Story Points**: `customfield_10034` (integer value)
- **Acceptance Criteria**: `customfield_10079` (structured document with bullet list format)
- **Epic Link**: `customfield_10014` (string with epic key, e.g., "ACC-4906")
- **Sprint**: `customfield_10020` (integer with sprint ID)
- **Components**: `components` (array with name objects: `[{"name": "Checkout Android SDK"}]`)
- **Acceptance Team**: `customfield_10517` (object with value: `{"value": "Experience"}`)
- **Assignee**: `assignee_account_id` (string with account ID format)

#### Sprint Assignment
```kotlin
// Get current active sprint
mcp__atlassian__searchJiraIssuesUsingJql(
    jql = "project = ACC AND sprint in openSprints() ORDER BY updated DESC",
    maxResults = 1,
    fields = ["customfield_10020"]
)

// Add ticket to active sprint
mcp__atlassian__editJiraIssue(
    issueIdOrKey = "ACC-XXXX",
    fields = {
        "customfield_10020": 1877  // Sprint ID - simple number format
    }
)
```

#### Searching Issues
```kotlin
// Search using JQL
mcp__atlassian__searchJiraIssuesUsingJql(
    jql = "project = ACC AND (summary ~ android OR description ~ android) ORDER BY updated DESC",
    maxResults = 10
)
```

### Board Integration
- **Team Expérience Board**: https://primerapi.atlassian.net/jira/software/c/projects/ACC/boards/469
- **Active Sprint**: "Exp Sprint 1" (ID: 1877)
- **Sprint Assignment**: Use customfield_10020 with sprint ID as integer

### Ticket Template (Based on ACC-5686)
```markdown
**Epic**: [Epic Name]
**Story Points**: [Number]
**Priority**: [High/Medium/Low]
**Time Estimate**: [X weeks traditional → Y weeks with Claude Code]

## Description
[Clear description of the work]

## Acceptance Criteria
- [ ] Criterion 1
- [ ] Criterion 2
- [ ] Criterion 3

## Current Status
[Current state/baseline]

## Files to Modify
- `/path/to/file1.kt`
- `/path/to/file2.kt`

## Related
[References to planning documents, other tickets, etc.]

🤖 Created automatically from planning document via Jira MCP integration.
```

### Common JQL Queries for Android SDK
```sql
-- Find Android-related tickets
project = ACC AND (summary ~ android OR description ~ android OR components = "Checkout Android SDK")

-- Find tickets in current sprint
project = ACC AND sprint in openSprints()

-- Find my assigned tickets
project = ACC AND assignee = currentUser()

-- Find tickets by component
project = ACC AND component = "Checkout Android SDK"

-- Find tickets by acceptance team
project = ACC AND "Acceptance Team" = "Experience"
```

### Integration Workflows
1. **Planning → Tickets**: Auto-create structured tickets from planning documents
2. **Code → Updates**: Link commits to tickets for progress tracking
3. **Sprint Management**: Programmatically manage sprint assignments
4. **Cross-Platform Sync**: Coordinate Android/iOS ticket dependencies
5. **Reporting**: Generate project insights from ticket data

### Error Handling
- **Bad Request**: Usually missing required fields (components, acceptance team)
- **Sprint Assignment**: Use simple integer format for sprint ID
- **Field Validation**: Check required fields via getJiraProjectIssueTypesMetadata

### Best Practices
- Always include structured acceptance criteria
- Reference planning documents in ticket descriptions
- Use consistent tagging (e.g., "[Android SDK]" prefix)
- Include file paths for development context
- Add time estimates for planning accuracy
- Use the 🤖 marker for automated ticket creation