# Smart Campus — CampusServices MVP

A simplified campus management platform built with **Java**, **MySQL**, **JDBC**, and **JWT Authentication**.

This project provides three main campus services:

* 📚 Library Management
* 🏫 Room Reservation System
* 📝 Administrative Requests

The system uses:

* Java OOP with inheritance
* JDBC database access
* JWT authentication
* Role-based authorization
* MySQL persistence

---

# Table of Contents

1. Project Overview
2. Features
3. Technologies Used
4. Project Architecture
5. User Roles
6. Main Functionalities
7. Backend Structure
8. Database Schema
9. Authentication
10. API Overview
11. Installation Guide
12. Environment Configuration
13. Running the Project
14. Default Seed Accounts
15. Business Rules
16. MVP Limitations
17. Future Improvements

---

# 1. Project Overview

Smart Campus — CampusServices is an MVP (Minimum Viable Product) designed to help universities digitize common student services.

The platform supports:

* students borrowing books
* room reservations
* administrative requests
* librarian management
* admin management

The project follows a clean separation between:

* Controllers
* Services
* Repositories
* Models

---

# 2. Features

## Authentication

* Login using email/password
* JWT token generation
* Protected endpoints
* Role-based access control

---

## Library Module

Students can:

* View books
* Borrow books
* Return books
* View their borrowings

Librarians can:

* Add books
* Update books
* Delete books

---

## Room Reservation Module

Students can:

* View rooms
* Reserve rooms
* Cancel reservations
* View their reservations

Admins can:

* Add rooms
* Change room availability

---

## Administrative Requests Module

Students can:

* Submit requests
* Follow request status

Admins can:

* View all requests
* Approve requests
* Reject requests

---

# 3. Technologies Used

## Backend

* Java
* JDBC
* JWT
* Maven (recommended)

## Database

* MySQL

## Authentication

* JWT Token Authentication

## API Style

* REST API
* JSON Request/Response

---

# 4. Project Architecture

```txt
src/
│
├── controllers/
│
├── services/
│
├── repositories/
│
├── models/
│
├── enums/
│
├── middleware/
│
├── utils/
│
├── config/
│
└── Main.java
```

---

# 5. User Roles

The system contains three roles:

```txt
STUDENT
ADMIN
LIBRARIAN
```

---

# 6. Main Functionalities

| Module         | Feature                 |
| -------------- | ----------------------- |
| Authentication | JWT login               |
| Library        | Borrow & return books   |
| Rooms          | Reserve rooms           |
| Requests       | Administrative requests |
| Admin          | Manage rooms & requests |
| Librarian      | Manage books            |

---

# 7. Backend Structure

## Models

Uses inheritance:

```txt
User
├── Student
├── Admin
└── Librarian
```

---

## Controllers

Responsible for:

* handling HTTP requests
* validating request data
* returning JSON responses

---

## Services

Responsible for:

* business logic
* validation
* permissions

---

## Repositories

Responsible for:

* SQL queries
* database communication
* CRUD operations

---

# 8. Database Schema

## Main Tables

```txt
users
students
admins
librarians
books
borrowings
rooms
reservations
administrative_requests
```

---

## Example Relationships

```txt
users 1──1 students
users 1──1 admins
users 1──1 librarians

students 1──N borrowings
students 1──N reservations
students 1──N administrative_requests

books 1──N borrowings
rooms 1──N reservations
```

---

# 9. Authentication

The application uses JWT authentication.

---

## Login Flow

1. User sends email/password
2. Backend validates credentials
3. Backend generates JWT token
4. Frontend stores token
5. Frontend sends token in protected requests

---

## Authorization Header

```http
Authorization: Bearer <accessToken>
```

---

## Protected Endpoints

All endpoints except login require JWT authentication.

---

# 10. API Overview

## Authentication

| Method | Endpoint          |
| ------ | ----------------- |
| POST   | `/api/auth/login` |
| GET    | `/api/auth/me`    |

---

## Library

| Method | Endpoint                   |
| ------ | -------------------------- |
| GET    | `/api/books`               |
| POST   | `/api/books/borrow`        |
| POST   | `/api/books/return`        |
| GET    | `/api/books/my-borrowings` |

