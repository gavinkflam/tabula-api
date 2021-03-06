# tabula-api

[![Travis CI Status](https://travis-ci.org/gavinkflam/tabula-api.svg?branch=master)](https://travis-ci.org/gavinkflam/tabula-api)
[![CircleCI Status](https://circleci.com/gh/gavinkflam/tabula-api/tree/master.png?style=shield&circle-token=:circle-token)](https://circleci.com/gh/gavinkflam/tabula-api/tree/master)
[![Codecov Coverage](https://codecov.io/gh/gavinkflam/tabula-api/branch/master/graph/badge.svg)](https://codecov.io/gh/gavinkflam/tabula-api)
[![Docker Build Status](https://img.shields.io/docker/build/gavinkflam/tabula-api.svg)](https://hub.docker.com/r/gavinkflam/tabula-api)
[![Dependencies Status](https://jarkeeper.com/gavinkflam/tabula-api/status.svg)](https://jarkeeper.com/gavinkflam/tabula-api)

An API server extracting tables from PDF files via [tabula-java][tabula-java].

## Quick examples with Docker and cURL

```bash
docker run -p 8080:8080 -e HOST=0.0.0.0 gavinkflam/tabula-api:1.0.0
curl -X POST -H 'Content-Type: multipart/form-data' \
  -F 'file=@my-pdf-file.pdf' -F 'guess=true' -F 'pages=all' \
  http://localhost:8080/api/extract
```

## Documentations

See [`/doc`](doc).

## License

MIT License

[tabula-java]: https://github.com/tabulapdf/tabula-java
