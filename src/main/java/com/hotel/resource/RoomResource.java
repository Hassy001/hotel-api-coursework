package com.hotel.resource;

import com.hotel.exception.RoomNotEmptyException;
import com.hotel.model.Room;
import com.hotel.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Context;
import java.util.Collection;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    @GET
    public Collection<Room> getAllRooms() {
        return DataStore.rooms.values();
    }

    @POST
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        Room created = new Room(String.valueOf(DataStore.nextRoomId.getAndIncrement()), room.getName(), room.getCapacity());
        DataStore.rooms.put(created.getId(), created);
        return Response.created(
                        UriBuilder.fromUri(uriInfo.getAbsolutePath())
                                .path(String.valueOf(created.getId()))
                                .build())
                .entity(created)
                .build();
    }

    @GET
    @Path("/{roomId}")
    public Room getRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            throw new NotFoundException("Room with id '" + roomId + "' does not exist.");
        }
        return room;
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            throw new NotFoundException("Room with id '" + roomId + "' does not exist.");
        }
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId);
        }
        DataStore.rooms.remove(roomId);
        return Response.noContent().build();
    }
}
