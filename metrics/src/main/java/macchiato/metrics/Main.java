package macchiato.metrics;

public final class Main {

	private Main() {
	}

	public static void main(final String... args) throws Exception {
		System.out.println("Macchiato Metrics start...");

		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				System.out.println("Macchiato Metrics shutdown.");
			}

		});
	}
}
