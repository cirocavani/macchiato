package macchiato.disruptor;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;

public final class Main {

	private Main() {
	}

	static interface WorkContext {

		<T> void forward(EventTranslator<T> adapter);

	}

	static interface WorkerFactory<T> {

		Worker<T> newInstance();

	}

	static interface Worker<T> {

		void process(T event, WorkContext context);

	}

	static interface HandlerFactory<T> {

		WorkHandler<T> newInstance();

	}

	static interface EngineAdapter<T> {

		void publish(EventTranslator<T> adapter);

	}

	static class DefaultContext<T> implements WorkContext {

		private final EngineAdapter<T> broker;

		public DefaultContext(final EngineAdapter<T> broker) {
			this.broker = broker;
		}

		@Override
		public <U> void forward(final EventTranslator<U> adapter) {
			@SuppressWarnings("unchecked")
			final EventTranslator<T> event = (EventTranslator<T>) adapter;
			broker.publish(event);
		}

	}

	static class Engine<T> implements EngineAdapter<T>, AutoCloseable {

		final int bufferSize;
		final int workers;
		final EventFactory<T> valueFactory;
		final HandlerFactory<T> handlerFactory;

		private ExecutorService executor;
		private Disruptor<T> broker;

		public Engine(final int bufferSize, final int workers, final EventFactory<T> valueFactory, final HandlerFactory<T> handlerFactory) {
			this.bufferSize = bufferSize;
			this.workers = workers;
			this.valueFactory = valueFactory;
			this.handlerFactory = handlerFactory;
		}

		public void start() {
			executor = Executors.newCachedThreadPool();
			broker = new Disruptor<>(valueFactory, bufferSize, executor);

			@SuppressWarnings("unchecked")
			final WorkHandler<T>[] workerPool = new WorkHandler[workers];
			for (int i = 0; i < workers; ++i) {
				workerPool[i] = handlerFactory.newInstance();
			}
			broker.handleEventsWithWorkerPool(workerPool);
			broker.start();
		}

		@Override
		public void close() {
			broker.shutdown();
			executor.shutdown();
		}

		@Override
		public void publish(final EventTranslator<T> adapter) {
			broker.publishEvent(adapter);
		}

	}

	static class EngineBuilder<T> {

		private final Class<T> type;

		private int bufferSize;
		private EventFactory<T> valueFactory;
		private int workers;
		private HandlerFactory<T> handlerFactory;

		private EngineBuilder(final Class<T> type) {
			this.type = type;
		}

		public static <U> EngineBuilder<U> newEngine(final Class<U> type) {
			return new EngineBuilder<>(type);
		}

		public EngineBuilder<T> buffer(final int bufferSize) {
			this.bufferSize = bufferSize;
			this.valueFactory = value(type);
			return this;
		}

		public EngineBuilder<T> buffer(final int bufferSize, final EventFactory<T> factory) {
			this.bufferSize = bufferSize;
			this.valueFactory = factory;
			return this;
		}

		public EngineBuilder<T> workers(final int workers, final Class<? extends Worker<T>> workerType, final WorkContext context) {
			this.workers = workers;
			this.handlerFactory = worker(type, workerType, context);
			return this;
		}

		public EngineBuilder<T> workers(final int workers, final WorkerFactory<T> factory, final WorkContext context) {
			this.workers = workers;
			this.handlerFactory = worker(type, factory, context);
			return this;
		}

		public EngineBuilder<T> workers(final int workers, final HandlerFactory<T> factory) {
			this.workers = workers;
			this.handlerFactory = factory;
			return this;
		}

		public Engine<T> build() {
			return new Engine<>(bufferSize, workers, valueFactory, handlerFactory);
		}

		private static <T> EventFactory<T> value(final Class<T> type) {
			return new EventFactory<T>() {

				@Override
				public T newInstance() {
					try {
						return type.newInstance();
					} catch (final Exception e) {
						throw new RuntimeException(e);
					}
				}

			};
		}

