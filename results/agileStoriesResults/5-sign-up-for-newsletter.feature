Feature: Visitor sign-up for the newsletter

  Scenario: Successful newsletter sign-up
    Given a visitor is on the newsletter sign-up page
    When they enter their email address <email*>
    And they click the sign-up button
    Then they should see a confirmation message "Thank you for signing up!"

    Examples:
      | email*                  |
      | valid@example.com       |
      | user@sub.domain.com     |
      | test.email+alias@gmail.com |
      | 1234567890@domain.com   |
      | user.name@domain.co.uk  |

  Scenario: Sign-up with missing email
    Given a visitor is on the newsletter sign-up page
    When they leave the email address <email*> field empty
    And they click the sign-up button
    Then they should see an error message "Email is required."

    Examples:
      | email* |
      | ""     |

  Scenario: Sign-up with invalid email
    Given a visitor is on the newsletter sign-up page
    When they enter an invalid email address <email*>
    And they click the sign-up button
    Then they should see an error message "Please enter a valid email address."

    Examples:
      | email*           |
      | invalid-email    |
      | @no-local-part.com |
      | user@.com        |
      | user@domain..com |