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

package com.navercorp.objectfarm.api.nodecontext;

import com.navercorp.objectfarm.api.type.JvmType;

public interface JvmContainerNodeContext {
	/**
	 * Resolves the size of a container based on the given container type.
	 * The size is determined by the random seed to ensure reproducible results.
	 *
	 * @param containerType the type of the container
	 * @return the resolved size of the container
	 */
	int resolveContainerSize(JvmType containerType);
}
