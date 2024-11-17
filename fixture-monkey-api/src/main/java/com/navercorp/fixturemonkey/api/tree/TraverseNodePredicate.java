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

package com.navercorp.fixturemonkey.api.tree;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ObjectProperty;
import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "1.1.4", status = Status.EXPERIMENTAL)
@FunctionalInterface
public interface TraverseNodePredicate {
	boolean test(ObjectProperty currentObjectProperty);

	class StartTraverseNodePredicate implements TraverseNodePredicate {

		@Override
		public boolean test(ObjectProperty currentObjectProperty) {
			return true;
		}
	}

	class PropertyTraverseNodePredicate implements TraverseNodePredicate {
		private final Property property;

		public PropertyTraverseNodePredicate(Property property) {
			this.property = property;
		}

		@Override
		public boolean test(ObjectProperty currentObjectProperty) {
			return property.equals(currentObjectProperty.getProperty());
		}
	}
}
