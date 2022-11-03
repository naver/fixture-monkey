package com.navercorp.fixturemonkey.javax.validation.spec;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

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
