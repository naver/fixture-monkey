package com.navercorp.fixturemonkey.arbitrary;

import static com.navercorp.fixturemonkey.Constants.NO_OR_ALL_INDEX_INTEGER_VALUE;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.generator.ArbitraryGenerator;

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

	public <U> void initializeElementSize() {
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

	@SuppressWarnings("rawtypes")
	public void apply(PreArbitraryManipulator<T> preArbitraryManipulator) {
		if (preArbitraryManipulator instanceof AbstractArbitrarySet) {
			Arbitrary<T> appliedArbitrary = preArbitraryManipulator.apply(status.arbitrary);
			this.setFixed(true);
			this.setArbitrary(appliedArbitrary);
		} else if (preArbitraryManipulator instanceof ArbitrarySetNullity) {
			this.setActive(!((ArbitrarySetNullity)preArbitraryManipulator).toNull());
		}
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

	<U> void update(ArbitraryNode<U> entryNode, ArbitraryGenerator generator) {
		if (!entryNode.isLeafNode() && !entryNode.isFixed()) {
			for (ArbitraryNode<?> nextChild : entryNode.getChildren()) {
				update(nextChild, generator);
			}

			entryNode.setArbitrary(
				generator.generate(entryNode.type, entryNode.children)
			);

		}

		entryNode.getPostArbitraryManipulators().forEach(
			operation -> entryNode.setArbitrary(operation.apply(entryNode.getArbitrary()))
		);

		if (entryNode.isNullable() && !entryNode.isManipulated()) {
			entryNode.setArbitrary(entryNode.getArbitrary().injectNull(entryNode.getNullInject()));
		}
	}

	private boolean matchExpression(Cursor cursor) {
		boolean sameName = cursor.getName().equals(this.getFieldName());
		boolean sameIndex = cursor.indexEquals(indexOfIterable);
		return sameName && sameIndex;
	}

	private boolean isLeafNode() {
		return this.getChildren().isEmpty() && this.getArbitrary() != null;
	}

	private FixtureNodeStatus<T> getStatus() {
		return status;
	}

	private static final class FixtureNodeStatus<T> {
		@Nullable
		private Arbitrary<T> arbitrary = null; // immutable
		@Nullable
		private ContainerSizeConstraint containerSizeConstraint = null; // immutable
		private List<PostArbitraryManipulator<T>> postArbitraryManipulators = new ArrayList<>();
		private boolean nullable = false;
		private boolean manipulated = false;
		private boolean active = true;
		private boolean fixed = false;

		public FixtureNodeStatus() {
		}

		private FixtureNodeStatus(
			@Nullable Arbitrary<T> arbitrary,
			@Nullable ContainerSizeConstraint containerSizeConstraint,
			boolean nullable,
			boolean manipulated,
			boolean active
		) {
			this.arbitrary = arbitrary;
			this.containerSizeConstraint = containerSizeConstraint;
			this.nullable = nullable;
			this.manipulated = manipulated;
			this.active = active;
		}

		@Nullable
		public Arbitrary<T> getArbitrary() {
			return arbitrary;
		}

		@Nullable
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

		public FixtureNodeStatus<T> copy() {
			return new FixtureNodeStatus<>(
				this.getArbitrary(),
				this.getContainerSizeConstraint(),
				this.isNullable(),
				this.isManipulated(),
				this.isActive()
			);
		}
	}

	public static final class FixtureNodeBuilder<T> {
		@SuppressWarnings("rawtypes")
		private List<ArbitraryNode> children = new ArrayList<>();
		@SuppressWarnings({"rawtypes", "unchecked"})
		private ArbitraryType<T> type = new ArbitraryType(Object.class);
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
