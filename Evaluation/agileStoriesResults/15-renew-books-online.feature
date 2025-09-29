Feature: Renew books online

  Scenario: Successfully renew a book
    Given I am logged in as a library member
    And the book with <bookId> is currently checked out to me
    When I choose to renew the book
    Then the due date of book <bookId> should be extended by <renewalPeriod> days

    Examples:
      | bookId | renewalPeriod |
      | 101    | 14            |
      | 202    | 21            |
      | 303    | 7             |
      | 404    | 0             |
      | 505    | 30            |

  Scenario: Fail to renew a book not checked out
    Given I am logged in as a library member
    And the book with <bookId> is not checked out to me
    When I choose to renew the book
    Then I should receive an error message <errorMessage>

    Examples:
      | bookId | errorMessage                  |
      | 606    | "Book not checked out"        |
      | 707    | "Book not checked out"        |

  Scenario: Fail to renew a book if renewal limit is reached
    Given I am logged in as a library member
    And the book with <bookId> is currently checked out to me
    And I have reached the maximum number of renewals for book <bookId>
    When I choose to renew the book
    Then I should receive an error message <errorMessage>

    Examples:
      | bookId | errorMessage                    |
      | 808    | "Renewal limit reached"         |
      | 909    | "Renewal limit reached"         |