---

## Librarian

| Method | Endpoint                      |
| ------ | ----------------------------- |
| POST   | `/api/librarian/books`        |
| POST   | `/api/librarian/books/update` |
| POST   | `/api/librarian/books/delete` |

---

## Rooms

| Method | Endpoint                        |
| ------ | ------------------------------- |
| GET    | `/api/rooms`                    |
| POST   | `/api/rooms/reserve`            |
| POST   | `/api/rooms/cancel-reservation` |
| GET    | `/api/rooms/my-reservations`    |

---

## Admin Rooms

| Method | Endpoint                               |
| ------ | -------------------------------------- |
| POST   | `/api/admin/rooms`                     |
| POST   | `/api/admin/rooms/update-availability` |

---

## Requests

| Method | Endpoint                    |
| ------ | --------------------------- |
| POST   | `/api/requests`             |
| GET    | `/api/requests/my-requests` |

---

## Admin Requests

| Method | Endpoint                      |
| ------ | ----------------------------- |
| GET    | `/api/admin/requests`         |
| POST   | `/api/admin/requests/approve` |
| POST   | `/api/admin/requests/reject`  |

---

# 11. Installation Guide

## Prerequisites

Install:

* Java 17+
* MySQL 8+
* Maven
* Git

---

## Clone Repository

```bash
git clone <repository-url>
cd smart-campus-campusservices
```

---

## Create Database

Open MySQL and run:

```sql
CREATE DATABASE smart_campus;
```

---

## Configure Database

Update database credentials inside:

```txt
src/config/DatabaseConfig.java
```

Example:

```java
public class DatabaseConfig {
    public static final String URL =
        "jdbc:mysql://localhost:3306/smart_campus";

    public static final String USER = "root";

    public static final String PASSWORD = "your_password";
}
```

---

## Run SQL Schema

Execute the SQL schema file:

```txt
database/schema.sql
```

Then execute seed data:

```txt
database/seed.sql
```

---

# 12. Environment Configuration

## JWT Secret

Inside:

```txt
src/config/JwtConfig.java
```

Example:

```java
public class JwtConfig {
    public static final String SECRET =
        "your_secret_key";

    public static final long EXPIRATION =
        86400000;
}
```

---

# 13. Running the Project

## Compile Project

```bash
mvn clean install
```

---

## Run Application

```bash
mvn exec:java
```

Or:

```bash
java Main
```

---

# 14. Default Seed Accounts

## Student

```txt
Email: student@example.com
Password: 1234
```

---

## Admin

```txt
Email: admin@example.com
Password: 1234
```

---

## Librarian

```txt
Email: librarian@example.com
Password: 1234
```

---

# 15. Business Rules

## Student Validity

If:

```txt
valid = false
```

the student cannot:

* borrow books
* reserve rooms
* submit requests

---

## Library Rules

* Maximum 3 active borrowings
* Only available books can be borrowed
* Returned books become AVAILABLE again
* Students can only return their own books

---

## Room Reservation Rules

* No overlapping reservations
* Rooms must be available
* Students cannot double-book themselves
* Students can only cancel their own reservations

---

## Administrative Request Rules

* New requests start as PENDING
* Admin can APPROVE or REJECT
* Rejected requests require refusal reason

---

# 16. MVP Limitations

The following are intentionally out of scope:

* Refresh tokens
* File uploads
* Email sending
* Notifications
* Payments
* Advanced permissions
* External APIs
* Real-time updates

---

# 17. Future Improvements

Possible future upgrades:

* Spring Boot migration
* Swagger/OpenAPI documentation
* Refresh token system
* Docker deployment
* Email notifications
* Frontend integration
* Role permission matrix
* Search and filtering
* Pagination
* Unit testing
* CI/CD pipeline

---

# Authors

Smart Campus — CampusServices Team


Frontend Team : Abdelhaddi ELMOUJIBI
Backend Team : Salma CHRAYAH & Marwa MACHACH
Database Team


---

# License

This project is for educational purposes.
