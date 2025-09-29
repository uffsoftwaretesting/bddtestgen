Feature: Forgot Password

  Scenario: Send reset link with valid email
    Given the <Email> is entered in the Email field
    When I click the SEND RESET LINK button
    Then a password reset link should be sent to <Email>

    Examples:
      | Email                    |
      | existinguser@example.com |
      | user123@mail.com         |
      | test.user@domain.com     |

  Scenario: Send reset link with invalid email format
    Given the <Email> is entered in the Email field
    When I click the SEND RESET LINK button
    Then I should see the error message "Please enter a valid email address."

    Examples:
      | Email       |
      | invalid.com |
      | @missing.com|
      | user@.com   |
      | user@com    |

  Scenario: Send reset link with non-existing email
    Given the <Email> is entered in the Email field
    When I click the SEND RESET LINK button
    Then I should see the message "If this email address is in our database, we will send you a password reset link."

    Examples:
      | Email                     |
      | nonexisting@mail.com      |
      | unknownuser@domain.com    |
      | noreply@notregistered.com |

  Scenario: Email field left empty
    Given the <Email> is entered in the Email field
    When I click the SEND RESET LINK button
    Then I should see the error message "This field is required!"

    Examples:
      | Email |
      | ""    |