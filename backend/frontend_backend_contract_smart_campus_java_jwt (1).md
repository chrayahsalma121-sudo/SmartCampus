# Frontend–Backend Contract
## Smart Campus — CampusServices MVP
### Java + MySQL + JWT Version with User Inheritance

---

## 1. Purpose

This document defines the communication contract between the frontend team and the backend team for the **Smart Campus — CampusServices** project.

The goal is to let both teams work separately while using the same:

- API endpoints
- request body formats
- response body formats
- authentication rules
- enum values
- business rules
- MVP boundaries

This version uses:

```txt
Java
MySQL
JDBC
JWT authentication
User inheritance
Minimized MVP scope
```

---

# 2. Project Scope

The system manages three main campus services:

1. **Library**
   - View books
   - Borrow a book
   - Return a book
   - Librarian can add, update, and delete books

2. **Room reservations**
   - View rooms
   - Reserve a room
   - Cancel a reservation
   - Admin can manage room availability

3. **Administrative requests**
   - Student submits a request
   - Student follows request status
   - Admin approves or rejects requests

The system also includes:

- login
- JWT authentication
- role-based access
- student validity check
- MySQL persistence

---

# 3. Backend Technical Decisions

Backend stack:

```txt
Java
MySQL
JDBC
JWT
No Spring Boot
No external EnrollSys API
```

The backend uses inheritance in Java:

```txt
User
├── Student
├── Admin
└── Librarian
```

---

# 4. General API Rules

## 4.1 Base API Prefix

```txt
/api
```

---

## 4.2 Success Response Format

All successful responses should follow this format:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {}
}
```

---

## 4.3 Error Response Format

All error responses should follow this format:

```json
{
  "success": false,
  "message": "Error message"
}
```

---

## 4.4 Date Format

```txt
YYYY-MM-DD
```

Example:

```txt
2026-05-07
```

---

## 4.5 Time Format

```txt
HH:mm
```

Example:

```txt
10:00
```

---

# 5. Authentication and JWT Rules

## 5.1 Login

The frontend sends email and password.  
The backend returns an access token and user information.

```http
POST /api/auth/login
```

### Request

```json
{
  "email": "student@example.com",
  "password": "1234"
}
```

### Student Response

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "jwt_token_here",
    "user": {
      "userId": 1,
      "studentId": 1,
      "fullName": "Marwa Machach",
      "email": "student@example.com",
      "role": "STUDENT",
      "filiere": "Big Data & Cloud Computing",
      "valid": true
    }
  }
}
```

### Admin Response

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "jwt_token_here",
    "user": {
      "userId": 2,
      "adminId": 1,
      "fullName": "Admin User",
      "email": "admin@example.com",
      "role": "ADMIN"
    }
  }
}
```

### Librarian Response

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "jwt_token_here",
    "user": {
      "userId": 3,
      "librarianId": 1,
      "fullName": "Library Manager",
      "email": "librarian@example.com",
      "role": "LIBRARIAN"
    }
  }
}
```

### Error Response

```json
{
  "success": false,
  "message": "Invalid email or password"
}
```

---

## 5.2 Current User

Returns the logged-in user from the JWT.

```http
GET /api/auth/me
Authorization: Bearer <accessToken>
```

### Response Example

```json
{
  "success": true,
  "message": "Current user retrieved successfully",
  "data": {
    "userId": 1,
    "studentId": 1,
    "fullName": "Marwa Machach",
    "email": "student@example.com",
    "role": "STUDENT",
    "filiere": "Big Data & Cloud Computing",
    "valid": true
  }
}
```

---

## 5.3 Protected Endpoints

All endpoints except login require JWT.

Frontend must send:

```http
Authorization: Bearer <accessToken>
```

---

## 5.4 Frontend JWT Responsibilities

The frontend should:

- store `accessToken`
- send it in the `Authorization` header
- store the user object after login
- use the user role to show the correct dashboard
- remove the token on logout

---

## 5.5 Backend JWT Responsibilities

The backend should:

- generate JWT on successful login
- include `userId` and `role` inside the token
- validate JWT for protected endpoints
- extract logged-in user from token
- reject requests with missing or invalid token
- check roles before restricted actions

---

## 5.6 Common JWT Errors

### Missing Token

```json
{
  "success": false,
  "message": "Missing authorization token"
}
```

### Invalid Token

```json
{
  "success": false,
  "message": "Invalid or expired token"
}
```

### Forbidden Role

```json
{
  "success": false,
  "message": "Access denied"
}
```

---

# 6. User Models

## 6.1 User

`User` is the parent model.

Common fields:

```json
{
  "userId": 1,
  "fullName": "Marwa Machach",
  "email": "student@example.com",
  "role": "STUDENT"
}
```

