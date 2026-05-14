# BDDTestGen Architecture Overview

This diagram represents the modernized architecture of BDDTestGen, following Clean Architecture principles with a clear separation between Domain, Application, and Infrastructure layers.

```mermaid
graph TD
    subgraph UI_Layer [UI Layer - Infrastructure]
        IDE[IntelliJ IDE Plugin]
        CLI[Batch CLI - Clikt]
    end

    subgraph Domain_Layer [Domain Layer - Business Logic]
        Provider[ILLMConfigProvider]
        Model[LLMModelConfig Model]
    end

    subgraph App_Layer [Application Layer - Orchestration]
        Executor[LLMExecutor Engine]
    end

    subgraph Adapters [Infrastructure Adapters]
        Settings[LLMSettings - XML Persistence]
        SettingsCLI[LLMSettingsCLI - JSON Parser]
    end

    subgraph Execution_Engines [Execution Engines]
        Native[Native Client - HttpClient]
        External[External Script Connector - ProcessBuilder]
    end

    IDE --> Settings
    CLI --> SettingsCLI
    
    Settings -- "implements / toDomain()" --> Provider
    SettingsCLI -- "implements / toDomain()" --> Provider
    
    Executor -- "requests config" --> Provider
    Provider -- "returns" --> Model
    
    Executor -- "IF native" --> Native
    Executor -- "IF script" --> External
    
    Native -- "REST call" --> OpenAI_Gemini[LLM APIs]
    External -- "Executes" --> PyBash[Custom Python/Bash Scripts]

    %% Styling
    classDef domain fill:#e1f5fe,stroke:#01579b,stroke-width:2px;
    classDef infra fill:#fff3e0,stroke:#e65100,stroke-width:2px;
    classDef app fill:#f3e5f5,stroke:#4a148c,stroke-width:2px;
    
    class Provider,Model domain;
    class Settings,SettingsCLI,IDE,CLI infra;
    class Executor app;
```

### Component Details

*   **Domain Layer**: Pure business logic and contracts.
*   **Infrastructure Adapters**: Logic to persist and read configurations (XML for IDE, JSON for CLI).
*   **Application Layer (LLMExecutor)**: Orchestrates the generation process, deciding between the Native Kotlin engine or the External Script Connector for custom integrations.
*   **External Interaction**: Native calls via `HttpClient` ensure zero-dependency execution for standard models.