Feature: Customize news feed based on user interests

  Scenario: Add interests to customize news feed
    Given I have logged into the news app
    And I navigate to the customization settings
    When I add <interest*> to my news feed preferences
    Then articles related to <interest*> should appear in my customized news feed

    Examples:
      | interest*    |
      | Technology   |
      | Sports       |
      | Politics     |
      | Health       |
      | Entertainment|

  Scenario: Remove interests from customized news feed
    Given I have logged into the news app
    And I navigate to the customization settings
    When I remove <interest*> from my news feed preferences
    Then articles related to <interest*> should not appear in my customized news feed

    Examples:
      | interest*    |
      | Technology   |
      | Sports       |
      | Politics     |
      | Health       |
      | Entertainment|

  Scenario: View customized news feed
    Given I have logged into the news app
    And I have set my news feed preferences to <interest*>
    When I view my news feed
    Then I should see articles related to <interest*> prominently displayed

    Examples:
      | interest*    |
      | Technology   |
      | Sports       |
      | Politics     |
      | Health       |
      | Entertainment|