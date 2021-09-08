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

package com.navercorp.fixturemonkey;

import static com.navercorp.fixturemonkey.Constants.HEAD_NAME;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators.F3;
import net.jqwik.api.Combinators.F4;

import com.navercorp.fixturemonkey.arbitrary.AbstractArbitrarySet;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpression;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryNullity;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySet;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySetArbitrary;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySetPostCondition;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySpecAny;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryTraverser;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryTree;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryType;
import com.navercorp.fixturemonkey.arbitrary.BuilderManipulator;
import com.navercorp.fixturemonkey.arbitrary.ContainerSizeManipulator;
import com.navercorp.fixturemonkey.arbitrary.LazyValue;
import com.navercorp.fixturemonkey.arbitrary.MetadataManipulator;
import com.navercorp.fixturemonkey.arbitrary.PostArbitraryManipulator;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizer;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizers;
import com.navercorp.fixturemonkey.customizer.ExpressionSpec;
import com.navercorp.fixturemonkey.customizer.WithFixtureCustomizer;
import com.navercorp.fixturemonkey.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.validator.ArbitraryValidator;

public final class ArbitraryBuilder<T> {
	private final ArbitraryTree<T> tree;
	private final ArbitraryTraverser traverser;
	private final List<BuilderManipulator> builderManipulators = new ArrayList<>();
	@SuppressWarnings("rawtypes")
	private final ArbitraryValidator validator;
	private final Map<Class<?>, ArbitraryGenerator> generatorMap;
	private final List<BiConsumer<T, ArbitraryBuilder<T>>> decomposedManipulators;
	private ArbitraryGenerator generator;
	private ArbitraryCustomizers arbitraryCustomizers;
	private boolean validOnly = true;

	@SuppressWarnings({"unchecked", "rawtypes"})
	ArbitraryBuilder(
		Class<T> clazz,
		ArbitraryOption options,
		ArbitraryGenerator generator,
		ArbitraryValidator validator,
		ArbitraryCustomizers arbitraryCustomizers,
		Map<Class<?>, ArbitraryGenerator> generatorMap
	) {
		this(
			new ArbitraryTree<>(
				ArbitraryNode.builder()
					.type(new ArbitraryType(clazz))
					.fieldName("HEAD_NAME")
					.build()
			),
			new ArbitraryTraverser(options),
			generator,
			validator,
			arbitraryCustomizers,
			new ArrayList<>(),
			new ArrayList<>(),
			generatorMap
		);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	ArbitraryBuilder(
		Supplier<T> valueSupplier,
		ArbitraryTraverser traverser,
		ArbitraryGenerator generator,
		ArbitraryValidator validator,
		ArbitraryCustomizers arbitraryCustomizers,
		Map<Class<?>, ArbitraryGenerator> generatorMap
	) {
		this(
			new ArbitraryTree<>(
				ArbitraryNode.builder()
					.value(valueSupplier)
					.fieldName("HEAD_NAME")
					.build()
			),
			traverser,
			generator,
			validator,
			arbitraryCustomizers,
			new ArrayList<>(),
			new ArrayList<>(),
			generatorMap
		);
	}

	@SuppressWarnings("rawtypes")
	private ArbitraryBuilder(
		ArbitraryTree<T> tree,
		ArbitraryTraverser traverser,
		ArbitraryGenerator generator,
		ArbitraryValidator validator,
		ArbitraryCustomizers arbitraryCustomizers,
		List<BuilderManipulator> builderManipulators,
		List<BiConsumer<T, ArbitraryBuilder<T>>> decomposedManipulators,
		Map<Class<?>, ArbitraryGenerator> generatorMap
	) {
		this.tree = tree;
		this.traverser = traverser;
		this.generator = getGenerator(generator, arbitraryCustomizers);
		this.validator = validator;
		this.arbitraryCustomizers = arbitraryCustomizers;
		this.builderManipulators.addAll(builderManipulators);
		this.decomposedManipulators = decomposedManipulators;
		this.generatorMap = generatorMap.entrySet().stream()
			.map(it -> new SimpleEntry<Class<?>, ArbitraryGenerator>(
				it.getKey(),
				getGenerator(it.getValue(), arbitraryCustomizers))
			)
			.collect(toMap(SimpleEntry::getKey, SimpleEntry::getValue));
	}

	public ArbitraryBuilder<T> validOnly(boolean validOnly) {
		this.validOnly = validOnly;
		return this;
	}

	public ArbitraryBuilder<T> generator(ArbitraryGenerator generator) {
		this.generator = getGenerator(generator, arbitraryCustomizers);
		return this;
	}

	@SuppressWarnings("unchecked")
	public Arbitrary<T> build() {
		ArbitraryBuilder<T> buildArbitraryBuilder = this.copy();
		return buildArbitraryBuilder.tree.result(() -> {
			ArbitraryTree<T> buildTree = buildArbitraryBuilder.tree;

			buildArbitraryBuilder.traverser.traverse(
				buildTree,
				false,
				buildArbitraryBuilder.generator
			);

			List<BuilderManipulator> actualManipulators = buildArbitraryBuilder.builderManipulators.stream()
				.filter(this.builderManipulators::contains)
				.collect(toList()); // post-decompose - build manipulators except for build - sample manipulators

			buildArbitraryBuilder.apply(actualManipulators);
			buildTree.update(buildArbitraryBuilder.generator, generatorMap);
			return buildTree.getArbitrary();
		}, this.validator, this.validOnly);
	}

	public T sample() {
		return this.build().sample();
	}

	@SuppressWarnings("unchecked")
	private T sampleInternal() {
		return (T)this.tree.result(() -> {
			ArbitraryTree<T> buildTree = this.tree;

			this.traverser.traverse(
				buildTree,
				false,
				this.generator
			);
			this.apply(this.builderManipulators);
			this.builderManipulators.clear();
			buildTree.update(this.generator, generatorMap);
			return buildTree.getArbitrary();
		}, this.validator, this.validOnly).sample();
	}

	public List<T> sampleList(int size) {
		return this.sampleStream().limit(size).collect(toList());
	}

	public Stream<T> sampleStream() {
		return this.build().sampleStream();
	}

	public ArbitraryBuilder<T> spec(ExpressionSpec expressionSpec) {
		this.builderManipulators.addAll(expressionSpec.getBuilderManipulators());
		return this;
	}

	public ArbitraryBuilder<T> specAny(ExpressionSpec... expressionSpecs) {
		this.builderManipulators.add(new ArbitrarySpecAny(Arrays.asList(expressionSpecs)));
		return this;
	}

	@SuppressWarnings("unchecked")
	public ArbitraryBuilder<T> set(String expression, @Nullable Object value) {
		if (value == null) {
			return this.setNull(expression);
		} else if (value instanceof Arbitrary) {
			return this.set(expression, (Arbitrary<T>)value);
		} else if (value instanceof ArbitraryBuilder) {
			return this.setBuilder(expression, (ArbitraryBuilder<?>)value);
		}
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		this.builderManipulators.add(new ArbitrarySet<>(arbitraryExpression, value));
		return this;
	}

	public ArbitraryBuilder<T> set(String expression, Object value, long limit) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		this.builderManipulators.add(new ArbitrarySet<>(arbitraryExpression, value, limit));
		return this;
	}

