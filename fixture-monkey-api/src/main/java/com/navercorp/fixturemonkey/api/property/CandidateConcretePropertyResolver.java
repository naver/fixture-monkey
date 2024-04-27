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

package com.navercorp.fixturemonkey.api.property;

import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * It resolves the concrete type {@link Property} of the given {@link Property}.
 */
@FunctionalInterface
@API(since = "1.0.16", status = Status.EXPERIMENTAL)
public interface CandidateConcretePropertyResolver {
	/**
	 * resolves the candidate concrete type properties for the given property.
	 *
	 * @param property it could be a property of concrete type or an abstract class or interface.
	 * @return the resolved property that is actually generated type of property.
	 * It will return property if property is a property of concrete type.
	 */
	List<Property> resolve(Property property);
}
