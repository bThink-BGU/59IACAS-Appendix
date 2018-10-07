# SimSat

This is a maven project, containing the satellite simulation explained in the paper.

## To Compile

You must have `Panelmatic` correctly registered in your local maven repo. This is
done by executing the following line in a terminal, from the `SimSat` directory:

```
mvn install:install-file
   -Dfile=panelmatic.jar
   -DgroupId=org
   -DartifactId=panelmatic
   -Dversion=1
   -Dpackaging=jar
   -DgeneratePom=true
```


## To Run:

1. Ensure you have Java version 8 or greater installed.
2. Ensure you have maven installed
3. Using a commandline console, navigate to this directory and type `mvn exec:java`.
