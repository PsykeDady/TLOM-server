package it.tlom.player;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/me")
@Produces(MediaType.APPLICATION_JSON)
public class PlayerRest {
    @Inject PlayerService playerService;

    @GET
    public Player currentPlayer() { return playerService.currentPlayer(); }
}