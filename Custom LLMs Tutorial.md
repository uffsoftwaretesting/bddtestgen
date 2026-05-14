# Integrating Custom LLMs into the BDDTestGen Plugin

This document outlines the process for integrating a custom Large Language Model (a.k.a. LLM) into the BDDTestGen plugin. With the migration to a native Kotlin architecture, integrating custom models is easier than ever and requires **zero external scripts or dependencies** like Python.

## Overview

The plugin natively communicates with LLM APIs using standard HTTP requests. To add a custom LLM, you only need to provide a **Specification File (`.json`)**. This file defines the configuration parameters (like API Key, Model, and Custom Endpoint) and dictates how the UI in the plugin's settings will be rendered.

By analyzing the name of your LLM or its custom endpoint, the plugin automatically routes the request using either the OpenAI-compatible or the Gemini-compatible format.

## Core Component: The Specification File (`.json`)

This JSON file is a list of objects, where each object defines a single configuration parameter for your LLM.

### Structure and Fields

Each object in the JSON array must contain the following fields:

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `name` | String | Yes | The display name of the parameter in the plugin's UI (e.g., "API Key"). |
| `argName` | String | Yes | The internal argument name that the plugin reads (e.g., `--api_key`). |
| `type` | String | Yes | The data type of the parameter (e.g., `string`, `float`, `boolean`). |
| `required` | Boolean | Yes | Specifies if this parameter is mandatory. |
| `ui_element` | String | Yes | The type of UI component to render for this parameter. See [UI Element Types](#ui-element-types) below. |
| `description`| String | Yes | A brief explanation of the parameter, which may be used for tooltips. |
| `default_value`| Any | Yes | A default value for the parameter. |
| `allowed_values`| Array/Object | No | A list of allowed values, used by `combobox` and `spinner` UI elements. |
| `step` | Number | No | The increment step for `spinner` UI elements. |

### Reserved Internal Arguments (`argName`)

The native Kotlin executor looks for specific `argName` values to construct the API request. To ensure your LLM works correctly, use the following `argName` conventions:

| `argName` | Description | Required |
| :--- | :--- | :--- |
| `--api_key` | The API Key for authenticating the request. | **Yes** |
| `--model` | The model identifier (e.g., `gpt-4`, `llama3`). | **Yes** |
| `--temperature` | Floating-point temperature for generation (e.g., `0.7`). | **Yes** |
| `--prompt_instruction_path` | (Or `--instruction_file`) Path to the instruction prompt `.txt`. | **Yes** |
| `--endpoint` | (Or `--api_base`) **Optional** Custom API endpoint URL. Useful for local models like LMStudio or Ollama (e.g., `http://localhost:1234/v1/chat/completions`). | No |

### Routing Logic

*   If your LLM Name contains **`gemini`** (case-insensitive), the plugin uses the Google Gemini REST format.
*   If your LLM Name contains **`deepseek`**, it points to `https://api.deepseek.com/chat/completions`.
*   **Default Behavior:** If the name doesn't match the above, the plugin assumes an **OpenAI-compatible** API and defaults to `https://api.openai.com/v1/chat/completions`.
*   **Custom URLs:** If you provide an `--endpoint` parameter, the plugin will override the default URLs and send an OpenAI-compatible JSON payload directly to your custom URL.

### Example (`custom_specifications.json`)

```json
[
  {
    "name": "Instruction Prompt Path",
    "argName": "--prompt_instruction_path",
    "type": "string",
    "required": true,
    "ui_element": "filePicker",
    "description": "Path to the predefined instruction",
    "default_value": ""
  },
  {
    "name": "API Key",
    "argName": "--api_key",
    "type": "string",
    "required": true,
    "ui_element": "textfield",
    "description": "API key for the model",
    "default_value": ""
  },
  {
    "name": "Model",
    "argName": "--model",
    "type": "string",
    "required": true,
    "ui_element": "combobox",
    "description": "The model to use",
    "allowed_values": [
      "llama-3-8b",
      "mistral-7b"
    ],
    "default_value": "llama-3-8b"
  },
  {
    "name": "Custom Endpoint",
    "argName": "--endpoint",
    "type": "string",
    "required": false,
    "ui_element": "textfield",
    "description": "Optional: Override the default API URL (e.g., for local LMStudio)",
    "default_value": "http://localhost:1234/v1/chat/completions"
  }
]
```

---

## How to Add a New LLM in IntelliJ IDEA

Once you have created your specification `.json` file, you can add it to the plugin:

1.  Navigate to **File > Settings > Tools > BDDTestGen**.
2.  Click on the dropdown menu at the top. It will show a list of existing LLM configurations.
3.  Select **"Insert new"** from the bottom of the list.
4.  A new set of fields will appear. Fill them out as follows:
    *   **LLM Name:** A unique, descriptive name for your LLM (e.g., "Local Llama 3"). *Note: If it contains 'gemini', it will use the Google format.*
    *   **Select Script File:** Type `native` (This field is kept for legacy compatibility but is ignored by the engine).
    *   **Select Configuration File:** Click "Browse" and locate your specification `.json` file.
    *   **Console Command:** Type `native`.
5.  After selecting the configuration file, the UI will dynamically render the parameters you defined in the JSON.
6.  Fill in the values (API Key, Model, Custom Endpoint, etc.).
7.  Click **"Apply"** or **"OK"** to save your new configuration.

Your custom, scriptless LLM is now fully configured and ready to generate Gherkin features!