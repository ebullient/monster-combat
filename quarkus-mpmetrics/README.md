# quarkus-micrometer project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

This project uses variables derived from git to populate image attributes.
Quarkus dev mode will complain if these attributes aren't set.

To set git project attributes and run in Quarkus dev mode, invoke the git plugin along with the dev mode plugin:

```bash
../mvnw pl.project13.maven:git-commit-id-plugin:revision quarkus:dev
```

## Packaging and running the application

The application is packageable using `../mvnw package`.

It produces the executable `quarkus-mpmetrics-nnn-SNAPSHOT-runner.jar` file in `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/quarkus-mpmetrics-nnn-SNAPSHOT-runner.jar`.

## Creating a native executable

You can create a native executable using: `../mvnw package -Pnative`.

Or you can use Docker to build the native executable using: `../mvnw package -Pnative -Dquarkus.native.container-build=true`.

You can then execute your binary: `./target/quarkus-micrometer-nnn-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/building-native-image-guide .

## Creating docker images

The docker-maven-plugin is present. It will build a jvm image if the target/lib dir is present (which it will be for a non-native build),
and a native image if the native profile is active.
