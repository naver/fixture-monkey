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

package com.navercorp.fixturemonkey.mockito.plugin;

import lombok.Data;

class MockitoPluginTestSpecs {
	@Data
	public static class Sample {
		private AbstractSample abstractSample;
		private InterfaceSample interfaceSample;
	}

	public abstract static class AbstractSample {
		private String value;

		public String getValue() {
			return this.value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	static class AbstractSampleImpl extends AbstractSample {
	}

	interface InterfaceSample {
		int getValue();
	}

	@Data
	public static class InterfaceSampleImpl implements InterfaceSample {
		private int value;

		@Override
		public int getValue() {
			return this.value;
		}
	}
}
