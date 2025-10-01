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

package com.navercorp.fixturemonkey.jackson.property;

import java.lang.annotation.Annotation;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.random.Randoms;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.2", status = Status.INTERNAL)
public abstract class JacksonAnnotations {
	@Nullable
	public static <T extends Annotation> T getJacksonAnnotation(Property property, Class<T> annotationClass) {
		T propertyAnnotation = property.getAnnotation(annotationClass).orElse(null);
		if (propertyAnnotation != null) {
			return propertyAnnotation;
		}

		Class<?> type = Types.getActualType(property.getType());
		return type.getAnnotation(annotationClass);
	}

	public static Class<?> getRandomJsonSubType(JsonSubTypes jsonSubTypes) {
		Type[] types = jsonSubTypes.value();
		int random = Randoms.nextInt(types.length);
		return types[random].value();
	}
}
