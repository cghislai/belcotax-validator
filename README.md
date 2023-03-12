# Belcotax validator

This project contains the belcotax validator extracted from the standalone jws applet (*sigh*), packaged in a shaded jar,
and bundled in a quarkus server proxifying it through a rest api.

[![Maven Central](https://img.shields.io/maven-central/v/com.charlyghislain.belcotax/belcotax-validator)](https://search.maven.org/search?q=g:com.charlyghislain.belcotax%20a:belcotax-validator)


Supporting multiple fiscal year (with multiple jnlp versions embedded) does not work correctly, some files are loaded from the classpath using hardcoded urls.
The aim is to update this project to support validating the last fiscal year for which the validator is released. Feel free to open an issue whenever a new 'validation module' is released on financien.belgium.be.

French technical documentation: https://financien.belgium.be/fr/E-services/Belcotaxonweb/documentation-technique

Dutch technical documentation: https://financien.belgium.be/nl/E-services/Belcotaxonweb/technische-documentatie

# Getting started

0. Compile java code using maven: `mvn clean package`
1. Build & start the quarkus image using docker-compose: `docker-compose up --build`. The webserver is now listening on port 28080 
2. Post your xml using http to the endpoint `http://127.0.0.1:28080/validate` for validation, or the endpoint
   `http://127.0.0.1:28080/bow` to get the bow content to upload to belcotax.

```
POST http://127.0.0.1:28080/validate?maxBlocking=30
Content-Type: text/xml
Accept: application/json
Accept-Language: fr
Content-Length: 4850
Connection: Keep-Alive
User-Agent: Apache-HttpClient/4.5.14 (Java/17.0.9)
Accept-Encoding: br,deflate,gzip,x-gzip

< <some xml file>

HTTP/1.1 200 OK
content-length: 1643
Content-Type: application/json;charset=UTF-8

{
  "blockingErrors": [
    {
      "errorCode": "Error.Blocking.Empty",
      "errorMessage": "Ligne: 13 Colonne: 31, Tag: v0021_contactpersoon : Le champ \"Nom\" est obligatoire."
    },
    {
      "errorCode": "Error.Blocking.BeneficiaireDataRequired",
      "errorMessage": "Ligne: 43 Colonne: 41, Tag: f2013_naam, N° BCE Débiteur: XXXX, Type de fiche: 28186, N° Séquentiel Fiche: 3 : Zones 2013 à 2018 (sauf 2014): l'identification et l'adresse du bénéficiaire de revenus sont obligatoires si le numéro national (zone 2011) n'est pas rempli."
    }
  ],
  "byPassableErrors": [],
  "fiscalYear": 2022,
  "warnings": [
    {
      "errorCode": "Error.Warning.fiche86_2011_NN_Mandatory",
      "errorMessage": "Le numéro national du débiteur est obligatoire"
    }
  ]
}
```

# Updating the validator

1. Fetch the jnlp file, eg eg 'https://ccff02.minfin.fgov.be/CCFF_SP7_2022/jnlp/belcotax.jnlp'
2. Download the jar content. url='${codebase}/${jar_href}', eg 'https://ccff02.minfin.fgov.be/CCFF_SP7_2022/jnlp/belcotax.jnlp'
3. Install the jar in the local mvn repo: ` mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file -Dfile=path/to/belcotax-standalone-XXXX-Y.Y.jar -DgroupId=be.fgov.minfin.belcotax -DartifactId=belcotax-standalone-XXX -Dversion=XXX.Y.Y   -Dpackaging=jar -DlocalRepositoryPath=./repo `
4. Create the new 'belcotax-XXX_valiation' module, shade the jar content, and update the validator 

```bash
 JNLP_URI="https://ccff02.minfin.fgov.be/CCFF_SP7_2022/jnlp/belcotax.jnlp"
 BASE_PATH=$(curl "$JNLP_URI" | xmllint --xpath '//jnlp/@codebase' - | sed 's/^[^=]\+=//' | jq -r)
  JAR_HREF=$(curl "$JNLP_URI" | xmllint --xpath '//jnlp/resources/jar/@href'  - | sed 's/^[^=]\+=//' | jq -r)
 
 JAR_URI="$BASE_PATH/$JAR_HREF"
 echo $JAR_URI
 exit 1
 curl $JAR_URI > belcotax-standalone.jar
 
 mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file -Dfile=belcotax-standalone.jar -DgroupId=be.fgov.minfin.belcotax -DartifactId=belcotax-standalone-2022 -Dversion=2022.2.0   -Dpackaging=jar -DlocalRepositoryPath=./repo
 
 rm -f belcotax-standalone.jar
```

```bash
curl https://financien.belgium.be/sites/default/files/downloads/161-xsd-2022-20230221.zip > xsd.zip

unzip xsd.zip

cp -f Belcotax-2022.xsd belcotax-2022-validation/src/main/resources/xsd/
rm -f 
rm xsd.zip 
rm -f Belcotax-2022.xsd 
```
