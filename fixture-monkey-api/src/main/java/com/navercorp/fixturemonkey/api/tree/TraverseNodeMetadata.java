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

package com.navercorp.fixturemonkey.api.tree;

import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyPath;
import com.navercorp.fixturemonkey.api.property.TreeRootProperty;
import com.navercorp.fixturemonkey.api.property.TypeDefinition;

@API(since = "1.1.4", status = Status.EXPERIMENTAL)
public interface TraverseNodeMetadata {
	TreeRootProperty getRootProperty();

	@Nullable
	Property getResolvedParentProperty();

	TypeDefinition getResolvedTypeDefinition();

	void setResolvedTypeDefinition(TypeDefinition typeDefinition);

	Property getOriginalProperty();

	TreeProperty getTreeProperty();

	LazyArbitrary<PropertyPath> getLazyPropertyPath();

	boolean manipulated();

	List<TreeNodeManipulator> getTreeNodeManipulators();

	@Nullable
	TreeNodeManipulator getAppliedTreeNodeManipulator();

	void addTreeNodeManipulator(TreeNodeManipulator treeNodeManipulator);

	double getNullInject();

	void setNullInject(double nullInject);
}
