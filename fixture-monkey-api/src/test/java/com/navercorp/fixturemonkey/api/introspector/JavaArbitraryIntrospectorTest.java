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

package com.navercorp.fixturemonkey.api.introspector;

import static org.assertj.core.api.BDDAssertions.then;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ObjectProperty;
import com.navercorp.fixturemonkey.api.property.PropertyCache;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.type.TypeReference;

@SuppressWarnings({"OptionalGetWithoutIsPresent", "ConstantConditions"})
class JavaArbitraryIntrospectorTest {
	private final JavaArbitraryIntrospector sut = new JavaArbitraryIntrospector();

	@Property
	void stringMatch() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "str";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		boolean actual = this.sut.match(context.getProperty());

		then(actual).isTrue();
	}

	@Property
	void stringIntrospect() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "str";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(String.class);
	}

	@Property
	void charMatch() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "chars";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		boolean actual = this.sut.match(context.getProperty());

		then(actual).isTrue();
	}

	@Property
	void charIntrospect() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "chars";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Character.class);
	}

	@Property
	void charWrapperMatch() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "charWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		boolean actual = this.sut.match(context.getProperty());

		then(actual).isTrue();
	}

	@Property
	void charWrapperIntrospect() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "charWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Character.class);
	}

	@Property
	void shortMatch() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "shorts";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		boolean actual = this.sut.match(context.getProperty());

		then(actual).isTrue();
	}

	@Property
	void shortIntrospect() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "shorts";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Short.class);
	}

	@Property
	void shortWrapperMatch() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "shortWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		boolean actual = this.sut.match(context.getProperty());

		then(actual).isTrue();
	}

	@Property
	void shortWrapperIntrospect() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "shortWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Short.class);
	}

	@Property
	void byteMatch() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "bytes";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		boolean actual = this.sut.match(context.getProperty());

		then(actual).isTrue();
	}

	@Property
	void byteIntrospect() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "bytes";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Byte.class);
	}

	@Property
	void byteWrapperMatch() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "byteWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		boolean actual = this.sut.match(context.getProperty());

		then(actual).isTrue();
	}

	@Property
	void byteWrapperIntrospect() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "byteWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Byte.class);
	}

	@Property
	void doubleMatch() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "doubles";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		boolean actual = this.sut.match(context.getProperty());

		then(actual).isTrue();
	}

	@Property
	void doubleIntrospect() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "doubles";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Double.class);
	}

	@Property
	void doubleWrapperMatch() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "doubleWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		boolean actual = this.sut.match(context.getProperty());

		then(actual).isTrue();
	}

	@Property
	void doubleWrapperIntrospect() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "doubleWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Double.class);
	}

	@Property
	void floatMatch() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "floats";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		boolean actual = this.sut.match(context.getProperty());

		then(actual).isTrue();
	}

	@Property
	void floatIntrospect() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "floats";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Float.class);
	}

	@Property
	void floatWrapperMatch() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "floatWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		boolean actual = this.sut.match(context.getProperty());

		then(actual).isTrue();
	}

	@Property
	void floatWrapperIntrospect() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "floatWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Float.class);
	}

	@Property
	void intMatch() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "ints";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		boolean actual = this.sut.match(context.getProperty());

		then(actual).isTrue();
	}

	@Property
	void intIntrospect() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "ints";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Integer.class);
	}

	@Property
	void intWrapperMatch() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "intWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		boolean actual = this.sut.match(context.getProperty());

		then(actual).isTrue();
	}

	@Property
	void intWrapperIntrospect() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "intWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Integer.class);
	}

	@Property
	void longMatch() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "longs";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		boolean actual = this.sut.match(context.getProperty());

		then(actual).isTrue();
	}

	@Property
	void longIntrospect() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "longs";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Long.class);
	}

	@Property
	void longWrapperMatch() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "longWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		boolean actual = this.sut.match(context.getProperty());

		then(actual).isTrue();
	}

	@Property
	void longWrapperIntrospect() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "longWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Long.class);
	}

	@Property
	void bigIntegerMatch() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "bigIntegers";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		boolean actual = this.sut.match(context.getProperty());

		then(actual).isTrue();
	}

	@Property
	void bigIntegerIntrospect() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "bigIntegers";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(BigInteger.class);
	}

	@Property
	void bigDecimalMatch() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "bigDecimals";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		boolean actual = this.sut.match(context.getProperty());

		then(actual).isTrue();
	}

	@Property
	void bigDecimalIntrospect() {
		// given
		TypeReference<JavaArbitraryTypeSpec> typeReference = new TypeReference<JavaArbitraryTypeSpec>() {
		};
		String propertyName = "bigDecimals";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(BigDecimal.class);
	}

	@Property
	void matchFail() {
		// given
		TypeReference<JavaTimeArbitraryTypeSpec> typeReference = new TypeReference<JavaTimeArbitraryTypeSpec>() {
		};
		String propertyName = "instant";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		boolean actual = this.sut.match(context.getProperty());

		then(actual).isFalse();
	}

	@Property
	void introspectEmpty() {
		// given
		TypeReference<JavaTimeArbitraryTypeSpec> typeReference = new TypeReference<JavaTimeArbitraryTypeSpec>() {
		};
		String propertyName = "instant";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual).isEqualTo(ArbitraryIntrospectorResult.EMPTY);
	}

	private ArbitraryGeneratorContext getArbitraryGeneratorContext(
		com.navercorp.fixturemonkey.api.property.Property property
	) {
		return new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				new ObjectProperty(
					property,
					PropertyNameResolver.IDENTITY,
					0.0D,
					null,
					Collections.emptyList()
				),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null),
			Collections.emptyList()
		);
	}
}
