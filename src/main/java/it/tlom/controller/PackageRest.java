package it.tlom.controller;

import it.tlom.model.ActivityPackage;
import it.tlom.service.PackageService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/api/v1/packages")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PackageRest {
    @Inject PackageService packageService;
    @GET public java.util.List<ActivityPackage> catalog() { return packageService.catalog(); }
    @GET @Path("/{packageId}") public ActivityPackage packageById(@PathParam("packageId") String packageId) { return packageService.packageById(packageId); }
    @POST @Path("/{packageId}/install") public Response install(@PathParam("packageId") String packageId, @Valid InstallRequest request) {
        var installation = packageService.install(packageId, request == null ? Map.of() : request.values());
        return Response.status(Response.Status.CREATED).entity(installation).build();
    }
    public record InstallRequest(Map<String, Object> values) { }
}