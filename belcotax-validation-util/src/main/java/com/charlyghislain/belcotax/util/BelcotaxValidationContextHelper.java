package com.charlyghislain.belcotax.util;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class BelcotaxValidationContextHelper {

    private final static List<String> LINE_TAGS = List.of("Ligne", "Lijn");
    private final static List<String> COLUMN_TAGS = List.of("Colonne", "Kolom");
    public static final String TAG_TAG = "Tag";

    public static BelcotaxValidationError mapWithContext(Locale locale,
                                                         String errorCode, String errorMessage,
                                                         List<String> contentLineList,
                                                         Map<String, Map<String, String>> xsdDocumentation) {
        ErrorMessageContext errorMessageContext = new ErrorMessageContext(errorMessage);
        int line = errorMessageContext.getLine();
        int column = errorMessageContext.getColumn();
        String message = errorMessageContext.getMessage();
        String tagValue = errorMessageContext.getTags().get(TAG_TAG);
        String contentMessage = extractContentMessage(contentLineList, line, tagValue, column);
        String elementDocumentation = getElementDocumentation(tagValue, xsdDocumentation, locale);


        BelcotaxValidationError errorWithContent = new BelcotaxValidationError();
        errorWithContent.setErrorCode(errorCode);
        errorWithContent.setErrorMessage(message);
        errorWithContent.setLine(line);
        errorWithContent.setColumn(column);
        errorWithContent.setOriginalErrorMessage(errorMessage);
        errorWithContent.setContentContext(contentMessage);
        errorWithContent.setElementDocumentation(elementDocumentation);
        errorWithContent.setTags(errorMessageContext.getTags());
        return errorWithContent;
    }

    private static String getElementDocumentation(String tagValue,
                                                  Map<String, Map<String, String>> xsdDocumentation,
                                                  Locale locale) {
        return Optional.ofNullable(xsdDocumentation.get(tagValue))
                .flatMap(docMessages -> Optional.ofNullable(docMessages.get(locale.getLanguage()))
                        .filter(s -> !s.isBlank())
                        .or(() -> Optional.ofNullable(docMessages.get(""))
                                .filter(s -> !s.isBlank()))
                )
                .orElse(null);
    }

    private static String extractContentMessage(List<String> contentLineList, int line, String tagValue, int column) {
        String contentMessage = null;
        if (line > 0 && line <= contentLineList.size()) {
            String lineCOntent = contentLineList.get(line - 1);

            if (tagValue != null) {
                Pattern pattern = Pattern.compile(String.format(".*<%s>(.*)</%s>.*", tagValue, tagValue));
                Matcher matcher = pattern.matcher(lineCOntent);
                if (matcher.matches()) {
                    contentMessage = matcher.group(1);
                } else if (column > 0) {
                    contentMessage = lineCOntent.substring(column - 1);
                }
            } else if (column > 0) {
                contentMessage = lineCOntent.substring(column - 1);
            }
        }
        return contentMessage;
    }

    @Getter
    private static class ErrorMessageContext {
        Map<String, String> tags;
        int line;
        int column;
        String message;
        Pattern mainPattern = Pattern.compile("^(.+) : (.+)$");

        public ErrorMessageContext(String errorMessage) {
            Matcher matcher = mainPattern.matcher(errorMessage);
            if (matcher.matches()) {
                String tagsString = matcher.group(1);
                message = matcher.group(2);
                tags = new HashMap<>();
                Arrays.stream(tagsString.split("[,]"))
                        .flatMap(this::normalizeTags)
                        .forEach(tag -> tags.put(tag[0], tag[1]));

                line = LINE_TAGS.stream()
                        .map(tags::get)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .map(Integer::parseUnsignedInt)
                        .orElse(0);
                column = COLUMN_TAGS.stream()
                        .map(tags::get)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .map(Integer::parseUnsignedInt)
                        .orElse(0);
            }
        }

        private Stream<String[]> normalizeTags(String tagWords) {
            // Should be 'tag: value', but also we have 'tag: value tag: value'
            List<String[]> subwords = new ArrayList<>();
            String remainingString = tagWords;
            Pattern pattern = Pattern.compile("^([^:]+): ([^: ]+)( .*)?$");
            Matcher matcher = pattern.matcher(remainingString);
            while (matcher.matches()) {
                String nextKey = matcher.group(1).strip();
                String value = matcher.group(2).strip();
                subwords.add(new String[]{nextKey, value});

                remainingString = remainingString.substring(nextKey.length() + value.length() + 2);
                matcher = pattern.matcher(remainingString);
            }

            return subwords.stream();
        }
    }
}
