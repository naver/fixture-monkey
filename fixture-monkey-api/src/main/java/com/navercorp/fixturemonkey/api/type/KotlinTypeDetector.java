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

import java.lang.annotation.Annotation;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.container.ConcurrentLruCache;

@SuppressWarnings("unchecked")
@API(since = "1.0.0", status = Status.INTERNAL)
public abstract class KotlinTypeDetector {
	@Nullable
	private static final Class<? extends Annotation> kotlinMetadata;
	private static final ConcurrentLruCache<Class<?>, Boolean> IS_KOTLIN_TYPE = new ConcurrentLruCache<>(2048);

	static {
		Class<?> metadata;
		ClassLoader classLoader = KotlinTypeDetector.class.getClassLoader();
		try {
			metadata = Class.forName("kotlin.Metadata", false, classLoader);
		} catch (ClassNotFoundException ex) {
			// Kotlin API not available - no Kotlin support
			metadata = null;
		}
		kotlinMetadata = (Class<? extends Annotation>)metadata;
	}

	public static boolean isKotlinType(Class<?> clazz) {
		return IS_KOTLIN_TYPE.computeIfAbsent(
			clazz,
			type -> kotlinMetadata != null && type.getDeclaredAnnotation(kotlinMetadata) != null
		);
	}
}
