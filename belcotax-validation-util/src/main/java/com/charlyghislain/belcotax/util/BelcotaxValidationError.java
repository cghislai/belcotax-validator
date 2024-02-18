package com.charlyghislain.belcotax.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BelcotaxValidationError {

    private String errorCode;
    private String errorMessage;

    private String originalErrorMessage;
    private String contentContext;
    private String elementDocumentation;
    private int line;
    private int column;
    private Map<String, String> tags;

    public BelcotaxValidationError(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
