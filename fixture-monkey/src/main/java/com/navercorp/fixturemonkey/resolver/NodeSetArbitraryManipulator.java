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

import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.NOT_NULL_INJECT;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
final class NodeSetArbitraryManipulator<T> implements NodeManipulator {
	private final Arbitrary<T> arbitrary;

	NodeSetArbitraryManipulator(Arbitrary<T> arbitrary) {
		this.arbitrary = arbitrary;
	}

	@Override
	public void manipulate(ArbitraryNode arbitraryNode) {
		ArbitraryProperty arbitraryProperty = arbitraryNode.getArbitraryProperty();
		arbitraryNode.setArbitraryProperty(arbitraryProperty.withNullInject(NOT_NULL_INJECT));
		arbitraryNode.setArbitrary(arbitrary);
	}
}
