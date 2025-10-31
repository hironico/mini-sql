# What is minisql ?
Mini SQL is a database agnostic frontend that aims to be very small yet powerfull and easy to use. It can run on all major platforms that support Java and connect to major DBMS that provide a public Java JDBC driver.

Mini SQL aim to be :
- very lightweight (core program is less than one MB)
- very fast 
- full of essential functionalities
- pleasant to use user interface (thanks [icons8](https://icons8.com), the creators of the fantastic Pichon and icons)

# Binaries

Mini SQL is packaged in a single binary JAR file containing all required dependencies.
Download the dist package and launch with the following command:
```java -jar hironico-minisql-dist-x.y.z.jar```

[![Release Status](https://github.com/hironico/mini-sql/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/hironico/mini-sql/actions/workflows/maven-publish.yml/badge.svg) 

[![Build Status](https://github.com/hironico/mini-sql/actions/workflows/main.yml/badge.svg)](https://github.com/hironico/mini-sql/actions/workflows/main.yml/badge.svg)

## Download 

Go to [Maven Central](https://central.sonatype.com/artifact/net.hironico/hironico-minisql-dist) and download the latest version.

# Database support 

Virtually, Mini SQL can connect to any JDBC compliant database server. 
For your convenience, we included in the distribution the following major vendors:
- Oracle
- TDS based : Sybase / IQ and MSSQL server
- Postgres

If you need to use a driver that is not in the provided distribution:
- Download the driver JAR file from the database vendor
- Launch Mini-SQL and open configuration
- In the Drivers tab browse for the driver file and load it into the classpath
- When opening again mini sql, configured additional drivers are automatically added to classpath.
- If changing the driver file's location, don't forget to update the configuration.

## Change the packaging 
To add or remove drivers embedded in the distribution uber jar, then update the pom.xml and rebuild.

# Development

## Recommended settings
The project needs to sign the JARs artifacts before publishing. The maven install goal
needs a gpg public key named "hironico".

- create or import the hironico pgp key.

``gpg --import your_key.pgp``

- modify the `$HOME/.m2/settings.xml` file to add a profile with `gpg.keyname` property :

````
<settings>
  <profiles>
      <profile>
          <id>gpg_default</id>
          <activation>
              <activeByDefault>true</activeByDefault>
          </activation>
          <properties>
              <gpg.keyname>hironico</gpg.keyname>
          </properties>
      </profile>
  </profiles>
</settings>
````
## Alternative settings

You can use environment variables and then use the provided settings.xml.
This file is used in CI pipelines to build and publish the distribution. 
Do not change it please.