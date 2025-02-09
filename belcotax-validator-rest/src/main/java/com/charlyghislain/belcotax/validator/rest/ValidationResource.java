package com.charlyghislain.belcotax.validator.rest;

import com.charlyghislain.belcotax.Belcotax2024Validator;
import com.charlyghislain.belcotax.util.BelcotaxValidationError;
import com.charlyghislain.belcotax.util.BelcotaxValidationException;
import com.charlyghislain.belcotax.util.BelcotaxValidationOptions;
import com.charlyghislain.belcotax.util.BelcotaxValidationResults;
import com.charlyghislain.belcotax.util.ValidatedBelcotax;
import com.charlyghislain.belcotax.util.ValidationApi;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Optional;

@Path("/validate")
public class ValidationResource implements ValidationApi {

    public BelcotaxValidationResults validateBelcotax(String acceptedLanguage,
                                                      String senderNumber,
                                                      Integer maxBlockingErrors,
                                                      InputStream xmlContent) {
        BelcotaxValidationOptions.BelcotaxValidationOptionsBuilder optionsBuilder = BelcotaxValidationOptions.builder();
        Optional.ofNullable(acceptedLanguage)
                .map(Locale::forLanguageTag)
                .ifPresent(optionsBuilder::errorsLocale);
        Optional.ofNullable(senderNumber)
                .ifPresent(optionsBuilder::senderSsin);
        Optional.ofNullable(maxBlockingErrors)
                .ifPresent(optionsBuilder::maxBlockingErrors);
        try {
            byte[] xmlBytes = xmlContent.readAllBytes();
            ValidatedBelcotax validatedBelcotax = Belcotax2024Validator.validateBelcotaxXml(new ByteArrayInputStream(xmlBytes), optionsBuilder.build());

            BelcotaxValidationResults belcotaxValidationResults = new BelcotaxValidationResults();
            belcotaxValidationResults.setFiscalYear(validatedBelcotax.getFiscalYear());
            belcotaxValidationResults.setBlockingErrors(validatedBelcotax.getBlockingErrors());
            belcotaxValidationResults.setByPassableErrors(validatedBelcotax.getByPassableErrors());
            belcotaxValidationResults.setWarnings(validatedBelcotax.getWarnings());
            return belcotaxValidationResults;
        } catch (BelcotaxValidationException | IOException e) {
            BelcotaxValidationError validationError = new BelcotaxValidationError(e.getClass().getName(), e.getMessage());
            Response response = Response.status(400)
                    .entity(validationError)
                    .build();
            throw new BadRequestException(response, e);
        }
    }

    public InputStream getBowFile(String acceptedLanguage,
                                  String senderNumber,
                                  Integer maxBlockingErrors,
                                  InputStream xmlContent) {
        BelcotaxValidationOptions.BelcotaxValidationOptionsBuilder optionsBuilder = BelcotaxValidationOptions.builder();
        Optional.ofNullable(acceptedLanguage)
                .map(Locale::forLanguageTag)
                .ifPresent(optionsBuilder::errorsLocale);
        Optional.ofNullable(senderNumber)
                .ifPresent(optionsBuilder::senderSsin);
        Optional.ofNullable(maxBlockingErrors)
                .ifPresent(optionsBuilder::maxBlockingErrors);
        try {
            ValidatedBelcotax validatedBelcotax = Belcotax2024Validator.validateBelcotaxXml(xmlContent, optionsBuilder.build());
            return validatedBelcotax.getBowInputStream();
        } catch (BelcotaxValidationException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public void close() throws Exception {
    }
}
