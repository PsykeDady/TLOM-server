package it.tlom.journey;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/me")
@Produces(MediaType.APPLICATION_JSON)
public class JourneyRest {
    @Inject JourneyService journeyService;
    @GET @Path("/journey") public JourneyService.JourneyResponse journey() { return journeyService.journey(); }
    @GET @Path("/wallet") public JourneyService.WalletResponse wallet() { return journeyService.wallet(); }
}