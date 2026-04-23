package com.hotel.resource;

import com.hotel.exception.SensorUnavailableException;
import com.hotel.model.Sensor;
import com.hotel.model.SensorReading;
import com.hotel.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public List<SensorReading> getReadings() {
        return DataStore.readings.getOrDefault(sensorId, new ArrayList<>());
    }

    @POST
    public Response addReading(SensorReading incoming) {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor with id '" + sensorId + "' does not exist.");
        }
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId);
        }

        SensorReading reading = new SensorReading(incoming.getValue());
        DataStore.readings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);

        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}
