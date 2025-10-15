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

import static com.navercorp.fixturemonkey.api.exception.Exceptions.throwAsUnchecked;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

@API(since = "0.5.4", status = Status.INTERNAL)
public abstract class Reflections {

	public static <T> T newInstance(Class<T> clazz) {
		try {
			return newInstance(clazz.getDeclaredConstructor());
		} catch (Throwable t) {
			throw throwAsUnchecked(t);
		}
	}

	public static Object invokeMethod(Method method, Object target, Object... args) {
		if (!method.isAccessible()) {
			method.setAccessible(true);
		}

		try {
			return method.invoke(target, args);
		} catch (Throwable t) {
			throw throwAsUnchecked(t);
		}
	}

	public static <T> T newInstance(Constructor<T> constructor, Object... args) {
		if (!constructor.isAccessible()) {
			constructor.setAccessible(true);
		}

		try {
			return constructor.newInstance(args);
		} catch (Throwable t) {
			throw throwAsUnchecked(t);
		}
	}

	@Nullable
	public static Method findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		Class<?> searchType = clazz;

		while (Object.class != searchType && searchType != null) {
			Method[] methods = searchType.isInterface() ? searchType.getMethods() : searchType.getDeclaredMethods();
			for (Method method : methods) {
				if (isTargetMethod(method, methodName, parameterTypes)) {
					return method;
				}
			}

			for (Class<?> interfaceClazz : searchType.getInterfaces()) {
				Method interfaceMethod = findMethod(interfaceClazz, methodName, parameterTypes);
				if (interfaceMethod != null) {
					return interfaceMethod;
				}
			}

			searchType = searchType.getSuperclass();
		}

		return null;
	}

	public static List<Field> findFields(Class<?> clazz) {
		return findDeclaredFields(clazz);
	}

	private static boolean isTargetMethod(Method method, String methodName, Class<?>[] parameterTypes) {
		if (!method.getName().equals(methodName)) {
			return false;
		}

		if (method.getParameterCount() != parameterTypes.length) {
			return false;
		}

		if (Arrays.equals(method.getParameterTypes(), parameterTypes)) {
			return true;
		}

		return false;
	}

	private static List<Field> findDeclaredFields(Class<?> clazz) {
		List<Field> result = new ArrayList<>(getSuperclassFields(clazz));
		result.addAll(getInterfaceFields(clazz)); // fields in interfaces

		List<Field> localFields = Arrays.stream(clazz.getDeclaredFields())
			.filter(field -> !field.isSynthetic())
			.collect(toList());
		result.addAll(localFields);

		return toUniqueFieldNameList(result);
	}

	private static List<Field> getSuperclassFields(Class<?> clazz) {
		Class<?> superclass = clazz.getSuperclass();
		if (superclass == null || superclass == Object.class) {
			return Collections.emptyList();
		}
		return findDeclaredFields(superclass);
	}

	private static List<Field> getInterfaceFields(Class<?> clazz) {
		List<Field> result = new ArrayList<>();

		for (Class<?> interfaceClass : clazz.getInterfaces()) {
			List<Field> localInterfaceFields = Arrays.asList(interfaceClass.getFields());
			List<Field> superinterfaceFields = getInterfaceFields(interfaceClass);

			result.addAll(superinterfaceFields);
			result.addAll(localInterfaceFields);
		}

		return toUniqueFieldNameList(result);
	}

	private static List<Field> toUniqueFieldNameList(List<Field> fields) {
		return fields.stream()
			.filter(distinctByKey(Field::getName))
			.collect(toList());
	}

	private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Set<Object> seen = ConcurrentHashMap.newKeySet();
		return t -> seen.add(keyExtractor.apply(t));
	}

}
