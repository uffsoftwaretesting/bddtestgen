Feature: Update Profile Information

  Scenario: Successfully update profile with valid data
    Given First Name <FirstName> is entered in the form
    And Last Name <LastName> is entered in the form
    And Phone Number <PhoneNumber> is entered in the form
    And Date of Birth <DateOfBirth> is selected in the date picker
    And Address <Address> is entered in the form
    When I click the SAVE CHANGES button
    Then a success message "Your profile has been updated successfully." is displayed

    Examples:
      | FirstName | LastName | PhoneNumber | DateOfBirth | Address        |
      | John      | Doe      | 1234567890  | 2000-01-01  | 123 Main St    |
      | Jane      | Smith    | 0987654321  | 1995-12-31  | 456 Elm St     |
      | Emily     | Davis    |             | 1990-06-15  | 789 Oak St     |
      | Michael   | Brown    | 1112223333  | 1985-09-10  |                |
      | Sarah     | Johnson  |             | 1975-04-20  | 135 Pine St    |

  Scenario: Fail to update profile with missing required fields
    Given First Name <FirstName> is entered in the form
    And Last Name <LastName> is entered in the form
    When I click the SAVE CHANGES button
    Then an error message "This field is required!" is displayed

    Examples:
      | FirstName | LastName |
      |           | Doe      |
      | John      |          |
      |           |          |

  Scenario: Fail to update profile with invalid phone number
    Given First Name <FirstName> is entered in the form
    And Last Name <LastName> is entered in the form
    And Phone Number <PhoneNumber> is entered in the form
    When I click the SAVE CHANGES button
    Then an error message "Please enter a valid 10-digit phone number." is displayed

    Examples:
      | FirstName | LastName | PhoneNumber |
      | Alice     | Cooper   | 12345       |
      | Bob       | Marley   | abcdefghij  |
      | Charlie   | Sheen    | 1234abc567  |

  Scenario: Fail to update profile with underage date of birth
    Given First Name <FirstName> is entered in the form
    And Last Name <LastName> is entered in the form
    And Date of Birth <DateOfBirth> is selected in the date picker
    When I click the SAVE CHANGES button
    Then an error message "You must be at least 18 years old to use this service." is displayed

    Examples:
      | FirstName | LastName | DateOfBirth |
      | David     | Beckham  | 2008-05-20  |
      | Emma      | Watson   | 2010-10-10  |
      | Frank     | Ocean    | 2006-12-25  |