# **W**eb Data **Inte**g**r**ation Framework (WInte.r)

A framework for the pre-processing, matching and fusion of data with focus on data from the web. Enables students and researchers to explore data integration methods and quickly set up experiments with standard methods. Then, the methods can be customised by plugging in different, pre-defined building blocks (such as blockers, matching rules, etc.) or new methods can be created by combining these building blocks in a new way.

**Quick Start**: Have a look at the code examples in our [Wiki](/wiki/)!

## Contents
- [Functionality](#functionality)
- [Framework Architecture](#framework-architecture)
  - [Matching](#matching)
  - [Building Blocks for Matching](#building-blocks-for-matching)
  - [Implemented Building Blocks](#implemented-building-blocks)
  - [Data Fusion](#data-fusion)
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

## Framework Architecture

The framework comprises a default model and implementations for various data integration tasks. The general workflow of an end-to-end data integration process looks like the following.
-	Load the data into dataset objects
-	Apply schema matching to get correspondences between attributes
-	Apply identity resolution to get correspondences between records
-	Transform the data into a consolidated schema using the correspondences between the attributes
-	Use the correspondences between the records to perform data fusion and create one consolidated dataset

![Matching Engine Overview](/img/matching_engine_overview.png)

### Matching
For schema matching and identity resolution, the MatchingEngine facade provides implementations for the following matching operations:

**Duplicate Detection / Identity Resolution**
Finds duplicate records in a single data set (duplicate detection) or in two different data sets (identity resolution). Accepts a matching rule and a blocker as parameters. First, the Blocker is used to generate candidate pairs of records, which are then evaluated using the matching rule.

**Label-based Schema Matching**
Finds similar attributes in two different data sets by comparing the attribute names. The user can specify a comparator, which defines a similarity measure and can contain pre- and post-processing of the attributes names (i.e. lower-casing, stopword-removal) and the similarity value (i.e., re-scaling).

**Instance-based Schema Matching**
Finds similar attributes in two different data sets by comparing the values of attributes. The user specifies a value generator that defines which values to compare. These can either be the same values as in the original data source or a transformation, such as word tokens or n-grams.

**Duplicate-based Schema Matching**
Finds similar attributes in two different data sets by comparing the values of duplicate records in both datasets. Accepts the correspondences between duplicate records, a SchemaMatchingRule and a SchemaBlocker as parameters. First, the SchemaBlocker is used to generate possible pairs of attributes. Then, the SchemaMatchingRule is applied to the values of all generated pairs for all duplicate records. Finally, the voting definition of the SchemaMatchingRule is used to aggregate the results to corespondences between attributes.

### Building Blocks for Matching

Internally, the MatchingEngine uses the algorithms from the de.uni_mannheim.informatik.wdi.matching.algorithms package. These algorithms are composed of different building blocks, which can be re-used for the implementation of further algorithms.
There are three types of building blocks: blockers, matching rules and aggregators:

**Blocker**: A blocker transforms one or multiple datasets into pairs of records. The main objective is to reduce the number of comparisons that have to be performed in the subsequent steps while still keeping the pairs that are actual matches. The blockers can receive correspondences as additional input. These can be used to perform the blocking and/or can be added to the generated pairs to be used by the following matcher.

**Matching Rule**: A matching rule is applied to a pair of records and determines if they are a match or not. A pair can contain additional correspondences that can be used by the matching rule to make its decision. There are two different types of matching rules:
- *filtering matching rule*: receives a pair of records and possibly many correspondences from the blocker and decides if this pair is a match, in which case it is produced as correspondence. Non-matching pairs are filtered out.
- *aggregable matching rule*: receives a pair of records and one of the correspondences from the blocker and decides if this pair is a match, in which case it is produced as correspondence. If multiple correspondences exist for a pair, it can create multiple output correspondences. The rule also specifies how such correspondences should be grouped for aggregation.

**Aggregator**: An aggregator combines multiple correspondences by aggregating their similarity scores. Such aggregations can be sum, average, voting or top-k.

These building blocks can be combined into two basic matcher architectures:

(1)	Rule-based Matching

![Rule-based Matching Process](/img/rule_based_matching.png)

(2)	Voting-based Matching

![Voting-based matching Process](/img/voting_based_matching.png)

### Implemented Building Blocks

**Blockers**:
-	*Standard Blocker*: Uses a blocking key generator to define record pairs. One or multiple blocking keys are generated for each record. All records with the same blocking key form a “block”. All possible pairs of records from the same block are created as result.
-	*Sorted Neighbourhood Method*: The records are sorted by their blocking key. Then, a sliding window is applied to the data and each record is paired with its neighbours.

**Matching Rules**:
-	*LinearCombinationMatchingRule* (FilteringMatchingRule): Applies a pre-defined set of comparators to the pair of records and calculates a linear combination of their scores as similarity value.
-	*VotingMatchingRule* (AggregableMatchingRule): Creates a correspondence for each causal correspondence provided by the blocker. In duplicate-based schema matching, the blocker creates the duplicates as causes for the attribute pairs and the voting rule then casts a vote for each duplicate.

**Aggregators**:
-	*CorrespondenceAggregator*: Aggregates the scores of correspondences in the same group. Possible Configurations: Sum, Count, Average, Voting
-	*TopKAggregator*: Filters out all but the k correspondences with the highest similarity per group.

## Data Fusion
The DataFusionEngine is initialised with a DataFusionStrategy. This strategy defines a ConflictResolutionFunction for each attribute. These functions determine how a final value is chosen from multiple possible values.

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
