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
import java.util.function.Supplier;

import com.navercorp.fixturemonkey.api.property.ElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;

public final class SupplierContainerPropertyGenerator implements ContainerPropertyGenerator {
	public static final SupplierContainerPropertyGenerator INSTANCE = new SupplierContainerPropertyGenerator();
	private static final ArbitraryContainerInfo CONTAINER_INFO = new ArbitraryContainerInfo(1, 1);

	@Override
	public ContainerProperty generate(ContainerPropertyGeneratorContext context) {
		Property property = context.getProperty();

		AnnotatedType valueAnnotatedType = getValueAnnotatedType(property);
		Property valueProperty = new ElementProperty(
			property,
			valueAnnotatedType,
			0,
			0
		);

		return new ContainerProperty(
			Collections.singletonList(valueProperty),
			CONTAINER_INFO
		);
	}

	private AnnotatedType getValueAnnotatedType(Property optionalProperty) {
		Class<?> type = Types.getActualType(optionalProperty.getType());
		if (type != Supplier.class) {
			throw new IllegalArgumentException(
				"type is not Supplier type. propertyType: " + type
			);
		}

		List<AnnotatedType> genericsTypes = Types.getGenericsTypes(optionalProperty.getAnnotatedType());
		if (genericsTypes.size() != 1) {
			throw new IllegalArgumentException(
				"Supplier genericTypes must be have 1 generics type for value. "
					+ "propertyType: " + optionalProperty.getType()
					+ ", genericsTypes: " + genericsTypes
			);
		}

		return genericsTypes.get(0);
	}
}
