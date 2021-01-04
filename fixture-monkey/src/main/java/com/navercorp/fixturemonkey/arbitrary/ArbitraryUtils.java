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

import static java.util.stream.Collectors.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

public class ArbitraryUtils {
	public static Arbitrary<UUID> uuid() {
		return Arbitraries.create(UUID::randomUUID);
	}

	public static Arbitrary<Instant> currentTime() {
		return Arbitraries.create(Instant::now);
	}

	public static <T> List<T> list(Arbitrary<T> arbitrary, int size) {
		return arbitrary.sampleStream()
			.limit(size)
			.collect(toList());
	}
}
