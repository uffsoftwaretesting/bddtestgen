Feature: Submit a Contact Form

Scenario: Submit a valid contact form
  Given <Your Name> is entered in the contact form
  And <Your Email> is entered in the contact form
  And <Subject> is entered in the contact form
  And <Message> is entered in the contact form
  When I click the SUBMIT button
  Then the system sends an email to the support team
  And I should see "Thank you for contacting us! We will get back to you shortly."

  Examples:
    | Your Name | Your Email        | Subject     | Message                              |
    | John Doe  | john@example.com  | Inquiry     | Hello, I would like more information.|
    | Jane Smith| jane.smith@mail.com| Feedback    | Your website is very helpful. Thanks.|

Scenario: Submit a contact form with missing required fields
  Given <Your Name> is entered in the contact form
  And <Your Email> is entered in the contact form
  And <Subject> is entered in the contact form
  And <Message> is entered in the contact form
  When I click the SUBMIT button
  Then I should see "This field is required!"

  Examples:
    | Your Name | Your Email | Subject  | Message |
    |           | john@example.com | Inquiry | Hello, I need details. |
    | John Doe  |            | Inquiry | Hello, I need details. |
    | John Doe  | john@example.com |         | Hello, I need details. |
    | John Doe  | john@example.com | Inquiry |                          |

Scenario: Submit a contact form with an invalid email
  Given <Your Name> is entered in the contact form
  And <Your Email> is entered in the contact form
  And <Subject> is entered in the contact form
  And <Message> is entered in the contact form
  When I click the SUBMIT button
  Then I should see "Please enter a valid email address."

  Examples:
    | Your Name | Your Email | Subject  | Message                              |
    | John Doe  | john@com   | Inquiry  | Hello, I would like more information.|
    | Jane Smith| jane.mail.com | Feedback | Your website is very helpful. Thanks.|

Scenario: Submit a contact form with a short message
  Given <Your Name> is entered in the contact form
  And <Your Email> is entered in the contact form
  And <Subject> is entered in the contact form
  And <Message> is entered in the contact form
  When I click the SUBMIT button
  Then I should see "Message must be at least 20 characters long."

  Examples:
    | Your Name | Your Email        | Subject | Message    |
    | John Doe  | john@example.com  | Inquiry | Hi there!  |
    | Jane Smith| jane.smith@mail.com| Feedback| Short msg. |