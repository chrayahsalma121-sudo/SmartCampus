# Frontend–Backend Contract
## Smart Campus — CampusServices MVP

## 1. Purpose

This document defines the communication contract between the frontend and backend teams for the **CampusServices** module.

The goal is to allow the frontend developer and backend developers to work smoothly by agreeing on:

- API structure
- endpoint paths
- request and response formats
- required fields
- entity relationships
- enum values
- MVP scope boundaries

This contract focuses only on the MVP scope needed for the class project.

---

## 2. Product Scope Reminder

CampusServices is one module of the larger **Smart Campus** project.

This module manages campus services used by students:

1. Library services
2. Room reservations
3. Administrative requests

The system has three main user roles:

1. `STUDENT`
2. `ADMIN`
3. `LIBRARIAN`

For this MVP, all roles are represented using one entity: `User`.

There is no real login/authentication system in this MVP.  
The frontend sends `userId` in requests, and the backend checks the user's role and validity.

---

## 3. General API Rules

### Base API Prefix

```txt
/api
```

Example:

```http
GET /api/books
```

---

## 3.1 Response Format

For single objects:

```json
{
  "id": 1,
  "createdAt": "2026-05-05T12:00:00",
  "updatedAt": "2026-05-05T12:00:00"
}
```

For list endpoints:

```json
[
  {
    "id": 1
  }
]
```

For success messages:

```json
{
  "message": "Operation completed successfully."
}
```

---

## 3.2 Error Format

All backend errors should use this format:

```json
{
  "detail": "Error message"
}
```

Example:

```json
{
  "detail": "Book is not available."
}
```

For validation errors:

```json
{
  "field": "title",
  "detail": "Title is required."
}
```

---

## 3.3 Authentication

No real JWT/session authentication is required in the MVP.

Instead:

- The frontend sends `userId` in the request body or query parameter.
- The backend checks if the user exists.
- The backend checks the user's role.
- The backend checks if a student is valid before allowing access to services.

Example:

```json
{
  "userId": 1
}
```

---

# 4. Users API

Users represent students, administrators, and librarians.

---

## 4.1 List Users

```http
GET /api/users
```

### Purpose

Returns all users.

### Response

```json
[
  {
    "id": 1,
    "fullName": "Marwa Machach",
    "email": "marwa@example.com",
    "role": "STUDENT",
    "valid": true
  },
  {
    "id": 2,
    "fullName": "Admin User",
    "email": "admin@example.com",
    "role": "ADMIN",
    "valid": true
  }
]
```

---

## 4.2 Create User

```http
POST /api/users
```

### Purpose

Creates a user for testing and role management.

### Request

```json
{
  "fullName": "Marwa Machach",
  "email": "marwa@example.com",
  "role": "STUDENT",
  "valid": true
}
```

### Response

Returns the created user object.

---

## 4.3 Get User Details

```http
GET /api/users/{id}
```

### Purpose

Returns one user by id.

### Response

```json
{
  "id": 1,
  "fullName": "Marwa Machach",
  "email": "marwa@example.com",
  "role": "STUDENT",
  "valid": true
}
```

---

# 5. Books API

Used for library catalogue management and borrowing.

---

## 5.1 List Books

```http
GET /api/books
```

### Purpose

Returns all books in the library catalogue.

### Query Parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| search | string | no | Search by title or author |
| available | boolean | no | Filter by availability |

### Response

```json
[
  {
    "id": 1,
    "title": "Java Basics",
    "author": "John Smith",
    "available": true,
    "borrowedByUserId": null,
    "expectedReturnDate": null
  },
  {
    "id": 2,
    "title": "Spring Boot Introduction",
    "author": "Jane Doe",
    "available": false,
    "borrowedByUserId": 1,
    "expectedReturnDate": "2026-05-20"
  }
]
```

---

## 5.2 Get Book Details

```http
GET /api/books/{id}
```

### Purpose

Returns details of one book.

### Response

```json
{
  "id": 1,
  "title": "Java Basics",
  "author": "John Smith",
  "available": true,
  "borrowedByUserId": null,
  "expectedReturnDate": null
}
```

---

## 5.3 Create Book

```http
POST /api/books
```

### Purpose

Allows a librarian to add a book.

### Request

```json
{
  "userId": 3,
  "title": "Java Basics",
  "author": "John Smith"
}
```

### Response

Returns the created book object.

### Business Rules

- `userId` must belong to a user with role `LIBRARIAN`.

---

## 5.4 Update Book

