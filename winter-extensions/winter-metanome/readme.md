# WInte.r - Metanome integration

This project integrates the WInte.r web tables data model with algorithms developed for the Metanome data profiling tool. 
Currently, this extensions supports the discovery of functional dependencies and approximate functional dependencies.

Contents:
- HyFD: The original HyFD algorithm. Packaging in the pom.xml was changed to exclude an older version of Lucene which conflicts with the version used in the WInte.r framework.
- tane approximate: The TANE algorithm implementation for Metanome. We added support for the calculation of approximate functional dependencies by changing the algorithm according to the original publication. Packaging in the pom.xml was changed to exclude an older version of Lucene which conflicts with the version used in the WInte.r framework.
- metanome integration: The actual extension. References the HyFD and tane approximate libraries and provides classes for interoperability between the WInte.r web tables data model and the Metanome algorithms

## Interoperability

The Metanome algorithms are designed to be generally applicable to various types of input data. 
However, we provide an input generator for data using the WInte.r web tables data model, which does not assume attribute names to be unique in a given table.

```de.uni_mannheim.informatik.dws.winter.webtables.metanome.WebTableFileInputGenerator```

## Dependency Issues

Metanome and WInte.r use different versions of Lucene, which can lead to runtime exceptions if the wrong version of Lucene is loaded. 
We hence provide versions of the Metanome algorithms which do not include this dependency in their jar.
If you still have issues with loading the correct version, try adding the WInte.r dependency *before* the WInte.r-Metanome dependency in your pom.xml file.