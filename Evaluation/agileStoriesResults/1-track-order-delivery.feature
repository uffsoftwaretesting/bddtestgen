Feature: Track order

  Scenario: Track a valid order
    Given an order with ID <orderID> is placed
    When I track the order
    Then the order status should be <status>
    And the estimated delivery is <deliveryDate>

    Examples:
      | orderID | status    | deliveryDate |
      | 1001    | Shipped   | 2023-10-15   |
      | 1002    | Delivered | 2023-10-10   |
      | 1003    | Pending   | 2023-10-20   |

  Scenario: Track an invalid order
    Given an order with ID <orderID> is placed
    When I track the order
    Then I should receive an <errorMessage>

    Examples:
      | orderID | errorMessage          |
      | 0       | "Order not found"     |
      | -1      | "Order not found"     |
      | 9999    | "Order not found"     |
      | "abc"   | "Invalid order ID"    |
      | ""      | "Invalid order ID"    |