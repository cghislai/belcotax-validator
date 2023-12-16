package com.charlyghislain.belcotax.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BelcotaxValidationError {

    private String errorCode;
    private String errorMessage;

}
