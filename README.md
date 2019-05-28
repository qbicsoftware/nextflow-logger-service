# flowstore

Store and find Nextflow weblog payloads.

Check out:

* [Run flowstore](#run-flowstore) - How to quickly run _flowstore_
* [Configure databases](#configure-databases) - How to link to a running database instance
* [Underlying Data Models](#underlying-data-models)

## Run flowstore

flowstore is bundled as an executable jar, so you can just start the application with:

```bash
java -jar nfxlogger-<version>.jar
```

Keep in mind that this will run the flowstore with the [default configuration](https://raw.githubusercontent.com/qbicsoftware/nextflow-logger-service/master/src/main/resources/application.yml) setup.

You can pass your own configuration file with:

```bash
java -Dmicronaut.config.files=myconfig.yml -jar nfxlogger-<version>.jar
```

## Development

flowstore's dependencies are managed with [Maven](https://maven.apache.org/). Clone the code from GitHub and checkout the `development` branch:

```bash
git clone git@github.com:qbicsoftware/nextflow-logger-service.git
git checkout --track origin/development
```

Be sure you have Maven installed on your system and that the application is in your system `PATH`, otherwise the following commands will not work.

Open your terminal and verify the Maven installation. This will look different on your system, but should output the installed Maven version:

```
> mvn --version
Apache Maven 3.6.0 (97c98ec64a1fdfee7767ce5ffb20918da4f719f3; 2018-10-24T20:41:47+02:00)
Maven home: /usr/local/Cellar/maven/3.6.0/libexec
Java version: 1.8.0_181, vendor: Azul Systems, Inc., runtime: /Users/sven1103/.sdkman/candidates/java/8.0.181-zulu/jre
Default locale: en_GB, platform encoding: UTF-8
OS name: "mac os x", version: "10.14.5", arch: "x86_64", family: "mac"
```

### Compile

```bash
mvn clean compile
```

### Run unit tests

```bash
mvn clean test
```

### Run integration tests

```
mvn clean verify
```

### Create executable jar

This will create an executable jar in your current working directory under `./target`.

```
mvn clean package
```

## Configure

Coming soon...

## Data Models

Coming soon...

## Author

This software is created by [sven1103](https://github.com/sven1103), developed at the Quantitative Biology Center, University of Tübingen, Germany.



