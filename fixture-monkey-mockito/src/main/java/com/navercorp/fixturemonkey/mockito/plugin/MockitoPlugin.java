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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptionsBuilder;
import com.navercorp.fixturemonkey.api.plugin.Plugin;
import com.navercorp.fixturemonkey.mockito.introspector.MockitoIntrospector;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class MockitoPlugin implements Plugin {
	@Override
	public void accept(FixtureMonkeyOptionsBuilder optionsBuilder) {
		optionsBuilder.fallbackIntrospector(it -> MockitoIntrospector.INSTANCE);
	}
}
