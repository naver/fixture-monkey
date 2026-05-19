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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.SingleElementProperty;
import com.navercorp.fixturemonkey.api.property.TypeParameterProperty;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.ReflectiveJvmType;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class OptionalContainerPropertyGenerator implements ContainerPropertyGenerator {
	public static final OptionalContainerPropertyGenerator INSTANCE = new OptionalContainerPropertyGenerator();

	private static final JvmType INTEGER_TYPE = new ReflectiveJvmType(Integer.class);
	private static final JvmType LONG_TYPE = new ReflectiveJvmType(Long.class);
	private static final JvmType DOUBLE_TYPE = new ReflectiveJvmType(Double.class);
	private static final ArbitraryContainerInfo CONTAINER_INFO = new ArbitraryContainerInfo(0, 1);

	@Override
	public ContainerProperty generate(ContainerPropertyGeneratorContext context) {
		Property property = context.getProperty();

		JvmType valueJvmType = getOptionalValueJvmType(property);

		SingleElementProperty singleElementProperty =
			new SingleElementProperty(property, new TypeParameterProperty(valueJvmType));

		return new ContainerProperty(
			Collections.singletonList(singleElementProperty),
			CONTAINER_INFO
		);
	}

	private JvmType getOptionalValueJvmType(Property optionalProperty) {
		Class<?> type = optionalProperty.getJvmType().getRawType();
		if (type == OptionalInt.class) {
			return INTEGER_TYPE;
		}

		if (type == OptionalLong.class) {
			return LONG_TYPE;
		}

		if (type == OptionalDouble.class) {
			return DOUBLE_TYPE;
		}

		if (type != Optional.class) {
			throw new IllegalArgumentException(
				"type is not Optional type. propertyType: " + type
			);
		}

		List<? extends JvmType> typeVariables = optionalProperty.getJvmType().getTypeVariables();
		if (typeVariables.size() != 1) {
			throw new IllegalArgumentException(
				"Optional typeVariables must be have 1 generics type for value. "
					+ "propertyType: " + type
					+ ", typeVariables: " + typeVariables
			);
		}

		return typeVariables.get(0);
	}
}
