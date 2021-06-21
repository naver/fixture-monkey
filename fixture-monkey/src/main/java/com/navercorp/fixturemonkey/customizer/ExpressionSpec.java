package com.navercorp.fixturemonkey.customizer;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.arbitrary.AbstractArbitrarySet;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpression;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryFilter;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySet;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySetArbitrary;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySetNullity;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySetPrefix;
import com.navercorp.fixturemonkey.arbitrary.ArbitrarySetSuffix;
import com.navercorp.fixturemonkey.arbitrary.ContainerSizeManipulator;
import com.navercorp.fixturemonkey.arbitrary.MetadataManipulator;
import com.navercorp.fixturemonkey.arbitrary.PostArbitraryManipulator;
import com.navercorp.fixturemonkey.arbitrary.PreArbitraryManipulator;

public final class ExpressionSpec {
	@SuppressWarnings("rawtypes")
	private final List<PreArbitraryManipulator> preArbitraryManipulators;
	@SuppressWarnings("rawtypes")
	private final List<PostArbitraryManipulator> postArbitraryManipulators;
	private final List<MetadataManipulator> metadataManipulators;

	public ExpressionSpec() {
		this.preArbitraryManipulators = new ArrayList<>();
		this.postArbitraryManipulators = new ArrayList<>();
		this.metadataManipulators = new ArrayList<>();
	}

	@SuppressWarnings("rawtypes")
	private ExpressionSpec(
		List<PreArbitraryManipulator> preArbitraryManipulators,
		List<PostArbitraryManipulator> postArbitraryManipulators,
		List<MetadataManipulator> metadataManipulators
	) {
		this.preArbitraryManipulators = preArbitraryManipulators;
		this.postArbitraryManipulators = postArbitraryManipulators;
		this.metadataManipulators = metadataManipulators;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public ExpressionSpec set(String expression, Object value) {
		if (value == null) {
			return this.setNull(expression);
		}
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		preArbitraryManipulators.add(new ArbitrarySet(fixtureExpression, value));
		return this;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public ExpressionSpec set(String expression, Object value, long limit) {
		if (value == null) {
			return this.setNull(expression);
		}
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		preArbitraryManipulators.add(new ArbitrarySet(fixtureExpression, value, limit));
		return this;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public <T> ExpressionSpec set(String expression, Arbitrary<T> arbitrary) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		preArbitraryManipulators.add(new ArbitrarySetArbitrary(fixtureExpression, arbitrary));
		return this;
	}

	public ExpressionSpec set(String expression, ExpressionSpec spec) {
		ExpressionSpec copied = spec.copy();
		copied.preArbitraryManipulators.forEach(it -> it.addPrefix(expression));
		copied.postArbitraryManipulators.forEach(it -> it.addPrefix(expression));
		copied.metadataManipulators.forEach(it -> it.addPrefix(expression));
		this.merge(copied);
		return this;
	}

	public ExpressionSpec setPrefix(String expression, String value) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		preArbitraryManipulators.add(new ArbitrarySetPrefix(fixtureExpression, Arbitraries.just(value)));
		return this;
	}

	public ExpressionSpec setSuffix(String expression, String value) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		preArbitraryManipulators.add(new ArbitrarySetSuffix(fixtureExpression, Arbitraries.just(value)));
		return this;
	}

