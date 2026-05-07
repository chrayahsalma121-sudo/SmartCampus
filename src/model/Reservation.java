package model;

import enums.ReservationStatus;
import java.time.LocalDate;
import java.time.LocalTime;

public class Reservation {

    private int reservationId;
    private int studentId;
    private int roomId;
    private LocalDate reservationDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private ReservationStatus status;

    public Reservation() {}

    public Reservation(int reservationId, int studentId, int roomId,
                       LocalDate reservationDate, LocalTime startTime,
                       LocalTime endTime, ReservationStatus status) {
        this.reservationId   = reservationId;
        this.studentId       = studentId;
        this.roomId          = roomId;
        this.reservationDate = reservationDate;
        this.startTime       = startTime;
        this.endTime         = endTime;
        this.status          = status;
    }

    public int getReservationId()                   { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }

    public int getStudentId()               { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getRoomId()              { return roomId; }
    public void setRoomId(int roomId)   { this.roomId = roomId; }

    public LocalDate getReservationDate()                       { return reservationDate; }
    public void setReservationDate(LocalDate reservationDate)   { this.reservationDate = reservationDate; }

    public LocalTime getStartTime()                 { return startTime; }
    public void setStartTime(LocalTime startTime)   { this.startTime = startTime; }

    public LocalTime getEndTime()               { return endTime; }
    public void setEndTime(LocalTime endTime)   { this.endTime = endTime; }

    public ReservationStatus getStatus()                    { return status; }
    public void setStatus(ReservationStatus status)         { this.status = status; }
}
