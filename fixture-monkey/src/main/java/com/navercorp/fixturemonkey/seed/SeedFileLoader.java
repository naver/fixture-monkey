/*
 * Fixture Monkey
 *
 * Copyright (c) 2021–present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.fixturemonkey.seed;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jspecify.annotations.Nullable;

public final class SeedFileLoader {
	private static final String DEFAULT_SEED_FILE_NAME = ".fixture-monkey-seed";

	public SeedFileLoader() {
	}

	public @Nullable Long loadSeedFromFile() {
		Path path = Paths.get(DEFAULT_SEED_FILE_NAME);
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
