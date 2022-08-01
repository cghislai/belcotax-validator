package com.charlyghislain.belcotax.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BelcotaxValidationError {

    private String errorCode;
    private String errorMessage;

}
