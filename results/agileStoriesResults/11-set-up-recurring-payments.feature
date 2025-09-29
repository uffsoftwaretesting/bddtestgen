Feature: Set up recurring payments

  Scenario Outline: Successfully set up a recurring payment
    Given I am logged into the mobile banking app
    And I navigate to the "Payments" section
    When I set up a recurring payment with <amount> to <payee> starting from <start_date> with a frequency of <frequency>
    Then the recurring payment should be scheduled successfully

    Examples:
      | amount | payee   | start_date | frequency |
      | 100    | Payee1  | 2023-10-01 | weekly    |
      | 200    | Payee2  | 2023-11-01 | monthly   |
      | 500.50 | Payee3  | 2023-12-01 | yearly    |
      | 0.01   | Payee4  | 2023-10-01 | daily     |
      | 9999   | Payee5  | 2023-10-01 | monthly   |
  
  Scenario Outline: Fail to set up recurring payment due to missing required fields
    Given I am logged into the mobile banking app
    And I navigate to the "Payments" section
    When I set up a recurring payment with <amount> to <payee> starting from <start_date> with a frequency of <frequency>
    Then I should receive an error message indicating missing required fields

    Examples:
      | amount | payee | start_date | frequency |
      |        | Payee1 | 2023-10-01 | weekly    |
      | 100    |       | 2023-10-01 | monthly   |
      | 200    | Payee2 |           | yearly    |
      | 300    | Payee3 | 2023-10-01 |          |
      |        |       |            |           |
  
  Scenario Outline: Fail to set up recurring payment with invalid amount
    Given I am logged into the mobile banking app
    And I navigate to the "Payments" section
    When I set up a recurring payment with <amount> to <payee> starting from <start_date> with a frequency of <frequency>
    Then I should receive an error message indicating an invalid amount

    Examples:
      | amount | payee  | start_date | frequency |
      | -100   | Payee1 | 2023-10-01 | weekly    |
      | abc    | Payee2 | 2023-11-01 | monthly   |
      | 0      | Payee3 | 2023-12-01 | yearly    |
      | -0.01  | Payee4 | 2023-10-01 | daily     |