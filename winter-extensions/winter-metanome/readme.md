# WInte.r - Metanome integration

This project integrates the WInte.r web tables data model with algorithms developed for the [Metanome data profiling tool](https://hpi.de/naumann/projects/data-profiling-and-analytics/metanome-data-profiling.html). 
Currently, this extensions supports the discovery of functional dependencies and approximate functional dependencies.

Contents:
- HyFD: The original [HyFD algorithm](https://github.com/HPI-Information-Systems/metanome-algorithms/tree/master/HyFD) [1]. Packaging in the pom.xml was changed to exclude an older version of Lucene which conflicts with the version used in the WInte.r framework.
- tane approximate: The [TANE algorithm implementation for Metanome](https://github.com/HPI-Information-Systems/metanome-algorithms/tree/master/tane) [2]. We added support for the calculation of approximate functional dependencies by changing the algorithm according to the original publication [3]. Packaging in the pom.xml was changed to exclude an older version of Lucene which conflicts with the version used in the WInte.r framework.
- metanome integration: The actual extension. References the HyFD and tane approximate libraries and provides classes for interoperability between the WInte.r web tables data model and the Metanome algorithms

## Interoperability

The Metanome algorithms are designed to be generally applicable to various types of input data. 
However, we provide an input generator for data using the WInte.r web tables data model, which does not assume attribute names to be unique in a given table.

```de.uni_mannheim.informatik.dws.winter.webtables.metanome.WebTableFileInputGenerator```

## Dependency Issues

Metanome and WInte.r use different versions of Lucene, which can lead to runtime exceptions if the wrong version of Lucene is loaded. 
We hence provide versions of the Metanome algorithms which do not include this dependency in their jar.
If you still have issues with loading the correct version, try adding the WInte.r dependency *before* the WInte.r-Metanome dependency in your pom.xml file.

## References

[1] Papenbrock, Thorsten, and Felix Naumann. "A hybrid approach to functional dependency discovery." Proceedings of the 2016 International Conference on Management of Data. ACM, 2016.

[2] Papenbrock, Thorsten, et al. "Functional dependency discovery: An experimental evaluation of seven algorithms." Proceedings of the VLDB Endowment 8.10 (2015): 1082-1093.

[3] Huhtala, Yka, et al. "TANE: An efficient algorithm for discovering functional and approximate dependencies." The computer journal 42.2 (1999): 100-111.
