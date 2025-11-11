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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorLoggingContext;

@API(since = "0.6.0", status = Status.MAINTAINED)
public final class FailoverIntrospector implements ArbitraryIntrospector {
	private static final Logger LOGGER = LoggerFactory.getLogger(FailoverIntrospector.class);

	private final List<ArbitraryIntrospector> introspectors;
	private final boolean enableLoggingFail;

	public FailoverIntrospector(List<ArbitraryIntrospector> introspectors) {
		this(introspectors, true);
	}

	public FailoverIntrospector(List<ArbitraryIntrospector> introspectors, boolean enableLoggingFail) {
		this.introspectors = introspectors;
		this.enableLoggingFail = enableLoggingFail;
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
			} catch (Exception ex) {
				ArbitraryGeneratorLoggingContext loggingContext = context.getLoggingContext();
				if (loggingContext.isEnableLoggingFail() || enableLoggingFail) {
					LOGGER.warn(
						String.format(
							"\"%s\" is failed to introspect \"%s\" type.",
							introspector.getClass().getSimpleName(),
							((Class<?>)context.getResolvedProperty().getType()).getName()
						),
						ex
					);
				}
				// omitted
			}
		}
		if (results.isEmpty()) {
			return ArbitraryIntrospectorResult.NOT_INTROSPECTED;
		}

		return new ArbitraryIntrospectorResult(
			new CombinableArbitrary() {
				@Override
				@SuppressWarnings({"return", "argument"})
				public Object combined() {
					Iterator<FailoverIntrospectorResult> iterator = results.iterator();
					FailoverIntrospectorResult result = null;
					while (iterator.hasNext()) {
						try {
							result = iterator.next();
							return result.getResult().getValue().combined();
						} catch (Exception ex) {
							ArbitraryGeneratorLoggingContext loggingContext = context.getLoggingContext();
							if (loggingContext.isEnableLoggingFail() || enableLoggingFail) {
								LOGGER.warn(
									String.format(
										"\"%s\" is failed to introspect \"%s\" type.",
										Objects.requireNonNull(result).getIntrospector().getClass().getSimpleName(),
										((Class<?>)context.getResolvedProperty().getType()).getName()
									),
									ex
								);
							}
							// omitted
						}
					}
					throw new IllegalArgumentException(
						String.format(
							"Failed to generate type \"%s\"",
							((Class<?>)context.getResolvedProperty().getType()).getSimpleName()
						)
					);
				}

				@SuppressWarnings("argument")
				@Override
				public Object rawValue() {
					Iterator<FailoverIntrospectorResult> iterator = results.iterator();
					FailoverIntrospectorResult result = null;
					while (iterator.hasNext()) {
						try {
							result = iterator.next();
							return result.getResult().getValue().rawValue();
						} catch (Exception ex) {
							ArbitraryGeneratorLoggingContext loggingContext = context.getLoggingContext();
							if (loggingContext.isEnableLoggingFail() || enableLoggingFail) {
								LOGGER.warn(
									String.format(
										"\"%s\" is failed to introspect type \"%s\"",
										Objects.requireNonNull(result).getIntrospector().getClass().getSimpleName(),
										((Class<?>)context.getResolvedProperty().getType()).getName()
									),
									ex
								);
							}
							// omitted
						}
					}
					throw new IllegalArgumentException(
						String.format(
							"Failed to generate type \"%s\"",
							((Class<?>)context.getResolvedProperty().getType()).getSimpleName()
						)
					);
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
