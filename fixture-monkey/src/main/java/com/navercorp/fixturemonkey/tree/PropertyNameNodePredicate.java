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

package com.navercorp.fixturemonkey.tree;

import static com.navercorp.fixturemonkey.Constants.ALL_INDEX_STRING;

import java.util.Objects;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ObjectProperty;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class PropertyNameNodePredicate implements NextNodePredicate {
	private final String propertyName;

	public PropertyNameNodePredicate(String propertyName) {
		this.propertyName = propertyName;
	}

	@Override
	public boolean test(ObjectProperty currentObjectProperty) {
		String nodePropertyName = currentObjectProperty.getResolvedPropertyName();
		return ALL_INDEX_STRING.equals(propertyName) || propertyName.equals(nodePropertyName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		PropertyNameNodePredicate that = (PropertyNameNodePredicate)obj;
		return propertyName.equals(that.propertyName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(propertyName);
	}
}
