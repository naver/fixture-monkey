package com.navercorp.fixturemonkey.api.property;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;

/**
 * Represent a location of property.
 * It is mainly used as a caching key or for logging.
 */
public final class PropertyPath implements Comparable<PropertyPath> {
	private final Property property;
	@Nullable
	private final PropertyPath parentPropertyPath;
	private final int depth;

	private final LazyArbitrary<String> expression = LazyArbitrary.lazy(this::initExpression);

	public PropertyPath(Property property, @Nullable PropertyPath parentPropertyPath, int depth) {
		this.property = property;
		this.parentPropertyPath = parentPropertyPath;
		this.depth = depth;
	}

	public Property getProperty() {
		return property;
	}

	public int getDepth() {
		return depth;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		PropertyPath that = (PropertyPath)obj;
		return depth == that.depth
			&& property.equals(that.property)
			&& Objects.equals(parentPropertyPath, that.parentPropertyPath);
	}

	@Override
	public int hashCode() {
		return Objects.hash(property, parentPropertyPath, depth);
	}

	@Override
	public int compareTo(PropertyPath obj) {
		return Integer.compare(obj.depth, this.depth);
	}

	public String getExpression() {
		return expression.getValue();
	}

	private String initExpression() {
		return parentPropertyPath == null ? "" : parentPropertyPath.getExpression()
			+ getDelimiter()
			+ getCurrentPropertyExpression();
	}

	private String getCurrentPropertyExpression() {
		if (property instanceof TreeRootProperty || property instanceof MapEntryElementProperty) {
			return "";
		} else if (property instanceof MapKeyElementProperty) {
			return "{key}";
		} else if (property instanceof MapValueElementProperty) {
			return "{value}";
		} else if (property instanceof ContainerElementProperty) {
			return "[" + ((ContainerElementProperty)property).getIndex() + "]";
		}
		return property.getName();
	}

	private String getDelimiter() {
		if (property instanceof ContainerElementProperty
			|| property instanceof TreeRootProperty
			|| property instanceof MapEntryElementProperty
			|| property instanceof MapKeyElementProperty
			|| property instanceof MapValueElementProperty
			|| parentPropertyPath == null
			|| "".equals(parentPropertyPath.getExpression())) {
			return "";
		}

		return ".";
	}
}
