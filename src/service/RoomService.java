package service;

import enums.ReservationStatus;
import enums.UserRole;
import model.Reservation;
import model.Room;
import repository.AdminRepository;
import repository.ReservationRepository;
import repository.RoomRepository;
import repository.StudentRepository;
import security.AuthenticatedUser;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class RoomService {

    private final RoomRepository        roomRepo        = new RoomRepository();
    private final ReservationRepository reservationRepo = new ReservationRepository();
    private final StudentRepository     studentRepo     = new StudentRepository();

    // =========================================================================
    // LIST ALL ROOMS  →  GET /api/rooms
    // =========================================================================
    public List<Room> listRooms() throws SQLException {
        return roomRepo.findAll();
    }

    // =========================================================================
    // RESERVE A ROOM  →  POST /api/rooms/reserve
    // Body: { roomId, reservationDate, startTime, endTime }
    // =========================================================================
    public Reservation reserveRoom(AuthenticatedUser authUser,
                                   int roomId,
                                   String reservationDateStr,
                                   String startTimeStr,
                                   String endTimeStr) throws Exception {

        // --- Role check ---
        if (authUser.getRole() != UserRole.STUDENT) {
            throw new Exception("Only students can reserve rooms.");
        }

        int studentId = authUser.getStudentId();

        // --- Student validity check ---
        if (!studentRepo.isStudentValid(studentId)) {
            throw new Exception("Your account is not valid. You cannot reserve rooms.");
        }

        // --- Parse dates ---
        LocalDate date  = LocalDate.parse(reservationDateStr);
        LocalTime start = LocalTime.parse(startTimeStr);
        LocalTime end   = LocalTime.parse(endTimeStr);

        if (!end.isAfter(start)) {
            throw new Exception("End time must be after start time.");
        }

        // --- Room exists and is available ---
        Room room = roomRepo.findById(roomId);
        if (room == null) {
            throw new Exception("Room not found.");
        }
        if (!room.isAvailable()) {
            throw new Exception("This room is not available for reservation.");
        }

        // --- Conflict: room already booked this slot ---
        if (reservationRepo.roomHasConflict(roomId, date, start, end)) {
            throw new Exception("This room is already reserved for the selected time slot.");
        }

        // --- Conflict: student already has a reservation this slot ---
        if (reservationRepo.studentHasConflict(studentId, date, start, end)) {
            throw new Exception("You already have a reservation during this time slot.");
        }

        // --- Create reservation ---
        Reservation reservation = new Reservation();
        reservation.setStudentId(studentId);
        reservation.setRoomId(roomId);
        reservation.setReservationDate(date);
        reservation.setStartTime(start);
        reservation.setEndTime(end);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        return reservationRepo.save(reservation);
    }

    // =========================================================================
    // CANCEL A RESERVATION  →  POST /api/rooms/cancel-reservation
    // Body: { reservationId }
    // =========================================================================
    public void cancelReservation(AuthenticatedUser authUser,
                                  int reservationId) throws Exception {

        // --- Role check ---
        if (authUser.getRole() != UserRole.STUDENT) {
            throw new Exception("Only students can cancel reservations.");
        }

        Reservation reservation = reservationRepo.findById(reservationId);
        if (reservation == null) {
            throw new Exception("Reservation not found.");
        }

        // --- Ownership check ---
        if (reservation.getStudentId() != authUser.getStudentId()) {
            throw new Exception("You can only cancel your own reservations.");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new Exception("Reservation is already cancelled.");
        }

        reservationRepo.cancel(reservationId);
    }

    // =========================================================================
    // MY RESERVATIONS  →  GET /api/rooms/my-reservations
    // =========================================================================
    public List<Reservation> myReservations(AuthenticatedUser authUser) throws Exception {

        if (authUser.getRole() != UserRole.STUDENT) {
            throw new Exception("Only students can view their reservations.");
        }

        return reservationRepo.findByStudentId(authUser.getStudentId());
    }

    // =========================================================================
    // ADMIN: LIST ALL RESERVATIONS  →  GET /api/admin/reservations
    // =========================================================================
    public List<Reservation> listAllReservations(AuthenticatedUser authUser) throws Exception {
        if (authUser.getRole() != UserRole.ADMIN) {
            throw new Exception("Only admins can view all reservations.");
        }
        return reservationRepo.findAll();
    }

    // =========================================================================
    // ADMIN: UPDATE ROOM AVAILABILITY  →  POST /api/admin/rooms/update-availability
    // Body: { roomId, available }
    // =========================================================================
    public Room updateAvailability(AuthenticatedUser authUser,
                                   int roomId,
                                   boolean available) throws Exception {

        if (authUser.getRole() != UserRole.ADMIN) {
            throw new Exception("Only admins can update room availability.");
        }

        Room room = roomRepo.findById(roomId);
        if (room == null) {
            throw new Exception("Room not found.");
        }

        roomRepo.updateAvailability(roomId, available);
        room.setAvailable(available);
        return room;
    }

    // =========================================================================
    // ADMIN: ADD ROOM  →  POST /api/admin/rooms
    // Body: { name, capacity, available }
    // =========================================================================
    public Room addRoom(AuthenticatedUser authUser,
                        String name,
                        int capacity,
                        boolean available) throws Exception {

        if (authUser.getRole() != UserRole.ADMIN) {
            throw new Exception("Only admins can add rooms.");
        }

        if (name == null || name.isBlank()) {
            throw new Exception("Room name is required.");
        }
        if (capacity <= 0) {
            throw new Exception("Capacity must be greater than 0.");
        }

        Room room = new Room();
        room.setName(name.trim());
        room.setCapacity(capacity);
        room.setAvailable(available);

        return roomRepo.save(room);
    }
}