	@SuppressWarnings("rawtypes")
	public ExpressionSpec setNull(String expression) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		preArbitraryManipulators.add(new ArbitrarySetNullity(fixtureExpression, true));
		return this;
	}

	@SuppressWarnings("rawtypes")
	public ExpressionSpec setNotNull(String expression) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		preArbitraryManipulators.add(new ArbitrarySetNullity(fixtureExpression, false));
		return this;
	}

	public ExpressionSpec size(String expression, int min, int max) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		metadataManipulators.add(new ContainerSizeManipulator(fixtureExpression, min, max));
		return this;
	}

	public <T> ExpressionSpec filter(String name, Predicate<T> predicate, long count) {
		filterSpec(Object.class, name, predicate, count);
		return this;
	}

	public <T> ExpressionSpec filter(String name, Predicate<T> predicate) {
		filterSpec(Object.class, name, predicate);
		return this;
	}

	public ExpressionSpec filterByte(String name, Predicate<Byte> bytePredicate) {
		filterSpec(Byte.class, name, bytePredicate);
		return this;
	}

	public ExpressionSpec filterInteger(String name, Predicate<Integer> integerPredicate) {
		filterSpec(Integer.class, name, integerPredicate);
		return this;
	}

	public ExpressionSpec filterLong(String name, Predicate<Long> longPredicate) {
		filterSpec(Long.class, name, longPredicate);
		return this;
	}

	public ExpressionSpec filterFloat(String name, Predicate<Float> floatPredicate) {
		filterSpec(Float.class, name, floatPredicate);
		return this;
	}

	public ExpressionSpec filterDouble(String name, Predicate<Double> doublePredicate) {
		filterSpec(Double.class, name, doublePredicate);
		return this;
	}

	public ExpressionSpec filterCharacter(String name, Predicate<Character> characterPredicate) {
		filterSpec(Character.class, name, characterPredicate);
		return this;
	}

	public ExpressionSpec filterString(String name, Predicate<String> stringPredicate) {
		filterSpec(String.class, name, stringPredicate);
		return this;
	}

	public <T> ExpressionSpec filterList(String name, Predicate<List<T>> predicate) {
		filterSpec(List.class, name, predicate);
		return this;
	}

	public <T> ExpressionSpec filterSet(String name, Predicate<Set<T>> predicate) {
		filterSpec(Set.class, name, predicate);
		return this;
	}

	public <K, V> ExpressionSpec filterMap(String name, Predicate<Map<K, V>> predicate) {
		filterSpec(Map.class, name, predicate);
		return this;
	}

	public ExpressionSpec list(String iterableName, Consumer<IterableSpec> iterableSpecSupplier) {
		DefaultIterableSpec iterableSpec = new DefaultIterableSpec(iterableName);
		iterableSpecSupplier.accept(iterableSpec);
		iterableSpec.visit(this);
		return this;
	}

	@SuppressWarnings("rawtypes")
	public ExpressionSpec copy() {
		List<PreArbitraryManipulator> copiedPreArbitraryManipulators = this.preArbitraryManipulators.stream()
			.map(PreArbitraryManipulator::copy)
			.collect(toList());

		List<PostArbitraryManipulator> copiedPostArbitraryManipulators = this.postArbitraryManipulators.stream()
			.map(PostArbitraryManipulator::copy)
			.collect(toList());

		List<MetadataManipulator> copiedMetadataManipulators = this.metadataManipulators.stream()
			.map(MetadataManipulator::copy)
			.collect(toList());

		return new ExpressionSpec(
			copiedPreArbitraryManipulators,
			copiedPostArbitraryManipulators,
			copiedMetadataManipulators
		);
	}

	@SuppressWarnings("rawtypes")
	public ExpressionSpec merge(ExpressionSpec fixtureSpec, boolean overwrite) {
		List<PreArbitraryManipulator> filteredPreArbitraryManipulators = fixtureSpec.preArbitraryManipulators.stream()
			.filter(
				it -> overwrite
					|| this.preArbitraryManipulators.stream()
					.noneMatch(manipulator -> manipulator.getArbitraryExpression().equals(it.getArbitraryExpression()))
			)
			.collect(toList());
		this.preArbitraryManipulators.addAll(filteredPreArbitraryManipulators);

		if (overwrite) {
			this.postArbitraryManipulators.removeIf(
				it -> fixtureSpec.postArbitraryManipulators.stream()
					.anyMatch(manipulator -> manipulator.getArbitraryExpression().equals(it.getArbitraryExpression()))
			); // remove redundant fixtureExpression
			this.postArbitraryManipulators.addAll(fixtureSpec.postArbitraryManipulators);
		} else {
			List<PostArbitraryManipulator> filteredPostArbitraryManipulators =
				fixtureSpec.postArbitraryManipulators.stream()
					.filter(
						it -> this.postArbitraryManipulators.stream()
							.noneMatch(
								manipulator -> manipulator.getArbitraryExpression().equals(it.getArbitraryExpression()))
					)
					.collect(toList());
			this.postArbitraryManipulators.addAll(filteredPostArbitraryManipulators);
		}

		List<MetadataManipulator> filteredMetadataManipulators = fixtureSpec.metadataManipulators.stream()
			.filter(
				it -> overwrite
					|| this.metadataManipulators.stream()
					.noneMatch(manipulator -> manipulator.getArbitraryExpression().equals(it.getArbitraryExpression()))
			)
			.collect(toList());
		this.metadataManipulators.addAll(filteredMetadataManipulators);

		return this;
	}

	public ExpressionSpec merge(ExpressionSpec fixtureSpec) {
		return this.merge(fixtureSpec, true);
	}

	public ExpressionSpec exclude(String... excludeExpressions) {
		List<ArbitraryExpression> excludeArbitraryExpression = Arrays.stream(excludeExpressions)
			.map(ArbitraryExpression::from)
			.collect(toList());

		this.preArbitraryManipulators.removeIf(it -> excludeArbitraryExpression.contains(it.getArbitraryExpression()));
		this.postArbitraryManipulators.removeIf(it -> excludeArbitraryExpression.contains(it.getArbitraryExpression()));
		this.metadataManipulators.removeIf(it -> excludeArbitraryExpression.contains(it.getArbitraryExpression()));

		return this;
	}

	public boolean hasSet(String expression) {
		return this.preArbitraryManipulators.stream()
			.anyMatch(it -> it.getArbitraryExpression().equals(ArbitraryExpression.from(expression)));
	}

	public boolean hasFilter(String expression) {
		return this.postArbitraryManipulators.stream()
			.anyMatch(it -> it.getArbitraryExpression().equals(ArbitraryExpression.from(expression)));
	}

	@SuppressWarnings("rawtypes")
	public Optional<Object> findSetValue(String expression) {
		return this.preArbitraryManipulators.stream()
			.filter(it ->
				it.getArbitraryExpression().equals(ArbitraryExpression.from(expression))
					&& AbstractArbitrarySet.class.isAssignableFrom(it.getClass())
			)
			.map(it -> ((AbstractArbitrarySet)it).getValue())
			.findAny();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private <T> void filterSpec(Class<T> clazz, String expression, Predicate predicate, long count) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		postArbitraryManipulators.add(new ArbitraryFilter<T>(clazz, fixtureExpression, predicate, count));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private <T> void filterSpec(Class<T> clazz, String expression, Predicate predicate) {
		ArbitraryExpression fixtureExpression = ArbitraryExpression.from(expression);
		postArbitraryManipulators.add(new ArbitraryFilter<T>(clazz, fixtureExpression, predicate));
	}

	@SuppressWarnings("rawtypes")
	public List<PreArbitraryManipulator> getPreArbitraryManipulators() {
		return preArbitraryManipulators;
	}

	@SuppressWarnings("rawtypes")
	public List<PostArbitraryManipulator> getPostArbitraryManipulators() {
		return postArbitraryManipulators;
	}

	public List<MetadataManipulator> getMetadataManipulators() {
		return metadataManipulators;
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
		return preArbitraryManipulators.equals(that.preArbitraryManipulators)
			&& postArbitraryManipulators.equals(that.postArbitraryManipulators)
			&& metadataManipulators.equals(that.metadataManipulators);
	}

	@Override
	public int hashCode() {
		return Objects.hash(preArbitraryManipulators, postArbitraryManipulators, metadataManipulators);
	}
}
