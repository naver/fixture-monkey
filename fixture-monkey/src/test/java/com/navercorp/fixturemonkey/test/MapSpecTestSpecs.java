package com.navercorp.fixturemonkey.test;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

public class MapSpecTestSpecs {
	@Setter
	@Getter
	public static class MapObject {
		private Map<String, String> strMap;
		private Map<List<String>, String> listMap;
		private Map<String, Map<String, String>> mapValueMap;
		private Map<Map<String, String>, String> mapKeyMap;
		private Map<Map<String, String>, Map<String, String>> mapKeyValueMap;
	}
}
