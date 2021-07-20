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

import static com.navercorp.fixturemonkey.Constants.NO_OR_ALL_INDEX_INTEGER_VALUE;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.TypeSupports;

public final class ArbitraryNode<T> {
	private static final String HEAD_NAME = "HEAD";

	@SuppressWarnings("rawtypes")
	private final List<ArbitraryNode> children;
	private final ArbitraryType<T> type;
	private final String fieldName;
	private final String metadata;
	private final int indexOfIterable;
	private final FixtureNodeStatus<T> status;
	private final boolean keyOfMapStructure;
	private final double nullInject;
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@SuppressWarnings("rawtypes")
	public ArbitraryNode(
		List<ArbitraryNode> children,
		ArbitraryType<T> type,
		String fieldName,
		String metadata,
		int indexOfIterable,
		FixtureNodeStatus<T> status,
		boolean keyOfMapStructure,
		double nullInject
	) {
		this.children = new ArrayList<>();
		children.forEach(this::addChildNode);
		this.type = type;
		this.fieldName = fieldName;
		this.metadata = metadata;
		this.indexOfIterable = indexOfIterable;
		this.status = status.copy();
		this.keyOfMapStructure = keyOfMapStructure;
		this.nullInject = nullInject;
	}

	public static <T> FixtureNodeBuilder<T> builder() {
		return new FixtureNodeBuilder<>();
	}

	public void addChildNode(ArbitraryNode<?> child) {
		children.add(child);
	}

	public boolean isEmpty() {
		return children.isEmpty();
	}

	@SuppressWarnings("rawtypes")
	public List<ArbitraryNode> findChildrenByCursor(Cursor cursor) {
		List<ArbitraryNode> foundChildren = new ArrayList<>();
		for (ArbitraryNode child : children) {
			if (child.isKeyOfMapStructure()) {
				continue;
			}
			if (child.matchExpression(cursor)) {
				child.setActive(true);
				child.setManipulated(true);
				foundChildren.add(child);
			}
		}
		return foundChildren;
	}

	@SuppressWarnings("rawtypes")
	public Optional<ArbitraryNode> findChild(Cursor cursor) {
		for (ArbitraryNode child : children) {
			if (child.isKeyOfMapStructure()) {
				continue;
			}

			if (child.matchExpression(cursor)) {
				return Optional.of(child);
			}
		}
		return Optional.empty();
	}

	public void initializeElementSize() {
		if (!type.isContainer()) {
			throw new IllegalStateException("Can not initialize element size because node is not container.");
		}

		if (type.isOptional()) {
			setContainerSizeConstraint(new ContainerSizeConstraint(0, 1));
			return;
		}

		if (getContainerSizeConstraint() != null) {
			return;
		}

		Integer min = null;
		Integer max = null;

		Size size = type.getAnnotation(Size.class);
		if (size != null) {
			min = size.min();
			max = size.max();
		}

		NotEmpty notEmpty = type.getAnnotation(NotEmpty.class);
		if (notEmpty != null) {
			if (min != null) {
				min = Math.max(1, min);
			} else {
				min = 1;
			}
		}

		setContainerSizeConstraint(new ContainerSizeConstraint(min, max));
	}

	public void apply(PreArbitraryManipulator<T> preArbitraryManipulator) {
		if (preArbitraryManipulator instanceof AbstractArbitrarySet) {
			Object toValue = ((AbstractArbitrarySet<T>)preArbitraryManipulator).getValue();
			Class<?> clazz = this.getType().getType();
			Class<?> toValueClazz = toValue.getClass();
			if (
				!TypeSupports.isCompatibleType(clazz, toValueClazz)
					&& !clazz.isAssignableFrom(toValueClazz)
					&& !Arbitrary.class.isAssignableFrom(toValueClazz)
			) {
				log.warn("field \"{}\" type is \"{}\", but given set value is \"{}\".",
					fieldName,
					clazz.getSimpleName(),
					toValueClazz.getSimpleName()
				);
			}
			Arbitrary<T> appliedArbitrary = preArbitraryManipulator.apply(status.arbitrary);
			this.setFixed(true);
			this.setArbitrary(appliedArbitrary);
		}
	}

