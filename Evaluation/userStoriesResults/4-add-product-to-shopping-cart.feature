Feature: Add Product to Shopping Cart

  Scenario: Successfully add a product with variations to the shopping cart
    Given the user is on the Product Details Page
    And <quantity> is entered in the Quantity input
    And <color> is selected from the Color dropdown
    And <size> is selected from the Size dropdown
    When the ADD TO CART button is clicked
    Then the system should display "<product_name> has been added to your cart."

    Examples:
      | quantity | color    | size   | product_name        |
      | 1        | Red      | Medium | T-Shirt             |
      | 5        | Blue     | Large  | Jacket              |
      | 10       | Green    | Small  | Hoodie              |
      | 50       | Black    | XL     | Sweater             |
      | 100      | White    | XXL    | Pants               |

  Scenario: Add a product with an invalid quantity
    Given the user is on the Product Details Page
    And <quantity> is entered in the Quantity input
    And <color> is selected from the Color dropdown
    And <size> is selected from the Size dropdown
    When the ADD TO CART button is clicked
    Then the system should display "Please enter a valid quantity."

    Examples:
      | quantity | color | size  |
      | 0        | Red   | Medium|
      | -1       | Blue  | Large |
      | " "      | Green | Small |

  Scenario: Add a product without selecting color and size
    Given the user is on the Product Details Page
    And <quantity> is entered in the Quantity input
    When the ADD TO CART button is clicked
    Then the system should display "Please select a color."
    And the system should display "Please select a size."

    Examples:
      | quantity |
      | 1        |
      | 5        |
      | 10       |

  Scenario: Add a product exceeding available stock
    Given the user is on the Product Details Page
    And <quantity> is entered in the Quantity input
    And <color> is selected from the Color dropdown
    And <size> is selected from the Size dropdown
    When the ADD TO CART button is clicked
    Then the system should display "The requested quantity is not available."

    Examples:
      | quantity | color | size  |
      | 101      | Red   | Medium|
      | 200      | Blue  | Large |
      | 500      | Green | Small |