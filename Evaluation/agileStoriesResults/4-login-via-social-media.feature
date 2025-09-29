Feature: Social Media Login

  Scenario: Successful login via social media account
    Given I am a new user
    When I choose to log in with my <socialMediaPlatform*> account
    And I authorize the application to access my social media information
    Then I should be logged into the platform
    And I should see a welcome message

    Examples:
      | socialMediaPlatform |
      | Facebook            |
      | Google              |
      | Twitter             |

  Scenario: Unsuccessful login due to denied authorization
    Given I am a new user
    When I choose to log in with my <socialMediaPlatform*> account
    And I deny the application access to my social media information
    Then I should not be logged into the platform
    And I should see an error message

    Examples:
      | socialMediaPlatform |
      | Facebook            |
      | Google              |
      | Twitter             |

  Scenario: Unsuccessful login due to unsupported social media platform
    Given I am a new user
    When I choose to log in with my <socialMediaPlatform*> account
    Then I should not be able to log in
    And I should see an unsupported platform error message

    Examples:
      | socialMediaPlatform |
      | MySpace             |
      | LinkedIn            |
      | Instagram           |