-- =============================================================================
-- SALMA'S SEED DATA — append these to sql/seed.sql
-- (after Marwa's seed inserts)
-- =============================================================================

-- Rooms
INSERT INTO rooms (name, capacity, available) VALUES
    ('Room A101',  30, TRUE),
    ('Room B204',  20, TRUE),
    ('Lab C301',   15, TRUE),
    ('Conference', 50, FALSE),
    ('Study Hall', 10, TRUE);

-- Sample reservations (assumes student_id = 1 exists from Marwa's seed)
INSERT INTO reservations (student_id, room_id, reservation_date, start_time, end_time, status) VALUES
    (1, 1, CURDATE(), '09:00:00', '11:00:00', 'CONFIRMED'),
    (1, 2, CURDATE(), '14:00:00', '16:00:00', 'CANCELLED');

-- Sample administrative requests
INSERT INTO administrative_requests (student_id, type, description, status, submission_date, refusal_reason) VALUES
    (1, 'TRANSCRIPT',           'Need transcript for university application.', 'PENDING',  CURDATE(), NULL),
    (1, 'SCHOOL_CERTIFICATE',   'Required for visa application.',               'APPROVED', CURDATE(), NULL),
    (1, 'ATTENDANCE_CERTIFICATE','For internship application.',                 'REJECTED', CURDATE(), 'Missing supporting documents.');
