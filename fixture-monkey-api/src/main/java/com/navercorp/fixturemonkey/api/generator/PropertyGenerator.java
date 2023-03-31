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

import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "0.4.0", status = Status.MAINTAINED)
@FunctionalInterface
public interface PropertyGenerator {
	@Deprecated // It would be removed in 0.6.0
	default Property generateRootProperty(AnnotatedType annotatedType) {
		throw new UnsupportedOperationException();
	}

	default List<Property> generateObjectChildProperties(AnnotatedType annotatedType) {
		return generateProperties(annotatedType);
	}

	List<Property> generateProperties(AnnotatedType annotatedType);

	@Deprecated // It would be removed in 0.6.0
	default Property generateElementProperty(
		Property containerProperty,
		AnnotatedType elementType,
		@Nullable Integer index,
		int sequence
	) {
		throw new UnsupportedOperationException();
	}

	@Deprecated // It would be removed in 0.6.0
	default Property generateMapEntryElementProperty(
		Property containerProperty,
		Property keyProperty,
		Property valueProperty
	) {
		throw new UnsupportedOperationException();
	}

	@Deprecated // It would be removed in 0.6.0
	default Property generateTupleLikeElementsProperty(
		Property containerProperty,
		List<Property> childProperties,
		@Nullable Integer index
	) {
		throw new UnsupportedOperationException();
	}
}
