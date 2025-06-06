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

import java.lang.reflect.AnnotatedType;
import java.util.Collections;
import java.util.List;

import org.apiguardian.api.API;

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.SingleElementProperty;
import com.navercorp.fixturemonkey.api.property.TypeParameterProperty;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "1.0.21", status = API.Status.EXPERIMENTAL)
public final class FunctionalInterfaceContainerPropertyGenerator implements ContainerPropertyGenerator {
	public static final FunctionalInterfaceContainerPropertyGenerator INSTANCE =
		new FunctionalInterfaceContainerPropertyGenerator();

	private static final ArbitraryContainerInfo CONTAINER_INFO = new ArbitraryContainerInfo(1, 1);

	@Override
	public ContainerProperty generate(ContainerPropertyGeneratorContext context) {
		Property property = context.getProperty();

		AnnotatedType lambdaReturnAnnotatedType = getLambdaReturnAnnotatedType(property);

		SingleElementProperty singleElementProperty =
			new SingleElementProperty(property, new TypeParameterProperty(lambdaReturnAnnotatedType));

		return new ContainerProperty(
			Collections.singletonList(singleElementProperty),
			CONTAINER_INFO
		);
	}

	private AnnotatedType getLambdaReturnAnnotatedType(Property lambdaProperty) {
		List<AnnotatedType> genericsTypes = Types.getGenericsTypes(lambdaProperty.getAnnotatedType());
		return genericsTypes.get(genericsTypes.size() - 1);
	}
}
