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

package com.navercorp.fixturemonkey.api.arbitrary;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.EdgeCases;
import net.jqwik.api.RandomDistribution;
import net.jqwik.api.RandomGenerator;
import net.jqwik.api.arbitraries.StringArbitrary;
import net.jqwik.engine.properties.arbitraries.DefaultStringArbitrary;

@API(since = "0.6.0", status = Status.EXPERIMENTAL)
public final class MonkeyStringArbitrary implements StringArbitrary {
	private final StringArbitrary delegate = new DefaultStringArbitrary();

	@Override
	public StringArbitrary ofMaxLength(int maxLength) {
		return delegate.ofMaxLength(maxLength);
	}

	@Override
	public StringArbitrary ofMinLength(int minLength) {
		return delegate.ofMinLength(minLength);
	}

	@Override
	public StringArbitrary withChars(char... chars) {
		return delegate.withChars(chars);
	}

	@Override
	public StringArbitrary withChars(CharSequence chars) {
		return delegate.withChars(chars);
	}

	@Override
	public StringArbitrary withCharRange(char from, char to) {
		return delegate.withCharRange(from, to);
	}

	@Override
	public StringArbitrary ascii() {
		return delegate.ascii();
	}

	@Override
	public StringArbitrary alpha() {
		return delegate.alpha();
	}

	@Override
	public StringArbitrary numeric() {
		return delegate.numeric();
	}

	@Override
	public StringArbitrary whitespace() {
		return delegate.whitespace();
	}

	@Override
	public StringArbitrary all() {
		return delegate.all();
	}

	@Override
	public StringArbitrary excludeChars(char... charsToExclude) {
		return delegate.excludeChars(charsToExclude);
	}

	@Override
	public StringArbitrary withLengthDistribution(RandomDistribution lengthDistribution) {
		return delegate.withLengthDistribution(lengthDistribution);
	}

	@Override
	public StringArbitrary repeatChars(double repeatProbability) {
		return delegate.repeatChars(repeatProbability);
	}

	@Override
	public RandomGenerator<String> generator(int genSize) {
		return delegate.generator(genSize);
	}

	@Override
	public EdgeCases<String> edgeCases(int maxEdgeCases) {
		return delegate.edgeCases(maxEdgeCases);
	}

	// TODO: implement filterCharacter method
	//
	// public StringArbitrary filterCharacter(Predicate<Character> predicate) {
	//
	// }
}
