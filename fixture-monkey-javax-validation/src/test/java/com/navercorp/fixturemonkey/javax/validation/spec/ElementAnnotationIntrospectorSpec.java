package com.navercorp.fixturemonkey.javax.validation.spec;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ElementAnnotationIntrospectorSpec {

	@NotEmpty
	private List<@NotBlank String> notEmptyContainerAndNotBlankElement;

	@Size(min = 1, max = 3)
	private List<@NotBlank String> sizeContainerAndNotBlankElement;
}
