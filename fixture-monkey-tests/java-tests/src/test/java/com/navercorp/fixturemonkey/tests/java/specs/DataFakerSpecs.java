package com.navercorp.fixturemonkey.tests.java.specs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class DataFakerSpecs {

	@NoArgsConstructor
	@Getter
	@Setter
	public static class User {
		private String fullName;
		private String email;
		private String homeAddress;
		private String phoneNumber;
	}

	@NoArgsConstructor
	@Getter
	@Setter
	public static class Finance {
		private String creditCard;
	}
}
