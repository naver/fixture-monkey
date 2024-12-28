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

import static com.navercorp.fixturemonkey.api.type.Types.generateAnnotatedTypeWithoutAnnotation;

import java.util.List;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "1.0.21", status = Status.EXPERIMENTAL)
public final class InterfaceCandidateConcretePropertyResolver<T> implements CandidateConcretePropertyResolver {
	private final List<Class<? extends T>> implementations;

	public InterfaceCandidateConcretePropertyResolver(List<Class<? extends T>> implementations) {
		this.implementations = implementations;
	}

	@Override
	public List<Property> resolve(Property property) {
		return implementations.stream()
			.map(implementation -> new ConcreteTypeProperty(
				generateAnnotatedTypeWithoutAnnotation(implementation),
				property
			))
			.collect(Collectors.toList());
	}
}
