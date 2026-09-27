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

package com.navercorp.fixturemonkey.kotest

import com.navercorp.fixturemonkey.api.random.Randoms
import io.kotest.property.Arb
import io.kotest.property.RandomSource
import io.kotest.property.arbitrary.single

/**
 * Samples this arb from the random [Randoms.current] decides, the same rule jqwik values follow.
 */
internal fun <T> Arb<T>.sampleInScope(): T = single(RandomSource.seeded(Randoms.current().nextLong()))
