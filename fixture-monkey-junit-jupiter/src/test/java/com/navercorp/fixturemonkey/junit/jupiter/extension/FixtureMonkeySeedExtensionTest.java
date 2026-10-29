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

	@Seed(20)
	@RepeatedTest(100)
	void pojoReturnsSame() {
		Person expected = new Person();
		expected.setName("\u94ed\ubf0e");
		expected.setAge(-3);

		Person actual = SUT.giveMeOne(Person.class);

		then(actual).isEqualTo(expected);
	}

	@Seed(20)
	@RepeatedTest(100)
	void mapReturnsSame() {
		Map<String, Integer> expected = new HashMap<>();
		expected.put("\u6681\u4db4\ua998\u6dfc\u1e49\u3e65\uaac0\u2a4d\u01b4\u583d\u5003\u9e37\u740b\u6a94\uc683\u049a"
			+ "\u6382\ufbfc", 5053161);
		expected.put("\uba4f\uc48e\u8e5b\u289d", -892191);

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
			"\u6f80\u6d1d\ua6bd\u7fb6\u09f0");
		expected.setOptions(
			Arrays.asList(
				"\u738c\ua76f\ud2dd\ucf1c\u7be7\u0dfc\u62f2\u69f6\uc984\ufe0e\u3360\ud4b5\u14c9\u2b11"
					+ "\u7fb0\uc2ef\ucf10\ubd06\u6b42\u9687\u1fa1\uc230\uc173\u8fd4\u7960\u5c85\uc2b4\u8995"
					+ "\u5721\u2ba4\u3806\u6b18\uc747\ubeeb\u01c2\u8296\ua791\u36aa\u3ab2\u9ca0\uc798\u45e3"
					+ "\u1868\u1b7e\uc917\u5cf4\ud2d1\u8cf0\u61ce\u5381\u7f3e\u99d0\u0eb7\u7d2b\u71a5\ua702"
					+ "\u017b\ufca9\ua7b7\uac86\u2d9d\ua6f4\u6044\u604a\ucff7\uffe0\u5c42\ua224\u0b16\u37ac"
					+ "\ud77b\u2292\u34eb\u1695\uc565\uca0f\uc3c3\uca95\u4352\ub72b\uc185\u014e\uba03\u95cf"
					+ "\u3d66\u296f\u561d\u7acb\ufba2\ua92c\u4a61\u3c11\uf9ce\u3828\u6784\u8fce\u9748\uab4c"
					+ "\u0a90\u3a7d\u7eac\u88d2\u6250\u2754\u3288\ubb16\ub712\u4245\uc790\u23c9\u6f3c\uce9e"
					+ "\u55c5\u7d0f\u69a2\ub35c\ud1b8\u353b\u5b6b\u48c0\ua897\ua6a1\u846a\u66ee\ucd43\u9600"
					+ "\u522c\ud61f\u2e0b\u9e9d\uad1d\ud223\ud3fa\u7beb\u50ab\u26f0\u5c04\u161a\u1207\u567a"
					+ "\u9775\u549c\ube2f\ufc08\u279f\u9c43\ud62b\u414c\u9329\u99ec\ub2e6\u0a29\u0b0b\u73e2"
					+ "\ud249\ud4bc\u88c7\uc62c\ub6b1\u4081\u572a\u0640\u6ec0\u7f04\u632a\u290c\u1da2\uc44d"
					+ "\u6d84\u605e\uc79b\u979d",
				"\u2518\u7be0\u8fe9\u6a76\u1c2f\u9e91\u0372\u97e7\uc3cc\ucca6\u0b6c\u71e6\u9337\ub8fa"
					+ "\u18ce\u434a\u9684\u776b",
				"\u817e\u657c\ua788\u5d47\u7f2f\u1278\u3740\u0efd\ud245\u446c\u1d41\uc13b\u3c2d\u2275"
					+ "\uc438\u7d6d\u6907"
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
