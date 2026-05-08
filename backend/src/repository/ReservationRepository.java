package repository;

import database.DatabaseConnection;
import enums.ReservationStatus;
import model.Reservation;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationRepository {

    // -------------------------------------------------------------------------
    // Save a new reservation
    // -------------------------------------------------------------------------
    public Reservation save(Reservation r) throws SQLException {
        String sql = """
            INSERT INTO reservations
                (student_id, room_id, reservation_date, start_time, end_time, status)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, r.getStudentId());
            ps.setInt(2, r.getRoomId());
            ps.setDate(3, Date.valueOf(r.getReservationDate()));
            ps.setTime(4, Time.valueOf(r.getStartTime()));
            ps.setTime(5, Time.valueOf(r.getEndTime()));
            ps.setString(6, r.getStatus().name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) r.setReservationId(keys.getInt(1));
            }
        }
        return r;
    }

    // -------------------------------------------------------------------------
    // Find reservation by id
    // -------------------------------------------------------------------------
    public Reservation findById(int reservationId) throws SQLException {
        String sql = """
            SELECT r.reservation_id, r.student_id, u.full_name AS student_name,
                   r.room_id, rooms.name AS room_name, r.reservation_date,
                   r.start_time, r.end_time, r.status
            FROM reservations r
            JOIN rooms ON rooms.room_id = r.room_id
            JOIN students s ON s.student_id = r.student_id
            JOIN users u ON u.user_id = s.user_id
            WHERE r.reservation_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // All reservations for a student
    // -------------------------------------------------------------------------
    public List<Reservation> findByStudentId(int studentId) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = """
            SELECT r.reservation_id, r.student_id, u.full_name AS student_name,
                   r.room_id, rooms.name AS room_name, r.reservation_date,
                   r.start_time, r.end_time, r.status
            FROM reservations r
            JOIN rooms ON rooms.room_id = r.room_id
            JOIN students s ON s.student_id = r.student_id
            JOIN users u ON u.user_id = s.user_id
            WHERE r.student_id = ?
            ORDER BY r.reservation_date DESC, r.start_time DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // All reservations (admin view)
    // -------------------------------------------------------------------------
    public List<Reservation> findAll() throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = """
            SELECT r.reservation_id, r.student_id, u.full_name AS student_name,
                   r.room_id, rooms.name AS room_name, r.reservation_date,
                   r.start_time, r.end_time, r.status
            FROM reservations r
            JOIN rooms ON rooms.room_id = r.room_id
            JOIN students s ON s.student_id = r.student_id
            JOIN users u ON u.user_id = s.user_id
            ORDER BY r.reservation_date DESC, r.start_time DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // Conflict check 1: is the room already booked for this slot?
    // -------------------------------------------------------------------------
    public boolean roomHasConflict(int roomId, LocalDate date,
                                   LocalTime start, LocalTime end) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM reservations
            WHERE room_id = ?
              AND reservation_date = ?
              AND status = 'CONFIRMED'
              AND start_time < ?
              AND end_time   > ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            ps.setDate(2, Date.valueOf(date));
            ps.setTime(3, Time.valueOf(end));
            ps.setTime(4, Time.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Conflict check 2: does the student already have a reservation this slot?
    // -------------------------------------------------------------------------
    public boolean studentHasConflict(int studentId, LocalDate date,
                                      LocalTime start, LocalTime end) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM reservations
            WHERE student_id = ?
              AND reservation_date = ?
              AND status = 'CONFIRMED'
              AND start_time < ?
              AND end_time   > ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setDate(2, Date.valueOf(date));
            ps.setTime(3, Time.valueOf(end));
            ps.setTime(4, Time.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Cancel a reservation (set status = CANCELLED)
    // -------------------------------------------------------------------------
    public boolean cancel(int reservationId) throws SQLException {
        String sql = "UPDATE reservations SET status = 'CANCELLED' WHERE reservation_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reservationId);
            return ps.executeUpdate() > 0;
        }
    }

    // -------------------------------------------------------------------------
    // Map ResultSet row → Reservation
    // -------------------------------------------------------------------------
    private Reservation mapRow(ResultSet rs) throws SQLException {
        String studentName = null;
        String roomName = null;
        try {
            studentName = rs.getString("student_name");
            roomName = rs.getString("room_name");
        } catch (SQLException ignored) {}

        return new Reservation(
            rs.getInt("reservation_id"),
            rs.getInt("student_id"),
            studentName,
            rs.getInt("room_id"),
            roomName,
            rs.getDate("reservation_date").toLocalDate(),
            rs.getTime("start_time").toLocalTime(),
            rs.getTime("end_time").toLocalTime(),
            ReservationStatus.valueOf(rs.getString("status"))
        );
    }
}