| Field | Type | Description |
|---|---|---|
| userId | int | Unique user id |
| fullName | string | User full name |
| email | string | User email |
| password | string | User password |
| role | string | STUDENT, ADMIN, or LIBRARIAN |

---

## 6.2 Student

`Student` extends `User`.

```json
{
  "studentId": 1,
  "userId": 1,
  "fullName": "Marwa Machach",
  "email": "student@example.com",
  "role": "STUDENT",
  "filiere": "Big Data & Cloud Computing",
  "valid": true
}
```

---

## 6.3 Admin

`Admin` extends `User`.

```json
{
  "adminId": 1,
  "userId": 2,
  "fullName": "Admin User",
  "email": "admin@example.com",
  "role": "ADMIN"
}
```

---

## 6.4 Librarian

`Librarian` extends `User`.

```json
{
  "librarianId": 1,
  "userId": 3,
  "fullName": "Library Manager",
  "email": "librarian@example.com",
  "role": "LIBRARIAN"
}
```

---

# 7. Enum Values

Frontend and backend must use exactly the same enum values.

## UserRole

```txt
STUDENT
ADMIN
LIBRARIAN
```

## BookStatus

```txt
AVAILABLE
BORROWED
```

## ReservationStatus

```txt
CONFIRMED
CANCELLED
```

## RequestStatus

```txt
PENDING
APPROVED
REJECTED
```

## RequestType

```txt
SCHOOL_CERTIFICATE
ATTENDANCE_CERTIFICATE
TRANSCRIPT
OTHER
```

---

# 8. Library API

All library endpoints require JWT.

---

## 8.1 List Books

Allowed roles:

```txt
STUDENT
LIBRARIAN
ADMIN
```

```http
GET /api/books
Authorization: Bearer <accessToken>
```

### Response

```json
{
  "success": true,
  "message": "Books retrieved successfully",
  "data": [
    {
      "bookId": 1,
      "title": "Java Programming",
      "author": "John Smith",
      "status": "AVAILABLE",
      "returnDate": null
    }
  ]
}
```

---

## 8.2 Borrow Book

Allowed role:

```txt
STUDENT
```

The frontend does not send `studentId`.  
The backend gets the student from the JWT.

```http
POST /api/books/borrow
Authorization: Bearer <studentAccessToken>
```

### Request

```json
{
  "bookId": 1
}
```

### Response

```json
{
  "success": true,
  "message": "Book borrowed successfully",
  "data": {
    "borrowingId": 1,
    "studentId": 1,
    "bookId": 1,
    "borrowDate": "2026-05-07",
    "returnDate": "2026-05-21"
  }
}
```

### Possible Errors

```json
{
  "success": false,
  "message": "Student is not valid"
}
```

```json
{
  "success": false,
  "message": "Book is not available"
}
```

```json
{
  "success": false,
  "message": "Student already has 3 borrowed books"
}
```

---

## 8.3 Return Book

Allowed role:

```txt
STUDENT
```

```http
POST /api/books/return
Authorization: Bearer <studentAccessToken>
```

### Request

```json
{
  "borrowingId": 1
}
```

### Response

```json
{
  "success": true,
  "message": "Book returned successfully"
}
```

---

## 8.4 List My Borrowings

Allowed role:

```txt
STUDENT
```

```http
GET /api/books/my-borrowings
Authorization: Bearer <studentAccessToken>
```

### Response

```json
{
  "success": true,
  "message": "Borrowings retrieved successfully",
  "data": [
    {
      "borrowingId": 1,
      "bookId": 1,
      "title": "Java Programming",
      "borrowDate": "2026-05-07",
      "returnDate": "2026-05-21",
      "returned": false
    }
  ]
}
```

---

# 9. Librarian Book Management API

All endpoints require role:

```txt
LIBRARIAN
```

---

## 9.1 Add Book

```http
POST /api/librarian/books
Authorization: Bearer <librarianAccessToken>
```

### Request

```json
{
  "title": "Clean Code",
  "author": "Robert C. Martin"
}
```

### Response

```json
{
  "success": true,
  "message": "Book added successfully",
  "data": {
    "bookId": 5,
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "status": "AVAILABLE"
  }
}
```

---

## 9.2 Update Book

```http
POST /api/librarian/books/update
Authorization: Bearer <librarianAccessToken>
```

### Request

```json
{
  "bookId": 5,
  "title": "Clean Code Updated",
  "author": "Robert C. Martin"
}
```

### Response

```json
{
  "success": true,
  "message": "Book updated successfully"
}
```

---

## 9.3 Delete Book

```http
POST /api/librarian/books/delete
Authorization: Bearer <librarianAccessToken>
```

### Request

```json
{
  "bookId": 5
}
```

### Response

```json
{
  "success": true,
  "message": "Book deleted successfully"
}
```

---

# 10. Rooms API

