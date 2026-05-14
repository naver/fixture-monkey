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

package com.navercorp.fixturemonkey.tests.java;

import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNoException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.adapter.tracing.AssemblyTracer;
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

class JacksonTest {

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.plugin(new JacksonPlugin())
		.defaultNotNull(true)
		.build();

	@Test
	void jsonTypeInfoName() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonTypeInfoIdName.class));
	}

	@Test
	void jsonTypeInfoList() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonTypeInfoList.class));
	}

	@Test
	void jsonTypeInfoIdClass() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonTypeInfoIdClass.class));
	}

	@Test
	void jsonTypeWithAnnotations() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(TypeWithAnnotationsValue.class));
	}

	@Test
	void jsonTypeWithAnnotationsList() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(TypeWithAnnotationsList.class));
	}

	@Test
	void jsonTypeInfoListInSetter() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonTypeInfoListInSetter.class));
	}

	@Test
	void jsonTypeInfoListIncludeWrapperObject() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonTypeInfoListIncludeWrapperObject.class));
	}

	@Test
	void jsonTypeInfoListInSetterIncludeWrapperObject() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonTypeInfoListInSetterIncludeWrapperObject.class));
	}

	@Test
	void jsonTypeWithAnnotationsIncludeWrapperObjectList() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(TypeWithAnnotationsIncludeWrapperObjectList.class));
	}

	@Test
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

	@Test
	void sampleListType() {
		List<JavaTypeObject> actual = SUT.giveMeOne(new TypeReference<List<JavaTypeObject>>() {
		});

		then(actual).isNotNull();
	}

	@Test
	void fixedListType() {
		List<JavaTypeObject> actual = SUT.giveMeBuilder(new TypeReference<List<JavaTypeObject>>() {
		}).fixed().sample();

		then(actual).isNotNull();
	}

	@Test
	void sampleSetType() {
		Set<JavaTypeObject> actual = SUT.giveMeOne(new TypeReference<Set<JavaTypeObject>>() {
		});

		then(actual).isNotNull();
	}

	@Test
	void fixedSetType() {
		Set<JavaTypeObject> actual = SUT.giveMeBuilder(new TypeReference<Set<JavaTypeObject>>() {
		}).fixed().sample();

		then(actual).isNotNull();
	}

	@Test
	void sampleArrayType() {
		JavaTypeObject[] actual = SUT.giveMeOne(new TypeReference<JavaTypeObject[]>() {
		});

		then(actual).isNotNull();
	}

	@Test
	void fixedArrayType() {
		JavaTypeObject[] actual = SUT.giveMeBuilder(new TypeReference<JavaTypeObject[]>() {
		}).fixed().sample();

		then(actual).isNotNull();
	}

	@Test
	void sampleOptionalType() {
		Optional<JavaTypeObject> actual = SUT.giveMeOne(new TypeReference<Optional<JavaTypeObject>>() {
		});

		then(actual).isNotNull();
	}

	@Test
	void fixedOptionalType() {
		Optional<JavaTypeObject> actual = SUT.giveMeBuilder(new TypeReference<Optional<JavaTypeObject>>() {
			})
			.fixed()
			.sample();

		then(actual).isNotNull();
	}

	@Test
	void sampleMapType() {
		Map<String, JavaTypeObject> actual = SUT.giveMeOne(new TypeReference<Map<String, JavaTypeObject>>() {
		});

		then(actual).isNotNull();
	}

	@Test
	void fixedMapType() {
		Map<String, JavaTypeObject> actual = SUT.giveMeBuilder(new TypeReference<Map<String, JavaTypeObject>>() {
			})
			.fixed()
			.sample();

		then(actual).isNotNull();
	}

	@Test
	void sampleMapEntryType() {
		Map.Entry<String, JavaTypeObject> actual = SUT.giveMeOne(
			new TypeReference<Map.Entry<String, JavaTypeObject>>() {
			}
		);

		then(actual).isNotNull();
	}

	@Test
	void fixedMapEntryType() {
		Map.Entry<String, JavaTypeObject> actual = SUT.giveMeBuilder(
				new TypeReference<Map.Entry<String, JavaTypeObject>>() {
				}
			)
			.fixed()
			.sample();

		then(actual).isNotNull();
	}

	@Test
	void sampleEnumKeyMap() {
		thenNoException().isThrownBy(() ->
			SUT.giveMeBuilder(new TypeReference<List<Map<Enum, String>>>() {
			}).size("$", 2).sample()
		);
	}

	@Test
	void sampleConstructorObject() {
		ConstructorObject actual = SUT.giveMeOne(ConstructorObject.class);

		then(actual).isNotNull();
	}

	@Test
	void setDecomposedValueShouldRespectJsonProperty() {
		// when
		JsonPropertyOuter actual = SUT.giveMeBuilder(JsonPropertyOuter.class)
			.set("inner", new JsonPropertyInner("expected"))
			.sample();

		// then
		then(actual.getInner()).isNotNull();
		then(actual.getInner().getOriginalField()).isEqualTo("expected");
	}

	@Test
	void setDecomposedValueJsonPropertyShouldRespectJsonProperty() {
		// when
		JsonPropertyWithOuter actual = SUT.giveMeBuilder(JsonPropertyWithOuter.class)
			.set("outer", new JsonPropertyInner("expected"))
			.sample();

		// then
		then(actual.getInner()).isNotNull();
		then(actual.getInner().getOriginalField()).isEqualTo("expected");
	}

	@Test
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

	@Test
	void setDecomposedValueShouldRespectJsonPropertyDiff() {
		// when
		String actual = SUT.giveMeBuilder(JsonPropertyInner.class)
			.set("renamed", "expected")
			.sample()
			.getOriginalField();

		// then
		then(actual).isEqualTo("expected");
	}

	@Test
	void sizeByJsonPropertyNameShouldWork() {
		// when
		JsonPropertyContainerOuter actual = SUT.giveMeBuilder(JsonPropertyContainerOuter.class)
			.size("body", 0)
			.sample();

		// then
		then(actual.getContents()).isEmpty();
	}

	// A. @JsonProperty + 조작자

	@Test
	void setNullByJsonPropertyName() {
		// when
		JsonPropertyWithNormalField actual = SUT.giveMeBuilder(JsonPropertyWithNormalField.class)
			.setNull("renamed")
			.sample();

		// then
		then(actual.getOriginalField()).isNull();
	}

	@Test
	void setNotNullByJsonPropertyName() {
		// when
		JsonPropertyWithNormalField actual = SUT.giveMeBuilder(JsonPropertyWithNormalField.class)
			.setNotNull("renamed")
			.sample();

		// then
		then(actual.getOriginalField()).isNotNull();
	}

	@Test
	void setLazyByJsonPropertyName() {
		// when
		String actual = SUT.giveMeBuilder(JsonPropertyInner.class)
			.setLazy("renamed", () -> "lazy")
			.sample()
			.getOriginalField();

		// then
		then(actual).isEqualTo("lazy");
	}

	@Test
	void setPostConditionByJsonPropertyName() {
		// when
		String actual = SUT.giveMeBuilder(JsonPropertyInner.class)
			.setPostCondition("renamed", String.class, it -> it != null && it.startsWith("a"))
			.sample()
			.getOriginalField();

		// then
		then(actual).startsWith("a");
	}

	@Test
	void setNullNestedJsonPropertyPath() {
		// when
		JsonPropertyOuter actual = SUT.giveMeBuilder(JsonPropertyOuter.class).setNull("inner.renamed").sample();

		// then
		then(actual.getInner()).isNotNull();
		then(actual.getInner().getOriginalField()).isNull();
	}

	@Test
	void setNotNullNestedJsonPropertyPath() {
		// when
		JsonPropertyOuter actual = SUT.giveMeBuilder(JsonPropertyOuter.class).setNotNull("inner.renamed").sample();

		// then
		then(actual.getInner()).isNotNull();
		then(actual.getInner().getOriginalField()).isNotNull();
	}

	@Test
	void setNullContainerByJsonPropertyName() {
		// when
		JsonPropertyContainerOuter actual = SUT.giveMeBuilder(JsonPropertyContainerOuter.class)
			.setNull("body")
			.sample();

		// then
		then(actual.getContents()).isNull();
	}

	@Test
	void setNotNullContainerByJsonPropertyName() {
		// when
		JsonPropertyContainerOuter actual = SUT.giveMeBuilder(JsonPropertyContainerOuter.class)
			.setNotNull("body")
			.sample();

		// then
		then(actual.getContents()).isNotNull();
	}

	@Test
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

	@Test
	void thenApplySetByJsonPropertyName() {
		// when
		JsonPropertyWithNormalField actual = SUT.giveMeBuilder(JsonPropertyWithNormalField.class)
			.thenApply((it, builder) -> builder.set("renamed", "applied"))
			.sample();

		// then
		then(actual.getOriginalField()).isEqualTo("applied");
	}

	@Test
	void acceptIfSetByJsonPropertyName() {
		// when
		JsonPropertyWithNormalField actual = SUT.giveMeBuilder(JsonPropertyWithNormalField.class)
			.acceptIf(it -> true, builder -> builder.set("renamed", "accepted"))
			.sample();

		// then
		then(actual.getOriginalField()).isEqualTo("accepted");
	}

	@Test
	void registerWithJsonPropertyFields() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JacksonPlugin())
			.defaultNotNull(true)
			.register(JsonPropertyWithNormalField.class, fixture ->
				fixture.giveMeBuilder(JsonPropertyWithNormalField.class).set("renamed", "registered")
			)
			.build();

		// when
		JsonPropertyWithNormalField actual = sut.giveMeOne(JsonPropertyWithNormalField.class);

		// then
		then(actual.getOriginalField()).isEqualTo("registered");
	}

	@Test
	void registerThenUserOverrideByJsonPropertyName() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JacksonPlugin())
			.defaultNotNull(true)
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

	@Test
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

	@Test
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

	@Test
	void jsonTypeInfoSetSiblingField() {
		// when
		JsonTypeInfoFieldOuter actual = SUT.giveMeBuilder(JsonTypeInfoFieldOuter.class).set("name", "hello").sample();

		// then
		then(actual.getName()).isEqualTo("hello");
		then(actual.getType()).isInstanceOf(Type.class);
	}

	@Test
	void jsonTypeInfoSetNull() {
		// when
		JsonTypeInfoFieldOuter actual = SUT.giveMeBuilder(JsonTypeInfoFieldOuter.class).setNull("type").sample();

		// then
		then(actual.getType()).isNull();
	}

	@Test
	void jsonTypeInfoSetNotNull() {
		// when
		JsonTypeInfoFieldOuter actual = SUT.giveMeBuilder(JsonTypeInfoFieldOuter.class).setNotNull("type").sample();

		// then
		then(actual.getType()).isNotNull();
	}

	@Test
	void jsonTypeInfoListSize() {
		// when
		JsonTypeInfoFieldOuter actual = SUT.giveMeBuilder(JsonTypeInfoFieldOuter.class).size("types", 3).sample();

		// then
		then(actual.getTypes()).hasSize(3);
		then(actual.getTypes()).allSatisfy(it -> then(it).isInstanceOf(Type.class));
	}

	@Test
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

	@Test
	void jsonTypeInfoRegister() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JacksonPlugin())
			.defaultNotNull(true)
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

	@Test
	void jsonTypeInfoInterfaceAnnotationsSetNotNull() {
		// when
		TypeWithAnnotationsValue actual = SUT.giveMeBuilder(TypeWithAnnotationsValue.class).setNotNull("type").sample();

		// then
		then(actual.getType()).isNotNull();
		then(actual.getType()).isInstanceOf(TypeWithAnnotations.class);
	}

	// C. @JsonFormat + adapter

	@Test
	void jsonFormatGeneration() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonFormatObject.class));
	}

	@Test
	void jsonFormatSetEnumField() {
		// when
		JsonFormatObject actual = SUT.giveMeBuilder(JsonFormatObject.class).set("enumValue", JsonEnum.TWO).sample();

		// then
		then(actual.getEnumValue()).isEqualTo(JsonEnum.TWO);
	}

	// D. JsonNode + adapter

	@Test
	void jsonNodeFieldGeneration() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonNodeWrapper.class));
	}

	@Test
	void jsonNodeFieldSetNull() {
		// when
		JsonNodeWrapper actual = SUT.giveMeBuilder(JsonNodeWrapper.class).setNull("value").sample();

		// then
		then(actual.getValue()).isNull();
	}

	// E. Constructor + @JsonProperty

	@Test
	void constructorJsonPropertySet() {
		// when
		JsonPropertyConstructor actual = SUT.giveMeBuilder(JsonPropertyConstructor.class).set("id", "test").sample();

		// then
		then(actual.getIdentifier()).isEqualTo("test");
	}

	@Test
	void constructorJsonPropertySetNull() {
		// when
		JsonPropertyConstructor actual = SUT.giveMeBuilder(JsonPropertyConstructor.class).setNull("id").sample();

		// then
		then(actual.getIdentifier()).isNull();
	}

	@Test
	void sampleNestedValueTypeWithDefaultNotNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JacksonPlugin())
			.defaultNotNull(true)
			.tracer(AssemblyTracer.console())
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
