# XML Data Modifier
XML processing library which allows to modify an xml by replacing its content or removing nodes.

Project description
------
The main purpose of this library is to work with big xml files which cannot be easily loaded into memory. The library uses java SAX to parse the file, stream it to a converter and then write the converted version to a new output file. Currently it supports two operations:
* Replacing content
* Removing tags 

Configuration
-----
In order to define the changes requested a configuration file must be specify containing all the rules to apply on the document. Each rule is specific for each xpath defined. A _skip_ rule also applies to that node children. It follows json sintax. The REPLACE rule uses regular expressions to match the string. It's also possible to simply define "*" in order to replace the whole tag content.

    { "rule_set": 
    [ { "xpath": "/xml/path/to/change",
      "rules": [ 
      { "type": "REPLACE",
          "match": "*",
          "replacement": "0"
      },
      { "xpath": "/xml/path/to/skip",
        "rules": [ 
        { "type": "SKIP"}
      }]
    } ] }

How to use
------
The library can be imported in an existing project or run as standalone jar.

    Usage: "java -jar <library> -i inputFilePath [-o outputFile] [-p propertyFilePath]"


