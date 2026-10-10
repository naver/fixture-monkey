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

package com.navercorp.fixturemonkey.experimental;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.ArbitraryBuilder;

@API(since = "0.6.12", status = Status.MAINTAINED)
public interface ExperimentalArbitraryBuilder<T> extends ArbitraryBuilder<T> {
	/**
	 * Selects builders registered by {@code registerByName} to apply to the samples of this builder.
	 * <p>
	 * Builders registered by name for the same type are alternatives: a sample applies the selected ones, or one of
	 * them when none is selected. A selected builder applies wherever its type appears in a sample, and has no effect
	 * on a sample its type does not appear in. Where they meet, a selected builder takes precedence over every
	 * registered builder that is not selected and over one selected before it, whether its type sits inside or outside
	 * theirs, for values, sizes, customizers and instantiators alike. A value set to an object as a whole is used as it
	 * is, so no instantiator applies to it. It yields to what this builder declares itself, except a container size
	 * declared through a wildcard path, which yields to a size any registered builder declares inside.
	 * <p>
	 * A value this builder sets to the whole object, such as one set to {@code "$"} (null included), the one a
	 * sampled {@code fixed()} keeps or the one {@code thenApply} produces, takes precedence over every selection, so
	 * names are selected before it. Selecting names in a builder passed to {@code register} is not supported.
	 *
	 * @param names the names to select, the later one taking precedence
	 * @return this builder
	 * @throws IllegalStateException    when this builder already sets a value to the whole object
	 * @throws IllegalArgumentException on sampling, when a name is not registered
	 */
	@API(since = "1.2.4", status = Status.EXPERIMENTAL)
	ExperimentalArbitraryBuilder<T> selectName(String... names);
}
