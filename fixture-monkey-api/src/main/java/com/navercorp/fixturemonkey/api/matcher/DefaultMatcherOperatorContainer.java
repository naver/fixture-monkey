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


package com.navercorp.fixturemonkey.api.matcher;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apiguardian.api.API;

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.api.type.Types.UnidentifiableType;

/**
 * It is for internal use only.
 */
@API(since = "1.1.16", status = INTERNAL)
public final class DefaultMatcherOperatorContainer<T>
	implements MatcherOperatorRegistry<T>, MatcherOperatorRetriever<T> {
	private final AtomicInteger sequenceIssuer = new AtomicInteger(0);
	private final List<PriorityMatcherOperator<T>> typeUnknownIntrospectors = new ArrayList<>();
	private final Map<Class<?>, List<PriorityMatcherOperator<T>>> typeAwareIntrospectors = new HashMap<>();
	private final Map<Class<?>, List<PriorityMatcherOperator<T>>> typeAssignableIntrospectors = new HashMap<>();

	@Override
	public void addFirst(MatcherOperator<T> matcherOperator) {
		addInternal(matcherOperator, true);
	}

	@Override
	public void addLast(MatcherOperator<T> matcherOperator) {
		addInternal(matcherOperator, false);
	}

	@Override
	public List<MatcherOperator<T>> getListByProperty(Property property) {
		Class<?> propertyType = Types.getActualType(property.getType());
		List<PriorityMatcherOperator<T>> acc = new ArrayList<>(typeUnknownIntrospectors);

		if (propertyType == UnidentifiableType.class) {
			return typeAssignableIntrospectors.getOrDefault(propertyType, Collections.emptyList()).stream()
				.map(it -> (MatcherOperator<T>)it)
				.collect(Collectors.toList());
		}

		for (Map.Entry<Class<?>, List<PriorityMatcherOperator<T>>> e : typeAssignableIntrospectors.entrySet()) {
			Class<?> anchorType = e.getKey();
			if (anchorType.isAssignableFrom(propertyType)) {
				acc.addAll(e.getValue());
			}
		}

		acc.addAll(typeAwareIntrospectors.getOrDefault(propertyType, Collections.emptyList()));

		return acc.stream()
			.sorted(Comparator.comparingInt(PriorityMatcherOperator::getPriority))
			.map(op -> (MatcherOperator<T>)op)
			.collect(Collectors.toList());
	}

	@Override
	public List<MatcherOperator<T>> getList() {
		List<PriorityMatcherOperator<T>> acc = new ArrayList<>(typeUnknownIntrospectors);
		typeAwareIntrospectors.values().forEach(acc::addAll);
		typeAssignableIntrospectors.values().forEach(acc::addAll);

		return acc.stream()
			.sorted(Comparator.comparingInt(PriorityMatcherOperator::getPriority))
			.map(op -> (MatcherOperator<T>)op)
			.collect(Collectors.toList());
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
			Class<?> type = ((ExactTypeMatcher)matcher).getType();
			typeAwareIntrospectors.computeIfAbsent(type, k -> new ArrayList<>()).add(priorityMatcherOperator);
		} else if (matcher instanceof AssignableTypeMatcher) {
			Class<?> anchorType = ((AssignableTypeMatcher)matcher).getAnchorType();
			typeAssignableIntrospectors.computeIfAbsent(anchorType, k -> new ArrayList<>())
				.add(priorityMatcherOperator);
		} else {
			typeUnknownIntrospectors.add(priorityMatcherOperator);
		}
	}
}
