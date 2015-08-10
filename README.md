# XML-Data-Remover
XML processing library which allows to modify an xml by replacing its content. 

Project description
------
The library uses java SAX to parse the file, so it's able to process big xml files without loading them in memory. Currently it supports two operations:
* Replacing content
* Removing tags 

Configuration
-----
In order to define the changes requested a configuration file must be specify containing all the rules to apply on the document. Each rule is specific of each xpath defined. A _skip_ rule also applies to that node children. It follows json sintax. 
  { "rule_set": 
  [ { "xpath": "/xml/path/to/change",
      "rules": [ {
          "type": "REPLACE",
          "match": "*",
          "replacement": "0"
      } ]
  } ] }

How to use
------
The library can be imported in an existing project or run as standalone jar.
  Usage: "java -jar <library> -i inputFilePath [-o outputFile] [-p propertyFilePath]"


