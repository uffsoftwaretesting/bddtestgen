Feature: Create New Customer

  Scenario: Successfully create a new customer
    Given the <First Name> is entered in the form
    And the <Last Name> is entered in the form
    And the <Email> is entered in the form
    And the <Phone Number> is entered in the form
    And the <Status> is selected from the dropdown
    When I click the SAVE CUSTOMER button
    Then I should be redirected to the customer list with a success message "Customer created successfully."

    Examples:
      | First Name | Last Name | Email                 | Phone Number | Status  |
      | John       | Doe       | john.doe@example.com  | 1234567890   | Active  |
      | Jane       | Smith     | jane.smith@example.com| 0987654321   | Inactive|

  Scenario: Show error for missing required fields
    Given the <First Name> is entered in the form
    And the <Last Name> is entered in the form
    And the <Email> is entered in the form
    And the <Status> is selected from the dropdown
    When I click the SAVE CUSTOMER button
    Then I should see an error message "This field is required!" next to the missing <Field>

    Examples:
      | First Name | Last Name | Email                | Status  | Field     |
      |            | Doe       | john.doe@example.com | Active  | First Name|
      | John       |           | john.doe@example.com | Active  | Last Name |
      | John       | Doe       |                      | Active  | Email     |
      | John       | Doe       | john.doe@example.com |         | Status    |

  Scenario: Show error for invalid email format
    Given the <Email> is entered in the form
    When I click the SAVE CUSTOMER button
    Then I should see an error message "Please enter a valid email address."

    Examples:
      | Email          |
      | johndoe.com    |
      | john@doe       |
      | john.doe@.com  |

  Scenario: Show error for non-unique email
    Given the <Email> is entered in the form
    When I click the SAVE CUSTOMER button
    Then I should see an error message "This email address is already in use."

    Examples:
      | Email                 |
      | existing@example.com  |

  Scenario: Show error for invalid phone number
    Given the <Phone Number> is entered in the form
    When I click the SAVE CUSTOMER button
    Then I should see an error message "Please enter a valid 10-digit phone number."

    Examples:
      | Phone Number |
      | 12345        |
      | 12345678901  |
      | abcdefghij   |