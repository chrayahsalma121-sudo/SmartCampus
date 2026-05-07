package model;

public class Room {

    private int roomId;
    private String name;
    private int capacity;
    private boolean available;

    public Room() {}

    public Room(int roomId, String name, int capacity, boolean available) {
        this.roomId   = roomId;
        this.name     = name;
        this.capacity = capacity;
        this.available = available;
    }

    public int getRoomId()          { return roomId; }
    public void setRoomId(int id)   { this.roomId = id; }

    public String getName()             { return name; }
    public void setName(String name)    { this.name = name; }

    public int getCapacity()                { return capacity; }
    public void setCapacity(int capacity)   { this.capacity = capacity; }

    public boolean isAvailable()                { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
