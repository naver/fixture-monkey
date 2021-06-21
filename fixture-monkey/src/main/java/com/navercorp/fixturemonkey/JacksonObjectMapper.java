package com.navercorp.fixturemonkey;

import static java.util.stream.Collectors.toList;

import java.util.List;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonObjectMapper {
	private static final List<Module> REGISTERED_MODULES = ObjectMapper.findModules().stream()
		.filter(module -> !module.getModuleName().equalsIgnoreCase("AfterburnerModule"))
		.collect(toList()); // afterburner only support "public setter" for deserializing
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
		.registerModules(REGISTERED_MODULES);

	public static ObjectMapper defaultObjectMapper() {
		return OBJECT_MAPPER.copy();
	}
}
