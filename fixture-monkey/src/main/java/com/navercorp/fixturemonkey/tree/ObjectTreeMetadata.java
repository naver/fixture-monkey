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

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "0.4.0", status = Status.INTERNAL)
public final class ObjectTreeMetadata {
	private final ObjectNode rootNode;
	private final Map<Property, List<ObjectNode>> nodesByProperty; // matchOperator
	private final Set<Annotation> annotations;

	public ObjectTreeMetadata(
		ObjectNode rootNode,
		Map<Property, List<ObjectNode>> propertyNodesByProperty,
		Set<Annotation> annotations
	) {
		this.rootNode = rootNode;
		this.nodesByProperty = propertyNodesByProperty;
		this.annotations = annotations;
	}

	public ObjectNode getRootNode() {
		return rootNode;
	}

	public Map<Property, List<ObjectNode>> getNodesByProperty() {
		return nodesByProperty;
	}

	public Set<Annotation> getAnnotations() {
		return annotations;
	}
}
