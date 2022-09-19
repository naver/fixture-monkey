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

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ContainerProperty;
import com.navercorp.fixturemonkey.api.generator.ObjectProperty;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class NodeLastEntryPredicate implements NextNodePredicate {
	public NodeLastEntryPredicate() {
	}

	@Override
	public boolean test(
		@Nullable ArbitraryProperty parentArbitraryProperty,
		ObjectProperty currentObjectProperty,
		@Nullable ContainerProperty currentContainerProperty
	) {
		if (parentArbitraryProperty == null
			|| parentArbitraryProperty.getContainerProperty() == null
			|| currentObjectProperty.getElementIndex() == null) {
			throw new IllegalArgumentException(
				"Only Map.Entry could be selected. now type : " + currentObjectProperty.getProperty()
					.getType()
					.getTypeName()
			);
		}

		int index = currentObjectProperty.getElementIndex();
		int elementPropertiesSize = parentArbitraryProperty.getContainerProperty().getElementProperties().size();
		return index == elementPropertiesSize - 1;
	}
}
