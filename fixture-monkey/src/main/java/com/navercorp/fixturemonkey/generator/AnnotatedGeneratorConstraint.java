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

package com.navercorp.fixturemonkey.generator;

import java.math.BigDecimal;

final class AnnotatedGeneratorConstraint {
	private final BigDecimal min;
	private final BigDecimal max;
	private final boolean minInclusive;
	private final boolean maxInclusive;

	public AnnotatedGeneratorConstraint(BigDecimal min, BigDecimal max, boolean minInclusive, boolean maxInclusive) {
		this.min = min;
		this.max = max;
		this.minInclusive = minInclusive;
		this.maxInclusive = maxInclusive;
	}

	public BigDecimal getMin() {
		return min;
	}

	public BigDecimal getMax() {
		return max;
	}

	public boolean isMinInclusive() {
		return minInclusive;
	}

	public boolean isMaxInclusive() {
		return maxInclusive;
	}

	public static AnnotatedGeneratorConstraintBuilder builder() {
		return new AnnotatedGeneratorConstraintBuilder();
	}

	public static class AnnotatedGeneratorConstraintBuilder {
		private BigDecimal min;
		private BigDecimal max;
		private boolean minInclusive = true;
		private boolean maxInclusive = true;

		public AnnotatedGeneratorConstraintBuilder min(BigDecimal min) {
			this.min = min;
			return this;
		}

		public AnnotatedGeneratorConstraintBuilder max(BigDecimal max) {
			this.max = max;
			return this;
		}

		public AnnotatedGeneratorConstraintBuilder minInclusive(boolean minInclusive) {
			this.minInclusive = minInclusive;
			return this;
		}

		public AnnotatedGeneratorConstraintBuilder maxInclusive(boolean maxInclusive) {
			this.maxInclusive = maxInclusive;
			return this;
		}

		public AnnotatedGeneratorConstraint build() {
			return new AnnotatedGeneratorConstraint(
				min,
				max,
				minInclusive,
				maxInclusive
			);
		}
	}
}
