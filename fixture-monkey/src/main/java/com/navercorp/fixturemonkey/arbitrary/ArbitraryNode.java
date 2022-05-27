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

package com.navercorp.fixturemonkey.arbitrary;

import static com.navercorp.fixturemonkey.Constants.DEFAULT_ELEMENT_MAX_SIZE;
import static com.navercorp.fixturemonkey.Constants.HEAD_NAME;
import static com.navercorp.fixturemonkey.Constants.NO_OR_ALL_INDEX_INTEGER_VALUE;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.TypeSupports;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpression.Cursor;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpression.ExpIndexCursor;

public final class ArbitraryNode<T> {
	@SuppressWarnings("rawtypes")
	private final List<ArbitraryNode> children;
	private final ArbitraryType<T> type;
	private final String propertyName;
	private final String metadata;
	private final int indexOfIterable;
	private final ArbitraryNodeStatus<T> status;
	private final boolean keyOfMapStructure;
	private final double nullInject;
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@SuppressWarnings("rawtypes")
	public ArbitraryNode(
		List<ArbitraryNode> children,
		ArbitraryType<T> type,
		String propertyName,
		String metadata,
		int indexOfIterable,
		ArbitraryNodeStatus<T> status,
		boolean keyOfMapStructure,
		double nullInject
	) {
		this.children = new ArrayList<>();
		children.forEach(this::addChildNode);
		this.type = type;
		this.propertyName = propertyName;
		this.metadata = metadata;
		this.indexOfIterable = indexOfIterable;
		this.status = status.copy();
		this.keyOfMapStructure = keyOfMapStructure;
		this.nullInject = nullInject;
	}

	@SuppressWarnings("rawtypes")
	public static ArbitraryNodeBuilder builder() {
		return new ArbitraryNodeBuilder();
	}

	@SuppressWarnings("rawtypes")
	public List<ArbitraryNode> findChildrenByCursor(Cursor cursor) {
		List<ArbitraryNode> foundChildren = new ArrayList<>();
		for (ArbitraryNode child : children) {
			if (child.isKeyOfMapStructure()) {
				continue;
			}
			ExpIndexCursor nodeCursor = new ExpIndexCursor(child.propertyName, child.indexOfIterable);
			if (cursor.equals(nodeCursor)) {
				child.mark();
				foundChildren.add(child);
			}
		}
		return foundChildren;
	}

	public int getElementSize() {
		if (!type.isContainer()) {
			throw new IllegalStateException("Can not initialize element size because node is not container.");
		} else if (type.isOptional()) {
			return new ContainerSizeConstraint(0, 1).getArbitraryElementSize();
		}

		if (getContainerSizeConstraint() != null) {
			return getContainerSizeConstraint().getArbitraryElementSize();
		}

		Integer min = null;
		Integer max = null;

		Size size = type.getAnnotation(Size.class);
		if (size != null) {
			min = size.min();
			if (size.max() != Integer.MAX_VALUE) { // not initialized for preventing OOM
				max = size.max();
			} else {
				max = min + DEFAULT_ELEMENT_MAX_SIZE;
			}
		}

		NotEmpty notEmpty = type.getAnnotation(NotEmpty.class);
		if (notEmpty != null) {
			if (min != null) {
				min = Math.max(1, min);
			} else {
				min = 1;
			}
		}

		return new ContainerSizeConstraint(min, max).getArbitraryElementSize();
	}

