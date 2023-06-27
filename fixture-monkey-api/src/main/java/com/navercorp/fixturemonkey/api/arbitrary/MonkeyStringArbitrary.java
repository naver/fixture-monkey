package com.navercorp.fixturemonkey.api.arbitrary;

import java.util.function.Predicate;

import net.jqwik.api.EdgeCases;
import net.jqwik.api.RandomDistribution;
import net.jqwik.api.RandomGenerator;
import net.jqwik.api.arbitraries.StringArbitrary;
import net.jqwik.engine.properties.arbitraries.DefaultStringArbitrary;

public class MonkeyStringArbitrary implements StringArbitrary {

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
