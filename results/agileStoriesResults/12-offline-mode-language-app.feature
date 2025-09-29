Feature: Offline Mode for Language Learning App

  Scenario: Access lessons in offline mode
    Given the app is launched
    And the user has previously downloaded lessons for offline use
    When the user selects <lesson_id> from the list of available lessons
    Then the lesson content should be displayed without requiring an internet connection

    Examples:
      | lesson_id |
      | 1         |
      | 2         |
      | 100       |
      | 0         |
      | 101       |
      | -1        |

  Scenario: Attempt to access non-downloaded lessons in offline mode
    Given the app is launched
    And the user has not downloaded lessons for offline use
    When the user selects <lesson_id> from the list of available lessons
    Then an error message should be displayed stating "Lesson not available offline"

    Examples:
      | lesson_id |
      | 1         |
      | 2         |
      | 100       |
      | 0         |
      | 101       |
      | -1        |

  Scenario: Download lesson for offline use
    Given the app is launched
    And the user is connected to the internet
    When the user selects <lesson_id> and chooses to download it for offline use
    Then the lesson should be downloaded and available for offline access

    Examples:
      | lesson_id |
      | 1         |
      | 2         |
      | 100       |
      | 0         |
      | 101       |
      | -1        |