	public boolean isNotSetContainerSize() {
		return getContainerSizeConstraint() == null;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public void apply(PreArbitraryManipulator preArbitraryManipulator) {
		if (preArbitraryManipulator instanceof ArbitrarySetArbitrary) {
			this.setFixed(true);
			this.setArbitrary((Arbitrary<T>)preArbitraryManipulator.getApplicableValue());
		} else if (preArbitraryManipulator instanceof AbstractArbitrarySet) {
			Object toValue = preArbitraryManipulator.getApplicableValue();

			if (toValue != null) {
				Class<?> clazz = this.getType().getType();
				Class<?> toValueClazz = toValue.getClass();
				if (
					!TypeSupports.isCompatibleType(clazz, toValueClazz)
						&& !clazz.isAssignableFrom(toValueClazz)
						&& !Arbitrary.class.isAssignableFrom(toValueClazz)
				) {
					log.warn("property \"{}\" type is \"{}\", but given set value is \"{}\".",
						propertyName,
						clazz.getSimpleName(),
						toValueClazz.getSimpleName()
					);
				}
			}
			this.status.setValue(new LazyValue(toValue));
		} else {
			throw new IllegalArgumentException("Not Implemented PreArbitraryManipulator");
		}
	}

	public void apply(ArbitraryNullity manipulator) {
		this.setActive(!manipulator.toNull());
	}

	public void addChildNode(ArbitraryNode<?> child) {
		children.add(child);
	}

	public void setNullable(boolean nullable) {
		this.getStatus().setNullable(nullable);
	}

	public void setArbitrary(Arbitrary<T> arbitrary) {
		this.getStatus().setArbitrary(arbitrary);
	}

	public void setManipulated(boolean manipulated) {
		this.getStatus().setManipulated(manipulated);
	}

	public void setActive(boolean active) {
		this.getStatus().setActive(active);
	}

	public void setContainerSizeConstraint(ContainerSizeConstraint containerSizeConstraint) {
		this.getStatus().setContainerSizeConstraint(containerSizeConstraint);
	}

	public void setFixed(boolean fixed) {
		this.getStatus().setFixed(fixed);
	}

	public void setValue(Supplier<T> value) {
		this.getStatus().setValue(new LazyValue<>(value));
	}

	public void setFixedValue(Supplier<T> value) {
		this.getStatus().setValue(new LazyValue<>(value, true));
	}

	public void clearValue() {
		this.getStatus().setValue(null);
	}

	public void setReset(boolean reset) {
		this.getStatus().setReset(reset);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public void addPostArbitraryOperation(PostArbitraryManipulator postArbitraryManipulator) {
		this.status.addPostArbitraryManipulator(postArbitraryManipulator);
	}

	public Arbitrary<T> getArbitrary() {
		ArbitraryNodeStatus<T> status = getStatus();
		if (!status.isActive() && status.isManipulated()) { // setNull
			return Arbitraries.just(null);
		}
		return status.getArbitrary();
	}

	public boolean isNullable() {
		return this.status.nullable;
	}

	public boolean isManipulated() {
		return status.manipulated;
	}

	public boolean isActive() {
		return status.active;
	}

	@SuppressWarnings("rawtypes")
	public List<ArbitraryNode> getChildren() {
		return children;
	}

	public ArbitraryType<T> getType() {
		if (type instanceof NullArbitraryType) {
			LazyValue<T> value = getValue();
			if (value == null) {
				return type;
			}
			return value.getArbitraryType();
		}
		return type;
	}

	public int getIndexOfIterable() {
		return indexOfIterable;
	}

	/**
	 * Deprecated Use getPropertyName instead.
	 * @return String
	 */
	@Deprecated
	public String getFieldName() {
		return this.getPropertyName();
	}

	public String getPropertyName() {
		return this.propertyName;
	}

	public String getMetadata() {
		return metadata;
	}

	public double getNullInject() {
		return nullInject;
	}

	public boolean isKeyOfMapStructure() {
		return keyOfMapStructure;
	}

	/**
	 * Deprecated Use getElementSize instead
	 */
	@Deprecated
	public ContainerSizeConstraint getContainerSizeConstraint() {
		return this.status.containerSizeConstraint;
	}

	public List<PostArbitraryManipulator<T>> getPostArbitraryManipulators() {
		return this.status.postArbitraryManipulators;
	}

	public boolean isFixed() {
		return this.getStatus().isFixed();
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean isLeafNode() {
		return (this.getChildren().isEmpty() || isMap()) && this.getArbitrary() != null;
	}

	@API(since = "0.4.0", status = Status.EXPERIMENTAL)
	public boolean isMap() {
		return this.type.isMap();
	}

	public boolean isHead() {
		return this.getPropertyName().equals(HEAD_NAME) && indexOfIterable == NO_OR_ALL_INDEX_INTEGER_VALUE;
	}

	public LazyValue<T> getValue() {
		return this.getStatus().getValue();
	}

	public boolean isReset() {
		return this.getStatus().isReset();
	}

	public boolean isDecomposedAsNull() {
		LazyValue<T> value = this.getValue();
		return value != null && value.isEmpty();
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public ArbitraryNode<T> copy() {
		List<ArbitraryNode> copyChildren = new ArrayList<>();
		List<ArbitraryNode> children = this.getChildren();
		for (ArbitraryNode child : children) {
			copyChildren.add(child.copy());
		}

		return ArbitraryNode.builder()
			.children(copyChildren)
			.type(this.getType())
			.propertyName(this.getPropertyName())
			.metadata(this.getMetadata())
			.indexOfIterable(this.getIndexOfIterable())
			.status(this.getStatus().copy())
			.keyOfMapStructure(this.isKeyOfMapStructure())
			.nullInject(this.getNullInject())
			.nullable(this.isNullable())
			.build();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ArbitraryNode<?> that = (ArbitraryNode<?>)obj;
		return indexOfIterable == that.indexOfIterable
			&& keyOfMapStructure == that.keyOfMapStructure && Double.compare(that.nullInject, nullInject) == 0
			&& children.equals(that.children) && type.equals(that.type) && propertyName.equals(that.propertyName)
			&& metadata.equals(that.metadata) && status.equals(that.status);
	}

	@Override
	public int hashCode() {
		return Objects.hash(
			children, type, propertyName, metadata, indexOfIterable, status, keyOfMapStructure, nullInject
		);
	}

	private ArbitraryNodeStatus<T> getStatus() {
		return status;
	}

	private void mark() {
		this.setActive(true);
		this.setManipulated(true);
		if (this.isDecomposedAsNull()) {
			this.setReset(true);
			this.setArbitrary(null);
			this.clearValue();
		}
	}

	public static final class ArbitraryNodeBuilder<T> {
		@SuppressWarnings("rawtypes")
		private List<ArbitraryNode> children = new ArrayList<>();
		@SuppressWarnings("unchecked")
		private ArbitraryType<T> type = NullArbitraryType.INSTANCE;
		private String propertyName = HEAD_NAME;
		private String metadata = "";
		private int indexOfIterable = NO_OR_ALL_INDEX_INTEGER_VALUE;
		private ArbitraryNodeStatus<T> status = new ArbitraryNodeStatus<>();
		private boolean keyOfMapStructure = false;
		private double nullInject = 0.3f;

		@SuppressWarnings("rawtypes")
		public ArbitraryNodeBuilder<T> children(List<ArbitraryNode> children) {
			this.children = children;
			return this;
		}

		public ArbitraryNodeBuilder<T> addChild(ArbitraryNode<?> node) {
			children.add(node);
			return this;
		}

		public ArbitraryNodeBuilder<T> type(ArbitraryType<T> type) {
			this.type = type;
			return this;
		}

		/**
		 * Deprecated Use getPropertyName instead.
		 * @param fieldName fieldName
		 * @return String
		 */
		@Deprecated
		public ArbitraryNodeBuilder<T> fieldName(String fieldName) {
			return this.propertyName(fieldName);
		}

		public ArbitraryNodeBuilder<T> propertyName(String propertyName) {
			this.propertyName = propertyName;
			return this;
		}

		public ArbitraryNodeBuilder<T> metadata(String metadata) {
			this.metadata = metadata;
			return this;
		}

		public ArbitraryNodeBuilder<T> status(ArbitraryNodeStatus<T> status) {
			this.status = status;
			return this;
		}

		public ArbitraryNodeBuilder<T> nullable(boolean nullable) {
			status.nullable = nullable;
			return this;
		}

		public ArbitraryNodeBuilder<T> keyOfMapStructure(boolean keyOfMapStructure) {
			this.keyOfMapStructure = keyOfMapStructure;
			return this;
		}

		public ArbitraryNodeBuilder<T> indexOfIterable(int indexOfIterable) {
			this.indexOfIterable = indexOfIterable;
			return this;
		}

		public ArbitraryNodeBuilder<T> nullInject(double nullInject) {
			this.nullInject = nullInject;
			return this;
		}

		@SuppressWarnings({"rawtypes", "unchecked"})
		public ArbitraryNodeBuilder<T> arbitrary(Arbitrary arbitrary) {
			this.status.arbitrary = arbitrary;
			return this;
		}

		public ArbitraryNodeBuilder<T> value(LazyValue<T> value) {
			this.status.value = value;
			return this;
		}

		public ArbitraryNodeBuilder<T> value(Supplier<T> valueSupplier) {
			this.status.value = new LazyValue<>(valueSupplier);
			return this;
		}

		public ArbitraryNodeBuilder<T> value(T value) {
			this.status.value = new LazyValue<>(value);
			return this;
		}

		public ArbitraryNodeBuilder<T> active(boolean active) {
			this.status.active = active;
			return this;
		}

		public ArbitraryNode<T> build() {
			return new ArbitraryNode<>(
				children,
				type,
				propertyName,
				metadata,
				indexOfIterable,
				status,
				keyOfMapStructure,
				nullInject
			);
		}
	}

	private static final class ArbitraryNodeStatus<T> {
		@Nullable
		private Arbitrary<T> arbitrary = null; // immutable
		private ContainerSizeConstraint containerSizeConstraint; // immutable
		private List<PostArbitraryManipulator<T>> postArbitraryManipulators = new ArrayList<>();
		private LazyValue<T> value = null;
		private boolean nullable = false;
		private boolean manipulated = false;
		private boolean active = true; // isNull
		private boolean fixed = false;
		private boolean reset = false;

		private ArbitraryNodeStatus() {
		}

		public ArbitraryNodeStatus(
			@Nullable Arbitrary<T> arbitrary,
			ContainerSizeConstraint containerSizeConstraint,
			List<PostArbitraryManipulator<T>> postArbitraryManipulators,
			LazyValue<T> value,
			boolean nullable,
			boolean manipulated,
			boolean active,
			boolean fixed,
			boolean reset
		) {
			this.arbitrary = arbitrary;
			this.containerSizeConstraint = containerSizeConstraint;
			this.postArbitraryManipulators = postArbitraryManipulators;
			this.value = value;
			this.nullable = nullable;
			this.manipulated = manipulated;
			this.active = active;
			this.fixed = fixed;
			this.reset = reset;
		}

		@Nullable
		public Arbitrary<T> getArbitrary() {
			return arbitrary;
		}

		public ContainerSizeConstraint getContainerSizeConstraint() {
			return containerSizeConstraint;
		}

		public boolean isNullable() {
			return nullable;
		}

		public boolean isManipulated() {
			return manipulated;
		}

		public boolean isActive() {
			return active;
		}

		public List<PostArbitraryManipulator<T>> getPostArbitraryManipulators() {
			return postArbitraryManipulators;
		}

		public boolean isFixed() {
			return fixed;
		}

		public LazyValue<T> getValue() {
			return value;
		}

		public boolean isReset() {
			return reset;
		}

		public void setArbitrary(@Nullable Arbitrary<T> arbitrary) {
			this.arbitrary = arbitrary;
		}

		public void setNullable(boolean nullable) {
			this.nullable = nullable;
		}

		public void setManipulated(boolean manipulated) {
			this.manipulated = manipulated;
		}

		public void setActive(boolean active) {
			this.active = active;
		}

		public void setReset(boolean reset) {
			this.reset = reset;
		}

		public void setContainerSizeConstraint(@Nullable ContainerSizeConstraint containerSizeConstraint) {
			this.containerSizeConstraint = containerSizeConstraint;
		}

		public void addPostArbitraryManipulator(PostArbitraryManipulator<T> postArbitraryManipulator) {
			this.postArbitraryManipulators.add(postArbitraryManipulator);
		}

		public void setPostArbitraryManipulators(List<PostArbitraryManipulator<T>> postArbitraryManipulators) {
			this.postArbitraryManipulators = postArbitraryManipulators;
		}

		public void setFixed(boolean fixed) {
			this.fixed = fixed;
		}

		public void setValue(LazyValue<T> value) {
			this.value = value;
		}

		public ArbitraryNodeStatus<T> copy() {
			return new ArbitraryNodeStatus<>(
				this.getArbitrary(),
				this.getContainerSizeConstraint(),
				this.getPostArbitraryManipulators(),
				this.getValue(),
				this.isNullable(),
				this.isManipulated(),
				this.isActive(),
				this.isFixed(),
				this.isReset()
			);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			ArbitraryNodeStatus<?> that = (ArbitraryNodeStatus<?>)obj;
			return nullable == that.nullable && manipulated == that.manipulated && active == that.active;
		}

		@Override
		public int hashCode() {
			return Objects.hash(nullable, manipulated, active);
		}
	}
}
