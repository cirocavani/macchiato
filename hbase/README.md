Macchiato HBase Sample
----------------------

http://hbase.apache.org/

*Apache HBase is an open-source, distributed, versioned, non-relational database modeled after Google's Bigtable.*

**HBase Setup**

From [http://hbase.apache.org/book.html#quickstart](http://hbase.apache.org/book.html#quickstart)

1. Download package `hbase-0.98.1-hadoop2-bin.tar.gz` from:

    [http://www.apache.org/dyn/closer.cgi/hbase/](http://www.apache.org/dyn/closer.cgi/hbase/)
    
        tar zxf hbase-0.98.1-hadoop2-bin.tar.gz
        cd hbase-0.98.1-hadoop2

2. Setup HBase and ZooKeeper directories:

    nano -w conf/hbase-site.xml
    
        <configuration>
          <property>
            <name>hbase.rootdir</name>
            <value>file:///home/cavani/Service/hbase/data</value>
          </property>
          <property>
            <name>hbase.zookeeper.property.dataDir</name>
            <value>/home/cavani/Service/hbase/zookeeper</value>
          </property>
        </configuration>

3. Setup Java and IPv4:

    nano -w conf/hbase-env.sh

        export JAVA_HOME=/home/cavani/Software/jdk1.8.0_05/
        ...
        export HBASE_OPTS="-XX:+UseConcMarkSweepGC -Djava.net.preferIPv4Stack=true"
        ...

4. Start HBase + ZooKeeper:

	(Start / Stop)

        bin/start-hbase.sh
        ...
        bin/stop-hbase.sh
        ...
    
    (Verification)
        
        netstat -nplt | grep java
        ... 0.0.0.0:2181 ...
        ... 0.0.0.0:60010 ...
        ...
        tail -f logs/hbase-cavani-master-cavani-workstation.log
        ...

     (Web UI)
     
        (master)
        http://127.0.0.1:60010/master-status
        
        (region server)
        http://127.0.0.1:56225/rs-status
