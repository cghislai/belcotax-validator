package com.charlyghislain.belcotax.util;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;

@Path("/validate")
public interface ValidationApi extends AutoCloseable {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_XML)
    BelcotaxValidationResults validateBelcotax(@HeaderParam("Accept-Language") String acceptedLanguage,
                                               @QueryParam("sender") String senderNumber,
                                               @QueryParam("maxBlocking") Integer maxBlockingErrors,
                                               InputStream xmlContent);

    @POST
    @Path("/bow")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Consumes(MediaType.TEXT_XML)
    InputStream getBowFile(@HeaderParam("Accept-Language") String acceptedLanguage,
                           @QueryParam("sender") String senderNumber,
                           @QueryParam("maxBlocking") Integer maxBlockingErrors,
                           InputStream xmlContent);

}
