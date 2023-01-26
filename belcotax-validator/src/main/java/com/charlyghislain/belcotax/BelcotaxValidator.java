package com.charlyghislain.belcotax;

import com.charlyghislain.belcotax.util.BelcotaxValidationException;
import com.charlyghislain.belcotax.util.BelcotaxValidationOptions;
import com.charlyghislain.belcotax.util.ValidatedBelcotax;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.InputStream;

/**
 * An utility class to validate belcotax xml files.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BelcotaxValidator {

    /**
     * Validates a belcotax xml file.
     * <p>
     * See <a href="https://finances.belgium.be/sites/default/files/downloads/161-belcotax-brochure-2021-20220317-fr.pdf">french 2021 leaflet</a>
     * <p>
     * See <a href="https://financien.belgium.be/sites/default/files/downloads/161-belcotax-brochure-2021-20220317-nl.pdf">dutch 2021 leaflet</a>
     *
     * @param xmlInputStream    The xml file content
     * @param validationOptions Validation options
     * @return A ValidatedBecotax instance, containing the validated '.bow' file content and the validation warnings/errors
     * @throws BelcotaxValidationException when an error prevented the validation to occur
     */
    public static ValidatedBelcotax validateBelcotaxXml(int fiscalYear, InputStream xmlInputStream, BelcotaxValidationOptions validationOptions) throws BelcotaxValidationException {
        switch (fiscalYear) {
            case 2021: {
                return Belcotax2021Validator.validatedBelcotaxXml(xmlInputStream, validationOptions);
            }
            case 2022: {
                return Belcotax2022Validator.validatedBelcotaxXml(xmlInputStream, validationOptions);
            }
            default:
                throw new BelcotaxValidationException("Fiscal year " + fiscalYear + " not yet supported");
        }
    }

}
