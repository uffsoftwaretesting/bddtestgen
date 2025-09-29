Feature: Categorize Transactions

  Scenario: Categorize a valid transaction
    Given a transaction with <transaction_id> and amount <amount*>
    When I categorize it as <category*>
    Then the transaction should be saved with the category <category*>

    Examples:
      | transaction_id | amount | category     |
      | 1              | 50     | Groceries    |
      | 2              | 100    | Utilities    |
      | 3              | 0      | Entertainment|
      | 4              | -10    | Refund       |
      | 5              | 5000   | Rent         |

  Scenario: Categorize a transaction with invalid data
    Given a transaction with <transaction_id> and amount <amount*>
    When I categorize it as <category*>
    Then I should see an error message indicating invalid data

    Examples:
      | transaction_id | amount | category  |
      | 6              | a      | Groceries |
      | 7              |        |           |
      | 8              | 50     |           |
      | 9              |        | Utilities |
      | 10             | -50000 |           |