```http
PATCH /api/books/{id}
```

### Purpose

Allows a librarian to update book information.

### Request Example

```json
{
  "userId": 3,
  "title": "Advanced Java",
  "author": "John Smith"
}
```

### Response

Returns the updated book object.

### Business Rules

- `userId` must belong to a user with role `LIBRARIAN`.

---

## 5.5 Delete Book

```http
DELETE /api/books/{id}?userId=3
```

### Purpose

Allows a librarian to delete a book.

### Response

```json
{
  "detail": "Book deleted successfully."
}
```

### Business Rules

- `userId` must belong to a user with role `LIBRARIAN`.

---

## 5.6 Borrow Book

```http
POST /api/books/{id}/borrow
```

### Purpose

Allows a valid student to borrow an available book.

### Request

```json
{
  "userId": 1
}
```

### Response

```json
{
  "message": "Book borrowed successfully.",
  "book": {
    "id": 1,
    "title": "Java Basics",
    "author": "John Smith",
    "available": false,
    "borrowedByUserId": 1,
    "expectedReturnDate": "2026-05-20"
  }
}
```

### Business Rules

- User must have role `STUDENT`.
- Student must be valid.
- Book must be available.
- Student cannot borrow more than 3 books at the same time.
- Backend sets `available = false`.
- Backend sets `expectedReturnDate`.

### Possible Errors

```json
{
  "detail": "Only students can borrow books."
}
```

```json
{
  "detail": "Student is not valid."
}
```

```json
{
  "detail": "Book is not available."
}
```

```json
{
  "detail": "Student cannot borrow more than 3 books."
}
```

---

## 5.7 Return Book

```http
POST /api/books/{id}/return
```

### Purpose

Returns a borrowed book.

### Request

```json
{
  "userId": 1
}
```

### Response

```json
{
  "message": "Book returned successfully.",
  "book": {
    "id": 1,
    "title": "Java Basics",
    "author": "John Smith",
    "available": true,
    "borrowedByUserId": null,
    "expectedReturnDate": null
  }
}
```

### Business Rules

- Book must currently be borrowed.
- Backend sets `available = true`.
- Backend removes `borrowedByUserId`.
- Backend removes `expectedReturnDate`.

---

# 6. Rooms API

Used to list and manage campus rooms.

---

## 6.1 List Rooms

```http
GET /api/rooms
```

### Purpose

Returns all rooms.

### Query Parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| minCapacity | number | no | Filter rooms by minimum capacity |

### Response

```json
[
  {
    "id": 1,
    "name": "Room A",
    "capacity": 20,
    "available": true
  },
  {
    "id": 2,
    "name": "Room B",
    "capacity": 10,
    "available": true
  }
]
```

---

## 6.2 Get Room Details

```http
GET /api/rooms/{id}
```

### Response

```json
{
  "id": 1,
  "name": "Room A",
  "capacity": 20,
  "available": true
}
```

---

## 6.3 Create Room

```http
POST /api/rooms
```

### Purpose

Allows an admin to add a room.

### Request

```json
{
  "userId": 2,
  "name": "Room A",
  "capacity": 20,
  "available": true
}
```

### Response

Returns the created room object.

### Business Rules

- `userId` must belong to a user with role `ADMIN`.

---

## 6.4 Update Room

```http
PATCH /api/rooms/{id}
```

### Purpose

Allows an admin to update room information or availability.

### Request Example

```json
{
  "userId": 2,
  "capacity": 25,
  "available": true
}
```

### Response

Returns the updated room object.

### Business Rules

- `userId` must belong to a user with role `ADMIN`.

---

## 6.5 Delete Room

```http
DELETE /api/rooms/{id}?userId=2
```

### Response

```json
{
  "detail": "Room deleted successfully."
}
```

### Business Rules

- `userId` must belong to a user with role `ADMIN`.

---

# 7. Reservations API

Used by students to reserve rooms.

---

## 7.1 List Reservations

```http
GET /api/reservations
```

### Purpose

Returns all reservations.

### Query Parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| userId | number | no | Filter reservations by student |
| roomId | number | no | Filter reservations by room |
| date | date | no | Filter reservations by date |

### Response

```json
[
  {
    "id": 1,
    "userId": 1,
    "roomId": 2,
    "roomName": "Room B",
    "startTime": "2026-05-10T10:00:00",
    "endTime": "2026-05-10T12:00:00",
    "status": "ACTIVE"
  }
]
```

---

## 7.2 Create Reservation

