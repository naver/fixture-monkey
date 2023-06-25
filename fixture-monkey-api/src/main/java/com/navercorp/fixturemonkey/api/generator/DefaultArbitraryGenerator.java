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

package com.navercorp.fixturemonkey.api.generator;

import java.util.function.Predicate;

import javax.validation.ConstraintViolationException;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.validator.ArbitraryValidator;

@API(since = "0.4.0", status = Status.MAINTAINED)
public class DefaultArbitraryGenerator implements ArbitraryGenerator {
	private final ArbitraryIntrospector arbitraryIntrospector;

	public DefaultArbitraryGenerator(ArbitraryIntrospector arbitraryIntrospector) {
		this.arbitraryIntrospector = arbitraryIntrospector;
	}

	public static JavaDefaultArbitraryGeneratorBuilder javaBuilder() {
		return new JavaDefaultArbitraryGeneratorBuilder();
	}

	@SuppressWarnings("unchecked")
	@Override
	public CombinableArbitrary generate(ArbitraryGeneratorContext context) {
		ArbitraryIntrospectorResult result = this.arbitraryIntrospector.introspect(context);
		if (result.getValue() != null) {
			double nullInject = context.getArbitraryProperty().getObjectProperty().getNullInject();
			ArbitraryValidator arbitraryValidator = context.getArbitraryValidator();

			return result.getValue()
				.injectNull(nullInject)
				.filter(this.validateFilter(arbitraryValidator, context.isValidOnly()));
		}

		return new FixedCombinableArbitrary(null);
	}

	@SuppressWarnings("rawtypes")
	private Predicate validateFilter(ArbitraryValidator validator, boolean validOnly) {
		return fixture -> {
			if (!validOnly) {
				return true;
			}

			if (fixture == null) {
				return true;
			}

			try {
				validator.validate(fixture);
				return true;
			} catch (ConstraintViolationException ex) {
				// dismiss
			}
			return false;
		};
	}
}
