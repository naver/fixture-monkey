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

import java.util.List;
import java.util.stream.Collectors;


import com.navercorp.objectfarm.api.nodecontext.JvmNodeContext;
import com.navercorp.objectfarm.api.nodepromoter.JvmNodePromoter;
import com.navercorp.objectfarm.api.type.JvmType;

public final class JavaNode implements JvmNode {
	private final JvmType type;
	private final String nodeName;
	private final JvmNodeContext context;

	public JavaNode(JvmType type, String nodeName, JvmNodeContext context) {
		this.type = type;
		this.nodeName = nodeName;
		this.context = context;
	}

	@Override
	public JvmType getType() {
		return type;
	}

	@Override
	public String getNodeName() {
		return nodeName;
	}

	@Override
	public List<JvmNode> getChildren(JvmNodePromoter promoter) {
		return this.context.getCandidateNodeGenerators().stream()
			.filter(it -> it.isSupported(type))
			.flatMap(it -> it.generateNextNodeCandidates(type).stream())
			.filter(promoter::canPromote)
			.map(candidate -> promoter.promote(candidate, context))
			.collect(Collectors.toList());
	}
}
