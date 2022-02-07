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

package com.navercorp.fixturemonkey.api.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public abstract class TypeReference<T> implements Comparable<TypeReference<T>> {
	private final Type type;

	protected TypeReference() {
		Type superClass = getClass().getGenericSuperclass();
		if (superClass instanceof Class<?>) {
			throw new IllegalArgumentException(
				"Internal error: TypeReference constructed without actual type information"
			);
		}

		this.type = ((ParameterizedType)superClass).getActualTypeArguments()[0];
	}

	protected TypeReference(Class<T> type) {
		this.type = type;
	}

	public Type getType() {
		return this.type;
	}

	@Override
	public int compareTo(TypeReference<T> obj) {
		return 0;
	}
}
