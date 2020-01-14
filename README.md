# Using Apache Spark with IPL Serialization

This branch contains modification that allow running Spark with serialization from a modified version of the [IPL project](https://github.com/dadepo/ipl)

This version has a dependency on the modified IPL, which can be see in the `pom.xml` as:

```
      <dependency>
          <groupId>ipl-spark</groupId>
          <artifactId>ibis-spark-io</artifactId>
          <version>2.3.3</version>
      </dependency>

      <dependency>
          <groupId>ipl-spark</groupId>
          <artifactId>ibis-spark-util</artifactId>
          <version>2.3.3</version>
      </dependency>
```

That means to build and use this version of Spark, you should have these dependencies installed in your
local maven repository. Please consult the documentation [here](https://github.com/dadepo/ipl/tree/handle-scala-options) on how 
to locally install these IPL jars.

Once the IPL jars are available, you can follow the steps below on how to build this modified version of 
Spark and run some of the example applications.

## Building 

- Ensure you have maven installed
- Ensure you are running with JDK 8 (TODO revisit and see how requirement can be removed)
- Ensure you have the modified IPL jars in your local repository
- Run the following command to build the spark-ibis-core `mvn -Dmaven.test.skip=true -pl core clean install`
- Run the following command to build the spark-ibis-sql `mvn -Dmaven.test.skip=true -pl sql/core clean install`
- These commands would generate `spark-ibis-sql_2.11-2.4.4.jar` and `spark-ibis-core_2.11-2.4.4.jar` 
in your local maven repository

## Running example applications

Follow the instructions on the [Spark overview page](https://spark.apache.org/docs/latest/#spark-overview) to download
Spark distribution and run example applications.

To integrate the modified version of Spark follow the following steps:

- Delete `spark-core-<version>.jar` from the `jars` directory
- Delete `spark-sql-<version>.jar` from the `jars` directory
- Copy the `spark-ibis-core-<version>.jar` generated in previous step, from the local maven repo to `jars` directory
- Copy the `spark-ibis-sql-<version>.jar` generated in previous step, from the local maven repo to to `jars` directory
- Copy the `spark-ibis-sql-<version>.jar` generated in previous step, from the local maven repo to to `jars` directory
- Copy the `ibis-spark-io-<version>.jar` from the local maven repo to to `jars` directory
- Copy the `ibis-spark-util--<version>.jar` from the local maven repo to to `jars` directory

After the above steps, you can now proceed to run the examples. eg:

```
./bin/run-example SparkPi 10
```

# Apache Spark

Spark is a fast and general cluster computing system for Big Data. It provides
high-level APIs in Scala, Java, Python, and R, and an optimized engine that
supports general computation graphs for data analysis. It also supports a
rich set of higher-level tools including Spark SQL for SQL and DataFrames,
MLlib for machine learning, GraphX for graph processing,
and Spark Streaming for stream processing.

<http://spark.apache.org/>


## Online Documentation

You can find the latest Spark documentation, including a programming
guide, on the [project web page](http://spark.apache.org/documentation.html).
This README file only contains basic setup instructions.

## Building Spark

Spark is built using [Apache Maven](http://maven.apache.org/).
To build Spark and its example programs, run:

    build/mvn -DskipTests clean package

(You do not need to do this if you downloaded a pre-built package.)

You can build Spark using more than one thread by using the -T option with Maven, see ["Parallel builds in Maven 3"](https://cwiki.apache.org/confluence/display/MAVEN/Parallel+builds+in+Maven+3).
More detailed documentation is available from the project site, at
["Building Spark"](http://spark.apache.org/docs/latest/building-spark.html).

For general development tips, including info on developing Spark using an IDE, see ["Useful Developer Tools"](http://spark.apache.org/developer-tools.html).

## Interactive Scala Shell

The easiest way to start using Spark is through the Scala shell:

    ./bin/spark-shell

Try the following command, which should return 1000:

    scala> sc.parallelize(1 to 1000).count()

## Interactive Python Shell

Alternatively, if you prefer Python, you can use the Python shell:

    ./bin/pyspark

And run the following command, which should also return 1000:

    >>> sc.parallelize(range(1000)).count()

## Example Programs

Spark also comes with several sample programs in the `examples` directory.
To run one of them, use `./bin/run-example <class> [params]`. For example:

    ./bin/run-example SparkPi

will run the Pi example locally.

You can set the MASTER environment variable when running examples to submit
examples to a cluster. This can be a mesos:// or spark:// URL,
"yarn" to run on YARN, and "local" to run
locally with one thread, or "local[N]" to run locally with N threads. You
can also use an abbreviated class name if the class is in the `examples`
package. For instance:

    MASTER=spark://host:7077 ./bin/run-example SparkPi

Many of the example programs print usage help if no params are given.

## Running Tests

Testing first requires [building Spark](#building-spark). Once Spark is built, tests
can be run using:

    ./dev/run-tests

Please see the guidance on how to
[run tests for a module, or individual tests](http://spark.apache.org/developer-tools.html#individual-tests).

There is also a Kubernetes integration test, see resource-managers/kubernetes/integration-tests/README.md

## A Note About Hadoop Versions

Spark uses the Hadoop core library to talk to HDFS and other Hadoop-supported
storage systems. Because the protocols have changed in different versions of
Hadoop, you must build Spark against the same version that your cluster runs.

Please refer to the build documentation at
["Specifying the Hadoop Version and Enabling YARN"](http://spark.apache.org/docs/latest/building-spark.html#specifying-the-hadoop-version-and-enabling-yarn)
for detailed guidance on building for a particular distribution of Hadoop, including
building for particular Hive and Hive Thriftserver distributions.

## Configuration

Please refer to the [Configuration Guide](http://spark.apache.org/docs/latest/configuration.html)
in the online documentation for an overview on how to configure Spark.

## Contributing

Please review the [Contribution to Spark guide](http://spark.apache.org/contributing.html)
for information on how to get started contributing to the project.
