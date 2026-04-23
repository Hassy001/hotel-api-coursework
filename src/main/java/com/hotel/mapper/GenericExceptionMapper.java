package com.hotel.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable e) {
        if (e instanceof javax.ws.rs.WebApplicationException) {
            int statusCode = ((javax.ws.rs.WebApplicationException) e).getResponse().getStatus();
            Status status = Status.fromStatusCode(statusCode);
            return Response.status(statusCode)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of(
                            "status", statusCode,
                            "error", status != null ? status.getReasonPhrase() : "HTTP Error",
                            "message", "The request could not be processed."
                    ))
                    .build();
        }
        LOG.log(Level.SEVERE, "Unhandled exception", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of(
                        "status", 500,
                        "error", "Internal Server Error",
                        "message", "An unexpected error occurred. Please contact support."
                ))
                .build();
    }
}
