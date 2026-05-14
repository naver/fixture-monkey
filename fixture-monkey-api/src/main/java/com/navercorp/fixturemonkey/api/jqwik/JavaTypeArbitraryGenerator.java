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

package com.navercorp.fixturemonkey.api.jqwik;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.arbitraries.BigDecimalArbitrary;
import net.jqwik.api.arbitraries.BigIntegerArbitrary;
import net.jqwik.api.arbitraries.ByteArbitrary;
import net.jqwik.api.arbitraries.CharacterArbitrary;
import net.jqwik.api.arbitraries.DoubleArbitrary;
import net.jqwik.api.arbitraries.FloatArbitrary;
import net.jqwik.api.arbitraries.IntegerArbitrary;
import net.jqwik.api.arbitraries.LongArbitrary;
import net.jqwik.api.arbitraries.ShortArbitrary;
import net.jqwik.api.arbitraries.StringArbitrary;

import com.navercorp.fixturemonkey.api.arbitrary.MonkeyStringArbitrary;

@API(since = "0.4.0", status = Status.MAINTAINED)
public interface JavaTypeArbitraryGenerator {

	default StringArbitrary strings() {
		return monkeyStrings();
	}

	/**
	 * Generate a MonkeyStringArbitrary object filtered so that it doesn't contain a ISOControl character
	 *
	 * @return the filtered MonkeyStringArbitrary object
	 */
	default MonkeyStringArbitrary monkeyStrings() {
		return new MonkeyStringArbitrary().filterCharacter(c -> !Character.isISOControl(c));
	}

	default CharacterArbitrary characters() {
		return Arbitraries.chars();
	}

	default ShortArbitrary shorts() {
		return Arbitraries.shorts();
	}

	default ByteArbitrary bytes() {
		return Arbitraries.bytes();
	}

	default DoubleArbitrary doubles() {
		return Arbitraries.doubles();
	}

	default FloatArbitrary floats() {
		return Arbitraries.floats();
	}

	default IntegerArbitrary integers() {
		return Arbitraries.integers();
	}

	default LongArbitrary longs() {
		return Arbitraries.longs();
	}

	default BigIntegerArbitrary bigIntegers() {
		return Arbitraries.bigIntegers();
	}

	default BigDecimalArbitrary bigDecimals() {
		return Arbitraries.bigDecimals();
	}
}
