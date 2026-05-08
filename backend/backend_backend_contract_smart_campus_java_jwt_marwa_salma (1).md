# Backend–Backend Contract
## Smart Campus — CampusServices MVP
### Java + MySQL + JWT Version with User Inheritance

> This contract is made for two backend developers working on the same Java backend project.
> Each developer must respect their assigned scope and avoid modifying the other developer’s files unless coordinated.

---

# 0. Assignment Header

## Project

Smart Campus — CampusServices Backend MVP

## Team Members

```txt
PERSON_A = Marwa
PERSON_B = Salma
```

---

# 1. Purpose

This contract defines how Marwa and Salma will split the backend implementation.

The goal is to allow both backend developers to work in parallel without breaking each other’s code.

The backend must respect the frontend–backend contract for:

- endpoint paths
- request shapes
- response shapes
- JWT authentication
- role authorization
- enum values
- database structure
- business rules
- MVP boundaries

---

# 2. Final Backend Decisions

This project uses:

```txt
Java
MySQL
JDBC
JWT authentication
No Spring Boot
No external EnrollSys API
```

The backend uses user inheritance:

```txt
User
├── Student
├── Admin
└── Librarian
```

The project keeps a minimized MVP scope.

---

# 3. Global Backend Rules

Both developers must respect these rules.

---

## 3.1 Do Not Break the Frontend–Backend Contract

All backend endpoints must follow the agreed frontend–backend contract.

Do not randomly rename:

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

Do not change the response format.

Success response:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {}
}
```

Error response:

```json
{
  "success": false,
  "message": "Error message"
}
```

---

## 3.2 MVP Only

Do not implement:

- refresh tokens
- token blacklist
- advanced session management
- external EnrollSys API
- notifications table
- email sending
- dashboard statistics
- file upload
- payment
- complex admin panel

---

## 3.3 Database

The project uses MySQL.

Main tables:

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

## 3.4 JWT Authentication

Authentication uses JWT.

Login endpoint:

```http
POST /api/auth/login
```

Current user endpoint:

```http
GET /api/auth/me
```

After login, the frontend sends:

```http
Authorization: Bearer <accessToken>
```

The backend must extract the logged-in user and role from the token.

Frontend should not send `studentId`, `adminId`, or `librarianId` in protected requests unless the endpoint is explicitly about viewing another resource.

---

## 3.5 Roles

Use exactly:

```txt
STUDENT
ADMIN
LIBRARIAN
```

---

# 4. Work Split Summary

## Marwa — Foundation + Security + Users + Library

Marwa owns:

- database connection
- SQL schema base
- user inheritance models
- authentication/login
- JWT generation and validation
- current user endpoint
- auth filter / auth helper
- user repositories
- library models
- book listing
- book borrowing
- book returning
- librarian book management

---

## Salma — Rooms + Administrative Requests

Salma owns:

- room model
- room repository
- room reservation service
- room reservation endpoints
- admin room availability
- administrative request model
- administrative request repository
- request submission
- request tracking
- admin approve/reject logic

Salma uses Marwa’s JWT/auth helpers to get the logged-in user.

---

# 5. Shared Foundation

Some files are shared and must be coordinated.

## Shared Files / Folders

```txt
src/Main.java
src/database/DatabaseConnection.java
src/security/
sql/schema.sql
sql/seed.sql
README.md
docs/
```

## Rule

If one developer changes a shared file, they must tell the other developer.

Avoid large unrelated edits in shared files.

---

# 6. Recommended Project Structure

```txt
SmartCampus/
│
├── src/
│   ├── Main.java
│   │
│   ├── model/
│   │   ├── User.java
│   │   ├── Student.java
│   │   ├── Admin.java
│   │   ├── Librarian.java
│   │   ├── Book.java
│   │   ├── Borrowing.java
│   │   ├── Room.java
│   │   ├── Reservation.java
│   │   └── AdministrativeRequest.java
│   │
│   ├── enums/
│   │   ├── UserRole.java
│   │   ├── BookStatus.java
│   │   ├── ReservationStatus.java
│   │   ├── RequestStatus.java
│   │   └── RequestType.java
│   │
│   ├── database/
│   │   └── DatabaseConnection.java
│   │
│   ├── security/
│   │   ├── JwtUtil.java
│   │   ├── AuthFilter.java
│   │   └── PasswordUtil.java
│   │
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── StudentRepository.java
│   │   ├── AdminRepository.java
│   │   ├── LibrarianRepository.java
│   │   ├── BookRepository.java
│   │   ├── BorrowingRepository.java
│   │   ├── RoomRepository.java
│   │   ├── ReservationRepository.java
│   │   └── AdministrativeRequestRepository.java
│   │
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── LibraryService.java
│   │   ├── RoomService.java
│   │   └── AdministrativeRequestService.java
│   │
│   └── controller/
│       ├── AuthController.java
│       ├── LibraryController.java
│       ├── RoomController.java
│       └── AdministrativeRequestController.java
│
├── sql/
│   ├── schema.sql
│   └── seed.sql
│
├── lib/
│   ├── mysql-connector-j.jar
│   └── jjwt.jar
│
├── docs/
│   ├── frontend_backend_contract.md
│   └── backend_backend_contract.md
│
├── README.md
└── .gitignore
```

---

# 7. Shared Enums

Both developers must use the same enum values.

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

# 8. Marwa Scope

## 8.1 Main Responsibility

Marwa builds the foundation, security, authentication, user inheritance, and library side.

This includes:

- database connection
- user inheritance
- JWT login
- current user endpoint
- protected endpoint helpers
- books
- borrowings
- librarian book management

---

## 8.2 Files Owned by Marwa

```txt
src/database/DatabaseConnection.java

