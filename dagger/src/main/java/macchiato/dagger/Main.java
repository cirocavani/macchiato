package macchiato.dagger;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;

public final class Main {

	private Main() {
	}

	@Qualifier
	@Documented
	@Retention(RUNTIME)
	static @interface Config {
	}

	static class App {

		@Inject
		@Config
		Properties config;

		public void start() {
			System.out.println(config);
		}

	}

	@Module(injects = App.class)
	static class MainModule {

		@Provides
		@Singleton
		@Config
		Properties provideConfig() {
			final Properties config = new Properties();
			config.setProperty("x", "1");
			config.setProperty("y", "2");
			return config;
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
