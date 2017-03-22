# Welcome to Kado
## Kado is a Presto GUI for query Hive Data and Scheuduled Tasks

<img
  src="https://github.com/chickling/kado/raw/master/doc/images/kado_start.png" 
/>

see the User Manual  for Kado

## Overview
<div style="text-align:center">
<img
  src="https://github.com/chickling/kado/raw/master/doc/images/kado_overview.png" width="80%"
/>
</div>

## Requirement
### System
- Windows or linux
- Java 8 Update 45 or higher (8u45+), 64-bit
- Maven 3.3.9+ (for building)

### Presto
- Hadoop cluster 2.6.0 ( HDFS,HIVE )
- Presto 0.143 

## Build Kado
Kado is a standard Maven project. Simply run the following command from the project root directory :

> mvn clean package

On the first build, Maven will download all the dependencies from the internet and cache them in the local repository (~/.m2/repository), which can take a considerable amount of time. Subsequent builds will be faster.

you will get the <i class="icon-folder-open"></i> war resouce when build finished
Put it to Web Container (jetty 9.2+) and enjoy Kado


## Running Kado in your IDE

### Overview

After building Kado for the first time, you can load the project into your IDE and run the Portal. We recommend using [IntelliJ IDEA](https://www.jetbrains.com/idea/). Because Kado is a standard Maven project, you can import it into your IDE using the root `pom.xml` file. In IntelliJ, choose Open Project from the Quick Start box or choose Open from the File menu and select the root `pom.xml` file.

After opening the project in IntelliJ, double check that the Java SDK is properly configured for the project:

Open the File menu and select Project Structure
In the SDKs section, ensure that a 1.8 JDK is selected (create one if none exist)
In the Kado section, ensure the Project language level is set to 8.0 as Kado makes use of several Java 8 language features

> Main Class : com.chickling.boot.JettyMain


## Docker image
Comming Soon
