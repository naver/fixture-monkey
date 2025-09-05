package com.navercorp.fixturemonkey.api.matcher;

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class DefaultMatcherOperatorRegistry<T> implements MatcherOperatorRegistry<T> {
	private final AtomicInteger sequenceIssuer = new AtomicInteger(0);
	private final List<PriorityMatcherOperator<T>> typeUnknownIntrospectors = new ArrayList<>();
	private final Map<Class<?>, List<PriorityMatcherOperator<T>>> typeAwareIntrospectors = new HashMap<>();
	private final Map<Class<?>, List<PriorityMatcherOperator<T>>> typeAssignableIntrospectors = new HashMap<>();

	public void addFirst(MatcherOperator<T> matcherOperator) {
		addInternal(matcherOperator,  true);
	}

	public void addLast(MatcherOperator<T> matcherOperator) {
		addInternal(matcherOperator, false);
	}

	private void addInternal(MatcherOperator<T> matcherOperator, boolean prepend) {
		int seq = sequenceIssuer.incrementAndGet();
		int sequence = prepend ? -seq : seq;

		Matcher matcher = matcherOperator.getMatcher();
		T operator = matcherOperator.getOperator();

		PriorityMatcherOperator<T> priorityMatcherOperator = new PriorityMatcherOperator<>(
			matcher,
			operator,
			sequence
		);

		if (matcher instanceof ExactTypeMatcher) {
			Class<?> type = ((ExactTypeMatcher) matcher).getType();
			typeAwareIntrospectors.computeIfAbsent(type, k -> new ArrayList<>()).add(priorityMatcherOperator);
		} else if (matcher instanceof AssignableTypeMatcher) {
			Class<?> anchorType = ((AssignableTypeMatcher) matcher).getAnchorType();
			typeAssignableIntrospectors.computeIfAbsent(anchorType, k -> new ArrayList<>()).add(priorityMatcherOperator);
		} else {
			typeUnknownIntrospectors.add(priorityMatcherOperator);
		}
	}

	@Override
	public List<MatcherOperator<T>> get(Property property) {
		Class<?> propertyType = Types.getActualType(property.getType());
		List<PriorityMatcherOperator<T>> acc = new ArrayList<>(typeUnknownIntrospectors);
		for (Map.Entry<Class<?>, List<PriorityMatcherOperator<T>>> e : typeAssignableIntrospectors.entrySet()) {
			Class<?> anchorType = e.getKey();
			if (anchorType.isAssignableFrom(propertyType)) {
				acc.addAll(e.getValue());
			}
		}

		acc.addAll(typeAwareIntrospectors.getOrDefault(propertyType, Collections.emptyList()));

		return acc.stream()
			.sorted(Comparator.comparingInt(PriorityMatcherOperator::getPriority))
			.map(op -> (MatcherOperator<T>) op)
			.collect(Collectors.toList());
	}

	@Override
	public List<MatcherOperator<T>> values() {
		List<PriorityMatcherOperator<T>> acc = new ArrayList<>(typeUnknownIntrospectors);
		typeAwareIntrospectors.values().forEach(acc::addAll);
		typeAssignableIntrospectors.values().forEach(acc::addAll);


		return acc.stream()
			.sorted(Comparator.comparingInt(PriorityMatcherOperator::getPriority))
			.map(op -> (MatcherOperator<T>) op)
			.collect(Collectors.toList());
	}
}
