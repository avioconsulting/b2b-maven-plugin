# B2B Maven Plugin

## Summary

This plugin helps adopt a development lifecycle when working with B2B artifacts including assisting with extracting out document types, trading partners, and agreements from your local install into source control and then "deploying" those to target environments.

## Maven goals

This plugin defines a new packaging type (b2b) and hooks into the Maven lifecycle at the following phases:
### generate-resources
* Runs the `b2bExport` goal if the `b2b.export` property is set to true. This will export document definitions or trading partners+agreements from the B2B server to source control (see below).
* By default, the Oracle B2B export will use generated IDs on the trading partner and trading partner agreement files. This makes it harder to look at diffs in source control and understand what has changed. The plugin automatically uses trading partner names to have a more consistent set of files for diffing, etc.
* For the same reason, this goal will also reorder XML nodes in a consistent fashion since the B2B export slightly changes the order each time.
### package
Runs the `b2bPackage` goal. This goal simply puts the XML files in the proper ZIP file structure in order to import it to the server.
### pre-integration-test
Runs the `b2bImport` goal. This goal is the most complex

## Building/installing

1. This plugin uses the B2B ANT task under the hood. As a result, it expects a JDeveloper/SOA Suite Quick Start install on the machine it's being run from.
2. Until the plugin is published, run ./gradlew clean install to install the plugin in your local `.m2` repository.
3. Ensure the machine running the plugin has network access to the port of the Weblogic managed server that the ESS services are running on

## Usage

There are 2 types of projects you can/should define:

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
  
  <properties>
    <b2b.artifact.type>DocumentDefinitions</b2b.artifact.type>
    <soa.t3.url>t3://soa_server_hostname:soa_server1_port</soa.t3.url>
    <weblogic.user>username</weblogic.user>
    <weblogic.password>thepassword</weblogic.password>
  </properties>
  
  <build>
    <plugins>
      <plugin>
         <groupId>com.avioconsulting</groupId>
         <artifactId>b2b-maven-plugin</artifactId>
         <version>1.0.8</version>
         <extensions>true</extensions>
         <configuration>
          <soaDeployUrl>${soa.t3.url}</soaDeployUrl>
          <!-- If you're using settings.xml for these, need to repeat them here, overriden values from settings.xml do not make it into the plugin for some reason -->
          <weblogicUser>${weblogic.user}</weblogicUser>
          <weblogicPassword>${weblogic.password}</weblogicPassword>
        </configuration>
     </plugin>
    </plugins>
  </build>
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

  <properties>
    <b2b.artifact.type>PartnersAndAgreements</b2b.artifact.type>
    <b2b.partners>AVIOConsulting,A_PARTNER</b2b.partners>
    <b2b.agreements>837Agreement,999Agreement</b2b.agreements>
    <soa.t3.url>t3://soa_server_hostname:soa_server1_port</soa.t3.url>
    <weblogic.user>username</weblogic.user>
    <weblogic.password>thepassword</weblogic.password>
  </properties>
  
  <build>
    <plugins>
      <plugin>
         <groupId>com.avioconsulting</groupId>
         <artifactId>b2b-maven-plugin</artifactId>
         <version>1.0.8</version>
         <extensions>true</extensions>
         <configuration>
          <soaDeployUrl>${soa.t3.url}</soaDeployUrl>
          <!-- If you're using settings.xml for these, need to repeat them here, overriden values from settings.xml do not make it into the plugin for some reason -->
          <weblogicUser>${weblogic.user}</weblogicUser>
          <weblogicPassword>${weblogic.password}</weblogicPassword>
        </configuration>
     </plugin>
    </plugins>
  </build>
</project>
```

No special tie-ins to JDev, you can organize that however you want

## Development Workflow

The general idea is to use the B2B console on your laptop as an "IDE". Everything goes in there. Then you run this to populate your repo:

```
mvn -Db2b.export=true generate-resources
# This will run an export from the server but will filter out anything that doesn't match your POM properties (see above)
```

Deploy the project like you would a SOA composite:

```
mvnd pre-integration-test
```

## Wish List

Before running export, produce a mapping of supported document definition refs in the tp file.
Then after the export, go back and make all those IDs in the TP and TPA file consistent.
