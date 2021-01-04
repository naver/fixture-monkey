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

package com.navercorp.fixturemonkey;

import static java.util.stream.Collectors.*;

import java.util.List;
import java.util.stream.Stream;

public class FixtureMonkey {

	public <T> Stream<T> giveMe(Class<T> type) {
		return this.giveMe(type, true);
	}

	public <T> List<T> giveMe(Class<T> type, int size) {
		return this.giveMe(type, size, true);
	}

	// TODO: implementation
	public <T> Stream<T> giveMe(Class<T> type, boolean validOnly) {
		return Stream.empty();
	}

	public <T> List<T> giveMe(Class<T> type, int size, boolean validOnly) {
		return this.giveMe(type, validOnly)
			.limit(size)
			.collect(toList());
	}

	public <T> T giveMeOne(Class<T> type) {
		return this.giveMeOne(type, true);
	}

	// TODO: implementation
	public <T> T giveMeOne(Class<T> type, boolean validOnly) {
		return null;
	}
}
