---
name: bddtestgen
description: Generates BDD Gherkin feature scenarios and test data from natural language user stories using the exact scientific guidelines (EP/BVA) of the BDDTestGen tool. Use this whenever the user requests generating BDD features, Cucumber scenarios, or running BDDTestGen on a user story file.
---

# BDDTestGen Declarative Agent Skill

This skill allows you (the agent) to generate Behavior Driven Development (BDD) `.feature` files natively

## When to Use This Skill
- Whenever the user asks to generate BDD scenarios, Gherkin/Cucumber features, or test cases from a user story file (such as `user-story.txt`).

## How to Execute the Skill

Instead of executing external compilers, you can act directly as the `BDDTestGen` engine and generate the Gherkin scenarios yourself, saving the result to the designated output folder to maintain compatibility with the workspace structures.

### Step 1: Read the User Story
Read the natural language user story provided by the user (usually in `user-story.txt` or similar).

### Step 2: Apply the Scientific BDD Generation Guidelines
Generate a single `.feature` Gherkin file. You must act according to the following scientific guidelines of the BDDTestGen tool:

1. **BDD Concepts:** Structure the file into a single `Feature`, describing the desired behavior; then elaborate a `Scenario Outline` detailing specific use cases; and finally specify the `Examples` block immediately below each Scenario Outline.
2. **Equivalence Partitioning (EP) & Boundary Value Analysis (BVA):** You must populate the `Examples` tables with a strategic range of concrete data values that satisfy the EP/BVA criteria (e.g., negative tests, bounds, empty inputs, valid/invalid equivalence classes).
3. **Gherkin Constraints:**
   - Keep the language of the Gherkin syntax and names matching the language of the input user story (e.g. Portuguese or English).
   - Preserve all variable names mentioned in the user story, enclosing them in angle brackets (`<variable_name>`) in the Scenario Outline.
   - Any word ending in an asterisk `*` in the user story denotes a required field. Include specific edge cases in the Examples to test missing inputs for these fields.
   - Only return the raw Gherkin content without notes, conversation, or markdown fences.

### Step 3: Write the Output Feature File
Write the natively generated `.feature` Gherkin content directly to the designated output path:
- **Output Path:** `c:\Users\gusta\Projetos\overleaf\bddtestgen_official\BDDTestGen source\test_output\chatgpt_output.feature`

### Step 4: Present the Results
Show the beautifully generated Gherkin feature scenarios to the user and confirm that the file was successfully saved.
