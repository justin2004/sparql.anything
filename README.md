# Welcome
sparql.anything is a system for Semantic Web re-engineering that allows users to ... query anything with SPARQL.

## Facade-X
sparql.anything uses a single generic abstraction for all data source formats called Facade-X.
Facade-X is a simplistic meta-model used by sparql.anything transformers to generate RDF data from diverse data sources.
Intuitively, Facade-X uses a subset of RDF as a general approach to represent the source content *as-it-is* but in RDF.
The model combines two type of elements: containers and literals.
Facade-X has always a single root container. 
Container members are a combination of key-value pairs, where keys are either RDF properties or container membership properties.
Instead, values can be either RDF literals or other containers.
This is a generic example of a Facade-X data object (more examples below):

```
@prefix fx: <urn:facade-x:ns#> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
[] rdf:_1 [
    fx:someKey "some value" ;
    rdf:_1 "another value with unspecified key" ;
    rdf:_2 [
        rdf:type fx:MyType
        rdf:_1 "another value" 
    ]
```

## Querying anything
sparql.anything extends the Apache Jena ARQ processors by *overloading* the SERVICE operator, as in the following example:

Suppose having this JSON file as input (also available at ``https://raw.githubusercontent.com/spice-h2020/sparql.anything/main/examples/example1.json``)

```
[
   {
      "name":"Friends",
      "genres":[
         "Comedy",
         "Romance"
      ],
      "language":"English",
      "status":"Ended",
      "premiered":"1994-09-22",
      "summary":"Follows the personal and professional lives of six twenty to thirty-something-year-old friends living in Manhattan.",
      "stars":[
         "Jennifer Aniston",
         "Courteney Cox",
         "Lisa Kudrow",
         "Matt LeBlanc",
         "Matthew Perry",
         "David Schwimmer"
      ]
   },
   {
      "name":"Cougar Town",
      "genres":[
         "Comedy",
         "Romance"
      ],
      "language":"English",
      "status":"Ended",
      "premiered":"2009-09-23",
      "summary":"Jules is a recently divorced mother who has to face the unkind realities of dating in a world obsessed with beauty and youth. As she becomes older, she starts discovering herself.",
      "stars":[
         "Courteney Cox",
         "David Arquette",
         "Bill Lawrence",
         "Linda Videtti Figueiredo",
         "Blake McCormick"
      ]
   }
]
```

With sparql.anything you can select the TV series starring "Courteney Cox" with the SPARQL query

```
PREFIX fx: <urn:facade-x:ns#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

SELECT ?seriesName
WHERE {

    SERVICE <facade-x:https://raw.githubusercontent.com/spice-h2020/sparql.anything/main/examples/example1.json> {
        ?tvSeries fx:name ?seriesName .
        ?tvSeries fx:stars ?star .
        ?star ?li "Courteney Cox" .
    }

}
```

and get this result without caring of transforming JSON to RDF. 

| seriesName    |
|---------------|
| "Cougar Town" |
| "Friends"     |



## Supported Formats
Currently, sparql.anything supports the following formats: "json", "html", "xml", "csv", "bin", "png","jpeg","jpg","bmp","tiff","tif", "ico", "txt" ... but the possibilities are limitless!

By default, these formats are transformed as follows.

<details><summary>JSON</summary>
    
    
|Input File|Output|
|---|---|
|<pre>{<br>  "stringArg":"stringValue",<br>  "intArg":1,<br>  "booleanArg":true,<br>  "nullArg": null,<br>  "arr":[0,1]<br>}</pre> | <pre>@prefix fx:    &lt;urn:facade-x:ns#&gt; .<br>@prefix rdf: &lt;http://www.w3.org/1999/02/22-rdf-syntax-ns#&gt; .<br>@prefix xsd: &lt;http://www.w3.org/2001/XMLSchema#&gt; .<br>[  <br>  fx:arr         [  <br>                   rdf:_0  "0"^^xsd:int ;<br>                   rdf:_1  "1"^^xsd:int<br>                 ] ;<br>  fx:booleanArg  true ;<br>  fx:intArg      "1"^^xsd:int ;<br>  fx:stringArg   "stringValue"<br>] .</pre> |


    
</details>


<details><summary>HTML</summary>

|Input File|Output|
|---|---|
|<pre></pre> | <pre></pre> |

</details>

<details><summary>XML</summary>

|Input File|Output|
|---|---|
|<pre></pre> | <pre></pre> |

</details>

<details><summary>CSV</summary>

|Input File|Output|
|---|---|
|<pre></pre> | <pre></pre> |

</details>

<details><summary>BIN, PNG, JPEG, JPG, BMP, TIFF, TIF, ICO </summary>

|Input File|Output|
|---|---|
|![Image example](https://raw.githubusercontent.com/ianare/exif-samples/master/jpg/Canon_40D.jpg)| <pre>[ &lt;http://www.w3.org/1999/02/22-rdf-syntax-ns#_1&gt;  "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"^^&lt;http://www.w3.org/2001/XMLSchema#base64Binary&gt; ] .
</pre> |

</details>

<details><summary>TXT</summary>

|Input File|Output|
|---|---|
|<pre>Hello World!</pre> | <pre>[ &lt;ttp://www.w3.org/1999/02/22-rdf-syntax-ns#_1&gt; "Hello World!" ] .</pre> |

</details>

<details><summary>Metadata</summary>

|Input File|Output|
|---|---|
|<pre></pre> | <pre></pre> |

</details>

## IRI schema


### General purpose arguments

### Format specific arguments

### Licence

