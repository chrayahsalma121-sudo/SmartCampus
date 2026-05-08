package repository;

import database.DatabaseConnection;
import model.Room;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomRepository {

    // -------------------------------------------------------------------------
    // Find all rooms
    // -------------------------------------------------------------------------
    public List<Room> findAll() throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT room_id, name, capacity, available FROM rooms";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rooms.add(mapRow(rs));
            }
        }
        return rooms;
    }

    // -------------------------------------------------------------------------
    // Find room by id
    // -------------------------------------------------------------------------
    public Room findById(int roomId) throws SQLException {
        String sql = "SELECT room_id, name, capacity, available FROM rooms WHERE room_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Insert new room (admin)
    // -------------------------------------------------------------------------
    public Room save(Room room) throws SQLException {
        String sql = "INSERT INTO rooms (name, capacity, available) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, room.getName());
            ps.setInt(2, room.getCapacity());
            ps.setBoolean(3, room.isAvailable());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) room.setRoomId(keys.getInt(1));
            }
        }
        return room;
    }

    // -------------------------------------------------------------------------
    // Update availability only (admin toggle)
    // -------------------------------------------------------------------------
    public boolean updateAvailability(int roomId, boolean available) throws SQLException {
        String sql = "UPDATE rooms SET available = ? WHERE room_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, available);
            ps.setInt(2, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    // -------------------------------------------------------------------------
    // Map ResultSet row → Room
    // -------------------------------------------------------------------------
    private Room mapRow(ResultSet rs) throws SQLException {
        return new Room(
            rs.getInt("room_id"),
            rs.getString("name"),
            rs.getInt("capacity"),
            rs.getBoolean("available")
        );
    }
}
