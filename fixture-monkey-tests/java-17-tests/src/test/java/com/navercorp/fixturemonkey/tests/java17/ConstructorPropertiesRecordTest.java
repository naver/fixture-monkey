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

package com.navercorp.fixturemonkey.tests.java17;

import static com.navercorp.fixturemonkey.api.experimental.JavaGetterMethodPropertySelector.javaGetter;
import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.tests.java17.RecordTestSpecs.BooleanRecord;
import com.navercorp.fixturemonkey.tests.java17.RecordTestSpecs.CanonicalConstructorRecord;
import com.navercorp.fixturemonkey.tests.java17.RecordTestSpecs.CompactConstructorRecord;
import com.navercorp.fixturemonkey.tests.java17.RecordTestSpecs.ComplexContainerRecord;
import com.navercorp.fixturemonkey.tests.java17.RecordTestSpecs.ContainerRecord;
import com.navercorp.fixturemonkey.tests.java17.RecordTestSpecs.DateTimeRecord;
import com.navercorp.fixturemonkey.tests.java17.RecordTestSpecs.IsPrefixBooleanRecord;
import com.navercorp.fixturemonkey.tests.java17.RecordTestSpecs.JavaTypeRecord;
import com.navercorp.fixturemonkey.tests.java17.RecordTestSpecs.NoArgsConstructorRecord;
import com.navercorp.fixturemonkey.tests.java17.RecordTestSpecs.TwoConstructorsRecord;

class ConstructorPropertiesRecordTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@Test
	void sampleJavaType() {
		JavaTypeRecord actual = SUT.giveMeOne(JavaTypeRecord.class);

		then(actual).isNotNull();
	}

	@Test
	void fixedJavaType() {
		JavaTypeRecord actual = SUT.giveMeBuilder(JavaTypeRecord.class)
			.fixed()
			.sample();

		then(actual).isNotNull();
	}

	@Test
	void sampleDateTime() {
		DateTimeRecord actual = SUT.giveMeOne(DateTimeRecord.class);

		then(actual).isNotNull();
	}

	@Test
	void fixedDateTime() {
		DateTimeRecord actual = SUT.giveMeBuilder(DateTimeRecord.class)
			.fixed()
			.sample();

		then(actual).isNotNull();
	}

	@Test
	void sampleContainer() {
		ContainerRecord actual = SUT.giveMeOne(ContainerRecord.class);

		then(actual).isNotNull();
	}

	@Test
	void fixedContainer() {
		ContainerRecord actual = SUT.giveMeBuilder(ContainerRecord.class)
			.fixed()
			.sample();

		then(actual).isNotNull();
	}

	@Test
	void sampleInterfaceContainer() {
		ComplexContainerRecord actual = SUT.giveMeOne(ComplexContainerRecord.class);

		then(actual).isNotNull();
	}

	@Test
	void fixedInterfaceContainer() {
		ComplexContainerRecord actual = SUT.giveMeBuilder(ComplexContainerRecord.class)
			.fixed()
			.sample();

		then(actual).isNotNull();
	}

	@Test
	void sampleTwoConstructorsRecord() {
		String actual = SUT.giveMeBuilder(TwoConstructorsRecord.class)
			.setNotNull("string")
			.sample()
			.string();

		then(actual).isNotNull();
	}

	@Test
	void recordShouldUseCanonicalConstructor() {
		String expected = "expected";

		String actual = SUT.giveMeBuilder(TwoConstructorsRecord.class)
			.set("string", expected)
			.sample()
			.string();

		then(actual).isEqualTo(expected);
	}

	@Test
	void recordUseCompactConstructor() {
		String expected = "expected";

		String actual = SUT.giveMeBuilder(CompactConstructorRecord.class)
			.set("string", expected)
			.sample()
			.string();

		then(actual).isEqualTo("12345");
	}

	@Test
	void fixedTwoConstructorsRecord() {
		String actual = SUT.giveMeBuilder(TwoConstructorsRecord.class)
			.setNotNull("string")
			.fixed()
			.sample()
			.string();

		then(actual).isNotNull();
	}

	@Test
	void sampleNoArgsConstructorRecord() {
		NoArgsConstructorRecord actual = SUT.giveMeOne(NoArgsConstructorRecord.class);

		then(actual).isNotNull();
	}

	@Test
	void fixedNoArgsConstructorRecord() {
		NoArgsConstructorRecord actual = SUT.giveMeBuilder(NoArgsConstructorRecord.class)
			.fixed()
			.sample();

		then(actual).isNotNull();
	}

	@Test
	void setIsPrefixPrimitiveBoolean() {
		boolean actual = SUT.giveMeBuilder(IsPrefixBooleanRecord.class)
			.set(javaGetter(IsPrefixBooleanRecord::isPrimitive), false)
			.sample()
			.isPrimitive();

		then(actual).isFalse();
	}

	@Test
	void setIsPrefixWrapperBoolean() {
		boolean actual = SUT.giveMeBuilder(IsPrefixBooleanRecord.class)
			.set(javaGetter(IsPrefixBooleanRecord::isWrapper), false)
			.sample()
			.isWrapper();

		then(actual).isFalse();
	}

	@Test
	void setPrimitiveBoolean() {
		boolean actual = SUT.giveMeBuilder(BooleanRecord.class)
			.set(javaGetter(BooleanRecord::primitive), false)
			.sample()
			.primitive();

		then(actual).isFalse();
	}

	@Test
	void setWrapperBoolean() {
		boolean actual = SUT.giveMeBuilder(BooleanRecord.class)
			.set(javaGetter(BooleanRecord::wrapper), false)
			.sample()
			.wrapper();

		then(actual).isFalse();
	}

	@Test
	void sampleCanonicalConstructorRecord() {
		String actual = SUT.giveMeOne(CanonicalConstructorRecord.class).string();

		then(actual).isNotNull();
	}
}
