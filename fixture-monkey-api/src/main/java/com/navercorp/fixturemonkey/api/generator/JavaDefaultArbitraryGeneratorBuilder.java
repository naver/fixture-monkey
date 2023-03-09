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
import java.util.function.UnaryOperator;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.introspector.ArrayIntrospector;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.BooleanIntrospector;
import com.navercorp.fixturemonkey.api.introspector.CompositeArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.EntryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.EnumIntrospector;
import com.navercorp.fixturemonkey.api.introspector.IterableIntrospector;
import com.navercorp.fixturemonkey.api.introspector.IteratorIntrospector;
import com.navercorp.fixturemonkey.api.introspector.JavaArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.JavaArbitraryResolver;
import com.navercorp.fixturemonkey.api.introspector.JavaTimeArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.JavaTimeArbitraryResolver;
import com.navercorp.fixturemonkey.api.introspector.JavaTimeTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.introspector.JavaTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.introspector.ListIntrospector;
import com.navercorp.fixturemonkey.api.introspector.MapEntryElementIntrospector;
import com.navercorp.fixturemonkey.api.introspector.MapIntrospector;
import com.navercorp.fixturemonkey.api.introspector.OptionalIntrospector;
import com.navercorp.fixturemonkey.api.introspector.QueueIntrospector;
import com.navercorp.fixturemonkey.api.introspector.SetIntrospector;
import com.navercorp.fixturemonkey.api.introspector.StreamIntrospector;
import com.navercorp.fixturemonkey.api.introspector.TupleLikeElementsIntrospector;
import com.navercorp.fixturemonkey.api.introspector.UuidIntrospector;

@SuppressWarnings("UnusedReturnValue")
@API(since = "0.4.0", status = Status.MAINTAINED)
public final class JavaDefaultArbitraryGeneratorBuilder {
	public static final ArbitraryIntrospector JAVA_INTROSPECTOR = new CompositeArbitraryIntrospector(
		Arrays.asList(
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
			new EntryIntrospector(),
			new MapEntryElementIntrospector(),
			new TupleLikeElementsIntrospector(),
			new ArrayIntrospector()
		)
	);

	private JavaTypeArbitraryGenerator javaTypeArbitraryGenerator = new JavaTypeArbitraryGenerator() {
	};

	private JavaArbitraryResolver javaArbitraryResolver = new JavaArbitraryResolver() {
	};

	private JavaTimeTypeArbitraryGenerator javaTimeTypeArbitraryGenerator = new JavaTimeTypeArbitraryGenerator() {
	};

	private JavaTimeArbitraryResolver javaTimeArbitraryResolver = new JavaTimeArbitraryResolver() {
	};
	private ArbitraryIntrospector priorityIntrospector = JavaDefaultArbitraryGeneratorBuilder.JAVA_INTROSPECTOR;
	private ArbitraryIntrospector containerIntrospector =
		JavaDefaultArbitraryGeneratorBuilder.JAVA_CONTAINER_INTROSPECTOR;
	private ArbitraryIntrospector objectIntrospector = BeanArbitraryIntrospector.INSTANCE;

	private ArbitraryIntrospector fallbackIntrospector = (context) -> ArbitraryIntrospectorResult.EMPTY;

	JavaDefaultArbitraryGeneratorBuilder() {
	}

	public JavaDefaultArbitraryGeneratorBuilder priorityIntrospector(ArbitraryIntrospector priorityIntrospector) {
		this.priorityIntrospector = priorityIntrospector;
		return this;
	}

	public JavaDefaultArbitraryGeneratorBuilder priorityIntrospector(
		UnaryOperator<ArbitraryIntrospector> priorityIntrospector
	) {
		this.priorityIntrospector = priorityIntrospector.apply(this.priorityIntrospector);
		return this;
	}

	public JavaDefaultArbitraryGeneratorBuilder containerIntrospector(ArbitraryIntrospector containerIntrospector) {
		this.containerIntrospector = containerIntrospector;
		return this;
	}

	public JavaDefaultArbitraryGeneratorBuilder containerIntrospector(
		UnaryOperator<ArbitraryIntrospector> containerIntrospector
	) {
		this.containerIntrospector = containerIntrospector.apply(this.containerIntrospector);
		return this;
	}

	public JavaDefaultArbitraryGeneratorBuilder objectIntrospector(ArbitraryIntrospector objectIntrospector) {
		this.objectIntrospector = objectIntrospector;
		return this;
	}

	public JavaDefaultArbitraryGeneratorBuilder objectIntrospector(
		UnaryOperator<ArbitraryIntrospector> objectIntrospector
	) {
		this.objectIntrospector = objectIntrospector.apply(this.objectIntrospector);
		return this;
	}

	public JavaDefaultArbitraryGeneratorBuilder fallbackIntrospector(ArbitraryIntrospector fallbackIntrospector) {
		this.fallbackIntrospector = fallbackIntrospector;
		return this;
	}

	public JavaDefaultArbitraryGeneratorBuilder fallbackIntrospector(
		UnaryOperator<ArbitraryIntrospector> fallbackIntrospector
	) {
		this.fallbackIntrospector = fallbackIntrospector.apply(this.fallbackIntrospector);
		return this;
	}

	public JavaDefaultArbitraryGeneratorBuilder javaTypeArbitraryGenerator(
		JavaTypeArbitraryGenerator javaTypeArbitraryGenerator
	) {
		this.javaTypeArbitraryGenerator = javaTypeArbitraryGenerator;
		return this;
	}

	public JavaDefaultArbitraryGeneratorBuilder javaArbitraryResolver(JavaArbitraryResolver javaArbitraryResolver) {
		this.javaArbitraryResolver = javaArbitraryResolver;
		return this;
	}

	public JavaDefaultArbitraryGeneratorBuilder javaTimeTypeArbitraryGenerator(
		JavaTimeTypeArbitraryGenerator javaTimeTypeArbitraryGenerator
	) {
		this.javaTimeTypeArbitraryGenerator = javaTimeTypeArbitraryGenerator;
		return this;
	}

	public JavaDefaultArbitraryGeneratorBuilder javaTimeArbitraryResolver(
		JavaTimeArbitraryResolver javaTimeArbitraryResolver
	) {
		this.javaTimeArbitraryResolver = javaTimeArbitraryResolver;
		return this;
	}

	public DefaultArbitraryGenerator build() {
		return new DefaultArbitraryGenerator(
			new CompositeArbitraryIntrospector(
				Arrays.asList(
					new JavaArbitraryIntrospector(javaTypeArbitraryGenerator, javaArbitraryResolver),
					new JavaTimeArbitraryIntrospector(
						javaTimeTypeArbitraryGenerator,
						javaTimeArbitraryResolver
					),
					this.priorityIntrospector,
					this.containerIntrospector,
					this.objectIntrospector,
					this.fallbackIntrospector
				)
			)
		);
	}
}
