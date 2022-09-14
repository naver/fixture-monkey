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

package com.navercorp.fixturemonkey.resolver;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ContainerProperty;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ObjectProperty;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.option.GenerateOptions;
import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ArbitraryTraverser {
	private final GenerateOptions generateOptions;

	public ArbitraryTraverser(GenerateOptions generateOptions) {
		this.generateOptions = generateOptions;
	}

	public ArbitraryNode traverse(
		Property property,
		@Nullable ArbitraryContainerInfo containerInfo
	) {
		ObjectPropertyGenerator objectPropertyGenerator =
			this.generateOptions.getObjectPropertyGenerator(property);
		ContainerPropertyGenerator containerPropertyGenerator =
			this.generateOptions.getContainerPropertyGenerator(property);
		boolean container = containerPropertyGenerator != null;

		ObjectProperty objectProperty = objectPropertyGenerator.generate(
			new ObjectPropertyGeneratorContext(
				property,
				null,
				null,
				container,
				this.generateOptions
			)
		);

		ContainerProperty containerProperty = null;
		if (container) {
			containerProperty = containerPropertyGenerator.generate(
				new ContainerPropertyGeneratorContext(
					property,
					null,
					containerInfo,
					generateOptions
				)
			);
		}

		ArbitraryProperty arbitraryProperty = new ArbitraryProperty(
			objectProperty,
			containerProperty
		);
		return this.traverse(arbitraryProperty);
	}

	private ArbitraryNode traverse(ArbitraryProperty arbitraryProperty) {
		List<ArbitraryNode> children = new ArrayList<>();

		ObjectProperty objectProperty = arbitraryProperty.getObjectProperty();
		ContainerProperty containerProperty = arbitraryProperty.getContainerProperty();

		if (containerProperty != null) {
			List<Property> elementProperties = containerProperty.getElementProperties();
			for (int index = 0; index < elementProperties.size(); index++) {
				Property elementProperty = elementProperties.get(index);
				ObjectPropertyGenerator objectPropertyGenerator =
					this.generateOptions.getObjectPropertyGenerator(elementProperty);
				ContainerPropertyGenerator containerPropertyGenerator =
					this.generateOptions.getContainerPropertyGenerator(elementProperty);
				boolean childContainer = containerPropertyGenerator != null;

				ObjectProperty elementObjectProperty = objectPropertyGenerator.generate(
					new ObjectPropertyGeneratorContext(
						elementProperty,
						index,
						arbitraryProperty,
						childContainer,
						this.generateOptions
					)
				);

				ContainerProperty elementContainerProperty = null;
				if (childContainer) {
					elementContainerProperty = containerPropertyGenerator.generate(
						new ContainerPropertyGeneratorContext(
							elementProperty,
							index,
							null,
							generateOptions
						)
					);
				}

				ArbitraryNode childNode = this.traverse(
					new ArbitraryProperty(elementObjectProperty, elementContainerProperty)
				);
				children.add(childNode);
			}
		} else {
			List<Property> childProperties = objectProperty.getChildProperties();
			for (Property childProperty : childProperties) {
				ObjectPropertyGenerator objectPropertyGenerator =
					this.generateOptions.getObjectPropertyGenerator(childProperty);
				ContainerPropertyGenerator containerPropertyGenerator =
					this.generateOptions.getContainerPropertyGenerator(childProperty);
				boolean childContainer = containerPropertyGenerator != null;

				ObjectProperty childObjectProperty = objectPropertyGenerator.generate(
					new ObjectPropertyGeneratorContext(
						childProperty,
						null,
						arbitraryProperty,
						childContainer,
						this.generateOptions
					)
				);

				ContainerProperty childContainerProperty = null;
				if (childContainer) {
					childContainerProperty = containerPropertyGenerator.generate(
						new ContainerPropertyGeneratorContext(
							childProperty,
							null,
							null,
							this.generateOptions
						)
					);
				}

				ArbitraryNode childNode = this.traverse(
					new ArbitraryProperty(childObjectProperty, childContainerProperty)
				);
				children.add(childNode);
			}
		}

		return new ArbitraryNode(
			arbitraryProperty,
			children
		);
	}

}
