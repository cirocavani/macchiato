Macchiato Kafka Sample
======================

http://kafka.apache.org/


Building Kafka
--------------

Building Kafka 0.8 with Scala 2.10.3.

1. Download `kafka-0.8.0-src.tgz`:

    [http://kafka.apache.org/downloads.html](http://kafka.apache.org/downloads.html)

        tar zxf kafka-0.8.0-src.tgz
        cd kafka-0.8.0-src

2. Apply Scala 2.10.3 patch:

    [https://github.com/ppurang/kafka/commit/dd72d83d5b5e65cb95bba157fdb91598fa8a508b](https://github.com/ppurang/kafka/commit/dd72d83d5b5e65cb95bba157fdb91598fa8a508b)

    core/build.sbt

        mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
          {
            case PathList("rootdoc.txt") => MergeStrategy.first
            case x => old(x)
          }
        }

    project/Build.scala

        -crossScalaVersions := Seq("2.8.0","2.8.2", "2.9.1", "2.9.2", "2.10.1"),
        +crossScalaVersions := Seq("2.8.0","2.8.2", "2.9.1", "2.9.2", "2.10.3"),
        -scalaVersion := "2.8.0",
        +scalaVersion := "2.10.3",

3. Build

        ./sbt update
        ./sbt package
        ./sbt assembly-package-dependency
        ./sbt release-tar

    Release `target/RELEASE/kafka_2.10.3-0.8.0.tar.gz`

4. Install

        tar zxf kafka_2.10.3-0.8.0.tar.gz
        cd kafka_2.10.3-0.8.0

...

Kafka Setup
-----------

From [http://kafka.apache.org/documentation.html#quickstart](http://kafka.apache.org/documentation.html#quickstart) (skip `Step 1`)

    bin/zookeeper-server-start.sh config/zookeeper.properties
    ...
    
    bin/kafka-server-start.sh config/server.properties
    ...
    
    bin/kafka-create-topic.sh --zookeeper localhost:2181 --replica 1 --partition 1 --topic test
    ...
