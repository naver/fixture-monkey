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

package com.navercorp.fixturemonkey.api.expression;

import java.util.List;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;

@API(since = "1.0.0", status = Status.EXPERIMENTAL)
public final class JoinJavaGetterPropertySelector<T, U> implements JavaGetterPropertySelector<T, U> {
	private final List<ExpressionGenerator> expressionGenerators;

	public JoinJavaGetterPropertySelector(List<ExpressionGenerator> expressionGenerators) {
		this.expressionGenerators = expressionGenerators;
	}

	@Override
	public String generate(PropertyNameResolver propertyNameResolver) {
		return expressionGenerators.stream()
			.map(it -> it.generate(propertyNameResolver))
			.collect(Collectors.joining());
	}
}