```http
POST /api/reservations
```

### Purpose

Allows a valid student to reserve a room.

### Request

```json
{
  "userId": 1,
  "roomId": 2,
  "startTime": "2026-05-10T10:00:00",
  "endTime": "2026-05-10T12:00:00"
}
```

### Response

```json
{
  "message": "Room reserved successfully.",
  "reservation": {
    "id": 1,
    "userId": 1,
    "roomId": 2,
    "roomName": "Room B",
    "startTime": "2026-05-10T10:00:00",
    "endTime": "2026-05-10T12:00:00",
    "status": "ACTIVE"
  }
}
```

### Business Rules

- User must have role `STUDENT`.
- Student must be valid.
- Room must be available.
- Room cannot be reserved if another reservation overlaps the same time slot.
- Student cannot have two reservations during the same time slot.
- `startTime` must be before `endTime`.

### Possible Errors

```json
{
  "detail": "Room is already reserved for this time slot."
}
```

```json
{
  "detail": "Student already has a reservation during this time slot."
}
```

---

## 7.3 Cancel Reservation

```http
DELETE /api/reservations/{id}?userId=1
```

### Purpose

Allows a student to cancel their reservation.

### Response

```json
{
  "detail": "Reservation cancelled successfully."
}
```

### Business Rules

- Student can cancel only their own reservation.
- Admin can cancel any reservation.

---

# 8. Administrative Requests API

Used by students and administrators.

---

## 8.1 List Administrative Requests

```http
GET /api/admin-requests
```

### Purpose

Returns administrative requests.

### Query Parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| userId | number | no | Filter by student |
| status | string | no | EN_ATTENTE, VALIDEE, REFUSEE |
| type | string | no | Filter by request type |

### Response

```json
[
  {
    "id": 1,
    "userId": 1,
    "studentName": "Marwa Machach",
    "type": "ATTESTATION_SCOLARITE",
    "status": "EN_ATTENTE",
    "submissionDate": "2026-05-05",
    "refusalReason": null
  }
]
```

---

## 8.2 Create Administrative Request

```http
POST /api/admin-requests
```

### Purpose

Allows a valid student to submit an administrative request.

### Request

```json
{
  "userId": 1,
  "type": "ATTESTATION_SCOLARITE"
}
```

### Response

```json
{
  "message": "Administrative request submitted successfully.",
  "request": {
    "id": 1,
    "userId": 1,
    "studentName": "Marwa Machach",
    "type": "ATTESTATION_SCOLARITE",
    "status": "EN_ATTENTE",
    "submissionDate": "2026-05-05",
    "refusalReason": null
  }
}
```

### Business Rules

- User must have role `STUDENT`.
- Student must be valid.
- Status is automatically set to `EN_ATTENTE`.
- Submission date is automatically set by backend.
- Type is required.

---

## 8.3 Get User Administrative Requests

```http
GET /api/admin-requests/user/{userId}
```

### Purpose

Returns all requests submitted by one student.

### Response

```json
[
  {
    "id": 1,
    "userId": 1,
    "studentName": "Marwa Machach",
    "type": "ATTESTATION_SCOLARITE",
    "status": "EN_ATTENTE",
    "submissionDate": "2026-05-05",
    "refusalReason": null
  }
]
```

---

## 8.4 Validate Administrative Request

```http
PATCH /api/admin-requests/{id}/validate
```

### Purpose

Allows an admin to validate a request.

### Request

```json
{
  "userId": 2
}
```

### Response

```json
{
  "message": "Administrative request validated successfully.",
  "request": {
    "id": 1,
    "userId": 1,
    "studentName": "Marwa Machach",
    "type": "ATTESTATION_SCOLARITE",
    "status": "VALIDEE",
    "submissionDate": "2026-05-05",
    "refusalReason": null
  }
}
```

### Business Rules

- `userId` must belong to a user with role `ADMIN`.
- Request must currently have status `EN_ATTENTE`.
- Status becomes `VALIDEE`.

---

## 8.5 Refuse Administrative Request

```http
PATCH /api/admin-requests/{id}/refuse
```

### Purpose

Allows an admin to refuse a request.

### Request

```json
{
  "userId": 2,
  "refusalReason": "Missing required information."
}
```

### Response

```json
{
  "message": "Administrative request refused successfully.",
  "request": {
    "id": 1,
    "userId": 1,
    "studentName": "Marwa Machach",
    "type": "ATTESTATION_SCOLARITE",
    "status": "REFUSEE",
    "submissionDate": "2026-05-05",
    "refusalReason": "Missing required information."
  }
}
```

