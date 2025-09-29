Feature: Mention colleagues in comments

  Scenario: Successfully mention a colleague
    Given a comment is being composed
    When I type "@" followed by <colleague_name*>
    Then the colleague <colleague_name*> should receive a notification

    Examples:
      | colleague_name  |
      | JohnDoe         |
      | JaneSmith       |
      | AlexJohnson     |

  Scenario: Attempt to mention a colleague with an invalid name
    Given a comment is being composed
    When I type "@" followed by <colleague_name*>
    Then I should see an error message indicating invalid mention

    Examples:
      | colleague_name  |
      | ""              |
      | " "             |
      | "InvalidName!"  |