# Digital Serial Port #

This is a implementation of a serial port and allows to access the hosts serial port from
within a [Digital](https://github.com/hneemann/Digital/) simulation. 

## How do I get set up? ##

The easiest way to build the necessary Jar is to use [maven](https://maven.apache.org/).

* JDK 1.8 is needed (either the Oracle JDK 1.8 or OpenJDK 1.8)  
* Clone the repository.
* Replace the `Digital.jar` which is included in this repo with the version you want to use.
* After that run `mvn install` to create the library jar file
