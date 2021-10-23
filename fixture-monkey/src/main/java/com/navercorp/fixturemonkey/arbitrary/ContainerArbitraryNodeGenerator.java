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

package com.navercorp.fixturemonkey.arbitrary;

import java.util.List;

import com.navercorp.fixturemonkey.api.property.FieldProperty;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.generator.FieldNameResolver;

public interface ContainerArbitraryNodeGenerator {
	default <T> List<ArbitraryNode<?>> generate(ArbitraryNode<T> nowNode, PropertyNameResolver propertyNameResolver) {
		return this.generate(
			nowNode,
			(FieldNameResolver)field -> propertyNameResolver.resolve(new FieldProperty(field))
		);
	}

	/**
	 * Deprecated Use generate instead.
	 */
	@Deprecated
	<T> List<ArbitraryNode<?>> generate(ArbitraryNode<T> nowNode, FieldNameResolver fieldNameResolver);
}
