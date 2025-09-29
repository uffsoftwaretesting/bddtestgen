Feature: Add a new shipping address

  Scenario: Successfully add a valid shipping address
    Given the user is on the "My Account - Address Book" screen
    And <FullName> is entered in the Full Name field
    And <AddressLine1> is entered in Address Line 1
    And <AddressLine2> is entered in Address Line 2
    And <City> is entered in City
    And <State> is selected in State/Province
    And <ZIPCode> is entered in ZIP/Postal Code
    And <Country> is selected in Country
    And Set as default address is <Default>
    When the user clicks the SAVE ADDRESS button
    Then the system should display "Address saved successfully."

    Examples:
      | FullName  | AddressLine1  | AddressLine2  | City     | State  | ZIPCode   | Country | Default |
      | John Doe  | 123 Main St   | Apt 4         | New York | NY     | 10001     | USA     | true    |
      | Jane Smith| 456 Elm St    |               | Chicago  | IL     | 60614     | USA     | false   |
      | Alice Lee | 789 Oak Ave   | Suite 2B      | Toronto  | ON     | M5H 2N2   | Canada  | true    |

  Scenario: Attempt to add an address with missing required fields
    Given the user is on the "My Account - Address Book" screen
    And <FullName> is entered in the Full Name field
    And <AddressLine1> is entered in Address Line 1
    And <City> is entered in City
    And <State> is selected in State/Province
    And <ZIPCode> is entered in ZIP/Postal Code
    And <Country> is selected in Country
    When the user clicks the SAVE ADDRESS button
    Then the system should display "This field is required!" for each missing field.

    Examples:
      | FullName | AddressLine1 | City    | State | ZIPCode | Country |
      |          | 123 Main St  | New York| NY    | 10001   | USA     |
      | John Doe |              | Chicago | IL    | 60614   | USA     |
      | Jane Doe | 456 Elm St   |         | IL    | 60614   | USA     |
      | Alice Lee| 789 Oak Ave  | Toronto |       | M5H 2N2 | Canada  |
      | Bob Brown| 101 Pine St  | Boston  | MA    |         | USA     |
      | Carol Ann| 202 Cedar Rd | Miami   | FL    | 33101   |         |

  Scenario: Enter an invalid ZIP/Postal Code
    Given the user is on the "My Account - Address Book" screen
    And <FullName> is entered in the Full Name field
    And <AddressLine1> is entered in Address Line 1
    And <City> is entered in City
    And <State> is selected in State/Province
    And <ZIPCode> is entered in ZIP/Postal Code
    And <Country> is selected in Country
    When the user clicks the SAVE ADDRESS button
    Then the system should display "Please enter a valid ZIP/Postal Code."

    Examples:
      | FullName  | AddressLine1  | City     | State  | ZIPCode | Country |
      | John Doe  | 123 Main St   | New York | NY     | 1234   | USA     |
      | Jane Smith| 456 Elm St    | Chicago  | IL     | 987654 | USA     |
      | Alice Lee | 789 Oak Ave   | Toronto  | ON     | 123    | Canada  |

  Scenario: Attempt to add an address when maximum limit is reached
    Given the user has already saved 5 addresses
    And the user is on the "My Account - Address Book" screen
    And <FullName> is entered in the Full Name field
    And <AddressLine1> is entered in Address Line 1
    And <City> is entered in City
    And <State> is selected in State/Province
    And <ZIPCode> is entered in ZIP/Postal Code
    And <Country> is selected in Country
    When the user clicks the SAVE ADDRESS button
    Then the system should display "You have reached the maximum number of saved addresses."

    Examples:
      | FullName  | AddressLine1  | City     | State  | ZIPCode | Country |
      | John Doe  | 123 Main St   | New York | NY     | 10001   | USA     |
      | Jane Smith| 456 Elm St    | Chicago  | IL     | 60614   | USA     |