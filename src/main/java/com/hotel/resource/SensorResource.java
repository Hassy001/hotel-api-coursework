package com.hotel.resource;

import com.hotel.exception.LinkedResourceNotFoundException;
import com.hotel.model.Room;
import com.hotel.model.Sensor;
import com.hotel.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    @GET
    public Collection<Sensor> getSensors(@QueryParam("type") String type) {
        Collection<Sensor> all = DataStore.sensors.values();
        if (type == null || type.isBlank()) {
            return all;
        }
        return all.stream()
                .filter(s -> type.equalsIgnoreCase(s.getType()))
                .collect(Collectors.toList());
    }

    @POST
    public Response createSensor(Sensor incoming) {
        Room room = DataStore.rooms.get(incoming.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException("Room", incoming.getRoomId());
        }

        Sensor sensor = new Sensor(incoming.getType(), incoming.getRoomId());
        if (incoming.getStatus() != null) {
            sensor.setStatus(incoming.getStatus());
        }
        DataStore.sensors.put(sensor.getId(), sensor);

        room.getSensorIds().add(sensor.getId());

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
