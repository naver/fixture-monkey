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

import java.util.Arrays;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Builders;
import net.jqwik.api.Builders.BuilderCombinator;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class StreamIntrospector implements ArbitraryIntrospector, Matcher {
	private static final List<Matcher> MATCHERS = Arrays.asList(
		new AssignableTypeMatcher(Stream.class),
		new AssignableTypeMatcher(IntStream.class),
		new AssignableTypeMatcher(LongStream.class),
		new AssignableTypeMatcher(DoubleStream.class)
	);

	@Override
	public boolean match(Property property) {
		return MATCHERS.stream().anyMatch(it -> it.match(property));
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		ArbitraryProperty property = context.getArbitraryProperty();
		ArbitraryContainerInfo containerInfo = property.getContainerInfo();
		if (containerInfo == null) {
			return ArbitraryIntrospectorResult.EMPTY;
		}

		List<Arbitrary<?>> childrenArbitraries = context.getChildrenArbitraries();

		BuilderCombinator<Builder<Object>> builderCombinator = Builders.withBuilder(Stream::builder);
		for (Arbitrary<?> childArbitrary : childrenArbitraries) {
			builderCombinator = builderCombinator.use(childArbitrary).in(Stream.Builder::add);
		}

		return new ArbitraryIntrospectorResult(builderCombinator.build(Stream.Builder::build));
	}
}
