package com.hotel.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Map<String, Object> discover() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("version", "1.0");
        info.put("contact", "admin@hotel.com");

        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        info.put("resources", resources);

        return info;
    }
}
