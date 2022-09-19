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

package com.navercorp.fixturemonkey.customizer;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.resolver.*;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.api.expression.ExpressionGenerator;
import com.navercorp.fixturemonkey.arbitrary.AbstractArbitraryExpressionManipulator;
import com.navercorp.fixturemonkey.arbitrary.AbstractArbitrarySet;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpression;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpressionManipulator;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryNullity;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySet;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySetArbitrary;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySetPostCondition;
import com.navercorp.fixturemonkey.arbitrary.BuilderManipulator;
import com.navercorp.fixturemonkey.arbitrary.ContainerSizeManipulator;
import com.navercorp.fixturemonkey.arbitrary.MetadataManipulator;
import com.navercorp.fixturemonkey.arbitrary.PostArbitraryManipulator;

public final class ExpressionSpec {
	private final List<BuilderManipulator> builderManipulators;

	public ExpressionSpec() {
		this(new ArrayList<>());
	}

	public ExpressionSpec(List<BuilderManipulator> builderManipulators) {
		this.builderManipulators = builderManipulators;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public ExpressionSpec set(String expression, @Nullable Object value) {
		if (value == null) {
			return this.setNull(expression);
		} else if (value instanceof Arbitrary) {
			this.set(expression, (Arbitrary<?>)value);
			return this;
		} else if (value instanceof ArbitraryBuilder) {
			return this.setBuilder(expression, (ArbitraryBuilder<?>)value);
		} else if (value instanceof ExpressionSpec) {
			return this.set(expression, (ExpressionSpec)value);
		}

		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ArbitrarySet(fixtureExpression, value));
		return this;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public ExpressionSpec set(String expression, Object value, long limit) {
		if (value == null) {
			return this.setNull(expression);
		} else if (value instanceof Arbitrary) {
			this.set(expression, (Arbitrary<?>)value);
			return this;
		} else if (value instanceof ArbitraryBuilder) {
			return this.setBuilder(expression, (ArbitraryBuilder<?>)value, limit);
		} else if (value instanceof ExpressionSpec) {
			return this.set(expression, (ExpressionSpec)value);
		}

		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ArbitrarySet(fixtureExpression, value, limit));
		return this;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private <T> ExpressionSpec set(String expression, Arbitrary<T> arbitrary) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ArbitrarySetArbitrary(fixtureExpression, arbitrary));
		return this;
	}

	private <T> ExpressionSpec setBuilder(String expression, ArbitraryBuilder<T> builder, long limit) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ArbitrarySetArbitrary<>(fixtureExpression, builder.build(), limit));
		return this;
	}

	private <T> ExpressionSpec setBuilder(String expression, ArbitraryBuilder<T> builder) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ArbitrarySetArbitrary<>(fixtureExpression, builder.build()));
		return this;
	}

	private ExpressionSpec set(String expression, ExpressionSpec spec) {
		if (spec == null) {
			return this.setNull(expression);
		}

		ExpressionSpec copied = spec.copy();
		for (BuilderManipulator arbitraryManipulator : copied.builderManipulators) {
			if (arbitraryManipulator instanceof AbstractArbitraryExpressionManipulator) {
				((AbstractArbitraryExpressionManipulator)arbitraryManipulator).addPrefix(expression);
			}
		}
		this.merge(copied);
		return this;
	}

	@API(since = "0.4.0", status = Status.EXPERIMENTAL)
	private ExpressionSpec set(ExpressionGenerator expressionGenerator, ExpressionSpec spec) {
		return this.set(expressionGenerator.generate(), spec);
	}

	/**
	 * Deprecated Use Set instead.
	 */
	@Deprecated
	public ExpressionSpec setPrefix(String expression, String value) {
		Arbitrary<String> combinedArbitrary = Combinators.combine(Arbitraries.just(value), Arbitraries.strings())
			.as((prefix, fromValue) -> {
				String concatString = prefix + fromValue;
				int remainLength = concatString.length() - prefix.length();
				return concatString.substring(0, Math.max(prefix.length(), remainLength));
			});
		return this.set(expression, combinedArbitrary);
	}

	/**
	 * Deprecated Use Set instead.
	 */
	@Deprecated
	public ExpressionSpec setSuffix(String expression, String value) {
		Arbitrary<String> combinedArbitrary = Combinators.combine(Arbitraries.just(value), Arbitraries.strings())
			.as((suffix, fromValue) -> {
				String concatString = fromValue + suffix;
				int remainLength = concatString.length() - suffix.length();
				return concatString.substring(Math.min(remainLength, suffix.length()));
			});
		return this.set(expression, combinedArbitrary);
	}

	public ExpressionSpec setNull(String expression) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ArbitraryNullity(fixtureExpression, true));
		return this;
	}

	public ExpressionSpec setNotNull(String expression) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ArbitraryNullity(fixtureExpression, false));
		return this;
	}

	public ExpressionSpec size(String expression, int size) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ContainerSizeManipulator(fixtureExpression, size, size));
		return this;
	}

	public ExpressionSpec size(String expression, int min, int max) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ContainerSizeManipulator(fixtureExpression, min, max));
		return this;
	}

	public ExpressionSpec minSize(String expression, int min) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ContainerSizeManipulator(fixtureExpression, min, null));
		return this;
	}

	public ExpressionSpec maxSize(String expression, int max) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ContainerSizeManipulator(fixtureExpression, null, max));
		return this;
	}

	public <T> ExpressionSpec setPostCondition(String expression, Class<T> clazz, Predicate<T> predicate, long count) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ArbitrarySetPostCondition<>(clazz, fixtureExpression, predicate, count));
		return this;
	}

	public <T> ExpressionSpec setPostCondition(String expression, Class<T> clazz, Predicate<T> predicate) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ArbitrarySetPostCondition<>(clazz, fixtureExpression, predicate));
		return this;
	}

	public ExpressionSpec list(String iterableName, Consumer<IterableSpec> iterableSpecSupplier) {
		DefaultIterableSpec iterableSpec = new DefaultIterableSpec(iterableName);
		iterableSpecSupplier.accept(iterableSpec);
		iterableSpec.visit(this);
		return this;
	}

	public ExpressionSpec copy() {
		List<BuilderManipulator> copiedArbitraryManipulators = this.builderManipulators.stream()
			.map(BuilderManipulator::copy)
			.collect(toList());

		return new ExpressionSpec(copiedArbitraryManipulators);
	}

	@SuppressWarnings("rawtypes")
	public ExpressionSpec merge(ExpressionSpec fixtureSpec, boolean overwrite) {
		List<BuilderManipulator> filteredOrderedArbitraryManipulators = fixtureSpec.builderManipulators.stream()
			.filter(it -> !(it instanceof PostArbitraryManipulator) && !(it instanceof MetadataManipulator))
			.map(ArbitraryExpressionManipulator.class::cast)
			.filter(it -> overwrite || !hasOrderedManipulators(it.getArbitraryExpression().toString()))
			.map(BuilderManipulator.class::cast)
			.collect(toList());

		this.builderManipulators.addAll(filteredOrderedArbitraryManipulators);

		List<PostArbitraryManipulator> postArbitraryManipulators = fixtureSpec.builderManipulators.stream()
			.filter(PostArbitraryManipulator.class::isInstance)
			.map(PostArbitraryManipulator.class::cast)
			.collect(toList());

		if (overwrite) {
			this.builderManipulators.removeIf(
				it -> it instanceof PostArbitraryManipulator
					&& fixtureSpec.hasPostArbitraryManipulators(
					((PostArbitraryManipulator<?>)it).getArbitraryExpression().toString()
				)
			); // remove redundant fixtureExpression
			this.builderManipulators.addAll(postArbitraryManipulators);
		} else {
			List<PostArbitraryManipulator> filteredPostArbitraryManipulators = postArbitraryManipulators.stream()
				.filter(it -> !this.hasPostArbitraryManipulators(it.getArbitraryExpression().toString()))
				.collect(toList());
			this.builderManipulators.addAll(filteredPostArbitraryManipulators);
		}

		List<MetadataManipulator> filteredMetadataManipulators = fixtureSpec.builderManipulators.stream()
			.filter(MetadataManipulator.class::isInstance)
			.map(MetadataManipulator.class::cast)
			.filter(it -> overwrite || this.hasMetadata(it.getArbitraryExpression().toString()))
			.collect(toList());

		this.builderManipulators.addAll(filteredMetadataManipulators);

		return this;
	}

	public ExpressionSpec merge(ExpressionSpec fixtureSpec) {
		return this.merge(fixtureSpec, true);
	}

	public ExpressionSpec exclude(String... excludeExpressions) {
		List<ArbitraryExpression> excludeArbitraryExpression = Arrays.stream(excludeExpressions)
			.map(ArbitraryExpression::from)
			.collect(toList());

		this.builderManipulators.removeIf(
			it -> it instanceof AbstractArbitraryExpressionManipulator
				&& excludeArbitraryExpression.contains(
				((AbstractArbitraryExpressionManipulator)it).getArbitraryExpression()
			)
		);
		return this;
	}

	public boolean hasOrderedManipulators(String expression) {
		return this.builderManipulators.stream()
			.filter(it -> !(it instanceof PostArbitraryManipulator) && !(it instanceof MetadataManipulator))
			.map(ArbitraryExpressionManipulator.class::cast)
			.anyMatch(it -> it.getArbitraryExpression().equals(ArbitraryExpression.from(expression)));
	}

	public boolean hasPostArbitraryManipulators(String expression) {
		return this.builderManipulators.stream()
			.filter(PostArbitraryManipulator.class::isInstance)
			.map(PostArbitraryManipulator.class::cast)
			.anyMatch(it -> it.getArbitraryExpression().equals(ArbitraryExpression.from(expression)));
	}

	public boolean hasSet(String expression) {
		return this.builderManipulators.stream()
			.filter(AbstractArbitrarySet.class::isInstance)
			.map(AbstractArbitrarySet.class::cast)
			.anyMatch(it -> it.getArbitraryExpression().equals(ArbitraryExpression.from(expression)));
	}

	public boolean hasPostCondition(String expression) {
		return this.builderManipulators.stream()
			.filter(ArbitrarySetPostCondition.class::isInstance)
			.map(ArbitrarySetPostCondition.class::cast)
			.anyMatch(it -> it.getArbitraryExpression().equals(ArbitraryExpression.from(expression)));
	}

	public boolean hasMetadata(String expression) {
		return this.builderManipulators.stream()
			.filter(MetadataManipulator.class::isInstance)
			.map(MetadataManipulator.class::cast)
			.anyMatch(it -> it.getArbitraryExpression().equals(ArbitraryExpression.from(expression)));
	}

	public Optional<Object> findSetValue(String expression) {
		return this.builderManipulators.stream()
			.filter(AbstractArbitrarySet.class::isInstance)
			.map(AbstractArbitrarySet.class::cast)
			.filter(it -> it.getArbitraryExpression().equals(ArbitraryExpression.from(expression)))
			.map(AbstractArbitrarySet::getInputValue)
			.findAny();
	}

	public List<BuilderManipulator> getBuilderManipulators() {
		return builderManipulators;
	}

	public List<ArbitraryManipulator> getArbitraryManipulators(ArbitraryTraverser traverser, ManipulateOptions manipulateOptions) {
		return this.builderManipulators.stream()
			.filter(it -> !(it instanceof ContainerSizeManipulator))
			.map(new BuilderManipulatorAdapter(traverser, manipulateOptions)::convertToArbitraryManipulator)
			.collect(toList());
	}

	public Map<NodeResolver, ArbitraryContainerInfo> getContainerInfosByNodeResolver(ArbitraryTraverser traverser, ManipulateOptions manipulateOptions) {
		return this.builderManipulators.stream()
			.filter(ContainerSizeManipulator.class::isInstance)
			.map(new BuilderManipulatorAdapter(traverser, manipulateOptions)::convertToContainerInfosByNodeResolverEntry)
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ExpressionSpec that = (ExpressionSpec)obj;
		return builderManipulators.equals(that.builderManipulators);
	}

	@Override
	public int hashCode() {
		return Objects.hash(builderManipulators);
	}
}
