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

package com.navercorp.fixturemonkey.api.tree;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.container.ConcurrentLruCache;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfoGenerator;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.NullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.property.CandidateConcretePropertyResolver;
import com.navercorp.fixturemonkey.api.property.CompositeCandidateConcretePropertyResolver;
import com.navercorp.fixturemonkey.api.property.DefaultCandidateConcretePropertyResolver;
import com.navercorp.fixturemonkey.api.property.LazyPropertyGenerator;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.property.TreeRootProperty;
import com.navercorp.fixturemonkey.api.tree.TraverseNodePredicate.PropertyTraverseNodePredicate;

@API(since = "1.0.4", status = Status.EXPERIMENTAL)
public final class TraverseContext {
	private static final ConcurrentLruCache<Property, List<Property>> CANDIDATE_CONCRETE_PROPERTIES_BY_PROPERTY =
		new ConcurrentLruCache<>(1024);

	private final TreeRootProperty rootProperty;
	private final List<TreeProperty> treeProperties;
	private final List<TreeNodeManipulator> treeManipulators;
	private final List<MatcherOperator<List<TreeNodeManipulator>>> registeredTreeManipulators;
	private final Map<Class<?>, List<Property>> propertyConfigurers;
	private final boolean validOnly;
	private final LazyPropertyGenerator resolvedPropertyGenerator;
	private final List<MatcherOperator<ObjectPropertyGenerator>> objectPropertyGenerators;
	private final ObjectPropertyGenerator defaultObjectPropertyGenerator;
	private final List<MatcherOperator<ContainerPropertyGenerator>> containerPropertyGenerators;
	private final List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers;
	private final PropertyNameResolver defaultPropertyNameResolver;
	private final List<MatcherOperator<CandidateConcretePropertyResolver>> candidateConcretePropertyResolvers;
	private final List<MatcherOperator<ArbitraryContainerInfoGenerator>> arbitraryContainerInfoGenerators;
	private final ArbitraryContainerInfoGenerator defaultArbitraryContainerInfoGenerator;
	private final List<MatcherOperator<NullInjectGenerator>> nullInjectGenerators;
	private final NullInjectGenerator defaultNullInjectGenerator;

	public TraverseContext(
		TreeRootProperty rootProperty,
		List<TreeProperty> treeProperties,
		List<TreeNodeManipulator> treeManipulators,
		List<MatcherOperator<List<TreeNodeManipulator>>> registeredTreeManipulators,
		Map<Class<?>, List<Property>> propertyConfigurers,
		boolean validOnly,
		LazyPropertyGenerator resolvedPropertyGenerator,
		List<MatcherOperator<ObjectPropertyGenerator>> objectPropertyGenerators,
		ObjectPropertyGenerator defaultObjectPropertyGenerator,
		List<MatcherOperator<ContainerPropertyGenerator>> containerPropertyGenerators,
		List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers,
		PropertyNameResolver defaultPropertyNameResolver,
		List<MatcherOperator<CandidateConcretePropertyResolver>> candidateConcretePropertyResolvers,
		List<MatcherOperator<ArbitraryContainerInfoGenerator>> arbitraryContainerInfoGenerators,
		ArbitraryContainerInfoGenerator defaultArbitraryContainerInfoGenerator,
		List<MatcherOperator<NullInjectGenerator>> nullInjectGenerators,
		NullInjectGenerator defaultNullInjectGenerator
	) {
		this.rootProperty = rootProperty;
		this.treeProperties = treeProperties;
		this.treeManipulators = treeManipulators;
		this.registeredTreeManipulators = registeredTreeManipulators;
		this.propertyConfigurers = propertyConfigurers;
		this.validOnly = validOnly;
		this.resolvedPropertyGenerator = resolvedPropertyGenerator;
		this.objectPropertyGenerators = objectPropertyGenerators;
		this.defaultObjectPropertyGenerator = defaultObjectPropertyGenerator;
		this.containerPropertyGenerators = containerPropertyGenerators;
		this.propertyNameResolvers = propertyNameResolvers;
		this.defaultPropertyNameResolver = defaultPropertyNameResolver;
		this.candidateConcretePropertyResolvers = candidateConcretePropertyResolvers;
		this.arbitraryContainerInfoGenerators = arbitraryContainerInfoGenerators;
		this.defaultArbitraryContainerInfoGenerator = defaultArbitraryContainerInfoGenerator;
		this.nullInjectGenerators = nullInjectGenerators;
		this.defaultNullInjectGenerator = defaultNullInjectGenerator;
	}

	@Nullable
	public TreeProperty getRootTreeProperty() {
		return treeProperties.get(0);
	}

	public List<TreeProperty> getTreeProperties() {
		return treeProperties;
	}

	public List<TreeNodeManipulator> getTreeManipulators() {
		return treeManipulators;
	}

	public Map<Class<?>, List<Property>> getPropertyConfigurers() {
		return propertyConfigurers;
	}

	public boolean isValidOnly() {
		return validOnly;
	}

