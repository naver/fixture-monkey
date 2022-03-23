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

import java.util.Objects;

import javax.annotation.Nullable;

import com.navercorp.fixturemonkey.ArbitraryBuilder;

public final class ContainerSizeManipulator extends AbstractArbitraryExpressionManipulator
	implements MetadataManipulator {
	private final Integer min;
	private final Integer max;

	public ContainerSizeManipulator(
		ArbitraryExpression arbitraryExpression,
		@Nullable Integer min,
		@Nullable Integer max
	) {
		super(arbitraryExpression);
		this.min = min;
		this.max = max;
	}

	@Nullable
	public Integer getMin() {
		return min;
	}

	@Nullable
	public Integer getMax() {
		return max;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void accept(ArbitraryBuilder arbitraryBuilder) {
		arbitraryBuilder.apply(this);
	}

	@Override
	public Priority getPriority() {
		return Priority.LOW;
	}

	@Override
	public int compareTo(PriorityManipulator priorityManipulator) {
		if (priorityManipulator instanceof ContainerSizeManipulator) {
			return getArbitraryExpression().compareTo(priorityManipulator.getArbitraryExpression());
		}
		return MetadataManipulator.super.compareTo(priorityManipulator);
	}

	@Override
	public ContainerSizeManipulator copy() {
		return new ContainerSizeManipulator(this.getArbitraryExpression(), this.min, this.max);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ContainerSizeManipulator that = (ContainerSizeManipulator)obj;
		return getArbitraryExpression().equals(that.getArbitraryExpression())
			&& Objects.equals(min, that.min)
			&& Objects.equals(max, that.max);
	}

	@Override
	public int hashCode() {
		return Objects.hash(getArbitraryExpression(), min, max);
	}
}