src/security/JwtUtil.java
src/security/AuthFilter.java
src/security/PasswordUtil.java

src/model/User.java
src/model/Student.java
src/model/Admin.java
src/model/Librarian.java
src/model/Book.java
src/model/Borrowing.java

src/enums/UserRole.java
src/enums/BookStatus.java

src/repository/UserRepository.java
src/repository/StudentRepository.java
src/repository/AdminRepository.java
src/repository/LibrarianRepository.java
src/repository/BookRepository.java
src/repository/BorrowingRepository.java

src/service/AuthService.java
src/service/LibraryService.java

src/controller/AuthController.java
src/controller/LibraryController.java
```

Marwa may also create the first version of:

```txt
sql/schema.sql
sql/seed.sql
README.md
```

---

## 8.3 Models Owned by Marwa

### User

Parent class.

Fields:

```txt
userId
fullName
email
password
role
```

### Student

Extends `User`.

Fields:

```txt
studentId
userId
filiere
valid
```

### Admin

Extends `User`.

Fields:

```txt
adminId
userId
```

### Librarian

Extends `User`.

Fields:

```txt
librarianId
userId
```

### Book

Fields:

```txt
bookId
title
author
status
```

### Borrowing

Fields:

```txt
borrowingId
studentId
bookId
borrowDate
returnDate
returned
```

---

## 8.4 Database Tables Owned by Marwa

```txt
users
students
admins
librarians
books
borrowings
```

Marwa must create SQL for these tables.

---

## 8.5 Endpoints Owned by Marwa

### Authentication

```http
POST /api/auth/login
GET /api/auth/me
```

### Library

```http
GET /api/books
POST /api/books/borrow
POST /api/books/return
GET /api/books/my-borrowings
```

### Librarian Book Management

```http
POST /api/librarian/books
POST /api/librarian/books/update
POST /api/librarian/books/delete
```

---

## 8.6 Marwa Business Rules

### Login and JWT

- User can login using email and password.
- Backend returns `accessToken`.
- Token contains at least:
  - `userId`
  - `role`
- Backend can use the token to find:
  - `studentId`
  - `adminId`
  - `librarianId`

### Current User

`GET /api/auth/me` should return the logged-in user based on JWT.

### Role Protection

Marwa provides helper logic to check:

```txt
STUDENT
ADMIN
LIBRARIAN
```

### Student Validity for Library

A student cannot borrow a book if:

```txt
valid = false
```

### Book Borrowing

- A book can only be borrowed if status is `AVAILABLE`.
- When borrowed, book status becomes `BORROWED`.
- Borrowing creates a new row in `borrowings`.
- A student cannot have more than 3 active borrowed books.
- Return date can be calculated as borrow date + 14 days.

### Book Return

- Student can only return their own borrowing.
- When returned, borrowing becomes `returned = true`.
- Book status becomes `AVAILABLE`.

### Librarian Management

Only a user with role `LIBRARIAN` should add, update, or delete books.

---

## 8.7 Marwa Must Not Implement

Marwa should not implement:

- room reservation logic
- reservation conflict rules
- administrative request logic
- admin request approval/rejection

Marwa may help with shared security setup, but must avoid taking over Salma’s scope without coordination.

---

# 9. Salma Scope

## 9.1 Main Responsibility

Salma builds the rooms and administrative request side.

This includes:

- room listing
- room reservation
- reservation cancellation
- admin room availability update
- student administrative requests
- admin request processing

---

## 9.2 Files Owned by Salma

```txt
src/model/Room.java
src/model/Reservation.java
src/model/AdministrativeRequest.java

