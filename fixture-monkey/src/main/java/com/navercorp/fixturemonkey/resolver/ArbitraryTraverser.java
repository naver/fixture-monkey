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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.option.GenerateOptions;
import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ArbitraryTraverser {
	private final GenerateOptions generateOptions;

	public ArbitraryTraverser(GenerateOptions generateOptions) {
		this.generateOptions = generateOptions;
	}

	public ArbitraryNode traverse(Property property) {
		ArbitraryPropertyGenerator arbitraryPropertyGenerator =
			this.generateOptions.getArbitraryPropertyGenerator(property);

		ArbitraryProperty rootArbitraryProperty = arbitraryPropertyGenerator.generate(
			new ArbitraryPropertyGeneratorContext(
				property,
				null,
				null,
				this.generateOptions
			)
		);

		return this.traverse(rootArbitraryProperty);
	}

	private ArbitraryNode traverse(ArbitraryProperty arbitraryProperty) {
		List<ArbitraryNode> children = new ArrayList<>();

		List<Property> childProperties = arbitraryProperty.getChildProperties();
		for (int index = 0; index < childProperties.size(); index++) {
			Property childProperty = childProperties.get(index);
			ArbitraryPropertyGenerator arbitraryPropertyGenerator =
				this.generateOptions.getArbitraryPropertyGenerator(childProperty);

			ArbitraryProperty childArbitraryProperty = arbitraryPropertyGenerator.generate(
				new ArbitraryPropertyGeneratorContext(
					childProperty,
					arbitraryProperty.getContainerInfo() != null ? index : null,
					arbitraryProperty,
					this.generateOptions
				)
			);

			ArbitraryNode childNode = this.traverse(childArbitraryProperty);
			children.add(childNode);
		}

		return new ArbitraryNode(
			arbitraryProperty,
			children
		);
	}
}
