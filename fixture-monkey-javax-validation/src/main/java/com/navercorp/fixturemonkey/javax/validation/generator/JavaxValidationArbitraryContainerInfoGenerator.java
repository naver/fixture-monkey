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

package com.navercorp.fixturemonkey.javax.validation.generator;

import java.util.Optional;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfoGenerator;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGeneratorContext;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class JavaxValidationArbitraryContainerInfoGenerator implements ArbitraryContainerInfoGenerator {
	@Override
	public ArbitraryContainerInfo generate(ContainerPropertyGeneratorContext context) {
		Integer min = null;
		Integer max = null;
		Optional<Size> sizeAnnotation = context.getProperty().getAnnotation(Size.class);
		if (sizeAnnotation.isPresent()) {
			Size size = sizeAnnotation.get();
			min = size.min();
			if (size.max() != Integer.MAX_VALUE) {    // not initialized for preventing OOM
				max = size.max();
			} else {
				int defaultArbitraryContainerSize = context.getGenerateOptions()
					.getDefaultArbitraryContainerSize();
				max = min + defaultArbitraryContainerSize;
			}
		}

		if (context.getProperty().getAnnotation(NotEmpty.class).isPresent()) {
			if (min != null) {
				min = Math.max(1, min);
			} else {
				min = 1;
			}
		}

		if (min == null) {
			min = 0;
		}

		if (max == null) {
			int defaultArbitraryContainerSize = context.getGenerateOptions()
				.getDefaultArbitraryContainerSize();
			max = min + defaultArbitraryContainerSize;
		}

		return new ArbitraryContainerInfo(min, max, false);
	}
}
