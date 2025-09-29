Feature: Track sprint velocity

  Scenario: Calculate sprint velocity based on completed story points
    Given the sprint has ended
    And <completedStoryPoints> story points were completed
    When I calculate the sprint velocity
    Then the sprint velocity should be <velocity>

    Examples:
      | completedStoryPoints | velocity |
      | 0                    | 0        |
      | 1                    | 1        |
      | 10                   | 10       |
      | 50                   | 50       |
      | 100                  | 100      |

  Scenario: Handle invalid story point entries
    Given the sprint has ended
    And <completedStoryPoints> story points were completed
    When I calculate the sprint velocity
    Then an error should be shown for <errorType>

    Examples:
      | completedStoryPoints | errorType    |
      | -1                   | negative     |
      | " "                  | empty        |
      | "abc"                | non-numeric  |