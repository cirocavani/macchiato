package macchiato.hbase;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

public final class Main {

	private Main() {
	}

	public static void main(final String... args) throws Exception {
		System.out.println("Macchiato HBase start...");

		final String zookeeper = "127.0.0.1:2181";
		final TableName tablename = TableName.valueOf("test");
		final byte[] family = Bytes.toBytes("m");

		final Configuration config = HBaseConfiguration.create();
		config.set("hbase.zookeeper.quorum", zookeeper);

		final Connection connection = ConnectionFactory.createConnection(config);

		final Admin admin = connection.getAdmin();
		if (admin.tableExists(tablename)) {
			dropTable(admin, tablename);
		}
		createTable(admin, tablename, family);
		admin.close();

		final Table table = connection.getTable(tablename);

		{
			System.out.println("Storing data...");

			final byte[] rowkey = Bytes.toBytes("identifier");
			final Map<byte[], byte[]> keyValues = new LinkedHashMap<>();
			keyValues.put(Bytes.toBytes("a"), Bytes.toBytes("1"));
			keyValues.put(Bytes.toBytes("b"), Bytes.toBytes("2"));

			final Put op = new Put(rowkey);
			for (final Map.Entry<byte[], byte[]> m : keyValues.entrySet()) {
				op.addColumn(family, m.getKey(), m.getValue());
			}
			table.put(op);

			System.out.println("Store done.");
		}

		{
			System.out.println("Fetching data...");

			final byte[] rowkey = Bytes.toBytes("identifier");
			final Get op = new Get(rowkey);
			op.addFamily(family);

			final Result result = table.get(op);
			final Map<byte[], byte[]> keyValues = result.getFamilyMap(family);
			for (final Map.Entry<byte[], byte[]> m : keyValues.entrySet()) {
				System.out.println(new String(m.getKey()) + "=" + new String(m.getValue()));
			}

			System.out.println("Fetch done.");
		}

		{
			System.out.println("Loading data...");

			final byte[] start = Bytes.toBytes("identifier");
			final Scan op = new Scan(start);
			op.addFamily(family);

			final ResultScanner result = table.getScanner(op);
			final Iterator<Result> i = result.iterator();
			while (i.hasNext()) {
				final Result r = i.next();
				System.out.println(new String(r.getRow()));
				final Map<byte[], byte[]> keyValues = r.getFamilyMap(family);
				for (final Map.Entry<byte[], byte[]> m : keyValues.entrySet()) {
					System.out.println("  " + new String(m.getKey()) + "=" + new String(m.getValue()));
				}
			}
			result.close();

			System.out.println("Load done.");
		}

		table.close();

		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				System.out.println("Macchiato HBase shutdown.");
			}

		});
	}

	static void dropTable(final Admin admin, final TableName name) throws IOException {
		System.out.println("Dropping table: " + name);
		if (admin.isTableEnabled(name)) {
			admin.disableTable(name);
		}
		admin.deleteTable(name);
		System.out.println("Drop done: " + name);
	}

	static void createTable(final Admin admin, final TableName name, final byte[] family) throws IOException {
		System.out.println("Creating table: " + name);
		final HTableDescriptor table = new HTableDescriptor(name);

		final HColumnDescriptor m = new HColumnDescriptor(family);
		m.setMaxVersions(1);

		table.addFamily(m);

		admin.createTable(table);
		System.out.println("Create done: " + name);
	}
}
