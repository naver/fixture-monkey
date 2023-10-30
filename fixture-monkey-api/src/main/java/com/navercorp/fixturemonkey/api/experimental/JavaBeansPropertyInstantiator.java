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

package com.navercorp.fixturemonkey.api.experimental;

import java.beans.PropertyDescriptor;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.6.12", status = Status.EXPERIMENTAL)
public final class JavaBeansPropertyInstantiator<T> implements PropertyInstantiator<T> {
	private Predicate<PropertyDescriptor> propertyDescriptorPredicate = it ->
		it.getReadMethod() != null && it.getWriteMethod() != null;

	public JavaBeansPropertyInstantiator<T> filter(
		Predicate<PropertyDescriptor> propertyDescriptorPredicate
	) {
		this.propertyDescriptorPredicate = this.propertyDescriptorPredicate.and(propertyDescriptorPredicate);
		return this;
	}

	public Predicate<PropertyDescriptor> getPropertyDescriptorPredicate() {
		return propertyDescriptorPredicate;
	}
}
