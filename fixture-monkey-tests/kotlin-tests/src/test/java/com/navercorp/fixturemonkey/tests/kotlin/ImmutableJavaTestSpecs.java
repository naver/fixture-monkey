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

package com.navercorp.fixturemonkey.tests.kotlin;

class ImmutableJavaTestSpecs {
	public static class ArrayObject {
		private final String[] array;

		public ArrayObject(String[] array) {
			this.array = array;
		}

		public String[] getArray() {
			return array;
		}
	}

	public static class NestedArrayObject {
		private final ArrayObject obj;

		public NestedArrayObject(ArrayObject obj) {
			this.obj = obj;
		}

		public ArrayObject getObj() {
			return obj;
		}
	}

}
