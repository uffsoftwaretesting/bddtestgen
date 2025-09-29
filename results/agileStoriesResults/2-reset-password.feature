Feature: Password Reset

  Scenario: Successfully reset password
    Given a user has forgotten their password
    When the user requests a password reset with <email*>
    And the user inputs a valid <new_password*> and confirms it
    Then the password should be successfully reset
    And the user should receive a confirmation message

    Examples:
      | email*               | new_password*   |
      | user@example.com     | NewPass123!     |
      | test@domain.co       | Password456$    |
      | valid@email.org      | SecurePass789@  |

  Scenario: Fail to reset password due to invalid email
    Given a user has forgotten their password
    When the user requests a password reset with <email*>
    Then the user should receive an error message indicating invalid email

    Examples:
      | email*               |
      | invalidemail.com     |
      | @noaddress.com       |
      | user@.com            |

  Scenario: Fail to reset password due to weak new password
    Given a user has forgotten their password
    When the user requests a password reset with <email*>
    And the user inputs an invalid <new_password*> and confirms it
    Then the password should not be reset
    And the user should receive an error message indicating password requirements

    Examples:
      | email*               | new_password* |
      | user@example.com     | short         |
      | test@domain.co       | 123456        |
      | valid@email.org      | password      |

  Scenario: Fail to reset password due to mismatched confirmation
    Given a user has forgotten their password
    When the user requests a password reset with <email*>
    And the user inputs a valid <new_password*> but mismatches it in confirmation
    Then the password should not be reset
    And the user should receive an error message indicating mismatched passwords

    Examples:
      | email*               | new_password* |
      | user@example.com     | NewPass123!   |
      | test@domain.co       | Password456$  |
      | valid@email.org      | SecurePass789@|