		private static <T> WorkerFactory<T> worker(final Class<? extends Worker<T>> type) {
			return new WorkerFactory<T>() {

				@Override
				public Worker<T> newInstance() {
					try {
						return type.newInstance();
					} catch (final Exception e) {
						throw new RuntimeException(e);
					}
				}

			};
		}

		private static <T> HandlerFactory<T> worker(final Class<T> type, final Class<? extends Worker<T>> workerType, final WorkContext context) {
			return worker(type, worker(workerType), context);
		}

		private static <T> HandlerFactory<T> worker(final Class<T> type, final WorkerFactory<T> factory, final WorkContext context) {
			return new HandlerFactory<T>() {
				int i = 0;

				@Override
				public WorkHandler<T> newInstance() {
					final int n = ++i;
					final Worker<T> worker = factory.newInstance();
					return new WorkHandler<T>() {

						@Override
						public void onEvent(final T event) throws Exception {
							System.out.println(type.getSimpleName() + " " + n + ": receiving " + event);
							worker.process(event, context);
						}

					};
				}
			};
		}
	}

	static long procTime = 0;
	static long storeTime = 0;
	static long tripTime = 0;

	public static void main(final String... args) throws Exception {
		System.out.println("Macchiato Disruptor start...");

		class RawData {
			byte[] bytes;

			@Override
			public String toString() {
				return Arrays.toString(bytes);
			}
		}

		class ModelData {
			String content;

			@Override
			public String toString() {
				return content;
			}
		}

		class DataProcessor implements Worker<RawData> {

			final Random rnd = new Random();

			@Override
			public void process(final RawData raw, final WorkContext context) {
				// Some work!
				try {
					final int t = rnd.nextInt(200);
					procTime += t;
					System.out.println("Processing will take " + t + "ms");
					Thread.sleep(t);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
				final String result = new String(raw.bytes);
				context.forward(new EventTranslator<ModelData>() {

					@Override
					public void translateTo(final ModelData event, final long sequence) {
						event.content = result;
					}

				});
			}

		}

		class DataStorage implements Worker<ModelData> {

			final Random rnd = new Random();

			@Override
			public void process(final ModelData event, final WorkContext context) {
				final String m = event.content;
				final int i = m.indexOf(',');
				final long t = Long.valueOf(m.substring(i + 1));
				tripTime += System.currentTimeMillis() - t;

				// some io bound op!
				try {
					final int ts = rnd.nextInt(1000);
					storeTime += ts;
					System.out.println("Storing will take " + ts + "ms");
					Thread.sleep(ts);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			}

		}

		final int processorBufferSize = 128;
		final int processorWorkerSize = 20;

		final int storageBufferSize = 128;
		final int storageWorkerSize = 20;

		final EngineBuilder<ModelData> storageBuilder = EngineBuilder.newEngine(ModelData.class);
		storageBuilder.buffer(storageBufferSize).workers(storageWorkerSize, DataStorage.class, null);
		final Engine<ModelData> storage = storageBuilder.build();

		final EngineBuilder<RawData> processorBuilder = EngineBuilder.newEngine(RawData.class);
		processorBuilder.buffer(processorBufferSize).workers(processorWorkerSize, DataProcessor.class, new DefaultContext<>(storage));
		final Engine<RawData> processor = processorBuilder.build();

		storage.start();
		processor.start();

		final long t = System.currentTimeMillis();
		final int k = 1_000;

		for (int i = 0; i < k; ++i) {
			final int n = i;
			System.out.println("Publishing " + n);
			final String m = n + "," + Long.toString(System.currentTimeMillis());
			final byte[] raw = m.getBytes();

			processor.publish(new EventTranslator<RawData>() {

				@Override
				public void translateTo(final RawData event, final long sequence) {
					event.bytes = raw;
				}

			});
		}

		processor.close();
		storage.close();

		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				final double p = procTime / 1000D;
				final double s = storeTime / 1000D;
				final double m = tripTime / 1000D / k;
				System.out.printf("Time: %,.3fs, proc: %,.3fs, store: %,.3fs, mean: %,.3fs\n", (System.currentTimeMillis() - t) / 1000D, p, s, m);
				System.out.println("Macchiato Disruptor shutdown.");
			}

		});
	}
}
