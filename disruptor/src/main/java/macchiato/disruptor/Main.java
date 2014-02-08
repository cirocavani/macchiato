package macchiato.disruptor;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;

public final class Main {

	private Main() {
	}

	static interface WorkerFactory<IN, OUT> {

		Worker<IN, OUT> newInstance();

	}

	static interface Worker<IN, OUT> {

		void process(IN event, EngineAdapter<OUT> broker);

	}

	static interface HandlerFactory<T> {

		WorkHandler<T> newInstance();

	}

	static interface EngineAdapter<T> {

		void publish(EventTranslator<T> adapter);

	}

	static class Engine<T> implements EngineAdapter<T>, AutoCloseable {

		final int bufferSize;
		final int handlers;
		final EventFactory<T> valueFactory;
		final HandlerFactory<T> handlerFactory;

		private ExecutorService executor;
		private Disruptor<T> broker;

		public Engine(final int bufferSize, final int handlers, final EventFactory<T> valueFactory, final HandlerFactory<T> handlerFactory) {
			this.bufferSize = bufferSize;
			this.handlers = handlers;
			this.valueFactory = valueFactory;
			this.handlerFactory = handlerFactory;
		}

		public void start() {
			executor = Executors.newCachedThreadPool();
			broker = new Disruptor<>(valueFactory, bufferSize, executor);

			@SuppressWarnings("unchecked")
			final WorkHandler<T>[] workerPool = new WorkHandler[handlers];
			for (int i = 0; i < handlers; ++i) {
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

		private int bufferSize;
		private EventFactory<T> valueFactory;
		private int handlers;
		private HandlerFactory<T> handlerFactory;

		public EngineBuilder<T> buffer(final int bufferSize, final EventFactory<T> factory) {
			this.bufferSize = bufferSize;
			this.valueFactory = factory;
			return this;
		}

		public EngineBuilder<T> handlers(final int handlers, final HandlerFactory<T> factory) {
			this.handlers = handlers;
			this.handlerFactory = factory;
			return this;
		}

		public Engine<T> build() {
			final Engine<T> engine = new Engine<>(bufferSize, handlers, valueFactory, handlerFactory);
			engine.start();
			return engine;
		}

	}

	static <T> EventFactory<T> value(final Class<T> type) {
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

	static <IN, OUT> WorkerFactory<IN, OUT> worker(final Class<? extends Worker<IN, OUT>> type) {
		return new WorkerFactory<IN, OUT>() {

			@Override
			public Worker<IN, OUT> newInstance() {
				try {
					return type.newInstance();
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}
			}

		};
	}

	static <IN, OUT> HandlerFactory<IN> pipe(final Class<IN> type, final Class<? extends Worker<IN, OUT>> work, final EngineAdapter<OUT> out) {
		return pipe(type, worker(work), out);
	}

	static <IN, OUT> HandlerFactory<IN> pipe(final Class<IN> type, final WorkerFactory<IN, OUT> factory, final EngineAdapter<OUT> out) {
		return new HandlerFactory<IN>() {
			int i = 0;

			@Override
			public WorkHandler<IN> newInstance() {
				final int n = ++i;
				final Worker<IN, OUT> worker = factory.newInstance();
				return new WorkHandler<IN>() {

					@Override
					public void onEvent(final IN event) throws Exception {
						System.out.println(type.getSimpleName() + " " + n + ": receiving " + event);
						worker.process(event, out);
					}

				};
			}
		};
	}

	static <T> Engine<T> createEngine(final Class<T> type, final int bufferSize, final int handlers, final HandlerFactory<T> handlerFactory) {
		final EngineBuilder<T> engine = new EngineBuilder<>();
		engine.buffer(bufferSize, value(type));
		engine.handlers(handlers, handlerFactory);
		return engine.build();
	}

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

		class DataProcessor implements Worker<RawData, ModelData> {

			@Override
			public void process(final RawData raw, final EngineAdapter<ModelData> broker) {
				// Some work!
				final String result = new String(raw.bytes);
				broker.publish(new EventTranslator<ModelData>() {

					@Override
					public void translateTo(final ModelData event, final long sequence) {
						event.content = result;
					}

				});
			}

		}

		class DataStorage implements Worker<ModelData, Object> {

			@Override
			public void process(final ModelData event, final EngineAdapter<Object> broker) {
				// noop
			}

		}

		final int rawBufferSize = 1024;
		final int rawWorkerSize = 10;

		final int dataBufferSize = 128;
		final int dataWorkerSize = 10;

		final Engine<ModelData> data = createEngine(ModelData.class, dataBufferSize, dataWorkerSize, pipe(ModelData.class, DataStorage.class, null));
		final Engine<RawData> raw = createEngine(RawData.class, rawBufferSize, rawWorkerSize, pipe(RawData.class, DataProcessor.class, data));

		for (int i = 0; i < 1000 * 1000; ++i) {
			final int n = i;
			System.out.println("Publishing " + n);
			raw.publish(new EventTranslator<RawData>() {

				@Override
				public void translateTo(final RawData event, final long sequence) {
					event.bytes = (n + " (" + sequence + ")").getBytes();
				}

			});
		}

		raw.close();
		data.close();

		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				System.out.println("Macchiato Disruptor shutdown...");
			}

		});
	}
}
