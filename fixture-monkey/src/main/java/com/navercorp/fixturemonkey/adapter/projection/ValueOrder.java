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

package com.navercorp.fixturemonkey.adapter.projection;

/**
 * Typed ordering for value candidates that encodes source priority.
 * <p>
 * {@link UserOrder} (from direct builder operations) always takes precedence over
 * {@link RegisterOrder} (from registered builders), regardless of sequence number.
 * Within the same order type, higher sequence numbers win.
 */
interface ValueOrder extends Comparable<ValueOrder> {
	int sequence();

	/**
	 * Returns a label identifying the origin of this value for tracing.
	 * "DIRECT" for user builder operations, "REGISTER" for registered builders.
	 */
	String sourceLabel();

	final class UserOrder implements ValueOrder {
		private final int sequence;

		private UserOrder(int sequence) {
			this.sequence = sequence;
		}

		static UserOrder of(int sequence) {
			return new UserOrder(sequence);
		}

		@Override
		public int sequence() {
			return sequence;
		}

		@Override
		public String sourceLabel() {
			return "DIRECT";
		}

		@Override
		public int compareTo(ValueOrder other) {
			if (other instanceof UserOrder) {
				return Integer.compare(this.sequence, ((UserOrder)other).sequence);
			}
			// UserOrder always wins over RegisterOrder
			return 1;
		}
	}

	final class RegisterOrder implements ValueOrder {
		private final int sequence;

		private RegisterOrder(int sequence) {
			this.sequence = sequence;
		}

		static RegisterOrder of(int sequence) {
			return new RegisterOrder(sequence);
		}

		@Override
		public int sequence() {
			return sequence;
		}

		@Override
		public String sourceLabel() {
			return "REGISTER";
		}

		@Override
		public int compareTo(ValueOrder other) {
			if (other instanceof RegisterOrder) {
				return Integer.compare(this.sequence, ((RegisterOrder)other).sequence);
			}
			// RegisterOrder always loses to UserOrder
			return -1;
		}
	}
}
