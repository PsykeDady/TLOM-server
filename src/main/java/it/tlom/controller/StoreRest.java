package it.tlom.controller;

import it.tlom.service.StoreService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/me/stores")
@Produces(MediaType.APPLICATION_JSON)
public class StoreRest {
    @Inject StoreService storeService;

    @GET
    public StoreService.StoreCatalog stores() { return storeService.stores(); }
}