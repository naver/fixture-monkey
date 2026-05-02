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

package com.navercorp.objectfarm.api.node;

import com.navercorp.objectfarm.api.type.JvmType;

/**
 * A fixed-size container resolver that always returns the same size.
 * <p>
 * This implementation is useful when you want all containers to have
 * a specific, predetermined size.
 */
public final class FixedContainerSizeResolver implements ContainerSizeResolver {
	private final int size;

	public FixedContainerSizeResolver(int size) {
		this.size = size;
	}

	@Override
	public int resolveContainerSize(JvmType containerType) {
		return size;
	}
}

