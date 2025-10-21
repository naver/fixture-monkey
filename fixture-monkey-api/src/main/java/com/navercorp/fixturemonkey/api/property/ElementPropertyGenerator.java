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

package com.navercorp.fixturemonkey.api.property;

import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfoGenerator;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGeneratorContext;

/**
 * It is for internal use only. It can be changed or removed at any time.
 */
@API(since = "1.1.0", status = Status.INTERNAL)
public final class ElementPropertyGenerator implements PropertyGenerator {
	private final Property originalContainerProperty;
	private final ContainerPropertyGenerator containerPropertyGenerator;
	private final ArbitraryContainerInfoGenerator containerInfoGenerator;
	@Nullable
	private ArbitraryContainerInfo containerInfo;

	/**
	 * Constructs a new {@link ElementPropertyGenerator}.
	 *
	 * @param originalContainerProperty  a compile-time property that is declared in a class file.
	 * @param containerPropertyGenerator it generates the element properties
	 * @param containerInfoGenerator     it decides the size of container
	 * @param containerInfo              it is used if the size of the container is customized,
	 *                                   it is null if there is no customizer.
	 */
	public ElementPropertyGenerator(
		Property originalContainerProperty,
		ContainerPropertyGenerator containerPropertyGenerator,
		ArbitraryContainerInfoGenerator containerInfoGenerator,
		@Nullable ArbitraryContainerInfo containerInfo
	) {
		this.originalContainerProperty = originalContainerProperty;
		this.containerInfoGenerator = containerInfoGenerator;
		this.containerPropertyGenerator = containerPropertyGenerator;
		this.containerInfo = containerInfo;
	}

	public void updateContainerInfo(ArbitraryContainerInfo containerInfo) {
		this.containerInfo = containerInfo;
	}

	/**
	 * Generates the element properties of the original container property.
	 *
	 * @param concreteProperty it can be the concrete container type.
	 * @return the element properties of original container property.
	 */
	@Override
	public List<Property> generateChildProperties(Property concreteProperty) {
		return containerPropertyGenerator.generate(
			new ContainerPropertyGeneratorContext(
				originalContainerProperty,
				containerInfo,
				containerInfoGenerator
			)
		).getElementProperties();
	}
}
