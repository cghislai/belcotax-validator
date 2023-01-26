package com.charlyghislain.belcotax;

import be.fgov.minfin.ccff.belcotax.input.xml.VerzendingRecord;
import be.fgov.minfin.ccff.belcotax.input.xml.VerzendingenMapper;
import be.fgov.minfin.ccff.belcotax.output.NullDataStore;
import be.fgov.minfin.ccff.belcotax.output.untyped.UntypedValidatorDataStore;
import be.fgov.minfin.ccff.belcotax.util.CryptOutputStream;
import be.fgov.minfin.ccff.belcotax.validation2.error.AbstractError;
import be.fgov.minfin.ccff.belcotax.validation2.error.BowErrorHandler;
import be.fgov.minfin.ccff.belcotax.validation2.error.BypassableErrorsActivated;
import be.fgov.minfin.ccff.belcotax.validation2.validator.context.ValidationContext;
import be.fgov.minfin.ccff.belcotax.validation2.validator.type.source.SourceType;
import be.fgov.minfin.ccff.belcotax.vo.converters.XmlVOConverter;
import be.fgov.minfin.ccff.bow.back.Environment;
import be.fgov.minfin.ccff.framework.util.time.Chronometer;
import com.charlyghislain.belcotax.util.BelcotaxValidationError;
import com.charlyghislain.belcotax.util.BelcotaxValidationException;
import com.charlyghislain.belcotax.util.BelcotaxValidationOptions;
import com.charlyghislain.belcotax.util.ValidatedBelcotax;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class Belcotax2022Validator {

    public static ValidatedBelcotax validatedBelcotaxXml(InputStream xmlContentStream, BelcotaxValidationOptions validationOptions) throws BelcotaxValidationException {
        try (
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                CryptOutputStream cryptOutputStream = new CryptOutputStream(outputStream);
                ZipOutputStream zip = new ZipOutputStream(cryptOutputStream);
        ) {
            zip.putNextEntry(new ZipEntry("data.xml"));
            zip.setLevel(9);

            Chronometer cronoConversion = new Chronometer("Conversion time");
            cronoConversion.start();

            BowErrorHandler errorHandler = new BowErrorHandler(SourceType.XML, new BypassableErrorsActivated());
            Optional.ofNullable(validationOptions.getMaxBlockingErrors())
                    .ifPresent(errorHandler::setMaxNbrOfBlockingErrors);
            NullDataStore dataStore = new NullDataStore();
            ValidationContext validationContext = new ValidationContext();
            validationContext.setSenderCbeNumber(validationOptions.getSenderSsin());
            UntypedValidatorDataStore validaStore = new UntypedValidatorDataStore(dataStore, new XmlVOConverter(), errorHandler, validationContext);

            byte[] xmlContentBytes = xmlContentStream.readAllBytes();
            ByteArrayInputStream firstPassInputStream = new ByteArrayInputStream(xmlContentBytes);

            VerzendingRecord vr = new VerzendingRecord(validaStore);
            VerzendingenMapper mapper = new VerzendingenMapper(vr, errorHandler);
            mapper.setValidating(true);
            mapper.setSchemaLocation(null);
            mapper.parse(firstPassInputStream);
            zip.write(xmlContentBytes);
            zip.closeEntry();
            zip.flush();
            cronoConversion.stop();

            zip.putNextEntry(new ZipEntry("statistic.properties"));
            zip.write("applet.name=CCFF_SP7_FlatFileConversionApplet\n".getBytes());
            zip.write(("applet.version=" + Environment.APPLET_VERSION + "\n").getBytes());
            zip.write(("conversion.time=" + cronoConversion.display() + "\n").getBytes());
            zip.write(("conversion.miliseconds=" + cronoConversion.getTimeElapsed() + "\n").getBytes());
            zip.finish();
            zip.flush();
            zip.close();

            byte[] outputBytes = outputStream.toByteArray();
            ByteArrayInputStream bowContentBytes = new ByteArrayInputStream(outputBytes);

            Collection<? extends AbstractError> blockingErrors = errorHandler.getBlockingErrors();
            Collection<? extends AbstractError> bypassableErrors = errorHandler.getBypassableErrors();
            Collection<? extends AbstractError> warnings = errorHandler.getWarnings();

            List<BelcotaxValidationError> blockingErrorList = blockingErrors
                    .stream()
                    .map(o -> convertError(o, validationOptions.getErrorsLocale()))
                    .collect(Collectors.toList());
            List<BelcotaxValidationError> bypassableErrorsList = bypassableErrors
                    .stream()
                    .map(o -> convertError(o, validationOptions.getErrorsLocale()))
                    .collect(Collectors.toList());
            List<BelcotaxValidationError> warningsList = warnings
                    .stream()
                    .map(o -> convertError(o, validationOptions.getErrorsLocale()))
                    .collect(Collectors.toList());
            ValidatedBelcotax validatedBelcotax = new ValidatedBelcotax();
            validatedBelcotax.setBowInputStream(bowContentBytes);
            validatedBelcotax.setBlockingErrors(blockingErrorList);
            validatedBelcotax.setByPassableErrors(bypassableErrorsList);
            validatedBelcotax.setWarnings(warningsList);
            validatedBelcotax.setFiscalYear(2022);

            return validatedBelcotax;
        } catch (Exception e) {
            throw new BelcotaxValidationException(e);
        }
    }

    private static BelcotaxValidationError convertError(AbstractError abstractError, Locale errorsLocale) {
        Locale locale = Optional.ofNullable(errorsLocale)
                .orElse(Locale.FRENCH);
        return new BelcotaxValidationError(
                abstractError.getErrorCode(),
                abstractError.render(locale, true, SourceType.XML)
        );
    }
}
