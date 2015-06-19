package macchiato.dagger;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import dagger.Component;
import dagger.Module;
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

		@Inject
		public App() {
		}

		public void start() {
			System.out.println(config);
		}

	}

	@Module
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

	@Singleton
	@Component(modules = MainModule.class)
	public interface Factory {

		App app();

	}

	public static void main(final String... args) throws Exception {
		System.out.println("Macchiato Dagger start...");

		final App app = DaggerMain_Factory.create().app();
		app.start();

		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				System.out.println("Macchiato Dagger shutdown.");
			}

		});
	}
}
