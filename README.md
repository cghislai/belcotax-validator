# Belcotax validator

This project contains the belcotax validator extracted from the standalone jws applet (*sigh*), packaged in a way that makes it
usable in any java project.

[![Maven Central](https://img.shields.io/maven-central/v/com.charlyghislain.belcotax/belcotax-validator)](https://search.maven.org/search?q=g:com.charlyghislain.belcotax%20a:belcotax-validator)


The aim is to update this project to support validating the current 1 or 2 fiscal years. Feel free to open an issue whenever a new 'validation module'
is released on financien.belgium.be.

French technical documentation: https://financien.belgium.be/fr/E-services/Belcotaxonweb/documentation-technique

Dutch technical documentation: https://financien.belgium.be/nl/E-services/Belcotaxonweb/technische-documentatie

# Getting started

0. Add a dependency to the belcotax-validator project

```xml
<dependency>
    <groupId>com.charlyghislain.belcotax</groupId>
    <artifactId>belcotax-validator</artifactId>
    <version><!-- check latest release --></version>
</dependency>
```

1. Build an xml according to the xsd provided on financien.belgium.be.

```java
int fiscalYear=2021;
InputStream xmlContentStream= //...
```

2. Instantiate the validation options and call the validator

```java
BelcotaxValidationOptions validationOptions=BelcotaxValidationOptions.builder()
        .senderSsin(/*... */)
        .errorLocale(Locale.FRENCH)
        .build();
ValidatedBelcotax validatedBelcotax = BelcotaxValidator.validateBelcoTaxXml(
        fiscalYear, xmlContentStream, validationOptions
);
```

3. Handle the errors, write the bow file

```java
List<BelcotaxValidationError> blockingErrors = validatedBelcotax.getBlockingErrors();
if (blockingErrors.isEmpty()) {
  try (
     InputStream bowInputStream = validatedBelcotax.getBowInputStream();
     OutputStream bowOutputStream = Files.newOutputStream(Paths.get("/tmp/belcotax.bow"));
  ) {
    bowInputStream.transferTo(bowOutputStream);
  }
} else {
    blockingErrors.forEach(e-> {
        System.err.println(e.getErrorMessage());
    });
}
```

4. Upload the bow file on belcotax on web
