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

import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.customizer.InnerSpecState.ContainerInfoHolder;
import com.navercorp.fixturemonkey.customizer.InnerSpecState.FilterHolder;
import com.navercorp.fixturemonkey.customizer.InnerSpecState.NodeResolverObjectHolder;

@API(since = "0.4.10", status = Status.EXPERIMENTAL)
public final class ManipulatorSet {
	private final List<NodeResolverObjectHolder> nodeResolverObjectHolders;
	private final List<ContainerInfoHolder> containerInfoManipulators;
	private final List<FilterHolder> postConditionManipulators;

	public ManipulatorSet(
		List<NodeResolverObjectHolder> nodeResolverObjectHolders,
		List<ContainerInfoHolder> containerInfoManipulators,
		List<FilterHolder> postConditionManipulators
	) {
		this.nodeResolverObjectHolders = nodeResolverObjectHolders;
		this.containerInfoManipulators = containerInfoManipulators;
		this.postConditionManipulators = postConditionManipulators;
	}

	public List<NodeResolverObjectHolder> getNodeResolverObjectHolders() {
		return nodeResolverObjectHolders;
	}

	public List<ContainerInfoHolder> getContainerInfoManipulators() {
		return containerInfoManipulators;
	}

	public List<FilterHolder> getPostConditionManipulators() {
		return postConditionManipulators;
	}

}