	public ArbitraryBuilder<T> set(String expression, @Nullable Arbitrary<?> value) {
		if (value == null) {
			return this.setNull(expression);
		}
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		this.builderManipulators.add(new ArbitrarySetArbitrary<>(arbitraryExpression, value));
		return this;
	}

	public ArbitraryBuilder<T> set(@Nullable Object value) {
		return this.set(HEAD_NAME, value);
	}

	public ArbitraryBuilder<T> setBuilder(String expression, ArbitraryBuilder<?> builder) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		this.builderManipulators.add(new ArbitrarySetArbitrary<>(arbitraryExpression, builder.build()));
		return this;
	}

	public ArbitraryBuilder<T> setBuilder(String expression, ArbitraryBuilder<?> builder, long limit) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		this.builderManipulators.add(new ArbitrarySetArbitrary<>(arbitraryExpression, builder.build(), limit));
		return this;
	}

	public ArbitraryBuilder<T> setNull(String expression) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		this.builderManipulators.add(new ArbitraryNullity(arbitraryExpression, true));
		return this;
	}

	public ArbitraryBuilder<T> setNotNull(String expression) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		this.builderManipulators.add(new ArbitraryNullity(arbitraryExpression, false));
		return this;
	}

	public ArbitraryBuilder<T> setPostCondition(Predicate<T> filter) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(HEAD_NAME);
		this.builderManipulators.add(new ArbitrarySetPostCondition<>(tree.getClazz(), arbitraryExpression, filter));
		return this;
	}

	public <U> ArbitraryBuilder<T> setPostCondition(String expression, Class<U> clazz, Predicate<U> filter) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		this.builderManipulators.add(new ArbitrarySetPostCondition<>(clazz, arbitraryExpression, filter));
		return this;
	}

	public <U> ArbitraryBuilder<T> setPostCondition(
		String expression,
		Class<U> clazz,
		Predicate<U> filter,
		long limit
	) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		this.builderManipulators.add(new ArbitrarySetPostCondition<>(clazz, arbitraryExpression, filter, limit));
		return this;
	}

	public ArbitraryBuilder<T> size(String expression, int size) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ContainerSizeManipulator(arbitraryExpression, size, size));
		return this;
	}

	public ArbitraryBuilder<T> size(String expression, int min, int max) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ContainerSizeManipulator(arbitraryExpression, min, max));
		return this;
	}

	public ArbitraryBuilder<T> minSize(String expression, int min) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(
			new ContainerSizeManipulator(arbitraryExpression, min, null)
		);
		return this;
	}

	public ArbitraryBuilder<T> maxSize(String expression, int max) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		builderManipulators.add(new ContainerSizeManipulator(arbitraryExpression, null, max));
		return this;
	}

	public ArbitraryBuilder<T> customize(Class<T> type, ArbitraryCustomizer<T> customizer) {
		this.arbitraryCustomizers = this.arbitraryCustomizers.mergeWith(
			Collections.singletonMap(type, customizer)
		);

		if (this.generator instanceof WithFixtureCustomizer) {
			this.generator = ((WithFixtureCustomizer)this.generator).withFixtureCustomizers(arbitraryCustomizers);
		}
		return this;
	}

	public <U> ArbitraryBuilder<U> map(Function<T, U> mapper) {
		return new ArbitraryBuilder<>(() -> mapper.apply(this.sample()),
			this.traverser,
			this.generator,
			this.validator,
			this.arbitraryCustomizers,
			this.generatorMap
		);
	}

	public <U, R> ArbitraryBuilder<R> zipWith(
		ArbitraryBuilder<U> other,
		BiFunction<T, U, R> combinator
	) {
		return new ArbitraryBuilder<>(() -> combinator.apply(this.sample(), other.sample()),
			this.traverser,
			this.generator,
			this.validator,
			this.arbitraryCustomizers,
			this.generatorMap
		);
	}

	public <U, V, R> ArbitraryBuilder<R> zipWith(
		ArbitraryBuilder<U> other,
		ArbitraryBuilder<V> another,
		F3<T, U, V, R> combinator
	) {
		return new ArbitraryBuilder<>(() -> combinator.apply(
			this.sample(),
			other.sample(),
			another.sample()
		),
			this.traverser,
			this.generator,
			this.validator,
			this.arbitraryCustomizers,
			this.generatorMap);
	}

	public <U, V, W, R> ArbitraryBuilder<R> zipWith(
		ArbitraryBuilder<U> other,
		ArbitraryBuilder<V> another,
		ArbitraryBuilder<W> theOther,
		F4<T, U, V, W, R> combinator
	) {
		return new ArbitraryBuilder<>(() -> combinator.apply(
			this.sample(),
			other.sample(),
			another.sample(),
			theOther.sample()
		),
			this.traverser,
			this.generator,
			this.validator,
			this.arbitraryCustomizers,
			this.generatorMap);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public <U> ArbitraryBuilder<U> zipWith(
		List<ArbitraryBuilder<?>> others,
		Function<List<?>, U> combinator
	) {
		return new ArbitraryBuilder<>(() -> {
			List combinedList = new ArrayList<>();
			combinedList.add(this.sample());
			for (ArbitraryBuilder<?> other : others) {
				combinedList.add(other.sample());
			}
			return combinator.apply(combinedList);
		},
			this.traverser,
			this.generator,
			this.validator,
			this.arbitraryCustomizers,
			this.generatorMap
		);
	}

	public ArbitraryBuilder<T> apply(BiConsumer<T, ArbitraryBuilder<T>> biConsumer) {
		ArbitraryBuilder<T> appliedBuilder = this.copy();

		this.decomposedManipulators.add(biConsumer);
		this.tree.setDecomposedValue(() -> {
			ArbitraryBuilder<T> copied = appliedBuilder.copy();
			T sample = copied.sampleInternal();
			copied.tree.setDecomposedValue(() -> sample); // fix builder value
			this.decomposedManipulators.forEach(it -> it.accept(sample, copied));
			this.builderManipulators.removeAll(appliedBuilder.builderManipulators); // remove pre-decompose manipulators
			return copied.sampleInternal();
		});

		return this;
	}

	public ArbitraryBuilder<T> acceptIf(Predicate<T> predicate, Consumer<ArbitraryBuilder<T>> consumer) {
		return this.apply((obj, builder) -> {
			if (predicate.test(obj)) {
				consumer.accept(builder);
			}
		});
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public ArbitraryBuilder<T> apply(MetadataManipulator manipulator) {
		ArbitraryExpression arbitraryExpression = manipulator.getArbitraryExpression();

		if (manipulator instanceof ContainerSizeManipulator) {
			ContainerSizeManipulator containerSizeManipulator = ((ContainerSizeManipulator)manipulator);
			Integer min = containerSizeManipulator.getMin();
			Integer max = containerSizeManipulator.getMax();

			Collection<ArbitraryNode> foundNodes = tree.findAll(arbitraryExpression);
			for (ArbitraryNode foundNode : foundNodes) {
				if (!foundNode.getType().isContainer()) {
					throw new IllegalArgumentException("Only Container can set size");
				}
				foundNode.setContainerMinSize(min);
				foundNode.setContainerMaxSize(max);
				traverser.traverse(foundNode, false, generator); // regenerate subtree
			}
		} else {
			throw new IllegalArgumentException("Not Implemented MetadataManipulator");
		}
		return this;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public void apply(AbstractArbitrarySet<T> fixtureSet) {
		Collection<ArbitraryNode> foundNodes = tree.findAll(fixtureSet.getArbitraryExpression());

		if (!foundNodes.isEmpty()) {
			for (ArbitraryNode<T> foundNode : foundNodes) {
				foundNode.apply(fixtureSet);
			}
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public ArbitraryBuilder<T> setNullity(ArbitraryNullity arbitraryNullity) {
		ArbitraryExpression arbitraryExpression = arbitraryNullity.getArbitraryExpression();
		Collection<ArbitraryNode> foundNodes = tree.findAll(arbitraryExpression);
		for (ArbitraryNode foundNode : foundNodes) {
			LazyValue<T> value = foundNode.getValue();
			if (!arbitraryNullity.toNull() && value != null && value.isEmpty()) { // decompose null value
				foundNode.clearValue();
				traverser.traverse(foundNode, foundNode.isKeyOfMapStructure(), generator);
			} else {
				foundNode.apply(arbitraryNullity);
			}
		}
		return this;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public ArbitraryBuilder<T> apply(PostArbitraryManipulator<T> postArbitraryManipulator) {
		Collection<ArbitraryNode> foundNodes = tree.findAll(postArbitraryManipulator.getArbitraryExpression());
		if (!foundNodes.isEmpty()) {
			for (ArbitraryNode<T> foundNode : foundNodes) {
				if (postArbitraryManipulator.isMappableTo(foundNode)) {
					foundNode.addPostArbitraryOperation(postArbitraryManipulator);
				}
			}
		}
		return this;
	}

	@SuppressWarnings("rawtypes")
	public void apply(List<BuilderManipulator> arbitraryManipulators) {
		List<MetadataManipulator> metadataManipulators = this.extractMetadataManipulatorsFrom(arbitraryManipulators);
		List<BuilderManipulator> orderedArbitraryManipulators =
			this.extractOrderedManipulatorsFrom(arbitraryManipulators);
		List<PostArbitraryManipulator> postArbitraryManipulators =
			this.extractPostArbitraryManipulatorsFrom(arbitraryManipulators);

		metadataManipulators.stream().sorted().forEachOrdered(it -> it.accept(this));
		orderedArbitraryManipulators.forEach(it -> it.accept(this));
		postArbitraryManipulators.forEach(it -> it.accept(this));
	}

	public ArbitraryBuilder<T> copy() {
		return new ArbitraryBuilder<>(
			this.tree.copy(),
			this.traverser,
			this.generator,
			this.validator,
			this.arbitraryCustomizers,
			this.builderManipulators.stream().map(BuilderManipulator::copy).collect(toList()),
			new ArrayList<>(this.decomposedManipulators),
			this.generatorMap
		);
	}

	private List<BuilderManipulator> extractOrderedManipulatorsFrom(List<BuilderManipulator> manipulators) {
		return manipulators.stream()
			.filter(it -> !(it instanceof MetadataManipulator))
			.filter(it -> !(it instanceof PostArbitraryManipulator))
			.collect(toList());
	}

	private List<MetadataManipulator> extractMetadataManipulatorsFrom(List<BuilderManipulator> manipulators) {
		return manipulators.stream()
			.filter(MetadataManipulator.class::isInstance)
			.map(MetadataManipulator.class::cast)
			.sorted()
			.collect(toList());
	}

	@SuppressWarnings("rawtypes")
	private List<PostArbitraryManipulator> extractPostArbitraryManipulatorsFrom(
		List<BuilderManipulator> manipulators
	) {
		return manipulators.stream()
			.filter(PostArbitraryManipulator.class::isInstance)
			.map(PostArbitraryManipulator.class::cast)
			.collect(toList());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ArbitraryBuilder<?> that = (ArbitraryBuilder<?>)obj;
		Class<?> generateClazz = tree.getClazz();
		Class<?> thatGenerateClazz = that.tree.getClazz();

		return generateClazz.equals(thatGenerateClazz)
			&& builderManipulators.equals(that.builderManipulators);
	}

	@Override
	public int hashCode() {
		Class<?> generateClazz = tree.getClazz();
		return Objects.hash(generateClazz, builderManipulators);
	}

	private ArbitraryGenerator getGenerator(ArbitraryGenerator generator, ArbitraryCustomizers customizers) {
		if (generator instanceof WithFixtureCustomizer) {
			generator = ((WithFixtureCustomizer)generator).withFixtureCustomizers(customizers);
		}
		return generator;
	}
}
