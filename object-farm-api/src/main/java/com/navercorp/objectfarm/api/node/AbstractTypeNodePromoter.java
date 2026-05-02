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

package com.navercorp.objectfarm.api.node;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

import com.navercorp.objectfarm.api.nodecandidate.JvmMapEntryNodeCandidate;
import com.navercorp.objectfarm.api.nodecandidate.JvmMapNodeCandidate;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Promotes abstract type (interface and abstract class) node candidates to concrete JvmNode instances.
 * <p>
 * This promoter uses the {@link InterfaceResolver} from the context to resolve
 * abstract types (both interfaces and abstract classes) to their concrete implementations
 * during node promotion.
 * <p>
 * If the context provides an InterfaceResolver, it will be used to determine the
 * concrete type. If no resolver is available or it returns null, the original
 * type is used (which may cause issues during instantiation).
 *
 * @since 1.1.0
 */
public final class AbstractTypeNodePromoter implements JvmNodePromoter {

	@Override
	public boolean canPromote(JvmNodeCandidate node) {
		// Map and MapEntry candidates have specialized promoters — don't intercept them
		if (node instanceof JvmMapNodeCandidate || node instanceof JvmMapEntryNodeCandidate) {
			return false;
		}
		int modifiers = node.getType().getRawType().getModifiers();
		return Modifier.isInterface(modifiers) || Modifier.isAbstract(modifiers);
	}

	@Override
	public List<JvmNode> promote(JvmNodeCandidate node, JvmNodeContext context) {
		JvmType nodeType = node.getType();

		return Collections.singletonList(new JavaNode(nodeType, node.getName(), null, node.getCreationMethod()));
	}
}
