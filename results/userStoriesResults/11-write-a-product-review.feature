Feature: Write a Product Review

  Scenario: Submit valid product review
    Given a logged-in user has purchased the product
    And <rating> star rating is selected
    And review title <reviewTitle> is entered
    And review body <reviewBody> is entered
    When the SUBMIT REVIEW button is clicked
    Then a confirmation message "Thank you for your review! It will be posted after being reviewed by our team." should appear

    Examples:
      | rating | reviewTitle       | reviewBody                                               |
      | 1      | "Great Product"   | "This is a fantastic product! I really enjoyed using it." |
      | 5      | "Highly Recommend"| "Exceeded my expectations in every way. Highly recommend!"|

  Scenario: Attempt to submit review without mandatory fields
    Given a logged-in user has purchased the product
    And <rating> star rating is selected
    And review title <reviewTitle> is entered
    And review body <reviewBody> is entered
    When the SUBMIT REVIEW button is clicked
    Then an error message "This field is required!" should appear

    Examples:
      | rating | reviewTitle   | reviewBody |
      |        | "Good"        | "Good."    |
      | 3      |               |            |

  Scenario: Attempt to submit review as non-purchaser
    Given a logged-in user has not purchased the product
    And <rating> star rating is selected
    And review title <reviewTitle> is entered
    And review body <reviewBody> is entered
    When the SUBMIT REVIEW button is clicked
    Then an error message "You can only review products you have purchased." should appear

    Examples:
      | rating | reviewTitle       | reviewBody                                               |
      | 4      | "Not Bad"         | "I have not used this product, but it looks good."       |

  Scenario: Attempt to submit review with short Review Body
    Given a logged-in user has purchased the product
    And <rating> star rating is selected
    And review title <reviewTitle> is entered
    And review body <reviewBody> is entered
    When the SUBMIT REVIEW button is clicked
    Then an error message "Your review must be at least 50 characters long." should appear

    Examples:
      | rating | reviewTitle   | reviewBody                |
      | 2      | "Okay"        | "Too short."              |