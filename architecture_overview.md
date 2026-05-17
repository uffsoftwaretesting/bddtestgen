# BDDTestGen Architecture Overview

This diagram represents the modernized architecture of BDDTestGen, following Clean Architecture principles with a focus on the **Generic API Studio (No-Code)** integration.

```mermaid
graph TD
    subgraph UI_Layer [UI Layer - Infrastructure]
        IDE[IntelliJ IDE Plugin]
        CLI[Batch CLI - Clikt]
    end

    subgraph Domain_Layer [Domain Layer - Pure Business Logic]
        Provider[ILLMConfigProvider]
        Model[LLMModelConfig Model]
        Param[ModelParameter]
    end

    subgraph App_Layer [Application Layer - Orchestration]
        Executor[LLMExecutor Engine]
        Templates[Template Processor]
    end

    subgraph Adapters [Infrastructure Adapters]
        Settings[LLMSettings - XML Persistence]
        SettingsCLI[LLMSettingsCLI - JSON Parser]
    end

    subgraph Connectors [LLM Connectors]
        Native[Native Built-in Clients]
        GenericAPI[Generic API Studio - No-Code]
    end

    IDE --> Settings
    CLI --> SettingsCLI
    
    Settings -- "implements / toDomain()" --> Provider
    SettingsCLI -- "implements / toDomain()" --> Provider
    
    Executor -- "requests config" --> Provider
    Provider -- "returns" --> Model
    
    Executor --> Templates
    Templates -- "resolves {{vars}}" --> Connectors
    
    Native -- "REST call" --> BuiltIn[OpenAI / Gemini / DeepSeek]
    GenericAPI -- "Dynamic REST" --> AnyAPI[Any LLM API Provider]

    %% Styling
    classDef domain fill:#e1f5fe,stroke:#01579b,stroke-width:2px;
    classDef infra fill:#fff3e0,stroke:#e65100,stroke-width:2px;
    classDef app fill:#f3e5f5,stroke:#4a148c,stroke-width:2px;
    
    class Provider,Model,Param domain;
    class Settings,SettingsCLI,IDE,CLI infra;
    class Executor,Templates app;
```

### Component Details

*   **Domain Layer**: Pure business logic and contracts. Agnostic to IntelliJ or CLI.
*   **Infrastructure Adapters**: Logic to persist and read configurations (XML for IDE, JSON for CLI).
*   **Template Processor**: Scans request templates for `{{placeholder}}` patterns, enabling auto-discovery of UI variables.
*   **Generic API Studio**: A declarative connector that eliminates the need for external scripts. It handles dynamic URL construction, JSON payload generation, and Smart Authentication (header vs query param logic).
*   **Connectors**: Standardized interfaces for communicating with LLM providers using modern `java.net.http.HttpClient`.

### Key Benefits
1. **Zero-Config Execution**: No local Python or scripts required.
2. **Auto-Discovery**: UI fields are generated automatically from API templates.
3. **Portability**: The same configuration works in both the IDE and the CLI.