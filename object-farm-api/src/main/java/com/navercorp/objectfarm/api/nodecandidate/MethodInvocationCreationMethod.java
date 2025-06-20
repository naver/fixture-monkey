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

package com.navercorp.objectfarm.api.nodecandidate;

import java.lang.reflect.Method;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

/**
 * Property is set via a method (setter, builder method, factory method).
 * <p>
 * This creation method is used when values are set through method invocation,
 * such as in:
 * <ul>
 *   <li>{@code BeanArbitraryIntrospector} - JavaBeans setter methods</li>
 *   <li>{@code BuilderArbitraryIntrospector} - Builder pattern methods</li>
 *   <li>{@code FactoryMethodArbitraryIntrospector} - Static factory methods</li>
 * </ul>
 * <p>
 * Example:
 * <pre>{@code
 * CreationMethod method = new MethodInvocationCreationMethod(setterMethod);
 * Method setter = ((MethodInvocationCreationMethod) method).getMethod();
 * setter.invoke(instance, value);
 * }</pre>
 */
public final class MethodInvocationCreationMethod implements CreationMethod {
	private final Method method;

	/**
	 * Creates a new MethodInvocationCreationMethod.
	 *
	 * @param method the method used to set the property value
	 */
	public MethodInvocationCreationMethod(Method method) {
		this.method = Objects.requireNonNull(method, "Method must not be null");
	}

	@Override
	public CreationMethodType getType() {
		return CreationMethodType.METHOD;
	}

	/**
	 * Returns the method used to set the property value.
	 *
	 * @return the method
	 */
	public Method getMethod() {
		return method;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		MethodInvocationCreationMethod that = (MethodInvocationCreationMethod)obj;
		return Objects.equals(method, that.method);
	}

	@Override
	public int hashCode() {
		return Objects.hash(method);
	}

	@Override
	public String toString() {
		return "MethodInvocationCreationMethod{method=" + method.getDeclaringClass().getSimpleName()
			+ "." + method.getName() + "()}";
	}
}
