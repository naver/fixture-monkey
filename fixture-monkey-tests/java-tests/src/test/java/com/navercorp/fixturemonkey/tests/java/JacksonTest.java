package com.navercorp.fixturemonkey.tests.java;

import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNoException;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.RepeatedTest;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.jackson.plugin.JacksonPlugin;
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

	@RepeatedTest(TEST_COUNT)
	void jsonTypeInfoName() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonTypeInfoIdName.class));
	}

	@RepeatedTest(TEST_COUNT)
	void jsonTypeInfoList() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonTypeInfoList.class));
	}

	@RepeatedTest(TEST_COUNT)
	void jsonTypeInfoIdClass() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonTypeInfoIdClass.class));
	}

	@RepeatedTest(TEST_COUNT)
	void jsonTypeWithAnnotations() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(TypeWithAnnotationsValue.class));
	}

	@RepeatedTest(TEST_COUNT)
	void jsonTypeWithAnnotationsList() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(TypeWithAnnotationsList.class));
	}

	@RepeatedTest(TEST_COUNT)
	void jsonTypeInfoListInSetter() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonTypeInfoListInSetter.class));
	}

	@RepeatedTest(TEST_COUNT)
	void jsonTypeInfoListIncludeWrapperObject() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonTypeInfoListIncludeWrapperObject.class));
	}

	@RepeatedTest(TEST_COUNT)
	void jsonTypeInfoListInSetterIncludeWrapperObject() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonTypeInfoListInSetterIncludeWrapperObject.class));
	}

	@RepeatedTest(TEST_COUNT)
	void jsonTypeWithAnnotationsIncludeWrapperObjectList() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(TypeWithAnnotationsIncludeWrapperObjectList.class));
	}

	@RepeatedTest(TEST_COUNT)
	void sampleListType() {
		List<TypeWithAnnotationsValue> actual = SUT.giveMeOne(
			new TypeReference<List<TypeWithAnnotationsValue>>() {
			}
		);

		then(actual).allMatch(Objects::nonNull);
	}
}