All endpoints require JWT.

---

## 10.1 List Rooms

Allowed roles:

```txt
STUDENT
ADMIN
```

```http
GET /api/rooms
Authorization: Bearer <accessToken>
```

### Response

```json
{
  "success": true,
  "message": "Rooms retrieved successfully",
  "data": [
    {
      "roomId": 1,
      "name": "Salle A",
      "capacity": 20,
      "available": true
    }
  ]
}
```

---

## 10.2 Reserve Room

Allowed role:

```txt
STUDENT
```

The frontend does not send `studentId`.  
The backend gets the student from the JWT.

```http
POST /api/rooms/reserve
Authorization: Bearer <studentAccessToken>
```

### Request

```json
{
  "roomId": 1,
  "reservationDate": "2026-05-10",
  "startTime": "10:00",
  "endTime": "12:00"
}
```

### Response

```json
{
  "success": true,
  "message": "Room reserved successfully",
  "data": {
    "reservationId": 1,
    "studentId": 1,
    "roomId": 1,
    "reservationDate": "2026-05-10",
    "startTime": "10:00",
    "endTime": "12:00",
    "status": "CONFIRMED"
  }
}
```

---

## 10.3 Cancel Reservation

Allowed role:

```txt
STUDENT
```

```http
POST /api/rooms/cancel-reservation
Authorization: Bearer <studentAccessToken>
```

### Request

```json
{
  "reservationId": 1
}
```

### Response

```json
{
  "success": true,
  "message": "Reservation cancelled successfully"
}
```

---

## 10.4 List My Reservations

Allowed role:

```txt
STUDENT
```

```http
GET /api/rooms/my-reservations
Authorization: Bearer <studentAccessToken>
```

### Response

```json
{
  "success": true,
  "message": "Reservations retrieved successfully",
  "data": [
    {
      "reservationId": 1,
      "roomId": 1,
      "roomName": "Salle A",
      "reservationDate": "2026-05-10",
      "startTime": "10:00",
      "endTime": "12:00",
      "status": "CONFIRMED"
    }
  ]
}
```

---

# 11. Admin Room Management API

All endpoints require role:

```txt
ADMIN
```

---

## 11.1 Update Room Availability

```http
POST /api/admin/rooms/update-availability
Authorization: Bearer <adminAccessToken>
```

### Request

```json
{
  "roomId": 1,
  "available": false
}
```

### Response

```json
{
  "success": true,
  "message": "Room availability updated successfully"
}
```

---

## 11.2 Add Room

```http
POST /api/admin/rooms
Authorization: Bearer <adminAccessToken>
```

### Request

```json
{
  "name": "Salle C",
  "capacity": 30,
  "available": true
}
```

### Response

```json
{
  "success": true,
  "message": "Room added successfully",
  "data": {
    "roomId": 3,
    "name": "Salle C",
    "capacity": 30,
    "available": true
  }
}
```

---

# 12. Administrative Requests API

---

## 12.1 Submit Administrative Request

Allowed role:

```txt
STUDENT
```

The frontend does not send `studentId`.  
The backend gets the student from the JWT.

```http
POST /api/requests
Authorization: Bearer <studentAccessToken>
```

### Request

```json
{
  "type": "SCHOOL_CERTIFICATE",
  "description": "I need a school certificate."
}
```

### Response

```json
{
  "success": true,
  "message": "Administrative request submitted successfully",
  "data": {
    "requestId": 1,
    "studentId": 1,
    "type": "SCHOOL_CERTIFICATE",
    "description": "I need a school certificate.",
    "status": "PENDING",
    "submissionDate": "2026-05-07",
    "refusalReason": null
  }
}
```

---

## 12.2 List My Requests

Allowed role:

```txt
STUDENT
```

```http
GET /api/requests/my-requests
Authorization: Bearer <studentAccessToken>
```

### Response

```json
{
  "success": true,
  "message": "Requests retrieved successfully",
  "data": [
    {
      "requestId": 1,
      "type": "SCHOOL_CERTIFICATE",
      "description": "I need a school certificate.",
      "status": "PENDING",
      "submissionDate": "2026-05-07",
      "refusalReason": null
    }
  ]
}
```

---

# 13. Admin Administrative Requests API

All endpoints require role:

```txt
ADMIN
```

---

## 13.1 Admin Views All Requests

```http
GET /api/admin/requests
Authorization: Bearer <adminAccessToken>
```

### Response

```json
{
  "success": true,
  "message": "All requests retrieved successfully",
  "data": [
    {
      "requestId": 1,
      "studentId": 1,
      "studentName": "Marwa Machach",
      "type": "SCHOOL_CERTIFICATE",
      "description": "I need a school certificate.",
      "status": "PENDING",
      "submissionDate": "2026-05-07",
      "refusalReason": null
    }
  ]
}
```

