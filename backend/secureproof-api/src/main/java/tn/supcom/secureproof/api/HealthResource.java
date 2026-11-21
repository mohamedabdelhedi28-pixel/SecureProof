package tn.supcom.secureproof.api;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import java.util.Map;

@Path("/health")
public class HealthResource {

    @GET
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response health() {
        return Response.ok(Map.of(
                "service", "secureproof-api",
                "status", "UP",
                "timestamp", Instant.now().toString()
        )).build();
    }
}
