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
import com.charlyghislain.belcotax.util.BelcotaxValidationContextHelper;
import com.charlyghislain.belcotax.util.BelcotaxValidationError;
import com.charlyghislain.belcotax.util.BelcotaxValidationException;
import com.charlyghislain.belcotax.util.BelcotaxValidationOptions;
import com.charlyghislain.belcotax.util.ValidatedBelcotax;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Belcotax2024Validator {

    public static ValidatedBelcotax validateBelcotaxXml(InputStream xmlContentStream, BelcotaxValidationOptions validationOptions) throws BelcotaxValidationException {
        byte[] xmlContentBytes;
        try {
            xmlContentBytes = xmlContentStream.readAllBytes();
        } catch (IOException e) {
            throw new BelcotaxValidationException("Unable to read xml content", e);
        }

        BowErrorHandler errorHandler = new BowErrorHandler(SourceType.XML, new BypassableErrorsActivated());
        byte[] bowContentBytes = writeZipEntry(xmlContentBytes, validationOptions, errorHandler);

        ValidatedBelcotax validatedBelcotax = createValidatedBelcotax(validationOptions, errorHandler, bowContentBytes, xmlContentBytes);
        return validatedBelcotax;

    }

    private static byte[] writeZipEntry(byte[] xmlContentBytes, BelcotaxValidationOptions validationOptions, BowErrorHandler errorHandler) throws BelcotaxValidationException {
        try (
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                CryptOutputStream cryptOutputStream = new CryptOutputStream(outputStream);
                ZipOutputStream zip = new ZipOutputStream(cryptOutputStream);
        ) {
            zip.putNextEntry(new ZipEntry("data.xml"));
            zip.setLevel(9);

            Chronometer cronoConversion = new Chronometer("Conversion time");
            cronoConversion.start();


            parseXml(validationOptions, errorHandler, xmlContentBytes);

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
            return outputBytes;
        } catch (Exception e) {
            throw new BelcotaxValidationException(e.getMessage(), e);
        }
    }

    private static ValidatedBelcotax createValidatedBelcotax(BelcotaxValidationOptions validationOptions, BowErrorHandler errorHandler, byte[] bowContentBytes, byte[] xmlContentBytes) {
        Collection<? extends AbstractError> blockingErrors = errorHandler.getBlockingErrors();
        Collection<? extends AbstractError> bypassableErrors = errorHandler.getBypassableErrors();
        Collection<? extends AbstractError> warnings = errorHandler.getWarnings();
        Locale errorsLocale = validationOptions.getErrorsLocale();
        List<String> linesList = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(xmlContentBytes)))
                .lines()
                .collect(Collectors.toList());

        List<BelcotaxValidationError> blockingErrorList = blockingErrors
                .stream()
                .map(o -> convertError(o, errorsLocale, linesList))
                .collect(Collectors.toList());
        List<BelcotaxValidationError> bypassableErrorsList = bypassableErrors
                .stream()
                .map(o -> convertError(o, errorsLocale, linesList))
                .collect(Collectors.toList());
        List<BelcotaxValidationError> warningsList = warnings
                .stream()
                .map(o -> convertError(o, errorsLocale, linesList))
                .collect(Collectors.toList());

        ByteArrayInputStream bowInputStream = new ByteArrayInputStream(bowContentBytes);

        ValidatedBelcotax validatedBelcotax = new ValidatedBelcotax();
        validatedBelcotax.setBowInputStream(bowInputStream);
        validatedBelcotax.setBlockingErrors(blockingErrorList);
        validatedBelcotax.setByPassableErrors(bypassableErrorsList);
        validatedBelcotax.setWarnings(warningsList);
        validatedBelcotax.setFiscalYear(2023);
        return validatedBelcotax;
    }

    private static void parseXml(BelcotaxValidationOptions validationOptions, BowErrorHandler errorHandler, byte[] xmlContentBytes) throws SAXException, IOException {
        Optional.ofNullable(validationOptions.getMaxBlockingErrors())
                .ifPresent(errorHandler::setMaxNbrOfBlockingErrors);
        NullDataStore dataStore = new NullDataStore();
        ValidationContext validationContext = new ValidationContext();
        validationContext.setSenderCbeNumber(validationOptions.getSenderSsin());
        UntypedValidatorDataStore validaStore = new UntypedValidatorDataStore(dataStore, new XmlVOConverter(), errorHandler, validationContext);
        parseXml(validaStore, errorHandler, xmlContentBytes);
    }

    private static void parseXml(UntypedValidatorDataStore validaStore, BowErrorHandler errorHandler, byte[] xmlContentBytes) throws SAXException, IOException {
        VerzendingRecord vr = new VerzendingRecord(validaStore);
        VerzendingenMapper mapper = new VerzendingenMapper(vr, errorHandler);
        mapper.setValidating(true);
        mapper.setSchemaLocation(null);
        ByteArrayInputStream firstPassInputStream = new ByteArrayInputStream(xmlContentBytes);
        mapper.parse(firstPassInputStream);
    }

    private static BelcotaxValidationError convertError(AbstractError abstractError, Locale errorsLocale, List<String> linesList) {
        Locale locale = Optional.ofNullable(errorsLocale)
                .orElse(Locale.FRENCH);
        String errorCode = abstractError.getErrorCode();
        String errorMessage = abstractError.render(locale, true, SourceType.XML);
        // We need to reparse to get content
        return BelcotaxValidationContextHelper.mapWithContext(locale, errorCode, errorMessage, linesList, XsdReader.XSD_DOCUMENTATION);
    }
}
