package it.tlom.controller;

import it.tlom.service.JourneyService;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/goal-occurrences")
@Produces(MediaType.APPLICATION_JSON)
public class GoalOccurrenceRest {
    @Inject JourneyService journeyService;

    @POST
    @Path("/{occurrenceId}/complete")
    public JourneyService.CompletionResponse complete(@PathParam("occurrenceId") String occurrenceId) {
        return journeyService.complete(occurrenceId);
    }
}