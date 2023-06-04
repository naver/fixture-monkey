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

package com.navercorp.fixturemonkey.javax.validation.matcher;

import org.apiguardian.api.API;

import com.navercorp.fixturemonkey.api.matcher.AnnotationPackageNameMatcher;
import com.navercorp.fixturemonkey.api.matcher.Matcher;

@API(since = "0.6.0", status = API.Status.MAINTAINED)
public final class JavaxMatchers {
	public static final Matcher JAVAX_PACKAGE_MATCHER = property ->
		new AnnotationPackageNameMatcher("javax.validation.constraints").match(property);
}
