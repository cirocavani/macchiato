Macchiato Kafka Sample
----------------------

http://kafka.apache.org/

*Apache Kafka is publish-subscribe messaging rethought as a distributed commit log.*


**Kafka Setup**

1. Download package `kafka_2.10-0.8.1.1.tgz` from:

    [http://kafka.apache.org/downloads.html](http://kafka.apache.org/downloads.html)
    
        tar zxf kafka_2.10-0.8.1.1.tgz
        cd kafka_2.10-0.8.1.1

2. Setup Kafka and ZooKeeper directories:

    nano -w config/server.properties
    
        ...
        log.dirs=/home/cavani/Service/kafka/data
        ...
        
    nano -w config/zookeeper.properties
    
        ...
        dataDir=/home/cavani/Service/kafka/zookeeper
        ...
        
3. Start Kafka and ZooKeeper:

From [http://kafka.apache.org/documentation.html#quickstart](http://kafka.apache.org/documentation.html#quickstart) (skip `Step 1`)

    bin/zookeeper-server-start.sh config/zookeeper.properties
    ...
    
    bin/kafka-server-start.sh config/server.properties
    ...
    
    bin/kafka-topics.sh --zookeeper localhost:2181 --create --replication-factor 1 --partition 1 --topic test
    ...
