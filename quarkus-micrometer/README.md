# quarkus-micrometer project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

This project uses variables derived from git to populate image attributes.
Quarkus dev mode will complain if these attributes aren't set.

To set git project attributes and run in Quarkus dev mode, invoke the git plugin along with the dev mode plugin:

```bash
../mvnw compile quarkus:dev
# OR
../mvnw pl.project13.maven:git-commit-id-plugin:revision quarkus:dev
```

