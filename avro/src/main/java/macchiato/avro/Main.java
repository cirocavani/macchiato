package macchiato.avro;

import static macchiato.avro.Main.MapBuilder.newMap;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.file.SeekableByteArrayInput;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

public final class Main {

	private Main() {
	}

	static class MapBuilder {
		
		final Map<CharSequence, CharSequence> map;
		
		private MapBuilder() {
			map = new LinkedHashMap<>();
		}
	
		MapBuilder set(CharSequence key, CharSequence value) {
			map.put(key, value);
			return this;
		}
		
		Map<CharSequence, CharSequence> build() {
			return Collections.unmodifiableMap(map);
		}
		
		static MapBuilder newMap() {
			return new MapBuilder();
		}
	}
	
	public static void main(final String... args) throws Exception {
		System.out.println("Macchiato Avro start...");

		Particle p1 = Particle.newBuilder()
				.setPrincipal("Someone")
				.setOperation("star")
				.setSource("github")
				.setTimestamp(System.currentTimeMillis())
				.setAttributes(newMap()
						.set("url", "https://github.com/cirocavani/macchiato")
						.build())
				.build();
		
		ByteArrayOutputStream raw = new ByteArrayOutputStream();

		DatumWriter<Particle> writer = new SpecificDatumWriter<>(Particle.class);
		DataFileWriter<Particle> out = new DataFileWriter<>(writer);
		out.create(p1.getSchema(), raw);
		out.append(p1);
		out.close();

		System.out.println(raw.size());

		SeekableByteArrayInput stream = new SeekableByteArrayInput(raw.toByteArray());

		DatumReader<Particle> reader = new SpecificDatumReader<>(Particle.class);
		DataFileReader<Particle> in = new DataFileReader<>(stream, reader);
		while (in.hasNext()) {
			Particle p = in.next();
			System.out.println(p);
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				System.out.println("Macchiato Avro shutdown...");
			}

		});
	}
}
