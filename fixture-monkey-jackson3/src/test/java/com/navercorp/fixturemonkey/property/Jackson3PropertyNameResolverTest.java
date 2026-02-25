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

package com.navercorp.fixturemonkey.property;

import static org.assertj.core.api.BDDAssertions.then;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.navercorp.fixturemonkey.api.property.FieldProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.jackson3.property.Jackson3PropertyNameResolver;

class Jackson3PropertyNameResolverTest {
	@Test
	void resolve() throws NoSuchFieldException {
		PropertyNameResolver sut = new Jackson3PropertyNameResolver();
		Field field = JacksonSample.class.getDeclaredField("name");
		Property property = new FieldProperty(field);
		then(sut.resolve(property)).isEqualTo("name");
	}

	@Test
	void resolveWithJsonProperty() throws NoSuchFieldException {
		PropertyNameResolver sut = new Jackson3PropertyNameResolver();
		Field field = JacksonSample.class.getDeclaredField("address");
		Property property = new FieldProperty(field);
		then(sut.resolve(property)).isEqualTo("baseAddress");
	}

	static class JacksonSample {
		private String name;

		@JsonProperty("baseAddress")
		private String address;
	}
}
