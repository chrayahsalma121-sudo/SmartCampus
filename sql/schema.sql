-- =============================================================================
-- SALMA'S TABLES — append these to sql/schema.sql
-- (after Marwa's users/students/admins/librarians/books/borrowings tables)
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
    request_id      INT           AUTO_INCREMENT PRIMARY KEY,
    student_id      INT           NOT NULL,
    type            ENUM('SCHOOL_CERTIFICATE', 'ATTENDANCE_CERTIFICATE', 'TRANSCRIPT', 'OTHER') NOT NULL,
    description     TEXT,
    status          ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    submission_date DATE          NOT NULL,
    refusal_reason  TEXT          DEFAULT NULL,

    CONSTRAINT fk_req_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);
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
