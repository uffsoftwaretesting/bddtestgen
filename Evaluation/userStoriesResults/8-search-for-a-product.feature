Feature: Search for a Product

  Scenario: Successful product search
    Given I am on the <page> with a search bar
    When I enter a search term <searchTerm> and click the search button
    Then the system displays a list of products that match the search term
    And the search userStoriesResults page shows "Showing userStoriesResults for '<searchTerm>'."

    Examples:
      | page                  | searchTerm    |
      | Home Page             | shoes         |
      | Product Listing Page  | laptop        |
      | Home Page             | t-shirt       |
      | Product Listing Page  | headphones    |
      | Home Page             | backpack      |

  Scenario: Search term is too short
    Given I am on the <page> with a search bar
    When I enter a search term <searchTerm> and click the search button
    Then an error message is displayed: "Search term must be at least 3 characters long."

    Examples:
      | page                  | searchTerm |
      | Home Page             | sh         |
      | Product Listing Page  | la         |
      | Home Page             | t          |
      | Product Listing Page  | h          |
      | Home Page             | ba         |

  Scenario: No products found for search term
    Given I am on the <page> with a search bar
    When I enter a search term <searchTerm> and click the search button
    Then a message is displayed: "No products found for '<searchTerm>'."
    And the search userStoriesResults page shows "Showing userStoriesResults for '<searchTerm>'."

    Examples:
      | page                  | searchTerm  |
      | Home Page             | xyz123      |
      | Product Listing Page  | qwerty      |
      | Home Page             | asdfgh      |
      | Product Listing Page  | zxcvbn      |
      | Home Page             | randomterm  |