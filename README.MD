
# Fee Service API

A RESTful service for managing student fee transactions, including fee collection and receipt generation.

## Overview

The Fee Service provides the following core functionalities:
- Collect fee payments
- Retrieve fee transactions by student
- Generate receipts for transactions
- Send  receipt email to the student

## Technology Stack

- Java 17
- Spring Boot
- Spring Data JPA
- H2 Database (in-memory)
- OpenAPI/Swagger for documentation

## Getting Started

### Prerequisites

- Java 17
- Maven

### Setup and Installation

1. Clone the repository
   ```bash
   git clone https://github.com/mohammedmustafa2348/fee.git
   cd fee
   ```

2. Build the application
   ```bash
   ./mvnw clean install
   ```

3. Run the application
   ```bash
   ./mvnw spring-boot:run
   ```

The application will start at `http://localhost:8080`

## API Endpoints

### Base URL: `/api/fees`

| Method | Endpoint | Description                            |
|--------|----------|----------------------------------------|
| POST   | `/` | Collect fee payment                    |
| GET    | `/` | Get all fee transactions               |
| GET    | `/student/{studentId}` | Get all fee transactions for a student |
| GET    | `/receipt/{referenceNumber}` | Generate receipt for a transaction     |

### Example Requests

1. Collect Fee Payment
   ```bash
   curl -X POST http://localhost:8082/api/fees \
   -H "Content-Type: application/json" \
   -d '{
     "studentId": "STU123",
     "amount": 500.00,
     "description": "Term 1 Fees"
   }'
   ```
2. Get All Student Transactions
   ```bash
   curl http://localhost:8082/api/fees
   ```

3. Get Student Transactions
   ```bash
   curl http://localhost:8082/api/fees/student/STU123
   ```

4. Generate Receipt
   ```bash
   curl http://localhost:8082/api/fees/receipt/TXN123
   ```

## API Documentation

Access the Swagger UI documentation at:

[http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
 

## Database Access

The H2 console is available at:

[http://localhost:8082/h2-console](http://localhost:8082/h2-console)
 

Connection details:
- JDBC URL: `jdbc:h2:mem:feedb`
- User Name: `sa`
- Password: [leave empty]

## Postman Collection

A ready-to-use Postman collection is available in the `docs/postman` directory. To use it:

1. Open Postman
2. Click on "Import"
3. Select the file `docs/postman/fee-service-collection.json`
4. The collection will be imported with all available endpoints

The collection includes example requests for all endpoints with proper headers and request bodies where applicable.

## Email Service Configuration

The application uses Gmail SMTP for sending email notifications. To set up the email service:

### 1. Gmail Configuration
1. You need a Gmail account
2. Configure the following properties in `application.properties`:
```
spring.mail.host=smtp.gmail.com 
spring.mail.port=587 
spring.mail.username=your-email@gmail.com 
spring.mail.password=your-app-password 
spring.mail.properties.mail.smtp.auth=true 
spring.mail.properties.mail.smtp.starttls.enable=true

email.from=your-sender-email@domain.com 
email.subject=Your transaction is Successful
```

### 2. Generating Gmail App Password
To generate an app password for Gmail:

1. Go to your Google Account settings: https://myaccount.google.com/
2. Navigate to "Security"
3. Enable "2-Step Verification" if not already enabled
4. Go to "App passwords" (under "2-Step Verification")
5. Select "App" from the dropdown menu
6. Choose "Other (Custom name)"
7. Enter a name for your application (e.g., "Fee Service")
8. Click "Generate"
9. Copy the 16-character generated password without spaces
10. Paste this password in your `application.properties` file for `spring.mail.password`

### 3. Security Best Practices
- Never commit the actual app password to version control
- Create an `application.properties.template` file with placeholder values
- Use environment variables or a secure configuration service in production
- Example of using environment variables:


### 4. Email Templates
- Email templates are located in `src/main/resources/templates/`
- The receipt template uses Thymeleaf for dynamic content

### 5. Testing Email Configuration
To test if your email configuration is working:
1. Set up the correct credentials in `application.properties`
2. Start the application
3. Make a test transaction
4. Check the logs for successful email delivery
5. Verify the receipt in the recipient's inbox

### Troubleshooting
If emails are not being sent:
1. Verify your app password is correct
2. Ensure 2-Step Verification is enabled
3. Check if your Gmail account hasn't blocked the app
4. Look for detailed error messages in the application logs
5. Verify that all SMTP properties are correctly configured
## Testing

Run the tests using:
```bash
./mvnw test
```

## Important Notes
- This service uses an in-memory H2 database; data will be reset when the application restarts
- All monetary amounts should be provided in the smallest currency unit (e.g., cents)
- Receipts are generated in real-time for completed transactions

## Error Handling
The service handles common errors including:
- Invalid fee amount
- Invalid student ID
- Non-existent reference numbers
- Invalid request format

## Security
- Input validation is implemented for all endpoints
- Request payload validation using Jakarta Validation
- Logging of all transactions for audit purposes

## Monitoring
The application includes:
- Transaction logging
- Request/Response logging
- Error tracking

## Contributing
1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request
