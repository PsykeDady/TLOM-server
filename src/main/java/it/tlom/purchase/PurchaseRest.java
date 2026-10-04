package it.tlom.purchase;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
@Path("/api/v1/stores")
public class PurchaseRest {
    @Inject PurchaseService purchaseService;

    @POST
    @Path("/{storeId}/purchases")
    @Consumes(MediaType.APPLICATION_JSON)
    public PurchaseService.PurchaseResult purchase(@PathParam("storeId") String storeId, PurchaseRequest request) {
        return purchaseService.purchase(storeId, request == null ? null : request.storeItemId(), request == null ? null : request.requestId());
    }
    public record PurchaseRequest(String storeItemId, String requestId) { }
}