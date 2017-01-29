# B2B Maven Plugin

## Summary

This plugin helps adopt a development lifecycle when working with B2B artifacts including assisting with extracting out document types, trading partners, and agreements from your local install into source control and then "deploying" those to target environments.

This goes hand in hand with avio-b2b-12.2.1-2.0.pom which adds the standard environment settings.

## Project Setup
2 types of projects:

### Document Definitions

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.evhc</groupId>
  <artifactId>tradingpartner1B2BDoc</artifactId>
  <version>1.0-SNAPSHOT</version>
  <description>Super POM for tradingpartner1</description>
  <packaging>b2b</packaging>

  <parent>
    <groupId>com.avioconsulting.oracle.soa</groupId>
    <artifactId>avio-b2b</artifactId>
    <version>12.2.1-2-0</version>
    <relativePath />
  </parent>

  <properties>
    <b2b.artifact.type>DocumentDefinitions</b2b.artifact.type>
  </properties>
</project>
```

### Trading Partner+Agreement Combos

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.evhc</groupId>
  <artifactId>TradingPartner1B2BAVIOConsulting</artifactId>
  <version>1.0-SNAPSHOT</version>
  <description>Super POM for tradingpartner1</description>
  <packaging>b2b</packaging>

  <parent>
    <groupId>com.avioconsulting.oracle.soa</groupId>
    <artifactId>avio-b2b</artifactId>
    <version>12.2.1-2-0</version>
    <relativePath />
  </parent>

  <properties>
    <b2b.artifact.type>PartnersAndAgreements</b2b.artifact.type>
    <b2b.partners>AVIOConsulting,A_PARTNER</b2b.partners>
    <b2b.agreements>837Agreement,999Agreement</b2b.agreements>
  </properties>
</project>
```

No special tie-ins to JDev, you can organize that however you want

## Development Workflow

The general idea is to use the B2B console on your laptop as an "IDE". Everything goes in there. Then you run this to populate your repo:

```
mvn -Denv=LOCAL -Db2b.export=true generate-resources
# This will run an export from the server but will filter out anything that doesn't match your POM properties (see above)
```

Deploy the project like you would a SOA composite:

```
mvnd -Denv=LOCAL pre-integration-test
```

## Wish List

Before running export, produce a mapping of supported document definition refs in the tp file.
Then after the export, go back and make all those IDs in the TP and TPA file consistent.