	public void apply(ArbitraryNullity manipulator) {
		this.setActive(!manipulator.toNull());
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

	public void setContainerMinSize(@Nullable Integer minSize) {
		FixtureNodeStatus<T> status = this.getStatus();
		if (status.getContainerSizeConstraint() == null) {
			status.setContainerSizeConstraint(new ContainerSizeConstraint(minSize, null));
			return;
		}
		status.setContainerSizeConstraint(status.getContainerSizeConstraint().withMinSize(minSize));
	}

	public void setContainerMaxSize(@Nullable Integer maxSize) {
		FixtureNodeStatus<T> status = this.getStatus();
		if (status.getContainerSizeConstraint() == null) {
			status.setContainerSizeConstraint(new ContainerSizeConstraint(null, maxSize));
			return;
		}
		status.setContainerSizeConstraint(status.getContainerSizeConstraint().withMaxSize(maxSize));
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

	@SuppressWarnings({"rawtypes", "unchecked"})
	public void addArbitraryOperation(PostArbitraryManipulator postArbitraryManipulator) {
		this.status.addPostArbitraryManipulator(postArbitraryManipulator);
	}

	public Arbitrary<T> getArbitrary() {
		FixtureNodeStatus<T> status = getStatus();
		if (!status.isActive() && status.isManipulated()) {
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

	public String getFieldName() {
		return fieldName;
	}

	public String getMetadata() {
		return metadata;
	}

	public double getNullInject() {
		return nullInject;
	}

	public String getExpression() {
		return fieldName + metadata;
	}

	public boolean isKeyOfMapStructure() {
		return keyOfMapStructure;
	}

	public ContainerSizeConstraint getContainerSizeConstraint() {
		return this.status.containerSizeConstraint;
	}

	public List<PostArbitraryManipulator<T>> getPostArbitraryManipulators() {
		return this.status.postArbitraryManipulators;
	}

	public boolean isFixed() {
		return getStatus().isFixed();
	}

	public boolean isLeafNode() {
		return this.getChildren().isEmpty() && this.getArbitrary() != null;
	}

	public LazyValue<T> getValue() {
		return this.getStatus().getValue();
	}

	@SuppressWarnings("rawtypes")
	public ArbitraryNode<T> copy() {
		List<ArbitraryNode> copyChildren = new ArrayList<>();
		List<ArbitraryNode> children = this.getChildren();
		for (ArbitraryNode child : children) {
			copyChildren.add(child.copy());
		}

		return ArbitraryNode.<T>builder()
			.children(copyChildren)
			.type(this.getType())
			.fieldName(this.getFieldName())
			.metadata(this.getMetadata())
			.indexOfIterable(this.getIndexOfIterable())
			.status(this.getStatus().copy())
			.keyOfMapStructure(this.isKeyOfMapStructure())
			.nullInject(this.getNullInject())
			.nullable(this.isNullable())
			.build();
	}

	private boolean matchExpression(Cursor cursor) {
		boolean sameName = cursor.nameEquals(this.getFieldName());
		boolean sameIndex = cursor.indexEquals(indexOfIterable);
		return sameName && sameIndex;
	}

	private FixtureNodeStatus<T> getStatus() {
		return status;
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
			&& children.equals(that.children) && type.equals(that.type) && fieldName.equals(that.fieldName)
			&& metadata.equals(that.metadata) && status.equals(that.status);
	}

	@Override
	public int hashCode() {
		return Objects.hash(
			children, type, fieldName, metadata, indexOfIterable, status, keyOfMapStructure, nullInject
		);
	}

	private static final class FixtureNodeStatus<T> {
		@Nullable
		private Arbitrary<T> arbitrary = null; // immutable
		private ContainerSizeConstraint containerSizeConstraint; // immutable
		private List<PostArbitraryManipulator<T>> postArbitraryManipulators = new ArrayList<>();
		private LazyValue<T> value = null;
		private boolean nullable = false;
		private boolean manipulated = false;
		private boolean active = true;
		private boolean fixed = false;

		private FixtureNodeStatus() {
		}

		public FixtureNodeStatus(
			@Nullable Arbitrary<T> arbitrary,
			ContainerSizeConstraint containerSizeConstraint,
			List<PostArbitraryManipulator<T>> postArbitraryManipulators,
			LazyValue<T> value,
			boolean nullable,
			boolean manipulated,
			boolean active,
			boolean fixed
		) {
			this.arbitrary = arbitrary;
			this.containerSizeConstraint = containerSizeConstraint;
			this.postArbitraryManipulators = postArbitraryManipulators;
			this.value = value;
			this.nullable = nullable;
			this.manipulated = manipulated;
			this.active = active;
			this.fixed = fixed;
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

		public FixtureNodeStatus<T> copy() {
			return new FixtureNodeStatus<>(
				this.getArbitrary(),
				this.getContainerSizeConstraint(),
				this.getPostArbitraryManipulators(),
				this.getValue(),
				this.isNullable(),
				this.isManipulated(),
				this.isActive(),
				this.isFixed()
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
			FixtureNodeStatus<?> that = (FixtureNodeStatus<?>)obj;
			return nullable == that.nullable && manipulated == that.manipulated && active == that.active;
		}

		@Override
		public int hashCode() {
			return Objects.hash(nullable, manipulated, active);
		}
	}

	public static final class FixtureNodeBuilder<T> {
		@SuppressWarnings("rawtypes")
		private List<ArbitraryNode> children = new ArrayList<>();
		@SuppressWarnings("unchecked")
		private ArbitraryType<T> type = NullArbitraryType.INSTANCE;
		private String fieldName = HEAD_NAME;
		private String metadata = "";
		private int indexOfIterable = NO_OR_ALL_INDEX_INTEGER_VALUE;
		private FixtureNodeStatus<T> status = new FixtureNodeStatus<>();
		private boolean keyOfMapStructure = false;
		private double nullInject = 0.3f;

		@SuppressWarnings("rawtypes")
		public FixtureNodeBuilder<T> children(List<ArbitraryNode> children) {
			this.children = children;
			return this;
		}

		public FixtureNodeBuilder<T> addChild(ArbitraryNode<?> node) {
			children.add(node);
			return this;
		}

		public FixtureNodeBuilder<T> type(ArbitraryType<T> type) {
			this.type = type;
			return this;
		}

		public FixtureNodeBuilder<T> fieldName(String fieldName) {
			this.fieldName = fieldName;
			return this;
		}

		public FixtureNodeBuilder<T> metadata(String metadata) {
			this.metadata = metadata;
			return this;
		}

		public FixtureNodeBuilder<T> status(FixtureNodeStatus<T> status) {
			this.status = status;
			return this;
		}

		public FixtureNodeBuilder<T> nullable(boolean nullable) {
			status.nullable = nullable;
			return this;
		}

		public FixtureNodeBuilder<T> keyOfMapStructure(boolean keyOfMapStructure) {
			this.keyOfMapStructure = keyOfMapStructure;
			return this;
		}

		public FixtureNodeBuilder<T> indexOfIterable(int indexOfIterable) {
			this.indexOfIterable = indexOfIterable;
			return this;
		}

		public FixtureNodeBuilder<T> nullInject(double nullInject) {
			this.nullInject = nullInject;
			return this;
		}

		@SuppressWarnings({"rawtypes", "unchecked"})
		public FixtureNodeBuilder<T> arbitrary(Arbitrary arbitrary) {
			this.status.arbitrary = arbitrary;
			return this;
		}

		public FixtureNodeBuilder<T> value(LazyValue<T> value) {
			this.status.value = value;
			return this;
		}

		public FixtureNodeBuilder<T> value(Supplier<T> valueSupplier) {
			this.status.value = new LazyValue<>(valueSupplier);
			return this;
		}

		public FixtureNodeBuilder<T> value(T value) {
			this.status.value = new LazyValue<>(value);
			return this;
		}

		public ArbitraryNode<T> build() {
			return new ArbitraryNode<>(
				children,
				type,
				fieldName,
				metadata,
				indexOfIterable,
				status,
				keyOfMapStructure,
				nullInject
			);
		}
	}
}
