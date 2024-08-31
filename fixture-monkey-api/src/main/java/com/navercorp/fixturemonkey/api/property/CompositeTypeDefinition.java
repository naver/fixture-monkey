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

import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.random.Randoms;

/**
 * It is for internal use only. It can be changed or removed at any time.
 * <p>
 * It is used to resolve the actual generated property when the property has one or more {@link TypeDefinition}s.
 */
@API(since = "1.1.0", status = Status.INTERNAL)
public final class CompositeTypeDefinition implements TypeDefinition {
	private final List<TypeDefinition> typeDefinitions;
	private final TypeDefinition resolvedTypeDefinition;

	public CompositeTypeDefinition(List<TypeDefinition> typeDefinitions) {
		this.typeDefinitions = typeDefinitions;
		this.resolvedTypeDefinition = typeDefinitions.get(Randoms.nextInt(typeDefinitions.size()));
	}

	public List<TypeDefinition> getTypeDefinitions() {
		return typeDefinitions;
	}

	@Override
	public Property getResolvedProperty() {
		return resolvedTypeDefinition.getResolvedProperty();
	}

	@Override
	public PropertyGenerator getPropertyGenerator() {
		return resolvedTypeDefinition.getPropertyGenerator();
	}
}
