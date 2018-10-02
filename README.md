# **W**eb Data **INTE**g**R**ation Framework (WInte.r)

The WInte.r framework [5] provides methods for end-to-end data integration. The framework implements well-known methods for data pre-processing, schema matching, identity resolution, data fusion, and result evaluation.  The methods are designed to be easily customizable by exchanging pre-defined building blocks, such as blockers, matching rules, similarity functions, and conflict resolution functions. In addition, these pre-defined building blocks can be used as foundation for implementing advanced integration methods.

## Contents
- [Functionality](#functionality)
- [Use cases](#use-cases)
- [Contact](#contact)
- [License](#license)
- [Acknowledgements](#acknowledgements)
- [References](#references)

**Quick Start**: The section below provides an overview of the functionality of the WInte.r framework. As alternatives to acquaint yourself with the framework, you can also read the [Winte.r Tutorial](../../wiki/Movies-use-case) or have a look at the code examples in our [Wiki](../../wiki)!

## Using WInte.r

You can include the WInte.r framework via the following Maven dependency:

```xml
<repositories>
	<repository>
		<id>releases</id>
		<url>https://breda.informatik.uni-mannheim.de/nexus/content/repositories/releases/</url>
	</repository>
</repositories>

<dependency>
	<groupId>de.uni_mannheim.informatik.dws</groupId>
	<artifactId>winter</artifactId>
	<version>1.2</version>
</dependency>
```



## Functionality
The WInte.r framework covers all central steps of the data integration process, including data loading, pre-processing, schema matching, identity resolution, as well as data fusion. This section gives an overview of the functionality and the alternative algorithms that are provided for each of these steps.

![Data Integration Process Example](/img/integration_overview.png)

**Data Loading**: WInte.r provides readers for standard data formats such as CSV, XML and JSON. In addition, WInte.r offers a specialized JSON format for representing tabular data from the Web together with meta-information about the origin and context of the data, as used by the [Web Data Commons (WDC) Web Tables Corpora](http://www.webdatacommons.org/webtables/index.html).

**Pre-processing(../../wiki/DataNormalisation)**: During pre-processing you prepare your data for the methods that you are going to apply later on in the integration process. WInte.r WebTables provides you with specialized pre-processing methods for tabular data, such as:
-	Data type detection
-	Unit of measurement normalization
-	Header detection
-	Subject column detection (also known as entity name column detection)

**[Schema Matching](../../wiki/SchemaMatching)**: Schema matching methods find attributes in two schemata that have the same meaning. WInte.r provides three pre-implemented schema matching algorithms which either rely on attribute labels or data values, or exploit an existing mapping of records (duplicate-based schema matching) in order to find attribute correspondences.
-	Label-based schema matching
-	Instance-based schema matching
-	Duplicate-based schema matching

**[Identity Resolution](../../wiki/IdentityResolution)**: Identity resolution methods (also known as data matching or record linkage methods) identify records that describe the same real-world entity. The pre-implemented identity resolution methods can be applied to a single dataset for duplicate detection or to multiple datasets in order to find record-level correspondences. Beside of manually defining identity resolution methods, WInte.r also allows you to [learn matching rules](../../wiki/Learning-Matching-Rules) from known correspondences. Identity resolution methods rely on blocking (also called indexing) in order to reduce the number of record comparisons. WInte.r provides following pre-implemented blocking and identity resolution methods:
-	Blocking by single/multiple blocking key(s)
-	Sorted-Neighbourhood Method
-	Token-based identity resolution
-	Rule-based identity resolution

**[Data Fusion](../../wiki/DataFusion)**: Data fusion methods combine data from multiple sources into a single, consolidated dataset. For this, they rely on the schema- and record-level correspondences that were discovered in the previous steps of the integration process. However, different sources may provide conflicting data values. WInte.r allows you to resolve such data conflicts (decide which value to include in the final dataset) by applying different conflict resolution functions.
-	11 pre-defined conflict resolution functions for strings, numbers and lists of values as well as data type independent functions.

## Use cases

WInte.r can be used out-of-the-box to integrate data from multiple data sources. The framework can also be used as foundation for implementing more advanced, use case-specific integration methods. In the following we provide an example use case from each category.

**Integration of Multiple Data Sources: Building a Movie Dataset**

The WInte.r framework is used to integrate data from multiple sources within the [Web Data Integration](http://dws.informatik.uni-mannheim.de/en/teaching/courses-for-master-candidates/ie670webdataintegration/) course offered by [Professor Bizer](http://dws.informatik.uni-mannheim.de/bizer) at the University of Mannheim. The basic case study in this course is the integration of movie data from multiple Web data sources. In addition, student teams use the WInte.r framework to integrate data about different topics as part of the projects that they conduct during the course.

**Integration of Large Numbers of Data Sources: Augmenting the DBpedia Knowledge base with Web Table Data**

Many web sites provide data in the form of HTML tables. Millions of such data tables have been extracted from the [CommonCrawl](http://commoncrawl.org/) web corpus by the [Web Data Commons](http://webdatacommons.org/webtables/) project [3]. Data from these tables can be used to fill missing values in large cross-domain knowledge bases such as DBpedia [2]. An example of how pre-defined building blocks from the WInte.r framework are combined into an advanced, use-case specific integration method is the T2K Match algorithm [1]. The algorithm is optimized to match millions of Web tables against a central knowledge base describing millions of instances belonging to hundreds of different classes  (such a people or locations) [2]. The full source code of the algorithm, which includes advanced matching methods that combine schema matching and identity resolution, is available in the [WInte.r T2K Match project](https://github.com/olehmberg/T2KMatch).

**Preprocessing for large-scale Matching: Stitching Web Tables for Improving Matching Quality**

Tables on web pages ("web tables") cover a diversity of topics and can be a source of information for different tasks such as knowledge base augmentation or the ad-hoc extension of datasets. However, to use this information, the tables must first be integrated, either with each other or into existing data sources. The challenges that matching methods for this purpose have to overcome are the high heterogeneity and the small size of the tables.
To counter these problems, web tables from the same web site can be stitched before running any of the existing matching systems. This means that web tables are combined based on a schema mapping, which results in fewer and larger stitched tables [4].
The source code of the stitching method is available in the [Web Tables Stitching project](https://github.com/olehmberg/WebTableStitching).

**Data Search for Data Mining (DS4DM)**

Analysts increasingly have the problem that they know that some data which they need for a project is available somewhere on the Web or in the corporate intranet, but they are unable to find the data. The goal of the ['Data Search for Data Mining' (DS4DM) project](http://ds4dm.de/) is to extend the data mining platform Rapidminer with data search and data integration functionalities which enable analysts to find relevant data in potentially very large data corpora, and to semi-automatically integrate the discovered data with existing local data.

## Contact

If you have any questions, please refer to the  [Winte.r Tutorial](../../wiki/Movies-use-case), [Wiki](../../wiki), and the [JavaDoc](https://olehmberg.github.io/winter/javadoc/) first. For further information contact oli [at] informatik [dot] uni-mannheim [dot] de

## License

The WInte.r framework can be used under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0).

If you use the WInte.r framework in any publication, please cite [5].

## Acknowledgements

WInte.r is developed at the [Data and Web Science Group](http://dws.informatik.uni-mannheim.de/) at the [University of Mannheim](http://www.uni-mannheim.de/).

## References
[1] Ritze, D., Lehmberg, O., & Bizer, C. (2015, July). Matching html tables to dbpedia. In Proceedings of the 5th International Conference on Web Intelligence, Mining and Semantics (p. 10). ACM.

[2] Ritze, D., Lehmberg, O., Oulabi, Y., & Bizer, C. (2016, April). Profiling the potential of web tables for augmenting cross-domain knowledge bases. In Proceedings of the 25th International Conference on World Wide Web (pp. 251-261). International World Wide Web Conferences Steering Committee.

[3] Lehmberg, O., Ritze, D., Meusel, R., & Bizer, C. (2016, April). A large public corpus of web tables containing time and context metadata. In Proceedings of the 25th International Conference Companion on World Wide Web (pp. 75-76). International World Wide Web Conferences Steering Committee.

[4] Lehmberg, O., & Bizer, C. (2017). Stitching web tables for improving matching quality. Proceedings of the VLDB Endowment, 10(11), 1502-1513.

[5] Lehmberg, O., Brinkmann, A., & Bizer, C. WInte. r - A Web Data Integration Framework. ISWC 2017.
