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

package com.navercorp.fixturemonkey.builder;

import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.arbitrary.BuilderManipulator;
import com.navercorp.fixturemonkey.resolver.ArbitraryResolver;
import com.navercorp.fixturemonkey.validator.ArbitraryValidator;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ArbitraryBuilder<T> {
	private final RootProperty rootProperty;
	private final List<BuilderManipulator> manipulators;
	private final ArbitraryResolver resolver;
	private final ArbitraryValidator validator;
	private boolean validOnly = true;

	public ArbitraryBuilder(
		RootProperty rootProperty,
		List<BuilderManipulator> manipulators,
		ArbitraryResolver resolver,
		ArbitraryValidator validator
	) {
		this.rootProperty = rootProperty;
		this.manipulators = manipulators;
		this.resolver = resolver;
		this.validator = validator;
	}

	public ArbitraryBuilder<T> validOnly(boolean validOnly) {
		this.validOnly = validOnly;
		return this;
	}

	@SuppressWarnings("unchecked")
	public Arbitrary<T> build() {
		return new ArbitraryValue<>(
			() -> (Arbitrary<T>)this.resolver.resolve(this.rootProperty, this.manipulators),
			this.validator,
			this.validOnly
		);
	}
}
