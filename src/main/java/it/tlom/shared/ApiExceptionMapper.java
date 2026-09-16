package it.tlom.shared;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<ApiException> {
    @Override
    public Response toResponse(ApiException exception) {
        return Response.status(exception.status())
                .entity(new ErrorResponse(exception.code(), "The request could not be completed."))
                .build();
    }

    public record ErrorResponse(String code, String message) { }
}