package com.navercorp.fixturemonkey.api.matcher;

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MatcherOperatorRegistry<T> {
	private final AtomicInteger sequenceIssuer = new AtomicInteger(0);
	private final Map<MatcherOperator<T>, Integer> priorityMap = new HashMap<>();
	private final List<MatcherOperator<T>> typeUnknownIntrospectors = new ArrayList<>();
	private final Map<Class<?>, List<MatcherOperator<T>>> typeAwareIntrospectors = new HashMap<>();

	public void addMatcherOperatorFirst(MatcherOperator<T> matcherOperator) {
		addInternal(matcherOperator,  true);
	}

	public void addMatcherOperator(MatcherOperator<T> matcherOperator) {
		addInternal(matcherOperator, false);
	}

	private void addInternal(MatcherOperator<T> matcherOperator, boolean prepend) {
		int seq = sequenceIssuer.incrementAndGet();
		int sequence = prepend ? -seq : seq;
		priorityMap.put(matcherOperator, sequence);

		Matcher matcher = matcherOperator.getMatcher();
		if (matcher instanceof ExactTypeMatcher) {
			Class<?> type = ((ExactTypeMatcher) matcher).getType();
			typeAwareIntrospectors.computeIfAbsent(type, k -> new ArrayList<>()).add(matcherOperator);
		} else {
			typeUnknownIntrospectors.add(matcherOperator);
		}
	}

	public List<MatcherOperator<T>> findAll(Property property) {
		Class<?> propertyType = Types.getActualType(property.getType());
		List<MatcherOperator<T>> acc = new ArrayList<>(typeUnknownIntrospectors);
		acc.addAll(typeAwareIntrospectors.getOrDefault(propertyType, Collections.emptyList()));

		acc.sort(Comparator.comparingInt(this::priorityOf));
		return acc;
	}

	public List<MatcherOperator<T>> findAll() {
		List<MatcherOperator<T>> acc = new ArrayList<>(typeUnknownIntrospectors);
		typeAwareIntrospectors.values().forEach(acc::addAll);

		acc.sort(Comparator.comparingInt(this::priorityOf));
		return acc;
	}

	private int priorityOf(MatcherOperator<T> mo) {
		return priorityMap.getOrDefault(mo, Integer.MAX_VALUE);
	}
}
