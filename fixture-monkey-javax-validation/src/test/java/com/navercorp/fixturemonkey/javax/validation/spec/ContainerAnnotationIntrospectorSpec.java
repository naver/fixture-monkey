package com.navercorp.fixturemonkey.javax.validation.spec;

import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContainerAnnotationIntrospectorSpec {
	private List<String> container;

	@Size
	private List<String> defaultSizeContainer;

	@Size(min = 5, max = 10)
	private List<String> sizeContainer;

	@Size(min = 3)
	private List<String> minSizeContainer;

	@Size(max = 5)
	private List<String> maxSizeContainer;

	@NotEmpty
	private List<String> notEmptyContainer;

	@Size(max = 5)
	@NotEmpty
	private List<String> notEmptyAndMaxSizeContainer;
}
