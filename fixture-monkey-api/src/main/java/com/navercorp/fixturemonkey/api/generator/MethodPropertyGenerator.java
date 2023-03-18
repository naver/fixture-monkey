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

package com.navercorp.fixturemonkey.api.generator;

import java.lang.reflect.AnnotatedType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

import com.navercorp.fixturemonkey.api.property.MethodProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.5.3", status = API.Status.EXPERIMENTAL)
public class MethodPropertyGenerator implements PropertyGenerator {
	@Override
	public Property generateRootProperty(
		AnnotatedType annotatedType
	) {
		return new RootProperty(annotatedType);
	}

	@Override
	public List<Property> generateObjectChildProperties(
		AnnotatedType annotatedType
	) {
		return Arrays.stream(Types.getActualType(annotatedType.getType()).getMethods())
			.map(MethodProperty::new)
			.collect(Collectors.toList());
	}

	@Override
	public Property generateElementProperty(
		Property containerProperty, AnnotatedType elementType,
		@Nullable Integer index, int sequence
	) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Property generateMapEntryElementProperty(
		Property containerProperty,
		Property keyProperty,
		Property valueProperty
	) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Property generateTupleLikeElementsProperty(
		Property containerProperty,
		List<Property> childProperties, @Nullable Integer index
	) {
		throw new UnsupportedOperationException();
	}
}
