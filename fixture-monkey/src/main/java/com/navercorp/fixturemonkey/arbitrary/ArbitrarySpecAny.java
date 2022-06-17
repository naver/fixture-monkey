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

package com.navercorp.fixturemonkey.arbitrary;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Objects;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.OldArbitraryBuilderImpl;
import com.navercorp.fixturemonkey.customizer.ExpressionSpec;

@API(since = "0.4.0", status = Status.DEPRECATED)
@Deprecated
public final class ArbitrarySpecAny implements BuilderManipulator {
	private final List<ExpressionSpec> specs;

	public ArbitrarySpecAny(List<ExpressionSpec> specs) {
		this.specs = specs;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void accept(OldArbitraryBuilderImpl arbitraryBuilder) {
		arbitraryBuilder.specAny(specs.toArray(new ExpressionSpec[0]));
	}

	@Override
	public BuilderManipulator copy() {
		List<ExpressionSpec> copiedSpecs = specs.stream()
			.map(ExpressionSpec::copy)
			.collect(toList());

		return new ArbitrarySpecAny(copiedSpecs);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ArbitrarySpecAny that = (ArbitrarySpecAny)obj;
		return specs.equals(that.specs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(specs);
	}
}
