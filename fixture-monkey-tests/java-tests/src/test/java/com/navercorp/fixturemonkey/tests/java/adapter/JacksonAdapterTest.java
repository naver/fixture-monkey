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

package com.navercorp.fixturemonkey.tests.java.adapter;

import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNoException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.RepeatedTest;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.adapter.JavaNodeTreeAdapterPlugin;
import com.navercorp.fixturemonkey.adapter.tracing.AdapterTracer;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.jackson.plugin.JacksonPlugin;
import com.navercorp.fixturemonkey.tests.java.specs.ImmutableSpecs.ContainerObject;
import com.navercorp.fixturemonkey.tests.java.specs.ImmutableSpecs.Enum;
import com.navercorp.fixturemonkey.tests.java.specs.ImmutableSpecs.JavaTypeObject;
import com.navercorp.fixturemonkey.tests.java.specs.JacksonSpecs.ConstructorObject;
import com.navercorp.fixturemonkey.tests.java.specs.JacksonSpecs.JsonEnum;
import com.navercorp.fixturemonkey.tests.java.specs.JacksonSpecs.JsonFormatObject;
import com.navercorp.fixturemonkey.tests.java.specs.JacksonSpecs.JsonNodeWrapper;
import com.navercorp.fixturemonkey.tests.java.specs.JacksonSpecs.JsonPropertyConstructor;
import com.navercorp.fixturemonkey.tests.java.specs.JacksonSpecs.JsonPropertyContainerOuter;
import com.navercorp.fixturemonkey.tests.java.specs.JacksonSpecs.JsonPropertyInner;
import com.navercorp.fixturemonkey.tests.java.specs.JacksonSpecs.JsonPropertyMultipleFields;
import com.navercorp.fixturemonkey.tests.java.specs.JacksonSpecs.JsonPropertyOuter;
import com.navercorp.fixturemonkey.tests.java.specs.JacksonSpecs.JsonPropertyWithNormalField;
import com.navercorp.fixturemonkey.tests.java.specs.JacksonSpecs.JsonPropertyWithOuter;
import com.navercorp.fixturemonkey.tests.java.specs.JacksonSpecs.JsonTypeInfoFieldOuter;
import com.navercorp.fixturemonkey.tests.java.specs.JacksonSpecs.JsonTypeInfoIdClass;
import com.navercorp.fixturemonkey.tests.java.specs.JacksonSpecs.JsonTypeInfoIdName;
import com.navercorp.fixturemonkey.tests.java.specs.JacksonSpecs.JsonTypeInfoList;
import com.navercorp.fixturemonkey.tests.java.specs.JacksonSpecs.JsonTypeInfoListInSetter;
import com.navercorp.fixturemonkey.tests.java.specs.JacksonSpecs.JsonTypeInfoListInSetterIncludeWrapperObject;
import com.navercorp.fixturemonkey.tests.java.specs.JacksonSpecs.JsonTypeInfoListIncludeWrapperObject;
import com.navercorp.fixturemonkey.tests.java.specs.JacksonSpecs.PayPreApproval;
import com.navercorp.fixturemonkey.tests.java.specs.JacksonSpecs.Type;
import com.navercorp.fixturemonkey.tests.java.specs.JacksonSpecs.TypeWithAnnotations;
import com.navercorp.fixturemonkey.tests.java.specs.JacksonSpecs.TypeWithAnnotationsIncludeWrapperObjectList;
import com.navercorp.fixturemonkey.tests.java.specs.JacksonSpecs.TypeWithAnnotationsList;
import com.navercorp.fixturemonkey.tests.java.specs.JacksonSpecs.TypeWithAnnotationsValue;

