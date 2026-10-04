package it.tlom.localization;


import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/localization")
public class LocalizationRest {
    @Inject LocalizationService localizationService;

    @GET
    @Path("/{locale}")
    public Response localization(@PathParam("locale") String locale, @HeaderParam("If-None-Match") String ifNoneMatch) {
        var resource = localizationService.forLocale(locale);
        var tag = new EntityTag(resource.revision());
        if (tag.toString().equals(ifNoneMatch)) return Response.notModified(tag).build();
        return Response.ok(resource).tag(tag).build();
    }
}