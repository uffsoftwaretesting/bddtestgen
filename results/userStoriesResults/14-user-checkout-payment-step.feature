Feature: User Checkout - Payment Step

  Scenario Outline: Successfully process payment
    Given <Cardholder Name> is entered in the payment form
    And <Card Number> is entered as the card number
    And <Expiration Date> is entered as the expiration date
    And <CVV> is entered as the CVV
    When I click the PLACE ORDER button
    Then the user is redirected to the "Order Confirmation" page

    Examples:
      | Cardholder Name | Card Number        | Expiration Date | CVV  |
      | John Doe        | 4111111111111111   | 12/25           | 123  |
      | Jane Smith      | 5500000000000004   | 11/24           | 4567 |
      
  Scenario Outline: Missing payment information
    Given <Cardholder Name> is entered in the payment form
    And <Card Number> is entered as the card number
    And <Expiration Date> is entered as the expiration date
    And <CVV> is entered as the CVV
    When I click the PLACE ORDER button
    Then the error message "This field is required!" should be displayed

    Examples:
      | Cardholder Name | Card Number | Expiration Date | CVV |
      | ""              | 4111111111111111 | 12/25 | 123 |
      | John Doe        | ""               | 12/25 | 123 |
      | John Doe        | 4111111111111111 | ""    | 123 |
      | John Doe        | 4111111111111111 | 12/25 | ""  |
      
  Scenario Outline: Invalid card number format
    Given <Cardholder Name> is entered in the payment form
    And <Card Number> is entered as the card number
    And <Expiration Date> is entered as the expiration date
    And <CVV> is entered as the CVV
    When I click the PLACE ORDER button
    Then the error message "Please enter a valid card number." should be displayed

    Examples:
      | Cardholder Name | Card Number | Expiration Date | CVV  |
      | John Doe        | 1234567890123456 | 12/25 | 123  |
      | Jane Smith      | 4111111111111110 | 12/25 | 123  |
      
  Scenario Outline: Expired card
    Given <Cardholder Name> is entered in the payment form
    And <Card Number> is entered as the card number
    And <Expiration Date> is entered as the expiration date
    And <CVV> is entered as the CVV
    When I click the PLACE ORDER button
    Then the error message "Card has expired." should be displayed

    Examples:
      | Cardholder Name | Card Number      | Expiration Date | CVV  |
      | John Doe        | 4111111111111111 | 01/20           | 123  |
      | Jane Smith      | 5500000000000004 | 02/21           | 4567 |
      
  Scenario Outline: Invalid CVV format
    Given <Cardholder Name> is entered in the payment form
    And <Card Number> is entered as the card number
    And <Expiration Date> is entered as the expiration date
    And <CVV> is entered as the CVV
    When I click the PLACE ORDER button
    Then the error message "Please enter a valid CVV." should be displayed

    Examples:
      | Cardholder Name | Card Number      | Expiration Date | CVV     |
      | John Doe        | 4111111111111111 | 12/25           | 12      |
      | Jane Smith      | 5500000000000004 | 12/25           | 12345   |
      
  Scenario Outline: Payment processing fails
    Given <Cardholder Name> is entered in the payment form
    And <Card Number> is entered as the card number
    And <Expiration Date> is entered as the expiration date
    And <CVV> is entered as the CVV
    When I click the PLACE ORDER button
    Then the error message "Your payment could not be processed. Please check your information or try another card." should be displayed

    Examples:
      | Cardholder Name | Card Number      | Expiration Date | CVV  |
      | John Doe        | 4111111111111111 | 12/25           | 123  |