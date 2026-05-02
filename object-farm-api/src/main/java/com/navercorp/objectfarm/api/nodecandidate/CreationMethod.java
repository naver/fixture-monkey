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

/**
 * Represents how an object property is created/set.
 * Each implementation contains the specific reflection object used for creation.
 * <p>
 * This interface provides metadata about how a property value will be set during
 * object instantiation, which is useful for:
 * <ul>
 *   <li>Runtime value setting decisions</li>
 *   <li>Debugging and logging</li>
 *   <li>Introspector selection in fixture-monkey</li>
 * </ul>
 * <p>
 * Example usage (Java 8):
 * <pre>{@code
 * CreationMethod method = new FieldAccessCreationMethod(nameField);
 *
 * if (method instanceof FieldAccessCreationMethod) {
 *     Field field = ((FieldAccessCreationMethod) method).getField();
 *     // use field
 * } else if (method instanceof ConstructorParamCreationMethod) {
 *     ConstructorParamCreationMethod ctorMethod = (ConstructorParamCreationMethod) method;
 *     Constructor<?> ctor = ctorMethod.getConstructor();
 *     int idx = ctorMethod.getParameterIndex();
 *     // use constructor and index
 * }
 * }</pre>
 */
public interface CreationMethod {

	/**
	 * Returns the type of creation method.
	 *
	 * @return the creation method type
	 */
	CreationMethodType getType();

	/**
	 * Enumeration of creation method types.
	 */
	enum CreationMethodType {
		/**
		 * Property is set via direct field access using reflection.
		 */
		FIELD,

		/**
		 * Property is passed as a constructor parameter.
		 */
		CONSTRUCTOR,

		/**
		 * Property is set via a method (setter, builder method, factory method).
		 */
		METHOD,

		/**
		 * Element is accessed via container index (List[i], Map entry, Array[i]).
		 */
		CONTAINER_ELEMENT
	}
}
