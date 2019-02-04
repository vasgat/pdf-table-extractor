# pdf-table-extractor
PDF Table Extractor is a tool that leverages tabulapdf library to extract tables from pdf files and performs some further processing and stores the resulted tables into csv formatted files.

**Note: [Here](http://easie.iti.gr/pdf-tables/) you can find an online version of the tool and some [documentation](http://easie.iti.gr/pdf-tables/documentation.html) for better usage**

You can run `pdf-table-extractor` from command line as follows: 

          java -jar .\pdf-table-extractor-1.0.jar ..\pdf_examples\COGSuppliers.pdf

          java -jar .\pdf-table-extractor-1.0.jar ..\pdf_examples\Benetton.pdf WITHOUT_RULINGS UPPER_MERGE
