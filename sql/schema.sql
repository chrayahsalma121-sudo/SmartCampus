-- =============================================================================
-- MARWA'S TABLES
-- Foundation tables: users, students, admins, librarians, books, borrowings
-- =============================================================================

CREATE TABLE IF NOT EXISTS users (
    user_id   INT          AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(200) NOT NULL,
    email     VARCHAR(200) NOT NULL UNIQUE,
    password  VARCHAR(255) NOT NULL,
    role      ENUM('STUDENT', 'ADMIN', 'LIBRARIAN') NOT NULL
);

CREATE TABLE IF NOT EXISTS students (
    student_id INT          AUTO_INCREMENT PRIMARY KEY,
    user_id    INT          NOT NULL UNIQUE,
    filiere    VARCHAR(200) NOT NULL,
    valid      BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS admins (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id  INT NOT NULL UNIQUE,
    CONSTRAINT fk_admin_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS librarians (
    librarian_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT NOT NULL UNIQUE,
    CONSTRAINT fk_librarian_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS books (
    book_id INT          AUTO_INCREMENT PRIMARY KEY,
    title   VARCHAR(300) NOT NULL,
    author  VARCHAR(200) NOT NULL,
    status  ENUM('AVAILABLE', 'BORROWED') NOT NULL DEFAULT 'AVAILABLE'
);

CREATE TABLE IF NOT EXISTS borrowings (
    borrowing_id INT     AUTO_INCREMENT PRIMARY KEY,
    student_id   INT     NOT NULL,
    book_id      INT     NOT NULL,
    borrow_date  DATE    NOT NULL,
    return_date  DATE    NOT NULL,
    returned     BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_borrow_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_borrow_book    FOREIGN KEY (book_id)    REFERENCES books(book_id)       ON DELETE CASCADE
);

-- =============================================================================
-- SALMA'S TABLES — rooms, reservations, administrative_requests
-- =============================================================================

CREATE TABLE IF NOT EXISTS rooms (
    room_id   INT          AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(150) NOT NULL,
    capacity  INT          NOT NULL CHECK (capacity > 0),
    available BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS reservations (
    reservation_id   INT  AUTO_INCREMENT PRIMARY KEY,
    student_id       INT  NOT NULL,
    room_id          INT  NOT NULL,
    reservation_date DATE NOT NULL,
    start_time       TIME NOT NULL,
    end_time         TIME NOT NULL,
    status           ENUM('CONFIRMED', 'CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    CONSTRAINT fk_res_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_res_room    FOREIGN KEY (room_id)    REFERENCES rooms(room_id)       ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS administrative_requests (
    request_id      INT  AUTO_INCREMENT PRIMARY KEY,
    student_id      INT  NOT NULL,
    type            ENUM('SCHOOL_CERTIFICATE', 'ATTENDANCE_CERTIFICATE', 'TRANSCRIPT', 'OTHER') NOT NULL,
    description     TEXT,
    status          ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    submission_date DATE NOT NULL,
    refusal_reason  TEXT DEFAULT NULL,
    CONSTRAINT fk_req_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);