### Business Rules

- `userId` must belong to a user with role `ADMIN`.
- Request must currently have status `EN_ATTENTE`.
- `refusalReason` is required.
- Status becomes `REFUSEE`.

---

# 9. Dashboard API

Optional but useful for frontend.

---

## 9.1 Dashboard Overview

```http
GET /api/dashboard/overview
```

### Purpose

Returns simple statistics for the home/dashboard page.

### Response

```json
{
  "users": {
    "total": 3,
    "students": 1,
    "admins": 1,
    "librarians": 1
  },
  "books": {
    "total": 10,
    "available": 8,
    "borrowed": 2
  },
  "rooms": {
    "total": 5
  },
  "reservations": {
    "total": 4,
    "active": 3
  },
  "adminRequests": {
    "total": 6,
    "pending": 2,
    "validated": 3,
    "refused": 1
  }
}
```

---

# 10. Entity Models

Frontend should use these shapes when creating mock data.

---

## 10.1 User

```json
{
  "id": 1,
  "fullName": "Marwa Machach",
  "email": "marwa@example.com",
  "role": "STUDENT",
  "valid": true
}
```

---

## 10.2 Book

```json
{
  "id": 1,
  "title": "Java Basics",
  "author": "John Smith",
  "available": true,
  "borrowedByUserId": null,
  "expectedReturnDate": null
}
```

---

## 10.3 Room

```json
{
  "id": 1,
  "name": "Room A",
  "capacity": 20,
  "available": true
}
```

---

## 10.4 Reservation

```json
{
  "id": 1,
  "userId": 1,
  "roomId": 2,
  "roomName": "Room B",
  "startTime": "2026-05-10T10:00:00",
  "endTime": "2026-05-10T12:00:00",
  "status": "ACTIVE"
}
```

---

## 10.5 Administrative Request

```json
{
  "id": 1,
  "userId": 1,
  "studentName": "Marwa Machach",
  "type": "ATTESTATION_SCOLARITE",
  "status": "EN_ATTENTE",
  "submissionDate": "2026-05-05",
  "refusalReason": null
}
```

---

# 11. Enum Values

Backend and frontend must use the same values.

---

## User Roles

```txt
STUDENT
ADMIN
LIBRARIAN
```

---

## Administrative Request Types

```txt
ATTESTATION_SCOLARITE
CERTIFICAT_PRESENCE
RELEVE_NOTES
AUTRE
```

---

## Administrative Request Status

```txt
EN_ATTENTE
VALIDEE
REFUSEE
```

---

## Reservation Status

```txt
ACTIVE
CANCELLED
```

---

# 12. MVP Boundaries

The backend should not implement the following in this iteration:

- real login/signup
- JWT authentication
- password management
- online payment
- email sending
- real external EnrollSys API
- complex notification system
- file upload for administrative documents
- advanced search
- advanced dashboard analytics

For the MVP, EnrollSys is simulated using the `valid` field inside the `User` entity.

---

# 13. Backend Delivery Checklist

The backend team should deliver:

- User CRUD
- Book CRUD
- Borrow book endpoint
- Return book endpoint
- Room CRUD
- Reservation creation
- Reservation cancellation
- Administrative request creation
- Administrative request validation/refusal
- Simple dashboard overview
- consistent error format
- enum values respected
- frontend mock data compatibility
- API documentation through this contract

---

# 14. Frontend Mocking Guide

Frontend can start before backend is finished by mocking the response shapes defined in this document.

Recommended frontend mock files:

```txt
mockUsers.ts
mockBooks.ts
mockRooms.ts
mockReservations.ts
mockAdminRequests.ts
mockDashboard.ts
```

Frontend should build components using the exact field names defined in this contract.

Important field names to respect:

```txt
fullName
role
valid
title
author
available
borrowedByUserId
expectedReturnDate
roomId
roomName
startTime
endTime
status
submissionDate
refusalReason
```

---

# 15. Integration Rules

Before integration, both teams should verify:

- endpoint paths match this contract
- request body fields match this contract
- response fields match this contract
- enum values match this contract
- frontend uses the same base URL
- frontend sends `userId` when required
- backend returns errors using `{ "detail": "..." }`
- backend uses JSON only
- no endpoint is invented without team agreement

---

# 16. Final Note

This contract is intentionally limited to the MVP.

Any feature not defined here should be considered out of scope unless explicitly approved by the team.
