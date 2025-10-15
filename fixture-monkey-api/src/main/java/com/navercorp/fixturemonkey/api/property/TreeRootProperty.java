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

package com.navercorp.fixturemonkey.api.property;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.type.Types;

/**
 * It is the property of the root node within the object tree.
 */
@API(since = "1.1.6", status = Status.EXPERIMENTAL)
public interface TreeRootProperty extends Property {
	/**
	 * Retrieves the actual property of the root node.
	 * Type, annotations are included in the returned property.
	 *
	 * @return the actual property of the root node
	 */
	Property getDelgatedProperty();

	@Override
	default Type getType() {
		return getAnnotatedType().getType();
	}

	@Override
	default AnnotatedType getAnnotatedType() {
		return getDelgatedProperty().getAnnotatedType();
	}

	@Override
	default String getName() {
		return "$";
	}

	@Override
	default List<Annotation> getAnnotations() {
		return Collections.emptyList();
	}

	@Nullable
	@Override
	default Object getValue(Object instance) {
		if (Types.getActualType(this.getType()) == instance.getClass()) {
			return instance;
		}

		throw new IllegalArgumentException(
			"RootProperty obj is not a root type. annotatedType: " + this.getAnnotatedType()
				+ ", objType: " + instance.getClass()
		);
	}
}
