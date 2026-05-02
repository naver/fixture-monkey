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

package com.navercorp.objectfarm.api.input;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Context for type parsing operations, allowing customization of parsing behavior.
 * <p>
 * This context provides:
 * <ul>
 *   <li>Type aliases for mapping custom type names to JvmType instances</li>
 *   <li>ClassLoader for loading classes during parsing</li>
 *   <li>Strict mode flag for controlling parsing behavior</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * TypeParseContext context = TypeParseContext.builder()
 *     .typeAlias("UserId", new JavaType(Long.class))
 *     .classLoader(myClassLoader)
 *     .strictMode(true)
 *     .build();
 * }</pre>
 */
public final class TypeParseContext {
	private final Map<String, JvmType> typeAliases;
	private final ClassLoader classLoader;
	private final boolean strictMode;

	private TypeParseContext(
		Map<String, JvmType> typeAliases,
		ClassLoader classLoader,
		boolean strictMode
	) {
		this.typeAliases = Collections.unmodifiableMap(new HashMap<>(typeAliases));
		this.classLoader = classLoader;
		this.strictMode = strictMode;
	}

	/**
	 * Creates a new builder for TypeParseContext.
	 *
	 * @return a new builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Returns a default context for simple cases.
	 *
	 * @return the default TypeParseContext
	 */
	public static TypeParseContext defaults() {
		return builder().build();
	}

	/**
	 * Returns the registered type aliases.
	 *
	 * @return unmodifiable map of type aliases
	 */
	public Map<String, JvmType> getTypeAliases() {
		return typeAliases;
	}

	/**
	 * Returns the JvmType for the given alias name.
	 *
	 * @param alias the alias name
	 * @return the JvmType if found, null otherwise
	 */
	@Nullable
	public JvmType getTypeAlias(String alias) {
		return typeAliases.get(alias);
	}

	/**
	 * Returns the ClassLoader used for loading classes during parsing.
	 *
	 * @return the ClassLoader
	 */
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	/**
	 * Returns whether strict mode is enabled.
	 * In strict mode, parsing errors are thrown as exceptions rather than
	 * being handled gracefully.
	 *
	 * @return true if strict mode is enabled
	 */
	public boolean isStrictMode() {
		return strictMode;
	}

	/**
	 * Builder for creating TypeParseContext instances.
	 */
	public static final class Builder {
		private final Map<String, JvmType> typeAliases = new HashMap<>();
		private ClassLoader classLoader = defaultClassLoader();
		private boolean strictMode = false;

		private Builder() {
		}

		/**
		 * Registers a type alias.
		 *
		 * @param alias the alias name
		 * @param type the JvmType to associate with the alias
		 * @return this builder
		 */
		public Builder typeAlias(String alias, JvmType type) {
			this.typeAliases.put(alias, type);
			return this;
		}

		/**
		 * Registers multiple type aliases.
		 *
		 * @param aliases the map of aliases to types
		 * @return this builder
		 */
		public Builder typeAliases(Map<String, JvmType> aliases) {
			this.typeAliases.putAll(aliases);
			return this;
		}

		/**
		 * Sets the ClassLoader for loading classes during parsing.
		 *
		 * @param classLoader the ClassLoader to use
		 * @return this builder
		 */
		public Builder classLoader(ClassLoader classLoader) {
			this.classLoader = classLoader;
			return this;
		}

		/**
		 * Enables or disables strict mode.
		 *
		 * @param strictMode true to enable strict mode
		 * @return this builder
		 */
		public Builder strictMode(boolean strictMode) {
			this.strictMode = strictMode;
			return this;
		}

		/**
		 * Builds the TypeParseContext.
		 *
		 * @return the built TypeParseContext
		 */
		public TypeParseContext build() {
			return new TypeParseContext(typeAliases, classLoader, strictMode);
		}

		private static ClassLoader defaultClassLoader() {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			if (cl != null) {
				return cl;
			}
			cl = TypeParseContext.class.getClassLoader();
			if (cl != null) {
				return cl;
			}
			return ClassLoader.getSystemClassLoader();
		}
	}
}
