Feature: Invite a Team Member

  Scenario: Successfully invite a new team member
    Given <Email Address> is entered in the form
    And <Role> is selected from the dropdown
    When I click the SEND INVITATION button
    Then the system should display "Invitation sent to <Email Address>."
    And the user should be added to the "Pending Invitations" list

    Examples:
      | Email Address        | Role   |
      | user1@example.com    | Admin  |
      | user2@example.com    | Member |
      | user3@example.com    | Viewer |

  Scenario: Email Address and Role are mandatory
    Given <Email Address> is entered in the form
    And <Role> is selected from the dropdown
    When I click the SEND INVITATION button
    Then the system should display "This field is required!"

    Examples:
      | Email Address | Role   |
      |               | Admin  |
      | user4@        |        |
      |               |        |

  Scenario: Email Address must be in a valid format
    Given <Email Address> is entered in the form
    And <Role> is selected from the dropdown
    When I click the SEND INVITATION button
    Then the system should display "Please enter a valid email address."

    Examples:
      | Email Address    | Role   |
      | invalid-email    | Admin  |
      | user@invalid     | Member |
      | user@.com        | Viewer |

  Scenario: Invitation cannot be sent to an email address already on the team
    Given <Email Address> is entered in the form
    And <Role> is selected from the dropdown
    When I click the SEND INVITATION button
    Then the system should display "This user is already a member of your team."

    Examples:
      | Email Address        | Role   |
      | existing1@example.com| Admin  |
      | existing2@example.com| Member |
      | existing3@example.com| Viewer |

  Scenario: Invitation cannot be sent to an email address with a pending invitation
    Given <Email Address> is entered in the form
    And <Role> is selected from the dropdown
    When I click the SEND INVITATION button
    Then the system should display "An invitation has already been sent to this email address."

    Examples:
      | Email Address          | Role   |
      | pending1@example.com   | Admin  |
      | pending2@example.com   | Member |
      | pending3@example.com   | Viewer |

  Scenario: Team member limit exceeded
    Given <Email Address> is entered in the form
    And <Role> is selected from the dropdown
    When I click the SEND INVITATION button
    Then the system should display "You have reached the maximum number of users for your plan. Please upgrade to invite more members."

    Examples:
      | Email Address        | Role   |
      | limitexceeded1@example.com | Admin  |
      | limitexceeded2@example.com | Member |
      | limitexceeded3@example.com | Viewer |