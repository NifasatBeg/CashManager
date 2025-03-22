# CashManagerService

CashManagerService is a Spring Boot-based microservice responsible for managing user cash flow. It consumes extracted expense data from DataExtractionService via Kafka and stores the information in a relational database. It also provides various endpoints to handle expense-related operations.

## Features

- Consumes extracted expense data from Kafka.
- Stores and manages expense records in the database.
- Provides RESTful APIs for expense-related operations.
- Supports CRUD operations on expenses.

## Tech Stack

- **Spring Boot**
- **Spring Data JPA**
- **Kafka**
- **MySQL**

