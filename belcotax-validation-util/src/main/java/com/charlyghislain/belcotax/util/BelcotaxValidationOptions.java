package com.charlyghislain.belcotax.util;

import lombok.Builder;
import lombok.Getter;

import java.util.Locale;

@Getter
@Builder
public class BelcotaxValidationOptions {

    private String senderSsin;

    private Integer maxBlockingErrors;

    private Locale errorsLocale;
}
