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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.mifmif.common.regex.Generex;

import dk.brics.automaton.RegExp;

@API(since = "0.6.9", status = Status.EXPERIMENTAL)
public final class RegexGenerator {
	private static final Map<String, String> PREDEFINED_CHARACTER_CLASSES;

	static {
		Map<String, String> characterClasses = new HashMap<>();
		characterClasses.put("\\\\d", "[0-9]");
		characterClasses.put("\\\\D", "[^0-9]");
		characterClasses.put("\\\\s", "[ \t\n\f\r]");
		characterClasses.put("\\\\S", "[^ \t\n\f\r]");
		characterClasses.put("\\\\w", "[a-zA-Z_0-9]");
		characterClasses.put("\\\\W", "[^a-zA-Z_0-9]");
		PREDEFINED_CHARACTER_CLASSES = Collections.unmodifiableMap(characterClasses);
	}

	public List<String> generateAll(String regex, int[] flags, @Nullable Integer min, @Nullable Integer max) {
		for (Map.Entry<String, String> charClass : PREDEFINED_CHARACTER_CLASSES.entrySet()) {
			regex = regex.replaceAll(charClass.getKey(), charClass.getValue());
		}

		RegExp regExp;
		if (flags.length == 0) {
			regExp = new RegExp(regex);
		} else {
			int intFlag = 0;
			for (int flag : flags) {
				intFlag = intFlag | flag;
			}
			regExp = new RegExp(regex, intFlag);
		}

		Generex generex = new Generex(regExp.toAutomaton());
		return this.generateAll(generex, min, max);
	}

	public List<String> generateAll(String regex) {
		return this.generateAll(regex, null, null);
	}

	public List<String> generateAll(String regex, @Nullable Integer min, @Nullable Integer max) {
		return this.generateAll(new Generex(regex), min, max);
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
}
