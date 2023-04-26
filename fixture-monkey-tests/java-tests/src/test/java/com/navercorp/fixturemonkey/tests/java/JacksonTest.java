package com.navercorp.fixturemonkey.tests.java;

import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNoException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.jackson.plugin.JacksonPlugin;
import com.navercorp.fixturemonkey.tests.java.ImmutableJavaTestSpecs.ContainerObject;
import com.navercorp.fixturemonkey.tests.java.ImmutableJavaTestSpecs.Enum;
import com.navercorp.fixturemonkey.tests.java.ImmutableJavaTestSpecs.JavaTypeObject;
import com.navercorp.fixturemonkey.tests.java.ImmutableJavaTestSpecs.RootJavaTypeObject;
import com.navercorp.fixturemonkey.tests.java.JacksonSpecs.JsonTypeInfoIdClass;
import com.navercorp.fixturemonkey.tests.java.JacksonSpecs.JsonTypeInfoIdName;
import com.navercorp.fixturemonkey.tests.java.JacksonSpecs.JsonTypeInfoList;
import com.navercorp.fixturemonkey.tests.java.JacksonSpecs.JsonTypeInfoListInSetter;
import com.navercorp.fixturemonkey.tests.java.JacksonSpecs.JsonTypeInfoListInSetterIncludeWrapperObject;
import com.navercorp.fixturemonkey.tests.java.JacksonSpecs.JsonTypeInfoListIncludeWrapperObject;
import com.navercorp.fixturemonkey.tests.java.JacksonSpecs.TypeWithAnnotationsIncludeWrapperObjectList;
import com.navercorp.fixturemonkey.tests.java.JacksonSpecs.TypeWithAnnotationsList;
import com.navercorp.fixturemonkey.tests.java.JacksonSpecs.TypeWithAnnotationsValue;

class JacksonTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.plugin(new JacksonPlugin())
		.defaultNotNull(true)
		.build();

	@Disabled
	@RepeatedTest(TEST_COUNT)
	void jsonTypeInfoName() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonTypeInfoIdName.class));
	}

	@Disabled
	@RepeatedTest(TEST_COUNT)
	void jsonTypeInfoList() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonTypeInfoList.class));
	}

	@Disabled
	@RepeatedTest(TEST_COUNT)
	void jsonTypeInfoIdClass() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonTypeInfoIdClass.class));
	}

	@Disabled
	@RepeatedTest(TEST_COUNT)
	void jsonTypeWithAnnotations() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(TypeWithAnnotationsValue.class));
	}

	@Disabled
	@RepeatedTest(TEST_COUNT)
	void jsonTypeWithAnnotationsList() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(TypeWithAnnotationsList.class));
	}

	@Disabled
	@RepeatedTest(TEST_COUNT)
	void jsonTypeInfoListInSetter() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonTypeInfoListInSetter.class));
	}

	@Disabled
	@RepeatedTest(TEST_COUNT)
	void jsonTypeInfoListIncludeWrapperObject() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonTypeInfoListIncludeWrapperObject.class));
	}

	@Disabled
	@RepeatedTest(TEST_COUNT)
	void jsonTypeInfoListInSetterIncludeWrapperObject() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonTypeInfoListInSetterIncludeWrapperObject.class));
	}

	@Disabled
	@RepeatedTest(TEST_COUNT)
	void jsonTypeWithAnnotationsIncludeWrapperObjectList() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(TypeWithAnnotationsIncludeWrapperObjectList.class));
	}

	@RepeatedTest(TEST_COUNT)
	void customizedByOption() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JacksonPlugin())
			.pushAssignableTypeFixtureCustomizer(JavaTypeObject.class, value -> value)
			.defaultNotNull(true)
			.build();

		JavaTypeObject actual = sut.giveMeOne(RootJavaTypeObject.class).getValue();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void customized() {
		JavaTypeObject actual = SUT.giveMeBuilder(RootJavaTypeObject.class)
			.customize(
				new MatcherOperator<>(
					new AssignableTypeMatcher(JavaTypeObject.class),
					obj -> obj
				)
			)
			.sample()
			.getValue();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleContainerType() {
		ContainerObject actual = SUT.giveMeOne(ContainerObject.class);

		then(actual.getPrimitiveArray()).isNotNull();
		then(actual.getArray()).isNotNull();
		then(actual.getComplexArray()).isNotNull();
		then(actual.getList()).isNotNull();
		then(actual.getComplexList()).isNotNull();
		then(actual.getSet()).isNotNull();
		then(actual.getComplexSet()).isNotNull();
		then(actual.getMap()).isNotNull();
		then(actual.getComplexMap()).isNotNull();
		then(actual.getMapEntry()).isNotNull();
		then(actual.getComplexMapEntry()).isNotNull();
		then(actual.getOptional()).isNotNull();
		then(actual.getOptionalInt()).isNotNull();
		then(actual.getOptionalLong()).isNotNull();
		then(actual.getOptionalDouble()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleListType() {
		List<JavaTypeObject> actual = SUT.giveMeOne(new TypeReference<List<JavaTypeObject>>() {
		});

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedListType() {
		List<JavaTypeObject> actual = SUT.giveMeBuilder(new TypeReference<List<JavaTypeObject>>() {
			})
			.fixed()
			.sample();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleSetType() {
		Set<JavaTypeObject> actual = SUT.giveMeOne(new TypeReference<Set<JavaTypeObject>>() {
		});

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedSetType() {
		Set<JavaTypeObject> actual = SUT.giveMeBuilder(new TypeReference<Set<JavaTypeObject>>() {
			})
			.fixed()
			.sample();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleArrayType() {
		JavaTypeObject[] actual = SUT.giveMeOne(new TypeReference<JavaTypeObject[]>() {
		});

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedArrayType() {
		JavaTypeObject[] actual = SUT.giveMeBuilder(new TypeReference<JavaTypeObject[]>() {
			})
			.fixed()
			.sample();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleOptionalType() {
		Optional<JavaTypeObject> actual = SUT.giveMeOne(new TypeReference<Optional<JavaTypeObject>>() {
		});

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedOptionalType() {
		Optional<JavaTypeObject> actual = SUT.giveMeBuilder(new TypeReference<Optional<JavaTypeObject>>() {
			})
			.fixed()
			.sample();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleMapType() {
		Map<String, JavaTypeObject> actual = SUT.giveMeOne(new TypeReference<Map<String, JavaTypeObject>>() {
		});

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedMapType() {
		Map<String, JavaTypeObject> actual = SUT.giveMeBuilder(new TypeReference<Map<String, JavaTypeObject>>() {
			})
			.fixed()
			.sample();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleMapEntryType() {
		Map.Entry<String, JavaTypeObject> actual = SUT.giveMeOne(
			new TypeReference<Map.Entry<String, JavaTypeObject>>() {
			});

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedMapEntryType() {
		Map.Entry<String, JavaTypeObject> actual = SUT.giveMeBuilder(
				new TypeReference<Map.Entry<String, JavaTypeObject>>() {
				})
			.fixed()
			.sample();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleEnumKeyMap() {
		thenNoException()
			.isThrownBy(() -> SUT.giveMeBuilder(new TypeReference<List<Map<Enum, String>>>() {
					})
					.size("$", 2)
					.sample()
			);
	}
}
