package com.navercorp.fixturemonkey.seed;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class SeedFileLoader {
	private static final String DEFAULT_SEED_FILE_NAME = ".fixture-monkey-seed";

	private SeedFileLoader() {
	}

	public static Long loadSeedFromFile() {
		return loadSeedFromFile(DEFAULT_SEED_FILE_NAME);
	}

	public static Long loadSeedFromFile(String fileName) {
		Path path = Paths.get(fileName);
		if (!Files.exists(path)) {
			return null;
		}
		try (BufferedReader reader = Files.newBufferedReader(path)) {
			String content = reader.readLine();
			if (content != null) {
				return Long.parseLong(content.trim());
			}
		} catch (IOException | NumberFormatException e) {
			return null;
		}
		return null;
	}
}
