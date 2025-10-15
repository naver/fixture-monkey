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

package com.navercorp.fixturemonkey.kotlin;

import java.util.List;

import javax.validation.constraints.Negative;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

import org.jspecify.annotations.Nullable;

public class ExpressionGeneratorJavaTestSpecs {
	public static class PersonJava {
		private final String name;
		@Negative
		private final DogJava dog;
		private final List<DogJava> dogs;
		private final List<List<DogJava>> nestedDogs;
		private final List<List<List<DogJava>>> nestedThriceDogs;
		@Nullable
		private final DogJava nullableDog;
		@Nullable
		private final List<DogJava> nullableDogs;
		private final boolean married;
		@Nullable
		private final Boolean happy;

		public PersonJava(
			String name,
			DogJava dog,
			List<DogJava> dogs,
			List<List<DogJava>> nestedDogs,
			List<List<List<DogJava>>> nestedThriceDogs,
			@Nullable DogJava nullableDog,
			@Nullable List<DogJava> nullableDogs,
			boolean married,
			@Nullable Boolean happy
		) {
			this.name = name;
			this.dog = dog;
			this.dogs = dogs;
			this.nestedDogs = nestedDogs;
			this.nestedThriceDogs = nestedThriceDogs;
			this.nullableDog = nullableDog;
			this.nullableDogs = nullableDogs;
			this.married = married;
			this.happy = happy;
		}

		public String getName() {
			return name;
		}

		@NotEmpty
		public DogJava getDog() {
			return dog;
		}

		public List<DogJava> getDogs() {
			return dogs;
		}

		public List<List<DogJava>> getNestedDogs() {
			return nestedDogs;
		}

		public List<List<List<DogJava>>> getNestedThriceDogs() {
			return nestedThriceDogs;
		}

		public void notGetter() {
		}

		@Nullable
		public DogJava getNullableDog() {
			return nullableDog;
		}

		@Nullable
		public List<DogJava> getNullableDogs() {
			return nullableDogs;
		}

		public boolean isMarried() {
			return married;
		}

		@Nullable
		public Boolean getHappy() {
			return happy;
		}
	}

	@Positive
	public static class DogJava {
		private final String name;
		private final List<Integer> loves;
		@Nullable
		private final String nullableName;

		private final boolean cute;

		public DogJava(String name, List<Integer> loves, @Nullable String nullableName, boolean cute) {
			this.name = name;
			this.loves = loves;
			this.nullableName = nullableName;
			this.cute = cute;
		}

		public String getName() {
			return name;
		}

		public List<Integer> getLoves() {
			return loves;
		}

		@Nullable
		public String getNullableName() {
			return nullableName;
		}

		public boolean isCute() {
			return cute;
		}
	}
}
