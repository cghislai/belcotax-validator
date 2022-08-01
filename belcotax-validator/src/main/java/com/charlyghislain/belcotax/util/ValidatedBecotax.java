package com.charlyghislain.belcotax.util;

import belcotax2021.be.fgov.minfin.ccff.belcotax.validation2.error.AbstractValidationError;
import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;
import java.util.List;

@Getter
@Setter
public class ValidatedBecotax {

    private Integer fiscalYear;
    private InputStream bowInputStream;
    private List<? extends AbstractValidationError> blockingErrors;
    private List<? extends AbstractValidationError> byPassableErrors;
    private List<? extends AbstractValidationError> warnings;
}
