JPMML-PostgreSQL [![Build Status](https://travis-ci.org/jpmml/jpmml-postgresql.png?branch=master)](https://travis-ci.org/jpmml/jpmml-postgresql)
================

PMML evaluator library for the [PostgreSQL database] (http://www.postgresql.org/).

# Features #

* Full support for PMML specification versions 3.0 through 4.2. The evaluation is handled by the [JPMML-Evaluator] (https://github.com/jpmml/jpmml-evaluator) library.

# Prerequisites #

* PostgreSQL version 8.X or 9.X database with a [PL/Java add-on module] (https://github.com/tada/pljava).

# Overview #

A working JPMML-PostgreSQL setup consists of a library JAR file and a number of model JAR files. The library JAR file is centered around the utility class `org.jpmml.postgresql.PMMLUtil`, which provides PL/Java compliant utility methods for handling most common PMML evaluation scenarios. A model JAR file contains a model launcher class and a corresponding PMML resource. Model JAR files can be added, replaced and/or removed on the go using PL/Java SQL commands. All changes take effect immediately. There is no need to modify PostgreSQL database configuration or restart the service.

The main responsibility of a model launcher class is to formalize the "public interface" of a PMML resource. Every PL/Java function must be backed by a public static method that takes a PostgreSQL tuple as an argument, and returns either a PostgreSQL scalar or a PostgreSQL tuple as a result.

The example model JAR file contains a DecisionTree model for the "iris" dataset. The model launcher class `org.jpmml.postgresql.DecisionTreeIris` defines two functions. The first method `#evaluate(java.sql.ResultSet)` defines a function that returns the PMML target field ("Species") as a PostgreSQL character type. The second method `#evaluate(java.sql.ResultSet, java.sql.ResultSet)` defines a function that returns the PMML target field ("Species") together with four output fields ("Predicted_Species", "Probability_setosa", "Probability_versicolor", "Probability_virginica") as a PostgreSQL tuple. The installation and removal of functions is completely automated using an SQL deployment descriptor mechanism.

# Installation #

Enter the project root directory and build using [Apache Maven] (http://maven.apache.org/):
```
mvn clean install
```

The build produces two JAR files:
* `pmml-postgresql/target/pmml-postgresql-runtime-1.0-SNAPSHOT.jar` - Library uber-JAR file. It contains the classes of the library JAR file `pmml-postgresql/target/pmml-postgresql-1.0-SNAPSHOT.jar`, plus all the classes of its transitive dependencies.
* `pmml-postgresql-example/target/pmml-postgresql-example-1.0-SNAPSHOT.jar` - Example model JAR file.

# Usage #

### Library

##### Installation

Install the library uber-JAR file:
```sql
SELECT sqlj.install_jar('file:///tmp/pmml-postgresql-runtime-1.0-SNAPSHOT.jar', 'jpmml', false);
```

The PL/Java function `sqlj.install_jar` takes three arguments:

1. The URL of the JAR file.
2. A symbolic name after which this JAR file is later known for.
3. A flag indicating if this JAR file contains an executable SQL deployment descriptor ("BEGIN INSTALL ...").

Add the library uber-JAR file to the classpath of the target schema:
```sql
SELECT sqlj.set_classpath('public', 'jpmml');
```

##### Removal

Remove the library uber-JAR file:
```sql
SELECT sqlj.remove_jar('jpmml', false);
```

The PL/Java function `sqlj.remove_jar` takes two arguments:

1. The symbolic name of the JAR file.
2. A flag indicating if this JAR file contains an executable SQL deployment descriptor ("BEGIN REMOVE ...").

PL/Java examines all classpaths and propagates the removal if appropriate.

### Example model

##### Installation

Install the example model JAR file:
```sql
SELECT sqlj.install_jar('file:///tmp/pmml-postgresql-example-1.0-SNAPSHOT.jar', 'DecisionTreeIris', true);
```

Behind the scenes, the SQL deployment descriptor orders the creation of two composite types and two functions as follows:
```sql
CREATE TYPE iris_request AS (
	"Sepal_Length" double precision,
	"Sepal_Width" double precision,
	"Petal_Length" double precision,
	"Petal_Width" double precision
);
CREATE TYPE iris_response AS (
	"Species" varchar,
	"Predicted_Species" varchar,
	"Probability_setosa" double precision,
	"Probability_versicolor" double precision,
	"Probability_virginica" double precision
);
CREATE FUNCTION iris_species(iris_request) RETURNS varchar
	AS 'org.jpmml.postgresql.DecisionTreeIris.evaluate'
	LANGUAGE java;
CREATE FUNCTION iris_species("Sepal_Length" double precision, "Sepal_Width" double precision, "Petal_Length" double precision, "Petal_Width" double precision) RETURNS varchar
	AS 'org.jpmml.postgresql.DecisionTreeIris.evaluate'
	LANGUAGE java;
CREATE FUNCTION iris(iris_request) RETURNS iris_response
	AS 'org.jpmml.postgresql.DecisionTreeIris.evaluate'
	LANGUAGE java;
CREATE FUNCTION iris("Sepal_Length" double precision, "Sepal_Width" double precision, "Petal_Length" double precision, "Petal_Width" double precision) RETURNS iris_response
	AS 'org.jpmml.postgresql.DecisionTreeIris.evaluate'
	LANGUAGE java;
```

Add the example model JAR file to the classpath of the target schema. The classpath is constructed by concatenating the symbolic name of the library uber-JAR file with the symbolic names of model JAR files (using comma `:` as a path separator character): 
```sql
SELECT sqlj.set_classpath('public', 'jpmml:DecisionTreeIris');
```

##### Usage

Predicting the iris species:
```sql
SELECT iris_species(7, 3.2, 4.7, 1.4);
```

Result:
```
 iris_species 
--------------
 versicolor
```

Predicting the iris species together with the calculated probabilities for each target category:
```sql
SELECT (iris(7, 3.2, 4.7, 1.4)).*;
```

Result:
```
  Species   | Predicted_Species | Probability_setosa | Probability_versicolor | Probability_virginica 
------------+-------------------+--------------------+------------------------+-----------------------
 versicolor | versicolor        |                  0 |               0.907407 |             0.0925926
```

##### Removal

Remove the example model JAR file:
```sql
SELECT sqlj.remove_jar('DecisionTreeIris', true);
```

Behind the scenes, the SQL deployment descriptor orders the deletion of two composite types and two functions as follows:
```sql
DROP FUNCTION iris(double precision, double precision, double precision, double precision);
DROP FUNCTION iris(iris_request);
DROP FUNCTION iris_species(double precision, double precision, double precision, double precision);
DROP FUNCTION iris_species(iris_request);
DROP TYPE iris_response;
DROP TYPE iris_request;
```

# License #

JPMML-PostgreSQL is dual-licensed under the [GNU Affero General Public License (AGPL) version 3.0] (http://www.gnu.org/licenses/agpl-3.0.html) and a commercial license.

# Additional information #

Please contact [info@openscoring.io] (mailto:info@openscoring.io)