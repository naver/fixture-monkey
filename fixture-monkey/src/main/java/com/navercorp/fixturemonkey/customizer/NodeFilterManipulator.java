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

package com.navercorp.fixturemonkey.customizer;

import static com.navercorp.fixturemonkey.api.type.Types.isAssignable;

import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.tree.ObjectNode;

@SuppressWarnings("rawtypes")
@API(since = "0.4.0", status = Status.MAINTAINED)
public final class NodeFilterManipulator implements NodeManipulator {
	private final Class<?> type;
	private final Predicate filter;

	public NodeFilterManipulator(Class<?> type, Predicate filter) {
		this.type = type;
		this.filter = filter;
	}

	@Override
	public void manipulate(ObjectNode objectNode) {
		Class<?> actualType = Types.getActualType(objectNode.getProperty().getType());
		if (!isAssignable(actualType, type)) {
			throw new IllegalArgumentException(
				"Wrong type filter is applied. Expected: " + type + ", Actual: " + actualType
			);
		}
		objectNode.addArbitraryFilter(filter);
	}
}
