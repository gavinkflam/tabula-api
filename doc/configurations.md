# Configurations

You can configure each runtime with environment variables or Java system
properties.

## Configuration items

- `host` - the interface bind address.

Default is `localhost`.

- `port` - the port listening for requests.

Default is `8080`.

## Docker run example

`docker run -p 8080:8080 -e HOST=0.0.0.0 gavinkflam/tabula-api:1.0.0`

## Java example

`java -Dhost=0.0.0.0 -jar tabula-java-1.0.0-standalone.jar`.

## Precedence

The configurations will be resolved in the following order.

A value defined in a higher precedence source will override the previous
definintions.

1. Default values
2. Environment variables
3. Java system properties
