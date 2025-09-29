Feature: Create a New Task

  Scenario: Successfully create a new task
    Given the <Task Title> is entered in the inputText
    And the <Project> is selected from the dropdown
    And the <Assignee> is selected from the user dropdown
    And the <Due Date> is picked from the datePicker
    And the <Priority> is selected from the dropdown
    And the <Description> is entered in the textarea
    When the CREATE TASK button is clicked
    Then the task is created
    And the user is redirected to the project's task board
    And a success message "Task '<Task Title>' created successfully" is displayed

    Examples:
      | Task Title              | Project    | Assignee  | Due Date   | Priority | Description     |
      | Implement login feature | Project A  | John Doe  | 2023-12-01 | Medium   | Implement login |
      | Design homepage         | Project B  | Jane Smith| 2023-12-05 | High     | Design UI       |
      | Write documentation     | Project C  |           | 2023-12-10 | Low      | Document API    |

  Scenario: Fail to create a task due to missing required fields
    Given the <Task Title> is entered in the inputText
    And the <Project> is selected from the dropdown
    And the <Due Date> is picked from the datePicker
    And the <Priority> is selected from the dropdown
    When the CREATE TASK button is clicked
    Then an error message "This field is required!" is displayed for each missing field

    Examples:
      | Task Title      | Project   | Due Date   | Priority |
      |                 | Project D | 2023-12-15 | Low      |
      | Update database |           | 2023-12-20 | Medium   |
      | Launch campaign | Project E |            | High     |

  Scenario: Fail to create a task due to Task Title exceeding character limit
    Given the <Task Title> is entered in the inputText
    And the <Project> is selected from the dropdown
    And the <Due Date> is picked from the datePicker
    And the <Priority> is selected from the dropdown
    When the CREATE TASK button is clicked
    Then an error message "Title cannot be longer than 100 characters." is displayed

    Examples:
      | Task Title                                                                                                                                     | Project   | Due Date   | Priority |
      | This is a very long task title that exceeds the one hundred character limit set by the business rules and should trigger an error message     | Project F | 2023-12-25 | Medium   |

  Scenario: Fail to create a task due to Due Date being in the past
    Given the <Task Title> is entered in the inputText
    And the <Project> is selected from the dropdown
    And the <Due Date> is picked from the datePicker
    And the <Priority> is selected from the dropdown
    When the CREATE TASK button is clicked
    Then an error message "Due date cannot be in the past." is displayed

    Examples:
      | Task Title    | Project   | Due Date   | Priority |
      | Test feature  | Project G | 2023-01-01 | High     |
      | Review design | Project H | 2023-10-01 | Medium   |