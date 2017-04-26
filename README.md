# **W**eb Data **Inte**g**r**ation Framework (WInte.r)

A framework for the pre-processing, matching and fusion of data with focus on data from the web. Enables students and researchers to explore data integration methods and quickly set up experiments with standard methods. Then, the methods can be customised by plugging in different, pre-defined building blocks (such as blockers, matching rules, etc.) or new methods can be created by combining these building blocks in a new way.

**Quick Start**: Have a look at the code examples in our [Wiki](/wiki/)!

## Contents
- [Functionality](#functionality)
- [Use cases](#use-cases)
- [References](#references)
- [Contact](#contact)
- [License](#license)
- [Acknowledgements](#acknowledgements)

## Functionality
WInte.r provides methods for end-to-end data integration: From data loading and pre-processing, via matching of records and attributes to the fusion of values from different sources.

![Data Integration Process Example](/img/integration_overview.png)

**Pre-processing**: During pre-processing you load your data and prepare it for the methods that you are going to apply. WInte.r WebTables provides you with specialised pre-processing methods for tabular data, for example, data type detection and unit conversion.
-	Data type detection & Unit conversion
-	Header detection
-	Subject column detection

**Matching Algorithms**: To combine multiple data sources, you must find out which records and which attributes have the same meaning in the different sources. WInte.r provides you with pre-implemented algorithms that can be configure for you specific use case. Alternativelly, you can combine existing building blocks into new matchers.
-	14 pre-defined similarity measures for strings, numbers, dates and lists of values
-	Modifiers allow for easy re-scaling of the values. Example: quadratic modifier re-scales any similarity measure to the square of its similarity.

**Schema Matching**: Methods to find attributes in two data sources that have the same meaning. The pre-implemented algorithms either compare the schemas using features generated from the meta-data (for example label-based schema matching) or exploit an existing mapping of records (duplicate-based schema matching).
-	Label-based schema matching
-	Instance-based schema matching
-	Duplicate-based schema matching

**Identity Resolution**: Methods to find records in two data sources that describe the same thing. The pre-implement algorithms can be applied to a single dataset for duplicate detection or to multiple datasets for identity resolution (also known as record linkage).
-	Blocking by single/multiple blocking key(s)
-	Sorted-Neighbourhood Method
-	Token-based identity resolution
-	Rule-based identity resolution

**Data Fusion**: With the mapping between the data sources, you can combine the data into a single, consolidated dataset. However, the different sources may provide conflicting values. To produce a consistent dataset, WInte.r implements a data fusion process that can be configured with various conflict resolution functions that decide which value to include in the final dataset.
-	11 pre-defined conflict resolution functions for strings, numbers and lists of values as well as data type independent functions.

## Use cases

**T2K Match: Integration of data sources using a central, consolidated schema**

Many web sites provide data on their web pages in the form of tables and large amounts of such tables have been collected by the Web Data Commons project [3]. To facilitate their content, these tables can be integrated with a cross-domain knowledge base. For this integration step, the T2K Match [1,2] algorithm creates a mapping from millions of extracted tables to a central knowledge base. The full source code of this algorithm, which includes advanced matching rules that combine schema matching and identity resolution without user interaction, is available in the WInter.T2KMatch project.

## Contact

If you have any questions, please refer to the [Wiki](/wiki/) first. For further information contact oli [at] informatik.uni-mannheim.de

## License

The WInte.r framework can be used under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0)

## Acknowledgements

WInte.r is developed at the [Data and Web Science Group](http://dws.informatik.uni-mannheim.de/) at the [University of Mannheim](http://www.uni-mannheim.de/).

## References
[1] Ritze, D., Lehmberg, O., & Bizer, C. (2015, July). Matching html tables to dbpedia. In Proceedings of the 5th International Conference on Web Intelligence, Mining and Semantics (p. 10). ACM.

[2] Ritze, D., Lehmberg, O., Oulabi, Y., & Bizer, C. (2016, April). Profiling the potential of web tables for augmenting cross-domain knowledge bases. In Proceedings of the 25th International Conference on World Wide Web (pp. 251-261). International World Wide Web Conferences Steering Committee.

[3] Lehmberg, O., Ritze, D., Meusel, R., & Bizer, C. (2016, April). A large public corpus of web tables containing time and context metadata. In Proceedings of the 25th International Conference Companion on World Wide Web (pp. 75-76). International World Wide Web Conferences Steering Committee.
