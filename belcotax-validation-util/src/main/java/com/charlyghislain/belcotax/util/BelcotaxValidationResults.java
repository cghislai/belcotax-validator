package com.charlyghislain.belcotax.util;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BelcotaxValidationResults {
    private Integer fiscalYear;
    private List<BelcotaxValidationError> blockingErrors;
    private List<BelcotaxValidationError> byPassableErrors;
    private List<BelcotaxValidationError> warnings;

}
