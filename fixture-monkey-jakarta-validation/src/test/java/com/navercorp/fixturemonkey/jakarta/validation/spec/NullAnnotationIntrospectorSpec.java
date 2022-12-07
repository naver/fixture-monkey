package com.navercorp.fixturemonkey.jakarta.validation.spec;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NullAnnotationIntrospectorSpec {
	@Null
	private String nullValue;

	@NotNull
	private String notNull;

	@NotBlank
	private String notBlank;

	@NotEmpty
	private String notEmpty;

	private String defaultValue;

	@NotEmpty
	private List<String> notEmptyContainer;
}
