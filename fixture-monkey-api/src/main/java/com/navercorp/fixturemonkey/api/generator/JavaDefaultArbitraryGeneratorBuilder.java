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
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.arbitrary.JavaTimeArbitraryGeneratorSet;
import com.navercorp.fixturemonkey.api.arbitrary.JavaTypeArbitraryGeneratorSet;
import com.navercorp.fixturemonkey.api.introspector.AnonymousArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.introspector.ArrayIntrospector;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.BooleanIntrospector;
import com.navercorp.fixturemonkey.api.introspector.CompositeArbitraryIntrospector;
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
import com.navercorp.fixturemonkey.api.introspector.MapEntryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.MapIntrospector;
import com.navercorp.fixturemonkey.api.introspector.OptionalIntrospector;
import com.navercorp.fixturemonkey.api.introspector.QueueIntrospector;
import com.navercorp.fixturemonkey.api.introspector.SetIntrospector;
import com.navercorp.fixturemonkey.api.introspector.StreamIntrospector;
import com.navercorp.fixturemonkey.api.introspector.UuidIntrospector;
import com.navercorp.fixturemonkey.api.jqwik.JqwikJavaTimeArbitraryGeneratorSet;
import com.navercorp.fixturemonkey.api.jqwik.JqwikJavaTypeArbitraryGeneratorSet;

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
			new MapEntryIntrospector(),
			new MapEntryElementIntrospector(),
			new ArrayIntrospector()
		)
	);
	public static final ArbitraryIntrospector DEFAULT_FALLBACK_INTROSPECTOR =
		(context) -> ArbitraryIntrospectorResult.NOT_INTROSPECTED;

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

	private ArbitraryIntrospector fallbackIntrospector = DEFAULT_FALLBACK_INTROSPECTOR;
	private JavaTypeArbitraryGeneratorSet javaTypeArbitraryGeneratorSet = null;
	private JavaTimeArbitraryGeneratorSet javaTimeArbitraryGeneratorSet = null;

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

	@Deprecated
	public JavaDefaultArbitraryGeneratorBuilder javaTypeArbitraryGenerator(
		JavaTypeArbitraryGenerator javaTypeArbitraryGenerator
	) {
		this.javaTypeArbitraryGenerator = javaTypeArbitraryGenerator;
		return this;
	}

	@Deprecated
	public JavaDefaultArbitraryGeneratorBuilder javaArbitraryResolver(JavaArbitraryResolver javaArbitraryResolver) {
		this.javaArbitraryResolver = javaArbitraryResolver;
		return this;
	}

	@Deprecated
	public JavaDefaultArbitraryGeneratorBuilder javaTimeTypeArbitraryGenerator(
		JavaTimeTypeArbitraryGenerator javaTimeTypeArbitraryGenerator
	) {
		this.javaTimeTypeArbitraryGenerator = javaTimeTypeArbitraryGenerator;
		return this;
	}

	@Deprecated
	public JavaDefaultArbitraryGeneratorBuilder javaTimeArbitraryResolver(
		JavaTimeArbitraryResolver javaTimeArbitraryResolver
	) {
		this.javaTimeArbitraryResolver = javaTimeArbitraryResolver;
		return this;
	}

	public JavaDefaultArbitraryGeneratorBuilder javaTypeArbitraryGeneratorSet(
		JavaTypeArbitraryGeneratorSet javaTypeArbitraryGeneratorSet
	) {
		this.javaTypeArbitraryGeneratorSet = javaTypeArbitraryGeneratorSet;
		return this;
	}

	public JavaDefaultArbitraryGeneratorBuilder javaTimeArbitraryGeneratorSet(
		JavaTimeArbitraryGeneratorSet javaTimeArbitraryGeneratorSet
	) {
		this.javaTimeArbitraryGeneratorSet = javaTimeArbitraryGeneratorSet;
		return this;
	}

	public DefaultArbitraryGenerator build() {
		if (this.fallbackIntrospector == DEFAULT_FALLBACK_INTROSPECTOR) {
			this.fallbackIntrospector = AnonymousArbitraryIntrospector.INSTANCE;
		}

		JavaTypeArbitraryGeneratorSet javaTypeArbitraryGeneratorSet = defaultIfNull(
			this.javaTypeArbitraryGeneratorSet,
			() -> new JqwikJavaTypeArbitraryGeneratorSet(
				javaTypeArbitraryGenerator,
				javaArbitraryResolver
			)
		);

		JavaTimeArbitraryGeneratorSet javaTimeArbitraryGeneratorSet = defaultIfNull(
			this.javaTimeArbitraryGeneratorSet,
			() -> new JqwikJavaTimeArbitraryGeneratorSet(
				javaTimeTypeArbitraryGenerator,
				javaTimeArbitraryResolver
			)
		);

		return new IntrospectedArbitraryGenerator(
			new CompositeArbitraryIntrospector(
				Arrays.asList(
					new JavaArbitraryIntrospector(javaTypeArbitraryGeneratorSet),
					new JavaTimeArbitraryIntrospector(javaTimeArbitraryGeneratorSet),
					this.priorityIntrospector,
					this.containerIntrospector,
					this.objectIntrospector,
					this.fallbackIntrospector
				)
			)
		);
	}

	private static <T> T defaultIfNull(@Nullable T obj, Supplier<T> defaultValue) {
		return obj != null ? obj : defaultValue.get();
	}
}
