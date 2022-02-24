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

import java.lang.reflect.AnnotatedType;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.matcher.TypeMatcher;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public class ArbitraryTypeIntrospectDelegator implements ArbitraryTypeIntrospector, TypeMatcher {
	private final TypeMatcher matcher;
	private final ArbitraryTypeIntrospector delegate;

	public ArbitraryTypeIntrospectDelegator(TypeMatcher matcher, ArbitraryTypeIntrospector delegate) {
		this.matcher = matcher;
		this.delegate = delegate;
	}

	@Override
	public boolean match(AnnotatedType type) {
		return this.matcher.match(type);
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		return this.delegate.introspect(context);
	}
}
