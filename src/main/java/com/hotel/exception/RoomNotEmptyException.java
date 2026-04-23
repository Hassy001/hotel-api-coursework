package com.hotel.exception;

public class RoomNotEmptyException extends RuntimeException {
    private final String roomId;

    public RoomNotEmptyException(String roomId) {
        super("Room " + roomId + " still has sensors attached and cannot be deleted.");
        this.roomId = roomId;
    }

    public String getRoomId() { return roomId; }
}