	public TraverseContext appendArbitraryProperty(
		TreeProperty treeProperty
	) {
		List<TreeProperty> treeProperties = new ArrayList<>(this.treeProperties);
		treeProperties.add(treeProperty);

		List<TreeNodeManipulator> registeredContainerManipulators =
			this.registeredTreeManipulators.stream()
				.filter(it -> it.match(treeProperty.getObjectProperty().getProperty()))
				.map(MatcherOperator::getOperator)
				.findFirst()
				.orElse(Collections.emptyList());

		List<TreeNodeManipulator> concatRegisteredContainerManipulator = registeredContainerManipulators.stream()
			.map(it -> it.withPrependNextNodePredicate(
				new PropertyTraverseNodePredicate(treeProperty.getObjectProperty().getProperty())
			))
			.collect(Collectors.toList());

		List<TreeNodeManipulator> concat = new ArrayList<>();
		concat.addAll(concatRegisteredContainerManipulator);
		concat.addAll(treeManipulators);
		return new TraverseContext(
			rootProperty,
			treeProperties,
			concat,
			this.registeredTreeManipulators,
			this.propertyConfigurers,
			this.validOnly,
			this.resolvedPropertyGenerator,
			this.objectPropertyGenerators,
			this.defaultObjectPropertyGenerator,
			this.containerPropertyGenerators,
			this.propertyNameResolvers,
			this.defaultPropertyNameResolver,
			this.candidateConcretePropertyResolvers,
			this.arbitraryContainerInfoGenerators,
			this.defaultArbitraryContainerInfoGenerator,
			this.nullInjectGenerators,
			this.defaultNullInjectGenerator
		);
	}

	public boolean isTraversed(Property property) {
		return treeProperties.stream()
			.skip(1)
			.anyMatch(it -> isSameType(property, it.getObjectProperty().getProperty()));
	}

	public void addContainerInfoManipulator(TreeNodeManipulator containerInfoManipulator) {
		if (!this.treeManipulators.contains(containerInfoManipulator)) {
			this.treeManipulators.add(containerInfoManipulator);
		}
	}

	@Nullable
	public TreeProperty getLastTreeProperty() {
		if (this.treeProperties.isEmpty()) {
			return null;
		}
		return this.treeProperties.get(this.treeProperties.size() - 1);
	}

	public TraverseContext withParentProperties() {
		List<TreeProperty> newTreeProperties = new ArrayList<>();

		if (!this.treeProperties.isEmpty()) {
			newTreeProperties.add(this.treeProperties.get(this.treeProperties.size() - 1));
		}

		return new TraverseContext(
			rootProperty,
			newTreeProperties,
			new ArrayList<>(this.treeManipulators),
			this.registeredTreeManipulators,
			this.propertyConfigurers,
			this.validOnly,
			this.resolvedPropertyGenerator,
			this.objectPropertyGenerators,
			this.defaultObjectPropertyGenerator,
			this.containerPropertyGenerators,
			this.propertyNameResolvers,
			this.defaultPropertyNameResolver,
			this.candidateConcretePropertyResolvers,
			this.arbitraryContainerInfoGenerators,
			this.defaultArbitraryContainerInfoGenerator,
			this.nullInjectGenerators,
			this.defaultNullInjectGenerator
		);
	}

	private static boolean isSameType(Property p1, Property p2) {
		boolean notMapEntry = !(p1 instanceof MapEntryElementProperty) || !(p2 instanceof MapEntryElementProperty);
		return notMapEntry && p1.getAnnotatedType().getType().equals(p2.getAnnotatedType().getType());
	}

	public ObjectPropertyGenerator getObjectPropertyGenerator(Property property) {
		return objectPropertyGenerators.stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(this.defaultObjectPropertyGenerator);
	}

	@Nullable
	public ContainerPropertyGenerator getContainerPropertyGenerator(Property property) {
		return containerPropertyGenerators.stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(null);
	}

	public PropertyNameResolver getPropertyNameResolver(Property property) {
		return propertyNameResolvers.stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(this.defaultPropertyNameResolver);
	}

	public ArbitraryContainerInfoGenerator getArbitraryContainerInfoGenerator(Property property) {
		return arbitraryContainerInfoGenerators.stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(this.defaultArbitraryContainerInfoGenerator);
	}

	public NullInjectGenerator getNullInjectGenerator(Property property) {
		return nullInjectGenerators.stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(this.defaultNullInjectGenerator);
	}

	public LazyPropertyGenerator getResolvedPropertyGenerator() {
		return resolvedPropertyGenerator;
	}

	public List<Property> resolveCandidateProperties(Property property) {
		CandidateConcretePropertyResolver candidateConcretePropertyResolver =
			getCandidateConcretePropertyResolver(property, candidateConcretePropertyResolvers);

		if (candidateConcretePropertyResolver == null) {
			return DefaultCandidateConcretePropertyResolver.INSTANCE.resolve(property);
		}

		return CANDIDATE_CONCRETE_PROPERTIES_BY_PROPERTY.computeIfAbsent(
			property,
			p -> {
				List<Property> resolvedCandidateProperties = new ArrayList<>();
				List<Property> candidateProperties = candidateConcretePropertyResolver.resolve(p);
				for (Property candidateProperty : candidateProperties) {
					// compares by type until a specific property implementation is created for the generic type.
					Type candidateType = candidateProperty.getType();

					if (p.getType().equals(candidateType)) {
						// prevents infinite recursion
						resolvedCandidateProperties.addAll(
							DefaultCandidateConcretePropertyResolver.INSTANCE.resolve(p)
						);
						continue;
					}
					resolvedCandidateProperties.addAll(resolveCandidateProperties(candidateProperty));
				}
				return resolvedCandidateProperties;
			}
		);
	}

	@Nullable
	private static CandidateConcretePropertyResolver getCandidateConcretePropertyResolver(
		Property property,
		List<MatcherOperator<CandidateConcretePropertyResolver>> candidateConcretePropertyResolvers
	) {
		List<CandidateConcretePropertyResolver> candidateConcretePropertyResolverList =
			candidateConcretePropertyResolvers.stream()
				.filter(it -> it.match(property))
				.map(MatcherOperator::getOperator)
				.collect(Collectors.toList());

		if (candidateConcretePropertyResolverList.isEmpty()) {
			return null;
		}

		return new CompositeCandidateConcretePropertyResolver(candidateConcretePropertyResolverList);
	}
}
