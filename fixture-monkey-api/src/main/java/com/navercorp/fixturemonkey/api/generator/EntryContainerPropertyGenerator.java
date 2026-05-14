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

import java.util.ArrayList;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.MapKeyElementProperty;
import com.navercorp.fixturemonkey.api.property.MapValueElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.TypeParameterProperty;
import com.navercorp.objectfarm.api.type.JvmType;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class EntryContainerPropertyGenerator implements ContainerPropertyGenerator {
	public static final EntryContainerPropertyGenerator INSTANCE =
		new EntryContainerPropertyGenerator();
	private static final ArbitraryContainerInfo CONTAINER_INFO = new ArbitraryContainerInfo(1, 1);

	@Override
	public ContainerProperty generate(ContainerPropertyGeneratorContext context) {
		Property property = context.getProperty();

		List<? extends JvmType> typeVariables = property.getJvmType().getTypeVariables();
		if (typeVariables.size() != 2) {
			throw new IllegalArgumentException(
				"Entry genericsTypes must be have 2 generics type for key and value. "
					+ "propertyType: " + property.getJvmType().getRawType()
					+ ", typeVariables: " + typeVariables
			);
		}

		JvmType keyType = typeVariables.get(0);
		JvmType valueType = typeVariables.get(1);

		List<Property> childProperties = new ArrayList<>();
		childProperties.add(
			new MapEntryElementProperty(
				property,
				new MapKeyElementProperty(
					property,
					new TypeParameterProperty(keyType),
					0
				),
				new MapValueElementProperty(
					property,
					new TypeParameterProperty(valueType),
					0
				)
			)
		);

		return new ContainerProperty(
			childProperties,
			CONTAINER_INFO
		);
	}
}
