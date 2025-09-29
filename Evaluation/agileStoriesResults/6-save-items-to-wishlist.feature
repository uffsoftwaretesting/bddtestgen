Feature: Save items to wishlist

  Scenario: Save an item to the wishlist
    Given I am a registered user
    And I am logged in
    When I select an item with ID <itemID>
    And I save the item to my wishlist
    Then the item with ID <itemID> should be added to my wishlist

    Examples:
      | itemID |
      | 1      |
      | 100    |
      | 999    |
      | 0      |
      | -1     |

  Scenario: Attempt to save an item to the wishlist without being logged in
    Given I am a registered user
    And I am not logged in
    When I select an item with ID <itemID>
    And I attempt to save the item to my wishlist
    Then I should receive an error message <errorMessage>

    Examples:
      | itemID | errorMessage        |
      | 1      | "Please log in"     |
      | 100    | "Please log in"     |
      | 999    | "Please log in"     |

  Scenario: Save a non-existent item to the wishlist
    Given I am a registered user
    And I am logged in
    When I select a non-existent item with ID <itemID>
    And I attempt to save the item to my wishlist
    Then I should receive an error message <errorMessage>

    Examples:
      | itemID | errorMessage          |
      | -1     | "Item does not exist" |
      | 1000   | "Item does not exist" |
      | 5000   | "Item does not exist" |