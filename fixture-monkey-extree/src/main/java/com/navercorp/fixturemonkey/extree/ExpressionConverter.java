package com.navercorp.fixturemonkey.extree;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.danekja.java.util.function.serializable.SerializableFunction;

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

public class ExpressionConverter {
	private ExpressionConverter() {
	}

	private static final ArbitraryExpressionVisitor ARBITRARY_EXPRESSION_VISITOR = new ArbitraryExpressionVisitor();

	public static <T> String to(SerializableFunction<T, ?> function) {
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
			String returnedString = "";

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

				Expression instanceExpression = member.getInstance();
				if (instanceExpression.getExpressionType() == ExpressionType.Invoke
					|| instanceExpression.getExpressionType() == ExpressionType.Convert) {
					returnedString = instanceExpression.accept(this);
					delimiter = ".";
				}

				if (methodName.equals("get")) {
					int index = Integer.parseInt(argumentString);
					return returnedString + "[" + index + "]";
				} else if (methodName.startsWith("get")) {
					return returnedString + delimiter + getterToFieldName(methodName);
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

		private String getterToFieldName(String getter) {
			String name = getter.substring(getter.indexOf("get") + 3);
			return Character.toLowerCase(name.charAt(0)) + name.substring(1);
		}
	}
}
