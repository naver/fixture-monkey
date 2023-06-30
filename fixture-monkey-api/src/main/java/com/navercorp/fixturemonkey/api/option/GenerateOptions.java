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

package com.navercorp.fixturemonkey.api.option;

import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.container.DecomposedContainerValueFactory;
import com.navercorp.fixturemonkey.api.customizer.FixtureCustomizer;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfoGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.NullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.PropertyGenerator;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.validator.ArbitraryValidator;

/**
 * Deprecated use {@link FixtureMonkeyOptions} instead.
 */
@API(since = "0.4.0", status = Status.MAINTAINED)
@Deprecated
public final class GenerateOptions extends FixtureMonkeyOptions {
	public GenerateOptions(
		List<MatcherOperator<PropertyGenerator>> propertyGenerators,
		PropertyGenerator defaultPropertyGenerator,
		List<MatcherOperator<ObjectPropertyGenerator>> objectPropertyGenerators,
		ObjectPropertyGenerator defaultObjectPropertyGenerator,
		List<MatcherOperator<ContainerPropertyGenerator>> containerPropertyGenerators,
		List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers,
		PropertyNameResolver defaultPropertyNameResolver,
		List<MatcherOperator<NullInjectGenerator>> nullInjectGenerators,
		NullInjectGenerator defaultNullInjectGenerator,
		List<MatcherOperator<ArbitraryContainerInfoGenerator>> arbitraryContainerInfoGenerators,
		ArbitraryContainerInfoGenerator defaultArbitraryContainerInfoGenerator,
		List<MatcherOperator<ArbitraryGenerator>> arbitraryGenerators,
		List<MatcherOperator<FixtureCustomizer>> arbitraryCustomizers,
		ArbitraryGenerator defaultArbitraryGenerator,
		ArbitraryValidator defaultArbitraryValidator,
		DecomposedContainerValueFactory decomposedContainerValueFactory
	) {
		super(propertyGenerators,
			defaultPropertyGenerator,
			objectPropertyGenerators,
			defaultObjectPropertyGenerator,
			containerPropertyGenerators,
			propertyNameResolvers,
			defaultPropertyNameResolver,
			nullInjectGenerators,
			defaultNullInjectGenerator,
			arbitraryContainerInfoGenerators,
			defaultArbitraryContainerInfoGenerator,
			arbitraryGenerators,
			arbitraryCustomizers,
			defaultArbitraryGenerator,
			defaultArbitraryValidator,
			decomposedContainerValueFactory
		);
	}

	public static GenerateOptionsBuilder builder() {
		return new GenerateOptionsBuilder();
	}
}
