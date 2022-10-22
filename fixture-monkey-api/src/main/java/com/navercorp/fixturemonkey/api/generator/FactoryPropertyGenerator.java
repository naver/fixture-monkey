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
import java.util.List;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.ElementProperty;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyCache;
import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.api.property.TupleLikeElementsProperty;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class FactoryPropertyGenerator implements PropertyGenerator {
	@Override
	public Property generateRootProperty(AnnotatedType annotatedType) {
		return new RootProperty(annotatedType);
	}

	@Override
	public List<Property> generateObjectChildProperties(AnnotatedType annotatedType) {
		return PropertyCache.getFactoryProperties(annotatedType);
	}

	@Override
	public Property generateElementProperty(
		Property containerProperty,
		AnnotatedType elementType,
		@Nullable Integer index,
		int sequence
	) {
		return new ElementProperty(containerProperty, elementType, index, sequence);
	}

	@Override
	public Property generateMapEntryElementProperty(
		Property containerProperty,
		Property keyProperty,
		Property valueProperty
	) {
		return new MapEntryElementProperty(containerProperty, keyProperty, valueProperty);
	}

	@Override
	public Property generateTupleLikeElementsProperty(
		Property containerProperty,
		List<Property> childProperties,
		@Nullable Integer index
	) {
		return new TupleLikeElementsProperty(containerProperty, childProperties, index);
	}
}
