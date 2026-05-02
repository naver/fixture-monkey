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

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Represents a candidate for creating a JvmNode that contains essential information
 * required before actual JvmNode creation.
 * <p>
 * JvmNodeCandidate serves as an intermediate representation that holds necessary metadata
 * and type information. It is later promoted to a JvmNode using a NodePromoter.
 * This two-phase approach (similar to Hibernate's two-phase loading) prevents
 * circular reference issues and allows for better control over the node creation process.
 * <p>
 * NodeCandidate can contain abstract types (e.g., List interface) which are later
 * resolved to concrete types (e.g., ArrayList class) during promotion to JvmNode.
 * <p>
 * The creation flow is: JvmNodeCandidate --[promoted by NodePromoter]--> JvmNode
 */
public interface JvmNodeCandidate {
	/**
	 * Returns the JVM type for this candidate.
	 * <p>
	 * Unlike JvmNode which must return concrete types, NodeCandidate may contain
	 * abstract types such as interfaces or abstract classes. These abstract types
	 * are resolved to concrete implementations during the promotion process.
	 *
	 * @return the JvmType for this candidate, which may be abstract or concrete
	 */
	JvmType getType();

	@Nullable
	String getName();

	/**
	 * Returns how this property will be created/set during object instantiation.
	 * <p>
	 * This metadata indicates the creation strategy:
	 * <ul>
	 *   <li>{@link FieldAccessCreationMethod} - Direct field access via reflection</li>
	 *   <li>{@link ConstructorParamCreationMethod} - Constructor parameter</li>
	 *   <li>{@link MethodInvocationCreationMethod} - Setter, builder, or factory method</li>
	 *   <li>{@link ContainerElementCreationMethod} - Container element by index</li>
	 * </ul>
	 *
	 * @return the creation method, or null if not specified
	 */
	@Nullable
	CreationMethod getCreationMethod();
}
