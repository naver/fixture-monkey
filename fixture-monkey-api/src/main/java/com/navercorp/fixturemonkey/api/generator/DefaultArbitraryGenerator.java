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

import java.util.Arrays;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.BooleanIntrospector;
import com.navercorp.fixturemonkey.api.introspector.CompositeArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.EnumIntrospector;
import com.navercorp.fixturemonkey.api.introspector.IterableIntrospector;
import com.navercorp.fixturemonkey.api.introspector.IteratorIntrospector;
import com.navercorp.fixturemonkey.api.introspector.JavaArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.JavaTimeArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ListIntrospector;
import com.navercorp.fixturemonkey.api.introspector.MapEntryElementIntrospector;
import com.navercorp.fixturemonkey.api.introspector.MapIntrospector;
import com.navercorp.fixturemonkey.api.introspector.OptionalIntrospector;
import com.navercorp.fixturemonkey.api.introspector.QueueIntrospector;
import com.navercorp.fixturemonkey.api.introspector.SetIntrospector;
import com.navercorp.fixturemonkey.api.introspector.StreamIntrospector;
import com.navercorp.fixturemonkey.api.introspector.TupleLikeElementsIntrospector;
import com.navercorp.fixturemonkey.api.introspector.UuidIntrospector;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class DefaultArbitraryGenerator implements ArbitraryGenerator {
	public static final ArbitraryIntrospector JAVA_INTROSPECTOR = new CompositeArbitraryIntrospector(
		Arrays.asList(
			new JavaArbitraryIntrospector(),
			new JavaTimeArbitraryIntrospector(),
			new BooleanIntrospector(),
			new EnumIntrospector(),
			new UuidIntrospector()
		)
	);
	public static final ArbitraryIntrospector JAVA_CONTAINER_INTROSPECTOR = new CompositeArbitraryIntrospector(
		Arrays.asList(
			new OptionalIntrospector(),
			new ListIntrospector(),
			new SetIntrospector(),
			new QueueIntrospector(),
			new StreamIntrospector(),
			new IterableIntrospector(),
			new IteratorIntrospector(),
			new MapIntrospector(),
			new MapEntryElementIntrospector(),
			new TupleLikeElementsIntrospector()
		)
	);

	private final ArbitraryIntrospector arbitraryIntrospector;

	public DefaultArbitraryGenerator() {
		this(
			Arrays.asList(
				JAVA_INTROSPECTOR,
				JAVA_CONTAINER_INTROSPECTOR,
				BeanArbitraryIntrospector.INSTANCE
			)
		);
	}

	public DefaultArbitraryGenerator(List<ArbitraryIntrospector> arbitraryIntrospectors) {
		this.arbitraryIntrospector = new CompositeArbitraryIntrospector(arbitraryIntrospectors);
	}

	public DefaultArbitraryGenerator(ArbitraryIntrospector arbitraryIntrospector) {
		this.arbitraryIntrospector = arbitraryIntrospector;
	}

	@Override
	public Arbitrary<?> generate(ArbitraryGeneratorContext context) {
		ArbitraryIntrospectorResult result = this.arbitraryIntrospector.introspect(context);
		if (result.getValue() != null) {
			double nullInject = context.getArbitraryProperty().getNullInject();
			return result.getValue()
				.injectNull(nullInject);
		}

		return Arbitraries.just(null);
	}
}
