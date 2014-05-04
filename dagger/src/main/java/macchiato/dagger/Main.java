package macchiato.dagger;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;

public final class Main {

	private Main() {
	}

	static class Config {

		void dump() {
			System.out.println("x=y");
		}
	}

	static class App {

		@Inject
		Config config;

		public void start() {
			config.dump();
		}

	}

	@Module(injects = App.class)
	static class MainModule {

		@Provides
		@Singleton
		Config provideConfig() {
			return new Config();
		}

	}

	public static void main(final String... args) throws Exception {
		System.out.println("Macchiato Dagger start...");

		final ObjectGraph objectGraph = ObjectGraph.create(new MainModule());
		final App app = objectGraph.get(App.class);
		app.start();

		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				System.out.println("Macchiato Dagger shutdown...");
			}

		});
	}
}
