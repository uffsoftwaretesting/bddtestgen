Feature: Change Account Password

  Scenario: Successfully change password
    Given <CurrentPassword> is the user's existing password
    And <NewPassword> and <ConfirmNewPassword> are valid and match
    When the UPDATE PASSWORD button is clicked
    Then the system should log out the user and redirect to the login page
    And display the message "Your password has been changed successfully. Please log in again."

    Examples:
      | CurrentPassword | NewPassword | ConfirmNewPassword |
      | oldPass123      | NewPass123  | NewPass123         |
      | myPass555       | MyNewPass1  | MyNewPass1         |

  Scenario: Field is required
    Given <CurrentPassword> is the user's existing password
    And <NewPassword> and <ConfirmNewPassword> are provided
    When the user leaves a required field empty
    Then display the error message "This field is required!"

    Examples:
      | CurrentPassword | NewPassword | ConfirmNewPassword |
      |                 | NewPass123  | NewPass123         |
      | oldPass123      |             | NewPass123         |
      | oldPass123      | NewPass123  |                    |

  Scenario: Incorrect current password
    Given <CurrentPassword> does not match the user's existing password
    When the UPDATE PASSWORD button is clicked
    Then display the error message "Incorrect current password."

    Examples:
      | CurrentPassword | NewPassword  | ConfirmNewPassword |
      | wrongPass123    | NewPass123   | NewPass123         |
      | incorrectPass   | MyNewPass1   | MyNewPass1         |

  Scenario: New password does not meet security requirements
    Given <CurrentPassword> is the user's existing password
    And <NewPassword> does not meet security requirements
    When the UPDATE PASSWORD button is clicked
    Then display the error message "Password must be at least 8 characters long and include an uppercase letter and a number."

    Examples:
      | CurrentPassword | NewPassword  | ConfirmNewPassword |
      | oldPass123      | short        | short              |
      | oldPass123      | alllowercase | alllowercase       |
      | oldPass123      | NoNumber     | NoNumber           |

  Scenario: New passwords do not match
    Given <CurrentPassword> is the user's existing password
    And <NewPassword> and <ConfirmNewPassword> do not match
    When the UPDATE PASSWORD button is clicked
    Then display the error message "The new passwords do not match."

    Examples:
      | CurrentPassword | NewPassword  | ConfirmNewPassword |
      | oldPass123      | NewPass123   | NewPass124         |
      | oldPass123      | MyNewPass1   | MyNewPass2         |

  Scenario: New password is the same as current password
    Given <CurrentPassword> is the user's existing password
    And <NewPassword> is the same as the current password
    When the UPDATE PASSWORD button is clicked
    Then display the error message "Your new password cannot be the same as your old one."

    Examples:
      | CurrentPassword | NewPassword  | ConfirmNewPassword |
      | oldPass123      | oldPass123   | oldPass123         |
      | mySecurePass1   | mySecurePass1| mySecurePass1      |