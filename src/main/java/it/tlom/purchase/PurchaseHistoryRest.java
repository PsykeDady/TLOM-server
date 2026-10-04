package it.tlom.purchase;
import java.util.List;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/me/purchases")
@Produces(MediaType.APPLICATION_JSON)
public class PurchaseHistoryRest {
    @Inject PurchaseService purchaseService;

    @GET
    public List<Purchase> purchases() { return purchaseService.purchases(); }
}