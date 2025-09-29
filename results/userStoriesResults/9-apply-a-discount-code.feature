Feature: Apply a Discount Code

  Scenario: Successfully apply a valid discount code
    Given a shopping cart with items
    And a <discount_code> that exists and is active
    And the discount code has not expired
    And the discount code has not been used by the user
    When I click the APPLY button
    Then the cart total is updated to reflect the discount
    And a success message is shown: "Discount code '<discount_code>' applied successfully."

    Examples:
      | discount_code    |
      | CODE123          |
      | PROMO50          |
      | SAVE10           |

  Scenario: Attempt to apply a non-existent discount code
    Given a shopping cart with items
    And a <discount_code> that does not exist
    When I click the APPLY button
    Then an error message is shown: "This discount code is not valid."

    Examples:
      | discount_code    |
      | INVALID          |
      | UNKNOWN          |

  Scenario: Attempt to apply a discount code to an empty cart
    Given an empty shopping cart
    And a <discount_code> that exists and is active
    When I click the APPLY button
    Then an error message is shown: "Your cart is empty."

    Examples:
      | discount_code    |
      | CODE123          |
      | PROMO50          |

  Scenario: Attempt to apply an expired discount code
    Given a shopping cart with items
    And a <discount_code> that has expired
    When I click the APPLY button
    Then an error message is shown: "This discount code has expired."

    Examples:
      | discount_code    |
      | EXPIRED1         |
      | OLD50            |

  Scenario: Attempt to apply a discount code that has already been used
    Given a shopping cart with items
    And a <discount_code> that has already been used by the user
    When I click the APPLY button
    Then an error message is shown: "You have already used this discount code."

    Examples:
      | discount_code    |
      | USED123          |
      | ALREADYUSED      |