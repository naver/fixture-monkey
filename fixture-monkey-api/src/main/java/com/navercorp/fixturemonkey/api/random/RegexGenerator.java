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

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.annotation.PreDestroy;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.github.curiousoddman.rgxgen.RgxGen;
import com.github.curiousoddman.rgxgen.config.RgxGenOption;
import com.github.curiousoddman.rgxgen.config.RgxGenProperties;
import com.mifmif.common.regex.Generex;

@API(since = "0.6.9", status = Status.MAINTAINED)
public final class RegexGenerator {
	private static final int DEFAULT_REGEXP_GENERATION_TIMEOUT_SEC = 10;
	private static final int FLAG_CASE_INSENSITIVE = 2;
	private static final ExecutorService executor = Executors.newSingleThreadExecutor();

	public String generate(String regex, int[] flags, Predicate<String> stringCondition) {
		boolean caseInSensitive = Arrays.stream(flags).anyMatch(it -> it == FLAG_CASE_INSENSITIVE);

		try {
			RgxGen rgxGen = generateRgxGen(regex, caseInSensitive);

			String result = executor.submit(() ->
				rgxGen.stream()
					.filter(stringCondition)
					.findFirst()
			).get(DEFAULT_REGEXP_GENERATION_TIMEOUT_SEC, TimeUnit.SECONDS).orElse(null);

			checkValidity(regex, result, caseInSensitive);
			return result;
		} catch (Exception ex) {
			throw new IllegalArgumentException(
				String.format(
					"String generation failed for the regular expression \"%s\"."
						+ " Either the regular expression is incorrect,"
						+ " or cannot produce a string that matches the regular expression.",
					regex
				)
			);
		}
	}

	private List<String> generateAll(Generex generex, @Nullable Integer min, @Nullable Integer max) {
		if (min == null) {
			min = 0;
		}

		if (max == null) {
			max = 255;
		}

		Integer regexMin = min;
		Integer regexMax = max;
		List<String> result = generex.getMatchedStrings(100).stream()
			.filter(it -> it.length() >= regexMin && it.length() <= regexMax)
			.collect(toList());
		Collections.shuffle(result);
		return result;
	}

	private static RgxGen generateRgxGen(String regex, boolean caseInSensitive) {
		RgxGenProperties properties = new RgxGenProperties();
		if (caseInSensitive) {
			RgxGenOption.CASE_INSENSITIVE.setInProperties(properties, true);
		}
		RgxGen rgxGen = new RgxGen(regex);
		rgxGen.setProperties(properties);
		return rgxGen;
	}

	private static void checkValidity(String regex, String result, boolean caseInSensitive) {
		Pattern pattern;
		if (caseInSensitive) {
			pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		} else {
			pattern = Pattern.compile(regex);
		}

		if (!pattern.matcher(result).matches()) {
			throw new NoSuchElementException();
		}
	}

	@PreDestroy
	public void terminateExecutor() {
		executor.shutdown();
	}
}
