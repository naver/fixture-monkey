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
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.github.curiousoddman.rgxgen.RgxGen;
import com.github.curiousoddman.rgxgen.config.RgxGenOption;
import com.github.curiousoddman.rgxgen.config.RgxGenProperties;
import com.github.curiousoddman.rgxgen.iterators.StringIterator;

@API(since = "0.6.9", status = Status.MAINTAINED)
public final class RegexGenerator {
	private static final int FLAG_CASE_INSENSITIVE = 2;
	public static final int DEFAULT_REGEXP_GENERATION_TIMEOUT_SEC = 10;
	public static final int DEFAULT_REGEXP_GENERATION_MAX_SIZE = 100;

	public List<String> generateAll(String regex, int[] flags, @Nullable Integer min, @Nullable Integer max) {
		ExecutorService executor = Executors.newSingleThreadExecutor();

		try {
			RgxGen rgxGen = generateRgxGen(regex, flags);

			int minLength = min == null ? 0 : min;
			int maxLength = max == null ? Integer.MAX_VALUE : max;

			List<String> result = new ArrayList<>();

			Future<?> future = executor.submit(() -> {
				StringIterator stringIterator = rgxGen.iterateUnique();
				Spliterator<String> spliterator =
					Spliterators.spliteratorUnknownSize(stringIterator, Spliterator.ORDERED);

				result.addAll(StreamSupport.stream(spliterator, false)
					.filter(it -> it.length() >= minLength && it.length() <= maxLength)
					.limit(DEFAULT_REGEXP_GENERATION_MAX_SIZE)
					.collect(Collectors.toList()));
			});

			future.get(DEFAULT_REGEXP_GENERATION_TIMEOUT_SEC, TimeUnit.SECONDS);

			Collections.shuffle(result);
			return result;
		} catch (Exception ex) {
			throw new IllegalArgumentException(
				String.format(
					"String generation failed for the regular expression \"%s\" provided in @Pattern."
						+ " Either the regular expression is incorrect, or it takes too much time to generate",
					regex
				)
			);
		} finally {
			executor.shutdown();
		}
	}

	private static RgxGen generateRgxGen(String regex, int[] flags) {
		RgxGenProperties properties = new RgxGenProperties();
		if (Arrays.stream(flags).anyMatch(it -> it == FLAG_CASE_INSENSITIVE)) {
			RgxGenOption.CASE_INSENSITIVE.setInProperties(properties, true);
		}
		RgxGen rgxGen = new RgxGen(regex);
		rgxGen.setProperties(properties);
		return rgxGen;
	}
}
