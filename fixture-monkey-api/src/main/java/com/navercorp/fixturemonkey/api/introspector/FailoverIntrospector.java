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

package com.navercorp.fixturemonkey.api.introspector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.exception.RetryableException;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.property.CompositePropertyGenerator;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;

@API(since = "0.6.0", status = Status.MAINTAINED)
public final class FailoverIntrospector implements ArbitraryIntrospector {
	private static final Logger LOGGER = LoggerFactory.getLogger(FailoverIntrospector.class);

	private final List<ArbitraryIntrospector> introspectors;

	public FailoverIntrospector(List<ArbitraryIntrospector> introspectors) {
		this.introspectors = introspectors;
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		List<FailoverIntrospectorResult> results = new ArrayList<>();
		for (ArbitraryIntrospector introspector : this.introspectors) {
			try {
				ArbitraryIntrospectorResult result = introspector.introspect(context);
				if (!ArbitraryIntrospectorResult.NOT_INTROSPECTED.equals(result)) {
					results.add(new FailoverIntrospectorResult(introspector, result));
				}
			} catch (RetryableException ex) {
				LOGGER.warn(
					String.format(
						"\"%s\" is failed to introspect \"%s\" type.",
						introspector.getClass().getSimpleName(),
						((Class<?>)context.getResolvedProperty().getType()).getName()
					),
					ex
				);
				// omitted
			}
		}
		if (results.isEmpty()) {
			return ArbitraryIntrospectorResult.NOT_INTROSPECTED;
		}

		return new ArbitraryIntrospectorResult(
			new CombinableArbitrary<Object>() {
				@Override
				public Object combined() {
					Exception lastException = null;
					Iterator<FailoverIntrospectorResult> iterator = results.iterator();
					FailoverIntrospectorResult result = null;
					while (iterator.hasNext()) {
						try {
							result = iterator.next();
							return result.getResult().getValue().combined();
						} catch (RetryableException ex) {
							LOGGER.warn(
								String.format(
									"\"%s\" is failed to introspect \"%s\" type.",
									Objects.requireNonNull(result).getIntrospector().getClass().getSimpleName(),
									((Class<?>)context.getResolvedProperty().getType()).getName()
								),
								ex
							);
							lastException = ex;
						}
					}

					if (lastException != null) {
						throw new IllegalArgumentException(lastException.getMessage(), lastException.getCause());
					}
					return NOT_GENERATED;
				}

				@Override
				public Object rawValue() {
					Iterator<FailoverIntrospectorResult> iterator = results.iterator();
					FailoverIntrospectorResult result = null;
					Exception lastException = null;
					while (iterator.hasNext()) {
						try {
							result = iterator.next();
							return result.getResult().getValue().rawValue();
						} catch (RetryableException ex) {
							LOGGER.warn(
								String.format(
									"\"%s\" is failed to introspect type \"%s\"",
									Objects.requireNonNull(result).getIntrospector().getClass().getSimpleName(),
									((Class<?>)context.getResolvedProperty().getType()).getName()
								),
								ex
							);
							lastException = ex;
						}
					}

					if (lastException != null) {
						throw new IllegalArgumentException(lastException.getMessage(), lastException.getCause());
					}
					return NOT_GENERATED;
				}

				@Override
				public void clear() {
					for (FailoverIntrospectorResult result : results) {
						result.getResult().getValue().clear();
					}
				}

				@Override
				public boolean fixed() {
					return results.stream()
						.allMatch(it -> it.getResult().getValue().fixed());
				}
			}
		);
	}

	@Nullable
	@Override
	public PropertyGenerator getRequiredPropertyGenerator(Property property) {
		return introspectors.stream()
			.map(it -> it.getRequiredPropertyGenerator(property))
			.filter(Objects::nonNull)
			.reduce((before, now) -> new CompositePropertyGenerator(Arrays.asList(before, now)))
			.orElse(null);
	}

	private static class FailoverIntrospectorResult {
		private final ArbitraryIntrospector introspector;
		private final ArbitraryIntrospectorResult result;

		public FailoverIntrospectorResult(ArbitraryIntrospector introspector, ArbitraryIntrospectorResult result) {
			this.introspector = introspector;
			this.result = result;
		}

		public ArbitraryIntrospector getIntrospector() {
			return introspector;
		}

		public ArbitraryIntrospectorResult getResult() {
			return result;
		}
	}
}
