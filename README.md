# BDD TestGen Plugin

<!-- Plugin description -->
BDD TestGen is an IntelliJ IDEA plugin designed to facilitate the process of generating BDD (Behavior-Driven Development) artifacts from user stories. By integrating with powerful Large Language Models (LLMs), this plugin helps you create comprehensive test scenarios directly within your IDE.
<!-- Plugin description end -->

This document provides instructions for manual installation, configuration, and usage of the BDD TestGen plugin.

## Prerequisites

Before you begin, ensure you have the following installed:

*   **IntelliJ IDEA:** Version 2025.2 or later.
*   *Note: BDD TestGen is now 100% native (Java/Kotlin) and **no longer requires Python** or any external dependencies.*

## Installation

To manually install the BDD TestGen plugin, follow these steps:

1.  **Download the Plugin:**
    Navigate to the "Releases" section of this repository and download the latest `.zip` file of the plugin.

2.  **Install from Disk:**
    *   Open IntelliJ IDEA.
    *   Go to `File` -> `Settings` (or `IntelliJ IDEA` -> `Preferences` on macOS).
    *   Select the "Plugins" tab.
    *   Click on the gear icon (⚙️) and choose "Install Plugin from Disk...".
    *   In the file browser, locate and select the downloaded `.zip` file.
    *   Click "OK" and restart IntelliJ IDEA when prompted.

## Configuration

Before using the plugin, you need to configure your desired LLM profile:

1.  **Access Settings:**
    In the IntelliJ IDEA menu, go to `Tools` -> `Change BDDTestGen Settings`.
2.  **Enter Your Settings and Credentials:**
    A dialog box will appear. Select the LLM you wish to use from the available profiles (e.g., ChatGPT, Gemini, DeepSeek). Fill in the necessary information (e.g., API keys, model names).
    *The plugin comes pre-configured to hit the official endpoints for these providers.*
3.  **Save and Select:**
    Click "OK" to save your settings. The selected LLM profile is now active and future runs will use it.

## Usage

To generate BDD test cases from a user story:

1. **Create or open a project**
	If already inside a project, This can be done from the Idea menu from "New" -> "Project" or "Open" -> <your_project_folder>.
	If in the Idea home screen, click "New Project" or "Open" for an existing one.
2. **Locate User Story:**
    In the Project view, find the file containing the user story you want to generate test cases for.
3.  **Run BDDTestGen:**
	Right click your user story or existing feature file to be enhanced.
4.  **Generate Test cases:**
	From the context menu that popped up when right clicking, select "Run BDDTestGen". The plugin will then process the file using your configured LLM and generate the corresponding BDD tests.

## CLI Usage (Command Line Interface)

BDDTestGen supports a fully automated, standalone CLI built with the Clikt framework. You can execute it directly from your terminal using the compiled `.jar` file or Gradle wrapper.

### Running the CLI

To see all available options and help:
```bash
./gradlew runCLI --args="--help"
```

To execute a generation:
```bash
./gradlew runCLI --args="--config <configFilePath> <inputFilePath>"
```

*   **`--config` (or `-c`)**: Path to a JSON configuration file defining the LLM profiles.
*   **`<inputFilePath>`**: Path to the plain-text user story file.

### Running from Compiled JAR

To run the CLI standalone without Gradle, you can package the plugin distribution and run it directly with `java` by loading the compiled class files and dependencies in the classpath:

1. **Package the Distribution:**
   Run the following task in the project root to generate the packaged `.zip` file:
   ```bash
   ./gradlew buildPlugin
   ```
   This will generate the distribution zip file in `BDDTestGen source/build/distributions/BDDTestGen-<version>.zip`.

2. **Unzip the Distribution:**
   Extract the generated `.zip` file. The extracted folder contains a `lib/` directory containing the main `BDDTestGen-<version>.jar` alongside all required dependencies (Clikt, kotlinx-serialization, kotlinx-coroutines, etc.).

3. **Execute the JAR:**
   Navigate into the extracted folder (or make sure the `lib` directory is accessible) and run the main class `org.jetbrains.plugins.featurefilegenerator.cli.BatchGenerateFeatureCLIKt` using the wildcard `lib/*` to load all jars into the classpath:

   *   **Command (Windows/Linux/macOS):**
       ```bash
       java -cp "lib/*" org.jetbrains.plugins.featurefilegenerator.cli.BatchGenerateFeatureCLIKt --config <configFilePath> <inputFilePath>
       ```

   *(Note: Make sure to surround `"lib/*"` with double quotes so your terminal doesn't expand the asterisk wildcard).*


### Configuration JSON Format

The CLI is completely native. You provide the JSON configuration with the required parameters, and the Kotlin engine takes care of the REST HTTP calls automatically.

Example `config.json`:
```json
{
  "llms": [
    {
      "name": "Gemini",
      "scriptFilePath": "native",
      "command": "native",
      "namedParameters": [
        { "type": "string", "argName": "--api_key", "value": "YOUR_API_KEY_HERE" },
        { "type": "string", "argName": "--model", "value": "gemini-3-flash" },
        { "type": "double", "argName": "--temperature", "value": 0.7 },
        { "type": "string", "argName": "--prompt_instruction_path", "value": "prompt.txt" },
        { "type": "string", "argName": "--output_dir_path", "value": "output_folder" }
      ]
    }
  ]
}
```

The CLI will dynamically load the configuration, execute the LLMs concurrently (if in a batch), and output the generated `.feature` files.


