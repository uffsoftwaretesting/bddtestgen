Feature: Save shopping cart for later

  Scenario: Save cart with items
    Given I have items in my cart
    When I choose to save my cart for later
    Then the cart should be saved with <number_of_items> items

    Examples:
      | number_of_items |
      | 1               |
      | 5               |
      | 0               |
      | 100             |
      | 101             |

  Scenario: Save empty cart
    Given my cart is empty
    When I choose to save my cart for later
    Then the cart should be saved with <number_of_items> items

    Examples:
      | number_of_items |
      | 0               |

  Scenario: Save cart with invalid items
    Given my cart has invalid items
    When I choose to save my cart for later
    Then I should receive an error message <error_message>

    Examples:
      | invalid_items | error_message         |
      | " "           | "Invalid item error"  |
      | null          | "Invalid item error"  |
      | "invalid123"  | "Invalid item error"  |