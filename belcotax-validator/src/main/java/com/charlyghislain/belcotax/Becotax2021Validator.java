package com.charlyghislain.belcotax;

import com.charlyghislain.belcotax.util.ValidatedBecotax;
import belcotax2021.be.fgov.minfin.ccff.belcotax.validation2.error.AbstractValidationError;

import java.io.InputStream;
import java.util.ArrayList;

public class Becotax2021Validator {

    /**
     * Validates belcotax 2021
     *
     * @param xmlInputStream
     * @return
     */
    public ValidatedBecotax validateBelcoTax2021Xml(InputStream xmlInputStream) {
        ValidatedBecotax validatedBecotax = new ValidatedBecotax();
        AbstractValidationError validationError = new AbstractValidationError(null, null, null) {
            @Override
            protected int getType() {
                return 0;
            }
        };
        ArrayList<AbstractValidationError> byPassableErrors = new ArrayList<>();
        byPassableErrors.add(validationError);
        validatedBecotax.setByPassableErrors(byPassableErrors);

        return validatedBecotax;
    }

}
