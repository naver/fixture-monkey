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
package com.navercorp.fixturemonkey.junit.jupiter.extension;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;

import lombok.Data;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.junit.jupiter.annotation.Seed;

@ExtendWith(FixtureMonkeySeedExtension.class)
class FixtureMonkeySeedExtensionTest {
	private static final FixtureMonkey SUT = FixtureMonkey.create();

	private static String noSeedAnnotationFirstValue;
	private static boolean noSeedAnnotationFirstValueCaptured;


	@Seed(1)
	@RepeatedTest(100)
	void seedReturnsSame() {
		String expected = "㼌✠섨ꝓ仛禦催ᘓ蓊類౺阹瞻";

		String actual = SUT.giveMeOne(String.class);

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void latterValue() {
		String expected = "婵얎⽒竻·俌欕悳잸횑ٻ킐結㗗蜵ꓣ몒둡塸聩";
		SUT.giveMeOne(String.class);

		String actual = SUT.giveMeOne(String.class);

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void containerReturnsSame() {
		List<String> expected = Collections.emptyList();

		List<String> actual = SUT.giveMeOne(new TypeReference<List<String>>() {
		});

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void containerMattersOrder() {
		Set<String> expected = new HashSet<>(Collections.singletonList("㼌✠섨ꝓ仛禦催ᘓ蓊類౺阹瞻"));

		Set<String> actual = SUT.giveMeOne(new TypeReference<Set<String>>() {
		});

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void multipleContainerReturnsDiff() {
		Set<String> firstSet = SUT.giveMeOne(new TypeReference<Set<String>>() {
		});

		List<String> secondList = SUT.giveMeOne(new TypeReference<List<String>>() {
		});

		then(firstSet).isNotEqualTo(secondList);
	}

	@Seed(1)
	@RepeatedTest(100)
	void multipleFixtureMonkeyInstancesReturnsAsOneInstance() {
		List<String> expected = Arrays.asList(
			"㼌✠섨ꝓ仛禦催ᘓ蓊類౺阹瞻",
			"婵얎⽒竻·俌欕悳잸횑ٻ킐結㗗蜵ꓣ몒둡塸聩"
		);
		FixtureMonkey firstFixtureMonkey = FixtureMonkey.create();
		FixtureMonkey secondFixtureMonkey = FixtureMonkey.create();

		List<String> actual = Arrays.asList(
			firstFixtureMonkey.giveMeOne(String.class),
			secondFixtureMonkey.giveMeOne(String.class)
		);

		then(actual).isEqualTo(expected);
	}

	@Seed(2)
	@RepeatedTest(100)
	void differentSeedReturnsDifferentValue() {
		String seedOneFirstValue = "㼌✠섨ꝓ仛禦催ᘓ蓊類౺阹瞻";

		String actual = SUT.giveMeOne(String.class);

		then(actual).isNotEqualTo(seedOneFirstValue);
	}

	@Seed(1)
	@RepeatedTest(100)
	void thirdSequentialStringReturnsSame() {
		String expected = "ᣮ鎊熇捺셾壍Ꜻꌩ垅凗❉償粐믩࠱哠횛䄻㹦䤔᫿琬梅䨊";
		SUT.giveMeOne(String.class);
		SUT.giveMeOne(String.class);

		String actual = SUT.giveMeOne(String.class);

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void integerReturnsSame() {
		Integer expected = 86904;
		Integer actual = SUT.giveMeOne(Integer.class);

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void longReturnsSame() {
		Long expected = -1555898L;
		Long actual = SUT.giveMeOne(Long.class);

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void giveMeListReturnsSame() {
		List<String> expected = Arrays.asList(
			"㼌✠섨ꝓ仛禦催ᘓ蓊類౺阹瞻",
			"婵얎⽒竻·俌欕悳잸횑ٻ킐結㗗蜵ꓣ몒둡塸聩",
			"ᣮ鎊熇捺셾壍Ꜻꌩ垅凗❉償粐믩࠱哠횛䄻㹦䤔᫿琬梅䨊"
		);

		List<String> actual = SUT.giveMe(String.class, 3);

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void arbitraryBuilderSampleReturnsSame() {
		String expected = "㼌✠섨ꝓ仛禦催ᘓ蓊類౺阹瞻";

		String actual = SUT.giveMeBuilder(String.class).sample();

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void builderSetPreservesDeterminism() {
		String fixedValue = "fixed";

		String actual = SUT.giveMeBuilder(String.class).set("$", fixedValue).sample();

		then(actual).isEqualTo(fixedValue);
	}

	@RepeatedTest(100)
	void noSeedAnnotationFallsBackToMethodHashDeterministically() {
		String actual = SUT.giveMeOne(String.class);

		if (!noSeedAnnotationFirstValueCaptured) {
			noSeedAnnotationFirstValue = actual;
			noSeedAnnotationFirstValueCaptured = true;
			return;
		}
		then(actual).isEqualTo(noSeedAnnotationFirstValue);
	}

	@Seed(1)
	@RepeatedTest(100)
	void pojoReturnsSame() {
		Person expected = new Person();
		expected.setName("㼌✠섨ꝓ仛禦催ᘓ蓊類౺阹瞻");
		expected.setAge(-3336);

		Person actual = SUT.giveMeOne(Person.class);

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void mapReturnsSame() {
		Map<String, Integer> expected = new HashMap<>();
		expected.put("⽒竻·俌欕悳잸횑ٻ킐結㗗蜵", 286);
		expected.put("㼌✠섨ꝓ仛禦催ᘓ蓊類౺阹瞻", 29413);

		Map<String, Integer> actual = SUT.giveMeOne(new TypeReference<Map<String, Integer>>() {
		});

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void seedResetsBetweenInvocationsDespitePollution() {
		String expected = "㼌✠섨ꝓ仛禦催ᘓ蓊類౺阹瞻";

		String actual = SUT.giveMeOne(String.class);
		for (int i = 0; i < 50; i++) {
			SUT.giveMeOne(String.class);
		}

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void thenApplyWithChildSampleIsDeterministic() {
		// given — a builder that uses thenApply to inject a separately-sampled child
		Parent actual = SUT.giveMeBuilder(Parent.class)
			.thenApply((parent, b) -> b.set("child", SUT.giveMeBuilder(Child.class).sample()))
			.sample();

		// then — under @Seed(1), every iteration must produce this exact child
		Child expected = new Child();
		expected.setLabel("ၤ鰁뎖㨭芣쬋瑅肽躣鮡臮祡눻촯⿅䕗ﵢ洞꙱ζ䓎⽟寙တ铓");
		expected.setOptions(Collections.emptyList());
		then(actual.getChild()).isEqualTo(expected);
	}

	@Data
	public static class Person {
		private String name;
		private Integer age;
	}

	@Data
	public static class Parent {
		private String name;
		private Child child;
	}

	@Data
	public static class Child {
		private String label;
		private List<String> options;
	}
}
