package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Reservation;
import model.Room;
import security.AuthFilter;
import security.AuthenticatedUser;
import service.RoomService;
import util.JsonUtil;
import util.ResponseUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * RoomController handles all room-related HTTP endpoints.
 *
 * Routes (register in Main.java):
 *   GET  /api/rooms                           → listRooms
 *   POST /api/rooms/reserve                   → reserveRoom
 *   POST /api/rooms/cancel-reservation        → cancelReservation
 *   GET  /api/rooms/my-reservations           → myReservations
 *   POST /api/admin/rooms/update-availability → updateAvailability
 *   POST /api/admin/rooms                     → addRoom
 */
public class RoomController implements HttpHandler {

    private final RoomService roomService = new RoomService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path   = exchange.getRequestURI().getPath();

        try {
            // ------------------------------------------------------------------
            // GET /api/rooms  — list all rooms (public, no auth required)
            // ------------------------------------------------------------------
            if (method.equals("GET") && path.equals("/api/rooms")) {
                handleListRooms(exchange);

            // ------------------------------------------------------------------
            // GET /api/rooms/my-reservations  — student's own reservations
            // ------------------------------------------------------------------
            } else if (method.equals("GET") && path.equals("/api/rooms/my-reservations")) {
                handleMyReservations(exchange);

            // ------------------------------------------------------------------
            // POST /api/rooms/reserve
            // ------------------------------------------------------------------
            } else if (method.equals("POST") && path.equals("/api/rooms/reserve")) {
                handleReserveRoom(exchange);

            // ------------------------------------------------------------------
            // POST /api/rooms/cancel-reservation
            // ------------------------------------------------------------------
            } else if (method.equals("POST") && path.equals("/api/rooms/cancel-reservation")) {
                handleCancelReservation(exchange);

            // ------------------------------------------------------------------
            // POST /api/admin/rooms/update-availability
            // ------------------------------------------------------------------
            } else if (method.equals("POST") && path.equals("/api/admin/rooms/update-availability")) {
                handleUpdateAvailability(exchange);

            // ------------------------------------------------------------------
            // POST /api/admin/rooms  — admin adds a room
            // ------------------------------------------------------------------
            } else if (method.equals("POST") && path.equals("/api/admin/rooms")) {
                handleAddRoom(exchange);

            // ------------------------------------------------------------------
            // GET /api/admin/reservations
            // ------------------------------------------------------------------
            } else if (method.equals("GET") && path.equals("/api/admin/reservations")) {
                handleAllReservations(exchange);

            } else {
                ResponseUtil.sendError(exchange, 404, "Endpoint not found.");
            }

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    // =========================================================================
    // GET /api/rooms
    // =========================================================================
    private void handleListRooms(HttpExchange exchange) throws IOException {
        try {
            List<Room> rooms = roomService.listRooms();

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < rooms.size(); i++) {
                sb.append(roomToJson(rooms.get(i)));
                if (i < rooms.size() - 1) sb.append(",");
            }
            sb.append("]");

            ResponseUtil.sendSuccess(exchange, 200, "Rooms retrieved successfully.", sb.toString());

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 500, e.getMessage());
        }
    }

    // =========================================================================
    // POST /api/rooms/reserve
    // Body: { "roomId": 1, "reservationDate": "2025-06-01", "startTime": "09:00", "endTime": "11:00" }
    // =========================================================================
    private void handleReserveRoom(HttpExchange exchange) throws IOException {
        try {
            AuthenticatedUser authUser = AuthFilter.getAuthenticatedUser(exchange);
            if (authUser == null) {
                ResponseUtil.sendError(exchange, 401, "Unauthorized. Token missing or invalid.");
                return;
            }

            Map<String, Object> body = JsonUtil.parseBody(exchange);

            int    roomId          = JsonUtil.getInt(body, "roomId");
            String reservationDate = JsonUtil.getString(body, "reservationDate");
            String startTime       = JsonUtil.getString(body, "startTime");
            String endTime         = JsonUtil.getString(body, "endTime");

            Reservation reservation = roomService.reserveRoom(
                authUser, roomId, reservationDate, startTime, endTime
            );

            ResponseUtil.sendSuccess(exchange, 201, "Room reserved successfully.", reservationToJson(reservation));

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    // =========================================================================
    // POST /api/rooms/cancel-reservation
    // Body: { "reservationId": 5 }
    // =========================================================================
    private void handleCancelReservation(HttpExchange exchange) throws IOException {
        try {
            AuthenticatedUser authUser = AuthFilter.getAuthenticatedUser(exchange);
            if (authUser == null) {
                ResponseUtil.sendError(exchange, 401, "Unauthorized. Token missing or invalid.");
                return;
            }

            Map<String, Object> body          = JsonUtil.parseBody(exchange);
            int                 reservationId = JsonUtil.getInt(body, "reservationId");

            roomService.cancelReservation(authUser, reservationId);

            ResponseUtil.sendSuccess(exchange, 200, "Reservation cancelled successfully.", "{}");

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    // =========================================================================
    // GET /api/rooms/my-reservations
    // =========================================================================
    private void handleMyReservations(HttpExchange exchange) throws IOException {
        try {
            AuthenticatedUser authUser = AuthFilter.getAuthenticatedUser(exchange);
            if (authUser == null) {
                ResponseUtil.sendError(exchange, 401, "Unauthorized. Token missing or invalid.");
                return;
            }

            List<Reservation> reservations = roomService.myReservations(authUser);

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < reservations.size(); i++) {
                sb.append(reservationToJson(reservations.get(i)));
                if (i < reservations.size() - 1) sb.append(",");
            }
            sb.append("]");

            ResponseUtil.sendSuccess(exchange, 200, "Reservations retrieved successfully.", sb.toString());

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    // =========================================================================
    // POST /api/admin/rooms/update-availability
    // Body: { "roomId": 2, "available": false }
    // =========================================================================
    private void handleUpdateAvailability(HttpExchange exchange) throws IOException {
        try {
            AuthenticatedUser authUser = AuthFilter.getAuthenticatedUser(exchange);
            if (authUser == null) {
                ResponseUtil.sendError(exchange, 401, "Unauthorized. Token missing or invalid.");
                return;
            }

            Map<String, Object> body      = JsonUtil.parseBody(exchange);
            int                 roomId    = JsonUtil.getInt(body, "roomId");
            boolean             available = JsonUtil.getBoolean(body, "available");

            Room updated = roomService.updateAvailability(authUser, roomId, available);

            ResponseUtil.sendSuccess(exchange, 200, "Room availability updated.", roomToJson(updated));

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    // =========================================================================
    // POST /api/admin/rooms
    // Body: { "name": "Lab B", "capacity": 30, "available": true }
    // =========================================================================
    private void handleAddRoom(HttpExchange exchange) throws IOException {
        try {
            AuthenticatedUser authUser = AuthFilter.getAuthenticatedUser(exchange);
            if (authUser == null) {
                ResponseUtil.sendError(exchange, 401, "Unauthorized. Token missing or invalid.");
                return;
            }

            Map<String, Object> body      = JsonUtil.parseBody(exchange);
            String              name      = JsonUtil.getString(body, "name");
            int                 capacity  = JsonUtil.getInt(body, "capacity");
            boolean             available = JsonUtil.getBoolean(body, "available");

            Room room = roomService.addRoom(authUser, name, capacity, available);

            ResponseUtil.sendSuccess(exchange, 201, "Room added successfully.", roomToJson(room));

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    // =========================================================================
    // GET /api/admin/reservations
    // =========================================================================
    private void handleAllReservations(HttpExchange exchange) throws IOException {
        try {
            AuthenticatedUser authUser = AuthFilter.getAuthenticatedUser(exchange);
            if (authUser == null) {
                ResponseUtil.sendError(exchange, 401, "Unauthorized. Token missing or invalid.");
                return;
            }

            List<Reservation> reservations = roomService.listAllReservations(authUser);

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < reservations.size(); i++) {
                sb.append(reservationToJson(reservations.get(i)));
                if (i < reservations.size() - 1) sb.append(",");
            }
            sb.append("]");

            ResponseUtil.sendSuccess(exchange, 200, "All reservations retrieved.", sb.toString());

        } catch (Exception e) {
            ResponseUtil.sendError(exchange, 400, e.getMessage());
        }
    }

    // =========================================================================
    // JSON helpers
    // =========================================================================
    private String roomToJson(Room r) {
        return String.format(
            "{\"roomId\":%d,\"name\":\"%s\",\"capacity\":%d,\"available\":%b}",
            r.getRoomId(), escape(r.getName()), r.getCapacity(), r.isAvailable()
        );
    }

    private String reservationToJson(Reservation r) {
        return String.format(
            "{\"reservationId\":%d,\"studentId\":%d,\"roomId\":%d," +
            "\"reservationDate\":\"%s\",\"startTime\":\"%s\",\"endTime\":\"%s\",\"status\":\"%s\"}",
            r.getReservationId(), r.getStudentId(), r.getRoomId(),
            r.getReservationDate(), r.getStartTime(), r.getEndTime(),
            r.getStatus().name()
        );
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