---

## 13.2 Approve Request

```http
POST /api/admin/requests/approve
Authorization: Bearer <adminAccessToken>
```

### Request

```json
{
  "requestId": 1
}
```

### Response

```json
{
  "success": true,
  "message": "Request approved successfully"
}
```

---

## 13.3 Reject Request

```http
POST /api/admin/requests/reject
Authorization: Bearer <adminAccessToken>
```

### Request

```json
{
  "requestId": 1,
  "refusalReason": "Missing information"
}
```

### Response

```json
{
  "success": true,
  "message": "Request rejected successfully"
}
```

---

# 14. MySQL Tables

Minimum database tables:

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

## 14.1 users

```txt
id
full_name
email
password
role
```

---

## 14.2 students

```txt
id
user_id
filiere
valid
```

---

## 14.3 admins

```txt
id
user_id
```

---

## 14.4 librarians

```txt
id
user_id
```

---

## 14.5 books

```txt
id
title
author
status
```

---

## 14.6 borrowings

```txt
id
student_id
book_id
borrow_date
return_date
returned
```

---

## 14.7 rooms

```txt
id
name
capacity
available
```

---

## 14.8 reservations

```txt
id
student_id
room_id
reservation_date
start_time
end_time
status
```

---

## 14.9 administrative_requests

```txt
id
student_id
type
description
status
submission_date
refusal_reason
```

---

# 15. Backend Business Rules

## Student validity

A student with:

```txt
valid = false
```

cannot:

- borrow a book
- reserve a room
- submit an administrative request

---

## Library rules

- A book can only be borrowed if its status is `AVAILABLE`.
- When a book is borrowed, its status becomes `BORROWED`.
- When a book is returned, its status becomes `AVAILABLE`.
- A student cannot have more than 3 active borrowed books.
- A student can only return their own borrowing.

---

## Room reservation rules

- A room must be available to be reserved.
- A room cannot have two reservations in the same date and time slot.
- A student cannot have two reservations at the same date and time slot.
- A student can only cancel their own reservation.
- Cancelled reservations should have status `CANCELLED`.

---

## Administrative request rules

- A new request always starts with status `PENDING`.
- An admin can change a request to `APPROVED`.
- An admin can change a request to `REJECTED`.
- A rejected request should include a refusal reason.

---

# 16. Frontend Responsibilities

The frontend should:

- call `/api/auth/login`
- store `accessToken`
- send `Authorization: Bearer <accessToken>` for protected endpoints
- call `/api/auth/me` if it needs to refresh current user info
- show student pages only for `STUDENT`
- show admin pages only for `ADMIN`
- show librarian pages only for `LIBRARIAN`
- block service actions if student `valid = false`
- use the exact enum values defined in this contract
- follow the exact request and response shapes

---

# 17. Backend Responsibilities

The backend should:

- connect to MySQL using JDBC
- generate JWT after login
- validate JWT on protected endpoints
- extract user identity and role from JWT
- create repositories for database queries
- create services for business logic
- create controllers/endpoints for frontend communication
- return consistent success/error responses
- check role permissions
- check student validity before student actions
- respect all enum values
- respect all business rules

---

# 18. MVP Boundaries

Do not implement in this version:

- refresh token
- token blacklist
- advanced session management
- external EnrollSys API
- file upload
- notifications table
- advanced dashboard
- payments
- email sending
- complex permission system

---

# 19. Backend Delivery Checklist

The backend team should deliver:

```txt
[ ] login endpoint with JWT
[ ] current user endpoint
[ ] JWT validation for protected endpoints
[ ] role checks
[ ] book listing endpoint
[ ] borrow book endpoint
[ ] return book endpoint
[ ] my borrowings endpoint
[ ] librarian add/update/delete book endpoints
[ ] room listing endpoint
[ ] reserve room endpoint
[ ] cancel reservation endpoint
[ ] my reservations endpoint
[ ] admin room availability endpoint
[ ] admin add room endpoint
[ ] submit administrative request endpoint
[ ] my requests endpoint
[ ] admin list requests endpoint
[ ] admin approve/reject request endpoints
[ ] MySQL schema
[ ] seed data for testing
[ ] README explaining how to run the backend
```

---

# 20. Frontend Mocking Guide

The frontend can start before the backend is complete by mocking the response objects from this contract.

Recommended mock files:

```txt
mockAuth.js
mockBooks.js
mockRooms.js
mockReservations.js
mockRequests.js
```

The frontend should use the same field names:

```txt
accessToken
userId
studentId
adminId
librarianId
bookId
roomId
reservationId
requestId
```

---

# 21. Final Note

This contract is intentionally simplified but now includes JWT.

Any feature not written in this document should be considered out of scope for the MVP unless the team agrees to add it later.
