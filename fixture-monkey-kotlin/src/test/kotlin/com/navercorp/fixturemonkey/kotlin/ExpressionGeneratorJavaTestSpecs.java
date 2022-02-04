package com.navercorp.fixturemonkey.kotlin;

import java.util.List;

import javax.validation.constraints.Negative;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

public class ExpressionGeneratorJavaTestSpecs {
	public static class PersonJava {
		private final String name;
		@Negative
		private final DogJava dog;
		private final List<DogJava> dogs;
		private final List<List<DogJava>> nestedDogs;
		private final List<List<List<DogJava>>> nestedThriceDogs;

		public PersonJava(
			String name,
			DogJava dog,
			List<DogJava> dogs,
			List<List<DogJava>> nestedDogs,
			List<List<List<DogJava>>> nestedThriceDogs
		) {
			this.name = name;
			this.dog = dog;
			this.dogs = dogs;
			this.nestedDogs = nestedDogs;
			this.nestedThriceDogs = nestedThriceDogs;
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

		public void notGetter(){}
	}

	@Positive
	public static class DogJava {
		private final String name;
		private final List<Integer> loves;

		public DogJava(String name, List<Integer> loves) {
			this.name = name;
			this.loves = loves;
		}

		public String getName() {
			return name;
		}

		public List<Integer> getLoves() {
			return loves;
		}
	}
}
