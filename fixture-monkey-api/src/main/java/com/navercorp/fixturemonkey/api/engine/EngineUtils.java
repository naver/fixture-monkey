package com.navercorp.fixturemonkey.api.engine;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * The {@link EngineUtils} class provides utility methods for engine.
 * Engine is a library that provides a way to generate random values.
 */
@API(since = "1.1.9", status = Status.EXPERIMENTAL)
public abstract class EngineUtils {
	private static final boolean USE_JQWIK_ENGINE;
	private static final boolean USE_KOTEST_ENGINE;

	static {
		boolean useJqwikEngine;
		boolean useKotestEngine;
		try {
			Class.forName("net.jqwik.engine.SourceOfRandomness");
			useJqwikEngine = true;
		} catch (ClassNotFoundException e) {
			useJqwikEngine = false;
		}
		USE_JQWIK_ENGINE = useJqwikEngine;

		try {
			Class.forName("io.kotest.property.Arb");
			useKotestEngine = true;
		} catch (ClassNotFoundException e) {
			useKotestEngine = false;
		}
		USE_KOTEST_ENGINE = useKotestEngine;
	}

	public static boolean useJqwikEngine() {
		return USE_JQWIK_ENGINE;
	}

	public static boolean useKotestEngine() {
		return USE_KOTEST_ENGINE;
	}
}
