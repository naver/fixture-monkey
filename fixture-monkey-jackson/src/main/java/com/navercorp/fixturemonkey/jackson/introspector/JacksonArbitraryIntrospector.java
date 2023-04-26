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

package com.navercorp.fixturemonkey.jackson.introspector;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.jackson.FixtureMonkeyJackson;

/**
 * It is deprecated since 0.5.4.
 * Use {@link JacksonObjectArbitraryIntrospector} instead.
 *
 * @see JacksonObjectArbitraryIntrospector
 */
@Deprecated
@API(since = "0.4.0", status = Status.DEPRECATED)
public final class JacksonArbitraryIntrospector implements ArbitraryIntrospector {

	public static final JacksonArbitraryIntrospector INSTANCE = new JacksonArbitraryIntrospector(
		FixtureMonkeyJackson.defaultObjectMapper()
	);

	private final JacksonObjectArbitraryIntrospector delegate;

	public JacksonArbitraryIntrospector(ObjectMapper objectMapper) {
		this.delegate = new JacksonObjectArbitraryIntrospector(objectMapper);
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		return delegate.introspect(context);
	}
}
