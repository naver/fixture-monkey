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
		String expected = "\u03b4\u914e\u2dc4\u8358\u8b0e\u941c\u3e90";

		String actual = SUT.giveMeOne(String.class);

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void latterValue() {
		String expected = "\u5f93\u5eac\u6874\u0212\ua91e\u5d9a\u4da5\u2c3e\u431c\ubba2\u2c84\ud12c\ucc3c\u2c18"
			+ "\u04a3\u4084\u4365\u8741";
		SUT.giveMeOne(String.class);

		String actual = SUT.giveMeOne(String.class);

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void containerReturnsSame() {
		List<String> expected = Arrays.asList(
			"\ud43c\u225f\u2eee\u390e\u444f\u3c85\uc48c\u8367\u34b9\u1f2c\u09ab\u145f\u5c8a\u013a"
				+ "\ub19b\u3126\ucca1\ube69\u5896\u62dd\u28ff\ufa48\u3900\u9059",
			"\u5347\ubc17\uad55\u8c59\u3c5d\u2489\u100a\u24ba\u4b7e\ud420\u285c\u7e23\u359d\u3d9b"
				+ "\ud08c\uce7a\u9da4\uc2ac\u2118\u0a83\ud0e9\u8ade\u27c6\uf9da\ufce3"
		);

		List<String> actual = SUT.giveMeOne(new TypeReference<List<String>>() {
		});

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void containerMattersOrder() {
		Set<String> expected = new HashSet<>(Arrays.asList(
			"\u5347\ubc17\uad55\u8c59\u3c5d\u2489\u100a\u24ba\u4b7e\ud420\u285c\u7e23\u359d\u3d9b"
				+ "\ud08c\uce7a\u9da4\uc2ac\u2118\u0a83\ud0e9\u8ade\u27c6\uf9da\ufce3",
			"\ubcee\u9017\u69d3\ufcf4\u188e\ub4f4?\u0226\u1e22\u6104\u8b40\u255a\u8390\ucbc3"
				+ "\u9522\u5644\ud5ad\u9eaa\u20bd\ua1ed\u4a9c\u7267\u5d75",
			"\ud43c\u225f\u2eee\u390e\u444f\u3c85\uc48c\u8367\u34b9\u1f2c\u09ab\u145f\u5c8a\u013a"
				+ "\ub19b\u3126\ucca1\ube69\u5896\u62dd\u28ff\ufa48\u3900\u9059"
		));

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
			"\u03b4\u914e\u2dc4\u8358\u8b0e\u941c\u3e90",
			"\u03b4\u914e\u2dc4\u8358\u8b0e\u941c\u3e90"
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
		String seedOneFirstValue = "\u03b4\u914e\u2dc4\u8358\u8b0e\u941c\u3e90";

		String actual = SUT.giveMeOne(String.class);

		then(actual).isNotEqualTo(seedOneFirstValue);
	}

	@Seed(1)
	@RepeatedTest(100)
	void thirdSequentialStringReturnsSame() {
		String expected = "\u86f8\u47d9\u2c00\u938f\u6d2e\u2105\u7cc7\u1e53\u8d26\u4feb\uca79\u9909\u9d59\u7883"
			+ "\u7f72\u537d";
		SUT.giveMeOne(String.class);
		SUT.giveMeOne(String.class);

		String actual = SUT.giveMeOne(String.class);

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void integerReturnsSame() {
		Integer expected = 3588295;
		Integer actual = SUT.giveMeOne(Integer.class);

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void longReturnsSame() {
		Long expected = -1740023L;
		Long actual = SUT.giveMeOne(Long.class);

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void giveMeListReturnsSame() {
		List<String> expected = Arrays.asList(
			"\u03b4\u914e\u2dc4\u8358\u8b0e\u941c\u3e90",
			"\u5f93\u5eac\u6874\u0212\ua91e\u5d9a\u4da5\u2c3e\u431c\ubba2\u2c84\ud12c\ucc3c\u2c18"
				+ "\u04a3\u4084\u4365\u8741",
			"\u86f8\u47d9\u2c00\u938f\u6d2e\u2105\u7cc7\u1e53\u8d26\u4feb\uca79\u9909\u9d59\u7883\u7f72\u537d"
		);

		List<String> actual = SUT.giveMe(String.class, 3);

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void arbitraryBuilderSampleReturnsSame() {
		String expected = "\u03b4\u914e\u2dc4\u8358\u8b0e\u941c\u3e90";

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
		expected.setName(
			"\ubb68\ubc57\u96ad\u6e20\uc69f\u2398\ud682\u9b2d\u3d9c\ubb6b\u922a\u5468\u821a\ubccc"
			+ "\u7f74\ufa34\u340b\u1c63\u0888\u01b1\u51ff\u6b38\u0739\u0866\u53c1\u4d04\u142a\u7bf0"
			+ "\u83ac\u8f6a\u6869\u8406\uc8a8\u5dfa\u5ccb\u74f8\u7ebe\u8742\u7ed1\u7c33\uc800\u2249"
			+ "\u3a46\u1766\ud3a5\u4f13\u69d9\ubd59\u02da\u4d1e\u4d33\u5fe2\ud0ee\u5482\uc0da\uf9fb"
			+ "\u1507\ub754\uadaa\ubefb\ua88a\uff4a\u22eb\u0883\ubb6e\u4917\uc317\u334a\u26fb\u4b3e"
			+ "\uade0\u57d1\ua87e\u7ce4\ufe81\u7cc5\u8383\u67b8\ua19a\u4f26\u5559\ua417\ucfcc\ua0de"
			+ "\u74f2\u25e4\u44c2\u22ca\u0f25\u670c\u8ccc\u16a9\u7930\u8621\u9ba0\ucc57\u89ed\u7038"
			+ "\uc065\ud27b\u3aed\ud51b\uc890\uae40\u9a50\u6f2c\u93f3\u4b35\ub1ab\u9790\u04a1\ua9e2"
			+ "\u265a\u1a9a\u7bc9\u3527\u07c7\u2205\u1134\u2887\u20e5\u5da1\u1679\uaf05\ud4e2\uc691"
			+ "\ud07c\u1c1b\u7225\u0193\ub63e\u4e46\u5a00\u7ec3\u0dd7\u8517\u9b07\u6209\ud066\uce26"
			+ "\u5352\u221e\u8520\u210f\ub08b\u76cd\ud76a\u3d32\ub3cf\u8197\u11a4\u795a\u6c84\u75f8"
			+ "\u4508\u6698\u9747\uc29b\u8821\uc8be\u3cbb\u2222\u7232\u7d7f\u0298\uc6a6\u2cda\u30b8"
			+ "\u98ee\ucece\u1431\u05f4\u5004\ud0dd\u4a6a\u47fa\u820c\u9f7e\u4e74\ucc94\u33c2\u96f7"
			+ "\u730f\u6721\u788d\ua371\u9e71\u0824\u4dea\u393a\u587a\u32eb\ud1b5\u85ae\u65f8\u1037"
			+ "\u3de6\u2784\u90bb\ua373\u8100\uabcb\ud073\u3813\u355a\u484b\ud6bf\u82e7\u5f7c\ufdf4"
			+ "\u5f6a\u1e5a\uca4f\u8e1c\ub4c8\uc310\uc804\u5f98\u220b\u7f3c\u0720\ufcb4\ufcc1\uc14b"
			+ "\u9051\u7b8b\u578d\ub873\u7482\u3e60}\u1b0e\u1065\u3c48\uc8db\u0e4d\u5e4d\ucdee"
			+ "\uc783\uc8d5\u3f5c\u7843\u3668\uc619\u458f\u3f63\u85f3\u26e0\uce35\u5340\uc9dd\u7ac2"
			+ "\ub3b3\u915e\u1491");
		expected.setAge(null);

		Person actual = SUT.giveMeOne(Person.class);

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void mapReturnsSame() {
		Map<String, Integer> expected = new HashMap<>();

		Map<String, Integer> actual = SUT.giveMeOne(new TypeReference<Map<String, Integer>>() {
		});

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void seedResetsBetweenInvocationsDespitePollution() {
		String expected = "\u03b4\u914e\u2dc4\u8358\u8b0e\u941c\u3e90";

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
		expected.setLabel(
			"\uc6c7\ub5e2\uc01f\ua7af\u30e6\u864f\u82b6\u47dd\ub7f3\ub459\u6ed8\u44ee\u078e\u2ea7"
			+ "\uaef5\u7ee1\u50a1\uc474\u4771\u76a9\u0a00\u5b35\uc8b2\u61bb\u4ef3\u11f8\ub31c\u9c4a"
			+ "\u7123\uccbc\u90c2");
		expected.setOptions(
			Arrays.asList(
				"\u133d\u6164\ud45a\u4742\ua59d\u0553\ube51\u2a11\ufd68\u2446\u30f7\u9345\u214a\ubdcf"
					+ "\u7b68\uc7d9\u4bfb\u544a\u27c0\ub0f0",
				"\u6087\u0b5e\u0ab3\u66d3\u4088\u592c\uffc5\u2da5\uab13\ubceb\ua730\u2946\ua1a1\u0bd5"
					+ "\ub28b\u00f7\ud74f\u4d73"
			)
		);
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
