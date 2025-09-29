Feature: User Registration

  Scenario Outline: Successful user registration
    Given <FullName> is entered in the registration form
    And <Email> is entered in the registration form
    And <Password> is entered in the registration form
    And <ConfirmPassword> is entered in the registration form
    And the user agrees to the Terms and Conditions
    When the REGISTER button is clicked
    Then the user should be redirected to the Login screen
    And a success message "Registration successful! Please log in." should be displayed

    Examples:
      | FullName | Email                 | Password    | ConfirmPassword |
      | John Doe | john.doe@example.com  | Password1   | Password1       |
      | Jane Doe | jane.doe@example.com  | Pass1234    | Pass1234        |

  Scenario Outline: Missing mandatory fields
    Given <FullName> is entered in the registration form
    And <Email> is entered in the registration form
    And <Password> is entered in the registration form
    And <ConfirmPassword> is entered in the registration form
    And the user agrees to the Terms and Conditions
    When the REGISTER button is clicked
    Then an error message "This field is required!" should be displayed below the empty fields

    Examples:
      | FullName | Email | Password | ConfirmPassword |
      |          |       |          |                 |
      | John Doe |       |          |                 |
      |          | john.doe@example.com |              |                 |
      | John Doe | john.doe@example.com | Pass1234     |                 |
      | John Doe | john.doe@example.com |              | Pass1234        |

  Scenario Outline: Invalid email format
    Given <FullName> is entered in the registration form
    And <Email> is entered in the registration form
    And <Password> is entered in the registration form
    And <ConfirmPassword> is entered in the registration form
    And the user agrees to the Terms and Conditions
    When the REGISTER button is clicked
    Then an error message "Please enter a valid email address." should be displayed below the Email field

    Examples:
      | FullName | Email     | Password  | ConfirmPassword |
      | John Doe | john.doe  | Pass1234  | Pass1234        |
      | Jane Doe | @example.com | Password1 | Password1   |
      | Jane Doe | jane.doe@example | Pass1234 | Pass1234 |

  Scenario Outline: Password not meeting criteria
    Given <FullName> is entered in the registration form
    And <Email> is entered in the registration form
    And <Password> is entered in the registration form
    And <ConfirmPassword> is entered in the registration form
    And the user agrees to the Terms and Conditions
    When the REGISTER button is clicked
    Then an error message "Password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, and one number." should be displayed below the Password field

    Examples:
      | FullName | Email                | Password  | ConfirmPassword |
      | John Doe | john.doe@example.com | pass1234  | pass1234        |
      | Jane Doe | jane.doe@example.com | PASSWORD1 | PASSWORD1       |
      | Jane Doe | jane.doe@example.com | Password  | Password        |

  Scenario Outline: Passwords do not match
    Given <FullName> is entered in the registration form
    And <Email> is entered in the registration form
    And <Password> is entered in the registration form
    And <ConfirmPassword> is entered in the registration form
    And the user agrees to the Terms and Conditions
    When the REGISTER button is clicked
    Then an error message "Passwords do not match." should be displayed below the Confirm Password field

    Examples:
      | FullName | Email                | Password  | ConfirmPassword |
      | John Doe | john.doe@example.com | Password1 | password1       |
      | Jane Doe | jane.doe@example.com | Pass1234  | Pass4321        |

  Scenario Outline: Terms and Conditions not agreed
    Given <FullName> is entered in the registration form
    And <Email> is entered in the registration form
    And <Password> is entered in the registration form
    And <ConfirmPassword> is entered in the registration form
    And the user does not agree to the Terms and Conditions
    When the REGISTER button is clicked
    Then an error message "You must agree to the Terms and Conditions." should be displayed

    Examples:
      | FullName | Email                | Password  | ConfirmPassword |
      | John Doe | john.doe@example.com | Password1 | Password1       |
      | Jane Doe | jane.doe@example.com | Pass1234  | Pass1234        |

  Scenario Outline: Email already registered
    Given <FullName> is entered in the registration form
    And <Email> is entered in the registration form
    And <Password> is entered in the registration form
    And <ConfirmPassword> is entered in the registration form
    And the user agrees to the Terms and Conditions
    When the REGISTER button is clicked
    Then an error message "This email is already registered." should be displayed below the Email field

    Examples:
      | FullName | Email                 | Password  | ConfirmPassword |
      | John Doe | existing@example.com  | Password1 | Password1       |
      | Jane Doe | existent@example.com  | Pass1234  | Pass1234        |