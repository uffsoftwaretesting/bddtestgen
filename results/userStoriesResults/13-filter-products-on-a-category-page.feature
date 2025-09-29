Feature: Filter Products on a Category Page

  Scenario: Apply valid price range filter
    Given the <minPrice>* is set in the price range filter
    And the <maxPrice>* is set in the price range filter
    When I click the APPLY FILTERS button
    Then the product grid should refresh to show products within the price range <minPrice> to <maxPrice>

    Examples:
      | minPrice | maxPrice |
      | 10       | 50       |
      | 0        | 100      |
      | 50       | 100      |
      | 0        | 1        |
      | 99       | 100      |

  Scenario: Apply invalid price range filter
    Given the <minPrice>* is set in the price range filter
    And the <maxPrice>* is set in the price range filter
    When I click the APPLY FILTERS button
    Then an error message "Minimum price cannot be higher than maximum price." should be displayed

    Examples:
      | minPrice | maxPrice |
      | 100      | 50       |
      | 50       | 0        |
      | 10       | 9        |
      | 100      | 99       |

  Scenario: Apply brand and color filters with matching products
    Given the <brand>* is selected in the brand filter
    And the <color>* is selected in the color filter
    When I click the APPLY FILTERS button
    Then the product grid should refresh to show products matching the brand <brand> and color <color>
    And the text "Active Filters: <brand>, <color>." should be displayed

    Examples:
      | brand   | color  |
      | Nike    | Red    |
      | Adidas  | Blue   |
      | Puma    | Green  |
      | Reebok  | Black  |
      | Nike    | White  |

  Scenario: Apply brand and color filters with no matching products
    Given the <brand>* is selected in the brand filter
    And the <color>* is selected in the color filter
    When I click the APPLY FILTERS button
    Then a message "No products match your filter criteria. Try removing some filters." should be displayed
    And the text "Active Filters: <brand>, <color>." should be displayed

    Examples:
      | brand   | color  |
      | BrandX  | Purple |
      | BrandY  | Orange |
      | BrandZ  | Yellow |
      | BrandA  | Pink   |
      | BrandB  | Cyan   |