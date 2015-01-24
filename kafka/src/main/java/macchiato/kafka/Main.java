package macchiato.kafka;

import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.consumer.TopicFilter;
import kafka.consumer.Whitelist;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import macchiato.kafka.Main.KafkaSubscriber.MessageHandler;

public final class Main {

	private Main() {
	}

	static class KafkaPublisher implements AutoCloseable {

		private final Properties props;

		private Producer<String, byte[]> kafka;

		public KafkaPublisher(final String brokers) {
			props = new Properties();
			props.put("metadata.broker.list", brokers);
			props.put("request.required.acks", "1");
			props.put("producer.type", "async");
		}

		public KafkaPublisher(final Properties props) {
			this.props = (Properties) props.clone();
		}

		public boolean isStarted() {
			return kafka != null;
		}

		public void start() {
			if (isStarted()) {
				return;
			}

			final ProducerConfig config = new ProducerConfig(props);
			kafka = new Producer<>(config);
		}

		@Override
		public void close() throws Exception {
			if (!isStarted()) {
				return;
			}
			try {
				kafka.close();
			} finally {
				kafka = null;
			}
		}

		public void send(final String topic, final byte[] message) {
			if (!isStarted()) {
				return;
			}
			kafka.send(new KeyedMessage<String, byte[]>(topic, message));
		}
	}

	static class KafkaSubscriber implements AutoCloseable {

		static interface MessageHandler {

			void process(byte[] message);

		}

		private final Properties props;

		private ConsumerConnector connector;
		private ExecutorService executor;

		public KafkaSubscriber(final String zookeeper, final String group) {
			props = new Properties();
			props.put("zookeeper.connect", zookeeper);
			props.put("group.id", group);
		}

		public KafkaSubscriber(final Properties props) {
			this.props = (Properties) props.clone();
		}

		public boolean isStarted() {
			return connector != null;
		}

		public void start(final String topic, final MessageHandler handler) {
			if (isStarted()) {
				return;
			}

			final ConsumerConfig config = new ConsumerConfig(props);
			connector = Consumer.createJavaConsumerConnector(config);

			final TopicFilter filter = new Whitelist(topic);
			final List<KafkaStream<byte[], byte[]>> streams = connector.createMessageStreamsByFilter(filter);

			executor = Executors.newFixedThreadPool(streams.size());

			for (final KafkaStream<byte[], byte[]> stream : streams) {
				executor.submit(new Runnable() {
					@Override
					public void run() {
						final ConsumerIterator<byte[], byte[]> it = stream.iterator();
						while (it.hasNext()) {
							final byte[] raw = it.next().message();
							handler.process(raw);
						}
					}
				});
			}

			executor.shutdown();
		}

		@Override
		public void close() throws Exception {
			if (!isStarted()) {
				return;
			}
			try {
				connector.shutdown();
			} finally {
				connector = null;
			}
			try {
				executor.awaitTermination(10, TimeUnit.SECONDS);
			} finally {
				executor = null;
			}
		}

	}

	public static void main(final String... args) throws Exception {
		System.out.println("Macchiato Kafka start...");

		final String brokers = "127.0.0.1:9092";
		final String zookeeper = "127.0.0.1:2181";
		final String topic = "test";
		final int n = 100;

		final KafkaSubscriber sub = new KafkaSubscriber(zookeeper, "group.test");
		sub.start(topic, new MessageHandler() {

			@Override
			public void process(final byte[] message) {
				System.out.printf("Receiving: '%s'\n", new String(message));
			}

		});

		try (final KafkaPublisher pub = new KafkaPublisher(brokers)) {
			pub.start();
			final Random rnd = new Random();
			for (int i = 0; i < n; ++i) {
				final String m = String.valueOf(i + 1);
				System.out.printf("Sending: '%s'\n", m);
				pub.send(topic, m.getBytes());
				Thread.sleep(rnd.nextInt(1000));
			}
		}

		Thread.sleep(5000);

		sub.close();

		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				System.out.println("Macchiato Kafka shutdown.");
			}

		});
	}
}
