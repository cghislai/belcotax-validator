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


# Updating the validator

1. Fetch the jnlp file
2. Download the jar content. url='${codebase}/${jar_href}', eg 'http://ccff02.minfin.fgov.be/CCFF_SP7_2015/jnlp/belcotax-standalone-2022-1.0.5.jar'
3. Install the jar in the local mvn repo: ` mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file -Dfile=path/to/belcotax-standalone-XXXX-Y.Y.jar -DgroupId=be.fgov.minfin.belcotax -DartifactId=belcotax-standalone-XXX -Dversion=XXX.Y.Y   -Dpackaging=jar -DlocalRepositoryPath=./repo `
4. Create the new 'belcotax-XXX_valiation' module, shade the jar content, and update the validator 
