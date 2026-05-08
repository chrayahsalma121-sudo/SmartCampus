-- =============================================================================
-- MARWA'S SEED DATA
-- Default accounts + sample books
-- =============================================================================

-- Default users (plain-text passwords for MVP)
INSERT INTO users (full_name, email, password, role) VALUES
    ('Marwa Machach',    'student@example.com',   '1234', 'STUDENT'),
    ('Admin User',       'admin@example.com',     '1234', 'ADMIN'),
    ('Library Manager',  'librarian@example.com', '1234', 'LIBRARIAN');

-- Role-specific rows (user_id 1 = student, 2 = admin, 3 = librarian)
INSERT INTO students   (user_id, filiere, valid)  VALUES (1, 'Big Data & Cloud Computing', TRUE);
INSERT INTO admins     (user_id)                  VALUES (2);
INSERT INTO librarians (user_id)                  VALUES (3);

-- Sample books
INSERT INTO books (title, author, status) VALUES
    ('Java Programming',           'John Smith',       'AVAILABLE'),
    ('Clean Code',                 'Robert C. Martin', 'AVAILABLE'),
    ('Design Patterns',            'Gang of Four',     'AVAILABLE'),
    ('The Pragmatic Programmer',   'Hunt & Thomas',    'AVAILABLE'),
    ('Introduction to Algorithms', 'Cormen et al.',    'AVAILABLE');

-- =============================================================================
-- SALMA'S SEED DATA
-- Rooms + sample reservations + sample administrative requests
-- =============================================================================

-- Rooms
INSERT INTO rooms (name, capacity, available) VALUES
    ('Room A101',   30, TRUE),
    ('Room B204',   20, TRUE),
    ('Lab C301',    15, TRUE),
    ('Conference',  50, FALSE),
    ('Study Hall',  10, TRUE);

-- Sample reservations (student_id = 1 from Marwa's seed above)
INSERT INTO reservations (student_id, room_id, reservation_date, start_time, end_time, status) VALUES
    (1, 1, CURDATE(), '09:00:00', '11:00:00', 'CONFIRMED'),
    (1, 2, CURDATE(), '14:00:00', '16:00:00', 'CANCELLED');

-- Sample administrative requests
INSERT INTO administrative_requests (student_id, type, description, status, submission_date, refusal_reason) VALUES
    (1, 'TRANSCRIPT',            'Need transcript for university application.', 'PENDING',  CURDATE(), NULL),
    (1, 'SCHOOL_CERTIFICATE',    'Required for visa application.',               'APPROVED', CURDATE(), NULL),
    (1, 'ATTENDANCE_CERTIFICATE','For internship application.',                  'REJECTED', CURDATE(), 'Missing supporting documents.');
