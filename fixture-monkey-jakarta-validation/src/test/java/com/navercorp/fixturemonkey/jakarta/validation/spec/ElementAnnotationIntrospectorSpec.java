package com.navercorp.fixturemonkey.jakarta.validation.spec;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
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
