Feature: Admin Dashboard User Statistics

  Scenario: View total number of users
    Given I am logged in as an admin
    When I navigate to the dashboard
    Then I should see the total number of users as <totalUsers>

    Examples:
      | totalUsers |
      | 0          |
      | 1          |
      | 50         |
      | 100        |
      | 999        |

  Scenario: View active users in the last 24 hours
    Given I am logged in as an admin
    When I navigate to the dashboard
    Then I should see the number of active users in the last 24 hours as <activeUsers>

    Examples:
      | activeUsers |
      | 0           |
      | 1           |
      | 10          |
      | 50          |
      | 100         |
      | 1000        |

  Scenario: View daily new user registrations
    Given I am logged in as an admin
    When I navigate to the dashboard
    Then I should see the number of new user registrations for today as <newRegistrations>

    Examples:
      | newRegistrations |
      | 0                |
      | 1                |
      | 5                |
      | 10               |
      | 20               |
      | 100              |

  Scenario: View average user session duration
    Given I am logged in as an admin
    When I navigate to the dashboard
    Then I should see the average user session duration as <avgSessionDuration> minutes

    Examples:
      | avgSessionDuration |
      | 0                  |
      | 1                  |
      | 5                  |
      | 10                 |
      | 30                 |
      | 60                 |
      | 120                |