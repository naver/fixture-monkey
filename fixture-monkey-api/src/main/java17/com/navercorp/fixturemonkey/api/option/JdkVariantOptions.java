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

import java.lang.reflect.Constructor;
import java.time.ZoneId;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.property.CandidateConcretePropertyResolver;
import com.navercorp.fixturemonkey.api.property.ConstructorParameterPropertyGenerator;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.api.property.SealedTypeCandidateConcretePropertyResolver;
import com.navercorp.fixturemonkey.api.type.Constructors;
import com.navercorp.fixturemonkey.api.type.TypeCache;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "1.0.14", status = Status.INTERNAL)
public final class JdkVariantOptions {
	private static final CandidateConcretePropertyResolver SEALED_TYPE_CANDIDATE_CONCRETE_PROPERTY_RESOLVER =
		new SealedTypeCandidateConcretePropertyResolver();
	private static final PropertyGenerator CANONICAL_CONSTRUCTOR_PARAMETER_PROPERTY_GENERATOR =
		new ConstructorParameterPropertyGenerator(
			p -> Constructors.findPrimaryConstructor(
				Types.getActualType(p.getType()),
				TypeCache.getDeclaredConstructors(Types.getActualType(p.getType())).toArray(Constructor[]::new)
			).stream().toList(),
			it -> true,
			it -> true
		);

	public void apply(FixtureMonkeyOptionsBuilder optionsBuilder) {
		optionsBuilder.insertFirstCandidateConcretePropertyResolvers(
				new MatcherOperator<>(
					p -> Types.getActualType(p.getType()).isSealed() && !Types.getActualType(p.getType()).isEnum(),
					SEALED_TYPE_CANDIDATE_CONCRETE_PROPERTY_RESOLVER
				)
			)
			.insertFirstPropertyGenerator(ZoneId.class, property -> List.of())
			.insertFirstPropertyGenerator(
				p -> Types.getActualType(p.getType()).isRecord(),
				CANONICAL_CONSTRUCTOR_PARAMETER_PROPERTY_GENERATOR
			);
	}
}
