/*
 * Fixture Monkey
 *
 * Copyright (c) 2021-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.fixturemonkey.api.random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.github.curiousoddman.rgxgen.RgxGen;
import com.github.curiousoddman.rgxgen.config.RgxGenOption;
import com.github.curiousoddman.rgxgen.config.RgxGenProperties;

@API(since = "0.6.9", status = Status.MAINTAINED)
public final class RegexGenerator {
	private static final int FLAG_CASE_INSENSITIVE = 2;
	public static final int DEFAULT_REGEXP_GENERATION_TIMEOUT_SEC = 10;
	public static final int DEFAULT_REGEXP_GENERATION_MAX_SIZE = 100;

	public List<String> generateAll(String regex, int[] flags, @Nullable Integer min, @Nullable Integer max) {
		boolean caseSensitive = Arrays.stream(flags).noneMatch(it -> it == FLAG_CASE_INSENSITIVE);
		ExecutorService executor = Executors.newSingleThreadExecutor();

		try {
			RgxGen rgxGen = generateRgxGen(regex, caseSensitive);

			int minLength = min == null ? 0 : min;
			int maxLength = max == null ? Integer.MAX_VALUE : max;

			List<String> result = new ArrayList<>();

			Future<?> future = executor.submit(() -> {
				result.addAll(
					rgxGen.stream()
						.filter(it -> it.length() >= minLength && it.length() <= maxLength)
						.limit(DEFAULT_REGEXP_GENERATION_MAX_SIZE)
						.collect(Collectors.toList())
				);
			});

			future.get(DEFAULT_REGEXP_GENERATION_TIMEOUT_SEC, TimeUnit.SECONDS);

			List<String> validResults = getValidResults(regex, result, caseSensitive);
			Collections.shuffle(validResults);
			return result;
		} catch (Exception ex) {
			throw new IllegalArgumentException(
				String.format(
					"String generation failed for the regular expression \"%s\" provided in @Pattern."
						+ " Either the regular expression is incorrect,"
						+ " or cannot produce a string that matches the regular expression.",
					regex
				)
			);
		} finally {
			executor.shutdown();
		}
	}

	private static RgxGen generateRgxGen(String regex, boolean caseSensitive) {
		RgxGenProperties properties = new RgxGenProperties();
		if (!caseSensitive) {
			RgxGenOption.CASE_INSENSITIVE.setInProperties(properties, true);
		}
		RgxGen rgxGen = new RgxGen(regex);
		rgxGen.setProperties(properties);
		return rgxGen;
	}

	private static List<String> getValidResults(String regex, List<String> result, boolean caseSensitive) {
		Pattern pattern;
		if (caseSensitive) {
			pattern = Pattern.compile(regex);
		} else {
			pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		}
		List<String> validResults = result.stream()
			.filter(it -> pattern.matcher(it).matches())
			.collect(Collectors.toList());

		if (validResults.isEmpty()) {
			throw new NoSuchElementException();
		}
		return validResults;
	}
}
