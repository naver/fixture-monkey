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

package com.navercorp.fixturemonkey.extree;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.danekja.java.util.function.serializable.SerializableFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.streamx.fluent.extree.expression.BinaryExpression;
import co.streamx.fluent.extree.expression.BlockExpression;
import co.streamx.fluent.extree.expression.ConstantExpression;
import co.streamx.fluent.extree.expression.DelegateExpression;
import co.streamx.fluent.extree.expression.Expression;
import co.streamx.fluent.extree.expression.ExpressionType;
import co.streamx.fluent.extree.expression.ExpressionVisitor;
import co.streamx.fluent.extree.expression.InvocableExpression;
import co.streamx.fluent.extree.expression.InvocationExpression;
import co.streamx.fluent.extree.expression.LambdaExpression;
import co.streamx.fluent.extree.expression.MemberExpression;
import co.streamx.fluent.extree.expression.NewArrayInitExpression;
import co.streamx.fluent.extree.expression.ParameterExpression;
import co.streamx.fluent.extree.expression.UnaryExpression;

public final class Ex {
	private static final ArbitraryExpressionVisitor ARBITRARY_EXPRESSION_VISITOR = new ArbitraryExpressionVisitor();
	private static final Logger LOG = LoggerFactory.getLogger(Ex.class);

	private Ex() {
	}

	static <T> String to(SerializableFunction<T, ?> function) {
		LambdaExpression<SerializableFunction<T, ?>> parsed = LambdaExpression.parse(function);
		Expression body = parsed.getBody();
		return body.accept(ARBITRARY_EXPRESSION_VISITOR);
	}

	private static class ArbitraryExpressionVisitor implements ExpressionVisitor<String> {
		@Override
		public String visit(BinaryExpression expression) {
			String prefix = expression.getFirst().accept(this);
			int index = Integer.parseInt(expression.getSecond().accept(this));
			return prefix + "[" + index + "]";
		}

		@Override
		public String visit(ConstantExpression expression) {
			return String.valueOf(expression.getValue());
		}

		@Override
		public String visit(InvocationExpression expression) {
			InvocableExpression target = expression.getTarget();
			List<Expression> arguments = expression.getArguments();

			String argumentString = "";
			String delimiter = "";
			String concatedString = "";

			if (arguments.size() == 1) {
				Expression argument = arguments.get(0);
				if (argument.getExpressionType() != ExpressionType.Parameter) {
					argumentString = argument.accept(this);
					delimiter = ".";
				}
			}

			if (target.getExpressionType() == ExpressionType.Lambda) {
				return argumentString + delimiter + target.accept(this);
			} else if (target.getExpressionType() == ExpressionType.MethodAccess) {
				MemberExpression member = (MemberExpression)target;
				Method method = (Method)member.getMember();
				String methodName = method.getName();
				Class<?> instanceType = member.getInstance().getResultType();
				Map<Method, String> fieldNamesByGetter = getFieldNamesByGetter(instanceType);

				Expression instanceExpression = member.getInstance();
				if (instanceExpression.getExpressionType() == ExpressionType.Invoke
					|| instanceExpression.getExpressionType() == ExpressionType.Convert) {
					concatedString = instanceExpression.accept(this);
					delimiter = ".";
				}

				if (Collection.class.isAssignableFrom(instanceType) && methodName.equals("get")) {
					int index = Integer.parseInt(argumentString);
					return concatedString + "[" + index + "]";
				} else {
					String fieldName = fieldNamesByGetter.get(method);
					if (fieldName == null) {
						throw new IllegalArgumentException("Given method is not getter. " + methodName);
					}
					return concatedString + delimiter + fieldName;
				}
			}

			return "";
		}

		@Override
		public String visit(LambdaExpression<?> expression) {
			Expression body = expression.getBody();
			if (body.getExpressionType() == ExpressionType.FieldAccess) {
				MemberExpression memberExpression = ((MemberExpression)body);

				Field field = (Field)(memberExpression).getMember();
				return field.getName();
			} else {
				return null;
			}
		}

		@Override
		public String visit(DelegateExpression expression) {
			return null;
		}

		@Override
		public String visit(MemberExpression expression) {
			if (expression.getExpressionType() == ExpressionType.FieldAccess) {
				Expression instanceExpression = expression.getInstance();
				String concatedString = "";
				String delimiter = "";
				if (instanceExpression.getExpressionType() == ExpressionType.Invoke
					|| instanceExpression.getExpressionType() == ExpressionType.Convert) {
					concatedString = instanceExpression.accept(this);
					delimiter = ".";
				}
				Field field = (Field)expression.getMember();
				return concatedString + delimiter + field.getName();
			}
			return null;
		}

		@Override
		public String visit(ParameterExpression expression) {
			return null;
		}

		@Override
		public String visit(UnaryExpression expression) {
			return expression.getFirst().accept(this);
		}

		@Override
		public String visit(BlockExpression expression) {
			return null;
		}

		@Override
		public String visit(NewArrayInitExpression expression) {
			return null;
		}

		private Map<Method, String> getFieldNamesByGetter(Class<?> clazz) {
			Map<Method, String> result = new HashMap<>();
			try {
				PropertyDescriptor[] descriptors = Introspector.getBeanInfo(clazz)
					.getPropertyDescriptors();
				for (PropertyDescriptor descriptor : descriptors) {
					Method readMethod = descriptor.getReadMethod(); // can not be null
					result.put(readMethod, descriptor.getName());
				}
			} catch (IntrospectionException e) {
				LOG.warn("Introspect bean property is failed. type: " + clazz, e);
			}

			return result;
		}
	}
}