src/enums/ReservationStatus.java
src/enums/RequestStatus.java
src/enums/RequestType.java

src/repository/RoomRepository.java
src/repository/ReservationRepository.java
src/repository/AdministrativeRequestRepository.java

src/service/RoomService.java
src/service/AdministrativeRequestService.java

src/controller/RoomController.java
src/controller/AdministrativeRequestController.java
```

Salma may update:

```txt
sql/schema.sql
sql/seed.sql
README.md
```

but must coordinate because these files are shared.

---

## 9.3 Models Owned by Salma

### Room

Fields:

```txt
roomId
name
capacity
available
```

### Reservation

Fields:

```txt
reservationId
studentId
roomId
reservationDate
startTime
endTime
status
```

### AdministrativeRequest

Fields:

```txt
requestId
studentId
type
description
status
submissionDate
refusalReason
```

---

## 9.4 Database Tables Owned by Salma

```txt
rooms
reservations
administrative_requests
```

Salma must add SQL for these tables.

---

## 9.5 Endpoints Owned by Salma

### Rooms

```http
GET /api/rooms
POST /api/rooms/reserve
POST /api/rooms/cancel-reservation
GET /api/rooms/my-reservations
```

### Admin Room Management

```http
POST /api/admin/rooms/update-availability
POST /api/admin/rooms
```

### Administrative Requests

```http
POST /api/requests
GET /api/requests/my-requests
```

### Admin Administrative Requests

```http
GET /api/admin/requests
POST /api/admin/requests/approve
POST /api/admin/requests/reject
```

---

## 9.6 Salma Business Rules

### JWT and User Identity

Salma must not trust `studentId` or `adminId` from the frontend for protected actions.

Salma should get the logged-in user from JWT using Marwa’s auth helper.

Example:

```txt
reserve room:
- token gives userId + role
- backend finds studentId
- backend uses that studentId
```

### Student Validity for Room Reservations

A student cannot reserve a room if:

```txt
valid = false
```

### Room Reservation

- Only `STUDENT` can reserve rooms.
- A room must be available to be reserved.
- A room cannot have two reservations in the same date and time slot.
- A student cannot have two reservations at the same date and time slot.
- A successful reservation has status `CONFIRMED`.

### Reservation Cancellation

- Only `STUDENT` can cancel a reservation.
- Student can only cancel their own reservation.
- Cancelling a reservation changes its status to `CANCELLED`.

### Admin Room Management

- Only `ADMIN` can add rooms.
- Only `ADMIN` can update room availability.

### Administrative Requests

- Only `STUDENT` can submit a request.
- A student cannot submit a request if `valid = false`.
- A new request always starts with `PENDING`.
- Request type is required.
- Only `ADMIN` can view all requests.
- Only `ADMIN` can approve a request.
- Only `ADMIN` can reject a request.
- A rejected request should include `refusalReason`.

---

## 9.7 Salma Must Not Implement

Salma should not implement:

- login/auth logic
- JWT generation
- JWT validation internals
- user inheritance models
- book management
- book borrowing
- book return logic
- librarian logic

Salma can use auth/user helper methods from Marwa’s side.

---

# 10. Integration Points Between Marwa and Salma

## 10.1 Auth Context Dependency

Salma needs a clean way to get the logged-in user from the JWT.

Marwa should provide a helper or object such as:

```java
AuthContext
```

or methods like:

```java
getCurrentUserId()
getCurrentUserRole()
getCurrentStudentId()
getCurrentAdminId()
```

Recommended simple approach:

```java
AuthenticatedUser authUser = AuthFilter.getAuthenticatedUser(request);
```

Expected fields:

```txt
userId
role
studentId nullable
adminId nullable
librarianId nullable
```

---

## 10.2 Student Validity Dependency

Salma needs to check if a student is valid before:

- reserving a room
- submitting an administrative request

Marwa should provide a method such as:

```java
public boolean isStudentValid(int studentId)
```

Recommended location:

```txt
StudentRepository.java
```

---

## 10.3 Admin Identity Dependency

Salma needs admin identity for admin actions.

Marwa should provide a way to map:

```txt
userId → adminId
```

Recommended method:

```java
public Integer findAdminIdByUserId(int userId)
```

Recommended location:

```txt
AdminRepository.java
```

---

## 10.4 Shared DatabaseConnection

Both developers must use:

```java
DatabaseConnection.getConnection()
```

Do not create separate connection systems.

---

## 10.5 Main.java Routing / Testing

If the backend is implemented as a simple console or lightweight HTTP server, both developers must coordinate how `Main.java` routes actions.

No one should delete the other person’s test menu or route setup.

---

# 11. Development Order

## Phase 1 — Marwa Starts

Marwa should first deliver:

```txt
DatabaseConnection
User / Student / Admin / Librarian models
UserRole enum
users/students/admins/librarians tables
seed users
JwtUtil
AuthFilter or auth helper
AuthService
AuthController
login endpoint
me endpoint
```

This is important because Salma needs authentication and student/admin data.

---

## Phase 2 — Parallel Development

After auth foundation exists, both can work in parallel.

Marwa works on:

```txt
Book
Borrowing
BookRepository
BorrowingRepository
LibraryService
LibraryController
```

Salma works on:

```txt
Room
Reservation
AdministrativeRequest
RoomRepository
ReservationRepository
AdministrativeRequestRepository
RoomService
AdministrativeRequestService
RoomController
AdministrativeRequestController
```

---

## Phase 3 — Integration

Both developers verify:

```txt
login returns JWT
JWT works on protected endpoints
auth/me works
role checks work
studentId/adminId/librarianId are resolved correctly
student validity checks work
book borrowing respects rules
room reservation respects rules
administrative requests respect rules
response shapes match frontend contract
MySQL schema runs correctly
seed data is usable
```

---

# 12. Branching Recommendation

Use one branch per backend developer.

```txt
backend/marwa-auth-users-library
backend/salma-rooms-requests
```

Suggested merge order:

```txt
1. Merge Marwa foundation first
2. Rebase Salma branch on latest main
3. Resolve SQL/schema conflicts
4. Merge Marwa library work
5. Merge Salma rooms/requests work
```

---

# 13. Commit Rules

Each commit should be clear.

Good examples:

```txt
Add JWT authentication utility
Add user inheritance models
Add login service
Add book repository
Add room reservation conflict check
Add administrative request approval logic
```

Bad examples:

```txt
update
fix
stuff
changes
```

---

# 14. Pull Request Rules

Each pull request should include:

```txt
What was implemented
Files changed
Database tables changed
Endpoints added
How to test
Known limitations
```

Avoid mixing unrelated work.

---

# 15. Testing Responsibilities

## Marwa Tests

Marwa should test:

```txt
login with student
login with admin
login with librarian
invalid login
JWT generated correctly
missing token rejected
invalid token rejected
auth/me returns current user
list books
borrow available book
refuse borrowed book
refuse invalid student
refuse if student has 3 books
return own book
refuse returning another student borrowing
add/update/delete book as librarian
refuse book management as student/admin
```

---

## Salma Tests

Salma should test:

```txt
list rooms
reserve available room as student
refuse reservation without token
refuse reservation as admin/librarian
refuse invalid student
refuse unavailable room
refuse same room same slot
refuse same student same slot
cancel own reservation
refuse cancelling another student reservation
student views own reservations
admin updates room availability
student submits request
invalid student cannot submit request
student views own requests
admin views all requests
admin approves request
admin rejects request with reason
refuse admin actions as student/librarian
```

---

# 16. Definition of Done

A backend task is done only when:

```txt
model is created
repository method is created
service business logic is created
controller method or endpoint is created
JWT/role protection is applied if needed
SQL table exists if needed
seed data exists if useful
response format matches contract
basic validation exists
manual test is done
README or notes are updated if needed
```

---

# 17. Final Backend MVP Checklist

## Marwa Checklist

```txt
[ ] DatabaseConnection completed
[ ] users table completed
[ ] students table completed
[ ] admins table completed
[ ] librarians table completed
[ ] User model completed
[ ] Student model completed
[ ] Admin model completed
[ ] Librarian model completed
[ ] UserRole enum completed
[ ] JwtUtil completed
[ ] AuthFilter/auth helper completed
[ ] PasswordUtil completed if used
[ ] Login endpoint completed
[ ] Login returns accessToken
[ ] Login returns role-specific user data
[ ] Auth/me endpoint completed
[ ] Protected endpoint helper completed
[ ] Book model completed
[ ] Borrowing model completed
[ ] BookStatus enum completed
[ ] books table completed
[ ] borrowings table completed
[ ] Book list endpoint completed
[ ] Borrow book endpoint completed
[ ] Return book endpoint completed
[ ] My borrowings endpoint completed
[ ] Librarian add book endpoint completed
[ ] Librarian update book endpoint completed
[ ] Librarian delete book endpoint completed
```

---

## Salma Checklist

```txt
[ ] Room model completed
[ ] Reservation model completed
[ ] AdministrativeRequest model completed
[ ] ReservationStatus enum completed
[ ] RequestStatus enum completed
[ ] RequestType enum completed
[ ] rooms table completed
[ ] reservations table completed
[ ] administrative_requests table completed
[ ] Room list endpoint completed
[ ] Reserve room endpoint completed
[ ] Cancel reservation endpoint completed
[ ] My reservations endpoint completed
[ ] Admin update room availability endpoint completed
[ ] Admin add room endpoint completed
[ ] Submit administrative request endpoint completed
[ ] My requests endpoint completed
[ ] Admin list requests endpoint completed
[ ] Admin approve request endpoint completed
[ ] Admin reject request endpoint completed
[ ] JWT role checks applied to all endpoints
```

---

# 18. AI Coding Instruction Block

Use this block at the beginning of AI coding sessions.

```txt
You are helping me implement the backend of Smart Campus — CampusServices MVP.

I am: [MARWA_OR_SALMA]

Follow the Backend–Backend Contract strictly.

Technology:
- Plain Java
- MySQL
- JDBC
- JWT
- No Spring Boot

Architecture:
- User inheritance: User, Student, Admin, Librarian
- Minimized MVP scope

If I am Marwa:
- Work only on database foundation, security/JWT, users, authentication, library, borrowings, and librarian book management.
- Do not implement rooms, reservations, or administrative requests unless I explicitly ask.

If I am Salma:
- Work only on rooms, reservations, administrative requests, and admin request/room actions.
- Do not implement auth, JWT generation, users, book management, or borrowing unless I explicitly ask.

Respect the frontend–backend contract response shapes.
Do not add out-of-scope MVP features.
```

---

# 19. Final Note

This backend split is designed to keep the work simple and organized.

Marwa builds:

```txt
foundation + JWT/auth + users + library
```

Salma builds:

```txt
rooms + reservations + administrative requests
```

Any change outside this contract should be discussed before implementation.
