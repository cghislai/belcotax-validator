package com.charlyghislain.belcotax.util;

import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;
import java.util.List;

@Getter
@Setter
public class ValidatedBelcotax {
    private Integer fiscalYear;
    private InputStream bowInputStream;
    private List<BelcotaxValidationError> blockingErrors;
    private List<BelcotaxValidationError> byPassableErrors;
    private List<BelcotaxValidationError> warnings;

}
