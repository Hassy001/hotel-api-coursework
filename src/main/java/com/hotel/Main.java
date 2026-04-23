package com.hotel;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());
    private static final String BASE_URI = "http://localhost:8080/api/v1/";

    public static void main(String[] args) throws IOException, InterruptedException {
        ResourceConfig config = new ResourceConfig()
                .packages("com.hotel");

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);

        LOG.info("Hotel API running at " + BASE_URI);
        LOG.info("Press Ctrl+C to stop...");
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));
        Thread.currentThread().join();
    }
}
