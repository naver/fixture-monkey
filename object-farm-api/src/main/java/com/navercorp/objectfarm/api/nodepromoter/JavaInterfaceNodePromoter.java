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

package com.navercorp.objectfarm.api.nodepromoter;

import java.lang.reflect.Modifier;
import java.util.function.Function;

import com.navercorp.objectfarm.api.node.JavaNode;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;
import com.navercorp.objectfarm.api.nodecontext.JvmNodeContext;
import com.navercorp.objectfarm.api.type.JvmType;

public final class JavaInterfaceNodePromoter implements JvmNodePromoter {
	private final Function<JvmType, JvmType> interfaceTypeResolver;

	public JavaInterfaceNodePromoter(Function<JvmType, JvmType> interfaceTypeResolver) {
		this.interfaceTypeResolver = interfaceTypeResolver;
	}

	@Override
	public boolean canPromote(JvmNodeCandidate node) {
		return Modifier.isInterface(node.getJvmType().getRawType().getModifiers());
	}

	@Override
	public JvmNode promote(JvmNodeCandidate node, JvmNodeContext context) {
		return new JavaNode(
			interfaceTypeResolver.apply(node.getJvmType()),
			node.getName(),
			context
		);
	}
}