class JacksonAdapterTest {

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.plugin(new JacksonPlugin())
		.defaultNotNull(true)
		.plugin(new JavaNodeTreeAdapterPlugin())
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
		}).fixed().sample();

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
		}).fixed().sample();

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
		}).fixed().sample();

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
			}
		);

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedMapEntryType() {
		Map.Entry<String, JavaTypeObject> actual = SUT.giveMeBuilder(
				new TypeReference<Map.Entry<String, JavaTypeObject>>() {
				}
			)
			.fixed()
			.sample();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleEnumKeyMap() {
		thenNoException().isThrownBy(() ->
			SUT.giveMeBuilder(new TypeReference<List<Map<Enum, String>>>() {
			}).size("$", 2).sample()
		);
	}

	@RepeatedTest(TEST_COUNT)
	void sampleConstructorObject() {
		ConstructorObject actual = SUT.giveMeOne(ConstructorObject.class);

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void setDecomposedValueShouldRespectJsonProperty() {
		// when
		JsonPropertyOuter actual = SUT.giveMeBuilder(JsonPropertyOuter.class)
			.set("inner", new JsonPropertyInner("expected"))
			.sample();

		// then
		then(actual.getInner()).isNotNull();
		then(actual.getInner().getOriginalField()).isEqualTo("expected");
	}

	@RepeatedTest(TEST_COUNT)
	void setDecomposedValueJsonPropertyShouldRespectJsonProperty() {
		// when
		JsonPropertyWithOuter actual = SUT.giveMeBuilder(JsonPropertyWithOuter.class)
			.set("outer", new JsonPropertyInner("expected"))
			.sample();

		// then
		then(actual.getInner()).isNotNull();
		then(actual.getInner().getOriginalField()).isEqualTo("expected");
	}

	@RepeatedTest(TEST_COUNT)
	void setDecomposedValueShouldRespectInnerJsonProperty() {
		// given
		String expected = "expected";

		// when
		String actual = SUT.giveMeBuilder(JsonPropertyOuter.class)
			.set("inner.renamed", expected)
			.sample()
			.getInner()
			.getOriginalField();

		// then
		then(actual).isEqualTo(expected);
	}

	@RepeatedTest(TEST_COUNT)
	void setDecomposedValueShouldRespectJsonPropertyDiff() {
		// when
		String actual = SUT.giveMeBuilder(JsonPropertyInner.class)
			.set("renamed", "expected")
			.sample()
			.getOriginalField();

		// then
		then(actual).isEqualTo("expected");
	}

	@RepeatedTest(TEST_COUNT)
	void sizeByJsonPropertyNameShouldWork() {
		// when
		JsonPropertyContainerOuter actual = SUT.giveMeBuilder(JsonPropertyContainerOuter.class)
			.size("body", 0)
			.sample();

		// then
		then(actual.getContents()).isEmpty();
	}

	// A. @JsonProperty + 조작자

	@RepeatedTest(TEST_COUNT)
	void setNullByJsonPropertyName() {
		// when
		JsonPropertyWithNormalField actual = SUT.giveMeBuilder(JsonPropertyWithNormalField.class)
			.setNull("renamed")
			.sample();

		// then
		then(actual.getOriginalField()).isNull();
	}

	@RepeatedTest(TEST_COUNT)
	void setNotNullByJsonPropertyName() {
		// when
		JsonPropertyWithNormalField actual = SUT.giveMeBuilder(JsonPropertyWithNormalField.class)
			.setNotNull("renamed")
			.sample();

		// then
		then(actual.getOriginalField()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void setLazyByJsonPropertyName() {
		// when
		String actual = SUT.giveMeBuilder(JsonPropertyInner.class)
			.setLazy("renamed", () -> "lazy")
			.sample()
			.getOriginalField();

		// then
		then(actual).isEqualTo("lazy");
	}

	@RepeatedTest(TEST_COUNT)
	void setPostConditionByJsonPropertyName() {
		// when
		String actual = SUT.giveMeBuilder(JsonPropertyInner.class)
			.setPostCondition("renamed", String.class, it -> it != null && it.startsWith("a"))
			.sample()
			.getOriginalField();

		// then
		then(actual).startsWith("a");
	}

	@RepeatedTest(TEST_COUNT)
	void setNullNestedJsonPropertyPath() {
		// when
		JsonPropertyOuter actual = SUT.giveMeBuilder(JsonPropertyOuter.class).setNull("inner.renamed").sample();

		// then
		then(actual.getInner()).isNotNull();
		then(actual.getInner().getOriginalField()).isNull();
	}

	@RepeatedTest(TEST_COUNT)
	void setNotNullNestedJsonPropertyPath() {
		// when
		JsonPropertyOuter actual = SUT.giveMeBuilder(JsonPropertyOuter.class).setNotNull("inner.renamed").sample();

		// then
		then(actual.getInner()).isNotNull();
		then(actual.getInner().getOriginalField()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void setNullContainerByJsonPropertyName() {
		// when
		JsonPropertyContainerOuter actual = SUT.giveMeBuilder(JsonPropertyContainerOuter.class)
			.setNull("body")
			.sample();

		// then
		then(actual.getContents()).isNull();
	}

	@RepeatedTest(TEST_COUNT)
	void setNotNullContainerByJsonPropertyName() {
		// when
		JsonPropertyContainerOuter actual = SUT.giveMeBuilder(JsonPropertyContainerOuter.class)
			.setNotNull("body")
			.sample();

		// then
		then(actual.getContents()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void setMultipleJsonPropertyFields() {
		// when
		JsonPropertyMultipleFields actual = SUT.giveMeBuilder(JsonPropertyMultipleFields.class)
			.set("name", "testName")
			.set("age", 25)
			.set("active", true)
			.sample();

		// then
		then(actual.getOriginalName()).isEqualTo("testName");
		then(actual.getOriginalAge()).isEqualTo(25);
		then(actual.isOriginalActive()).isTrue();
	}

	@RepeatedTest(TEST_COUNT)
	void thenApplySetByJsonPropertyName() {
		// when
		JsonPropertyWithNormalField actual = SUT.giveMeBuilder(JsonPropertyWithNormalField.class)
			.thenApply((it, builder) -> builder.set("renamed", "applied"))
			.sample();

		// then
		then(actual.getOriginalField()).isEqualTo("applied");
	}

	@RepeatedTest(TEST_COUNT)
	void acceptIfSetByJsonPropertyName() {
		// when
		JsonPropertyWithNormalField actual = SUT.giveMeBuilder(JsonPropertyWithNormalField.class)
			.acceptIf(it -> true, builder -> builder.set("renamed", "accepted"))
			.sample();

		// then
		then(actual.getOriginalField()).isEqualTo("accepted");
	}

	@RepeatedTest(TEST_COUNT)
	void registerWithJsonPropertyFields() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JacksonPlugin())
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(JsonPropertyWithNormalField.class, fixture ->
				fixture.giveMeBuilder(JsonPropertyWithNormalField.class).set("renamed", "registered")
			)
			.build();

		// when
		JsonPropertyWithNormalField actual = sut.giveMeOne(JsonPropertyWithNormalField.class);

		// then
		then(actual.getOriginalField()).isEqualTo("registered");
	}

	@RepeatedTest(TEST_COUNT)
	void registerThenUserOverrideByJsonPropertyName() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JacksonPlugin())
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(JsonPropertyWithNormalField.class, fixture ->
				fixture.giveMeBuilder(JsonPropertyWithNormalField.class).set("renamed", "registered")
			)
			.build();

		// when
		JsonPropertyWithNormalField actual = sut
			.giveMeBuilder(JsonPropertyWithNormalField.class)
			.set("renamed", "overridden")
			.sample();

		// then
		then(actual.getOriginalField()).isEqualTo("overridden");
	}

	@RepeatedTest(TEST_COUNT)
	void setLazyNestedJsonPropertyPath() {
		// when
		String actual = SUT.giveMeBuilder(JsonPropertyOuter.class)
			.setLazy("inner.renamed", () -> "lazyNested")
			.sample()
			.getInner()
			.getOriginalField();

		// then
		then(actual).isEqualTo("lazyNested");
	}

	@RepeatedTest(TEST_COUNT)
	void sizeAndSetElementByJsonPropertyName() {
		// when
		JsonPropertyContainerOuter actual = SUT.giveMeBuilder(JsonPropertyContainerOuter.class)
			.size("body", 2)
			.set("body[0]", "first")
			.sample();

		// then
		then(actual.getContents()).hasSize(2);
		then(actual.getContents().get(0)).isEqualTo("first");
	}

	// B. @JsonTypeInfo + adapter

	@RepeatedTest(TEST_COUNT)
	void jsonTypeInfoSetSiblingField() {
		// when
		JsonTypeInfoFieldOuter actual = SUT.giveMeBuilder(JsonTypeInfoFieldOuter.class).set("name", "hello").sample();

		// then
		then(actual.getName()).isEqualTo("hello");
		then(actual.getType()).isInstanceOf(Type.class);
	}

	@RepeatedTest(TEST_COUNT)
	void jsonTypeInfoSetNull() {
		// when
		JsonTypeInfoFieldOuter actual = SUT.giveMeBuilder(JsonTypeInfoFieldOuter.class).setNull("type").sample();

		// then
		then(actual.getType()).isNull();
	}

	@RepeatedTest(TEST_COUNT)
	void jsonTypeInfoSetNotNull() {
		// when
		JsonTypeInfoFieldOuter actual = SUT.giveMeBuilder(JsonTypeInfoFieldOuter.class).setNotNull("type").sample();

		// then
		then(actual.getType()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void jsonTypeInfoListSize() {
		// when
		JsonTypeInfoFieldOuter actual = SUT.giveMeBuilder(JsonTypeInfoFieldOuter.class).size("types", 3).sample();

		// then
		then(actual.getTypes()).hasSize(3);
		then(actual.getTypes()).allSatisfy(it -> then(it).isInstanceOf(Type.class));
	}

	@RepeatedTest(TEST_COUNT)
	void jsonTypeInfoSetSiblingFieldAndSetNotNull() {
		// when
		JsonTypeInfoFieldOuter actual = SUT.giveMeBuilder(JsonTypeInfoFieldOuter.class)
			.set("name", "hello")
			.setNotNull("type")
			.sample();

		// then
		then(actual.getName()).isEqualTo("hello");
		then(actual.getType()).isNotNull();
		then(actual.getType()).isInstanceOf(Type.class);
	}

	@RepeatedTest(TEST_COUNT)
	void jsonTypeInfoRegister() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JacksonPlugin())
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(JsonTypeInfoFieldOuter.class, fixture ->
				fixture.giveMeBuilder(JsonTypeInfoFieldOuter.class).set("name", "registered")
			)
			.build();

		// when
		JsonTypeInfoFieldOuter actual = sut.giveMeOne(JsonTypeInfoFieldOuter.class);

		// then
		then(actual.getName()).isEqualTo("registered");
		then(actual.getType()).isInstanceOf(Type.class);
	}

	@RepeatedTest(TEST_COUNT)
	void jsonTypeInfoInterfaceAnnotationsSetNotNull() {
		// when
		TypeWithAnnotationsValue actual = SUT.giveMeBuilder(TypeWithAnnotationsValue.class).setNotNull("type").sample();

		// then
		then(actual.getType()).isNotNull();
		then(actual.getType()).isInstanceOf(TypeWithAnnotations.class);
	}

	// C. @JsonFormat + adapter

	@RepeatedTest(TEST_COUNT)
	void jsonFormatGeneration() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonFormatObject.class));
	}

	@RepeatedTest(TEST_COUNT)
	void jsonFormatSetEnumField() {
		// when
		JsonFormatObject actual = SUT.giveMeBuilder(JsonFormatObject.class).set("enumValue", JsonEnum.TWO).sample();

		// then
		then(actual.getEnumValue()).isEqualTo(JsonEnum.TWO);
	}

	// D. JsonNode + adapter

	@RepeatedTest(TEST_COUNT)
	void jsonNodeFieldGeneration() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonNodeWrapper.class));
	}

	@RepeatedTest(TEST_COUNT)
	void jsonNodeFieldSetNull() {
		// when
		JsonNodeWrapper actual = SUT.giveMeBuilder(JsonNodeWrapper.class).setNull("value").sample();

		// then
		then(actual.getValue()).isNull();
	}

	// E. Constructor + @JsonProperty

	@RepeatedTest(TEST_COUNT)
	void constructorJsonPropertySet() {
		// when
		JsonPropertyConstructor actual = SUT.giveMeBuilder(JsonPropertyConstructor.class).set("id", "test").sample();

		// then
		then(actual.getIdentifier()).isEqualTo("test");
	}

	@RepeatedTest(TEST_COUNT)
	void constructorJsonPropertySetNull() {
		// when
		JsonPropertyConstructor actual = SUT.giveMeBuilder(JsonPropertyConstructor.class).setNull("id").sample();

		// then
		then(actual.getIdentifier()).isNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleNestedValueTypeWithDefaultNotNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JacksonPlugin())
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin()
				.tracer(AdapterTracer.console()))
			.build();

		// when
		PayPreApproval actual = sut.giveMeOne(PayPreApproval.class);

		// then
		then(actual).isNotNull();
		then(actual.getPayMethod()).isNotNull();
		then(actual.getPayMethod().getFirstPayMethod()).isNotNull();
		then(actual.getPayMethod().getFirstPayMethod().getPayMethodType()).isNotNull();
	}

}
