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

package com.navercorp.fixturemonkey.api.option;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * It is a public API to customize the ObjectTree.
 * <p>
 * It should contain the customizing options for the ObjectTree, not for the node.
 */
@API(since = "1.1.4", status = Status.EXPERIMENTAL)
public final class BuilderContextInitializer {
	private final boolean validOnly;

	private BuilderContextInitializer(boolean validOnly) {
		this.validOnly = validOnly;
	}

	public static BuilderContextInitializer validOnly(boolean validOnly) {
		return new BuilderContextInitializer(validOnly);
	}

	public boolean isValidOnly() {
		return validOnly;
	}
}
