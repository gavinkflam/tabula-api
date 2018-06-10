# Extract tables from PDF file

`POST /api/extract`

The parameters and documentations are derived from `tabula-java` 1.0.2.

You should consult the [document][tabula-java-doc] for more details and quirks.

## Content types

You should specify the content type to output the results.

The following MIME types are supported.

- `text/csv` for CSV
- `application/json` for JSON
- `text/tab-separated-values` for TSV

## Multipart form parameters

- `area` - Portion of the page to analyze. Accepts top, left, bottom, right.

  Default is entire page.

  Example: `269.875,12.75,790.5,561`

  If all values are between 0-100 (inclusive) and preceded by '%',
  input will be taken as % of actual height or width of the page.

  Example: `%0,0,100,50`

  To specify multiple areas, `area` parameter should be repeated.

- `columns` - X coordinates of column boundaries.

  Example: `10.1,20.2,30.3`

- `file` - The PDF file to extract work on.

- `guess` - Guess the portion of the page to analyze per page.

  Options: `True`/`true` or `False`/`false`

- `lattice` - Force PDF to be extracted using lattice-mode extraction.

  Only if there are ruling lines separating each cell, as an Excel spreadsheet.

  Options: `True`/`true` or `False`/`false`

- `pages` - Comma separated list of ranges, or all.

  Default is `1`.

  Examples: `1-3,5-7`, `3` or `all`.

- `password` - Password to decrypt document.

  Default is empty.

- `stream` - Force PDF to be extracted using stream-mode extraction.

  Only if there are ruling lines separating each cell.

  Options: `True`/`true` or `False`/`false`

## Example request

```
> POST /api/extract HTTP/1.1
> Accept: text/csv
> Content-Length: 9036
> Content-Type: multipart/form-data; boundary=--------------------------a1a408b648314ce0

< HTTP/1.1 100 Continue
< Content-Length: 0

> --------------------------a1a408b648314ce0
> Content-Disposition: form-data; name="file"; filename="multi-column.pdf"
> Content-Type: application/pdf
>
> Binary content skipped.
> --------------------------a1a408b648314ce0
> Content-Disposition: form-data; name="area"
>
> %0,0,100,50
> --------------------------a1a408b648314ce0
> Content-Disposition: form-data; name="area"
>
> %0,50,100,100
> --------------------------a1a408b648314ce0
> Content-Disposition: form-data; name="pages"
>
> 1
> --------------------------a1a408b648314ce0--
```

## Example response

```
HTTP/1.1 200 OK
Content-Type: text/csv

1,100,200
2,101,201
3,102,202
4,103,203
5,104,204
More CSV content skipped.
```

[tabula-java-doc]: https://github.com/tabulapdf/tabula-java/blob/v1.0.2/README.md
