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

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.property.PropertyCache;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;

class JavaArbitraryTypeIntrospectorTest {
	private final JavaArbitraryTypeIntrospector sut = new JavaArbitraryTypeIntrospector();

	@Property
	void stringMatch() {
		// given
		String propertyName = "str";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context.getType());

		then(actual).isTrue();
	}

	@Property
	void stringIntrospect() {
		// given
		String propertyName = "str";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(String.class);
	}

	@Property
	void charMatch() {
		// given
		String propertyName = "chars";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context.getType());

		then(actual).isTrue();
	}

	@Property
	void charIntrospect() {
		// given
		String propertyName = "chars";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Character.class);
	}

	@Property
	void charWrapperMatch() {
		// given
		String propertyName = "charWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context.getType());

		then(actual).isTrue();
	}

	@Property
	void charWrapperIntrospect() {
		// given
		String propertyName = "charWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Character.class);
	}

	@Property
	void shortMatch() {
		// given
		String propertyName = "shorts";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context.getType());

		then(actual).isTrue();
	}

	@Property
	void shortIntrospect() {
		// given
		String propertyName = "shorts";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Short.class);
	}

	@Property
	void shortWrapperMatch() {
		// given
		String propertyName = "shortWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context.getType());

		then(actual).isTrue();
	}

	@Property
	void shortWrapperIntrospect() {
		// given
		String propertyName = "shortWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Short.class);
	}

	@Property
	void byteMatch() {
		// given
		String propertyName = "bytes";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context.getType());

		then(actual).isTrue();
	}

	@Property
	void byteIntrospect() {
		// given
		String propertyName = "bytes";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Byte.class);
	}

	@Property
	void byteWrapperMatch() {
		// given
		String propertyName = "byteWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context.getType());

		then(actual).isTrue();
	}

	@Property
	void byteWrapperIntrospect() {
		// given
		String propertyName = "byteWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Byte.class);
	}

	@Property
	void doubleMatch() {
		// given
		String propertyName = "doubles";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context.getType());

		then(actual).isTrue();
	}

	@Property
	void doubleIntrospect() {
		// given
		String propertyName = "doubles";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Double.class);
	}

	@Property
	void doubleWrapperMatch() {
		// given
		String propertyName = "doubleWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context.getType());

		then(actual).isTrue();
	}

	@Property
	void doubleWrapperIntrospect() {
		// given
		String propertyName = "doubleWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Double.class);
	}

	@Property
	void floatMatch() {
		// given
		String propertyName = "floats";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context.getType());

		then(actual).isTrue();
	}

	@Property
	void floatIntrospect() {
		// given
		String propertyName = "floats";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Float.class);
	}

	@Property
	void floatWrapperMatch() {
		// given
		String propertyName = "floatWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context.getType());

		then(actual).isTrue();
	}

	@Property
	void floatWrapperIntrospect() {
		// given
		String propertyName = "floatWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Float.class);
	}

	@Property
	void intMatch() {
		// given
		String propertyName = "ints";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context.getType());

		then(actual).isTrue();
	}

	@Property
	void intIntrospect() {
		// given
		String propertyName = "ints";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Integer.class);
	}

	@Property
	void intWrapperMatch() {
		// given
		String propertyName = "intWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context.getType());

		then(actual).isTrue();
	}

	@Property
	void intWrapperIntrospect() {
		// given
		String propertyName = "intWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Integer.class);
	}

	@Property
	void longMatch() {
		// given
		String propertyName = "longs";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context.getType());

		then(actual).isTrue();
	}

	@Property
	void longIntrospect() {
		// given
		String propertyName = "longs";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Long.class);
	}

	@Property
	void longWrapperMatch() {
		// given
		String propertyName = "longWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context.getType());

		then(actual).isTrue();
	}

	@Property
	void longWrapperIntrospect() {
		// given
		String propertyName = "longWrapper";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Long.class);
	}

	@Property
	void bigIntegerMatch() {
		// given
		String propertyName = "bigIntegers";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context.getType());

		then(actual).isTrue();
	}

	@Property
	void bigIntegerIntrospect() {
		// given
		String propertyName = "bigIntegers";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(BigInteger.class);
	}

	@Property
	void bigDecimalMatch() {
		// given
		String propertyName = "bigDecimals";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context.getType());

		then(actual).isTrue();
	}

	@Property
	void bigDecimalIntrospect() {
		// given
		String propertyName = "bigDecimals";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(BigDecimal.class);
	}

	@Property
	void matchFail() {
		// given
		String propertyName = "instant";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		boolean actual = this.sut.match(context.getType());

		then(actual).isFalse();
	}

	@Property
	void introspectEmpty() {
		// given
		String propertyName = "instant";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(JavaTimeArbitraryTypeSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual).isEqualTo(ArbitraryIntrospectorResult.EMPTY);
	}
}
