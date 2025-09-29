Feature: Filter items by price

  Scenario: Filter items within a specific price range
    Given the item list is loaded
    When I filter items with a minimum price of <min_price> and a maximum price of <max_price>
    Then only items priced between <min_price> and <max_price> should be displayed

    Examples:
      | min_price | max_price |
      | 0         | 100       |
      | 50        | 150       |
      | 100       | 200       |
      | 0         | 0         |
      | 200       | 200       |
      | 99.99     | 199.99    |
      | 0         | 1000      |
      | 1         | 1         |
      | 0.01      | 10.01     |

  Scenario: Filter items with invalid price range
    Given the item list is loaded
    When I filter items with a minimum price of <min_price> and a maximum price of <max_price>
    Then an error message should be displayed

    Examples:
      | min_price | max_price |
      | -1        | 100       |
      | 100       | 50        |
      | 200       | -100      |
      | -50       | -10       |
      | 0         | -1        |