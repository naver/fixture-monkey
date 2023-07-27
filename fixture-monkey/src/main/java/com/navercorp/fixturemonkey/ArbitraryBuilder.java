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

package com.navercorp.fixturemonkey;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators.F3;
import net.jqwik.api.Combinators.F4;

import com.navercorp.fixturemonkey.api.expression.ExpressionGenerator;
import com.navercorp.fixturemonkey.api.validator.ArbitraryValidator;
import com.navercorp.fixturemonkey.customizer.InnerSpec;

/**
 * A builder instance for generating an instance of given type.
 * A complex class could be built by given methods.
 *
 * @param <T> type to generate
 */
@API(since = "0.4.0", status = Status.MAINTAINED)
public interface ArbitraryBuilder<T> {
	/**
	 * Set one or more properties referenced by expression to {@code value}.
	 *
	 * @param expression expression similar to Java syntax, including {@code .}, {@code []}
	 *                   common expression including {@code *} referencing properties.
	 * @param value      various type of value including {@link Supplier}, {@link Arbitrary},
	 *                   {@link ArbitraryBuilder}, {@code NOT_NULL}, {@code null}.
	 * @return an {@link ArbitraryBuilder} whose properties referenced by an {@code expression} are set as
	 * {@code value}.
	 */
	ArbitraryBuilder<T> set(String expression, @Nullable Object value);

	/**
	 * Set the number of {@code limit} properties referenced by expression to {@code value}.
	 *
	 * @param expression expression similar to Java syntax, including {@code .}, {@code []}
	 *                   common expression including {@code *} referencing properties.
	 * @param value      various type of value including {@link Supplier}, {@link Arbitrary},
	 *                   {@link ArbitraryBuilder}, {@code NOT_NULL}, {@code null}.
	 * @param limit      the count of affected properties referenced by {@code expression}
	 * @return an {@link ArbitraryBuilder} whose properties referenced by an {@code expression} are
	 * set as {@code value}.
	 * @see ArbitraryBuilder#set(String, Object)
	 */
	ArbitraryBuilder<T> set(String expression, @Nullable Object value, int limit);

	/**
	 * Set one or more properties referenced by expression to {@code value}.
	 *
	 * @param expressionGenerator it generates expression dynamically.
	 * @param value               various type of value including {@link Supplier}, {@link Arbitrary},
	 *                            {@link ArbitraryBuilder}, {@code NOT_NULL}, {@code null}.
	 * @return an {@link ArbitraryBuilder} whose properties referenced by an {@code expressionGenerator}
	 * are set as {@code value}.
	 * @see ArbitraryBuilder#set(String, Object)
	 */
	ArbitraryBuilder<T> set(ExpressionGenerator expressionGenerator, @Nullable Object value);

	/**
	 * Set one or more properties referenced by expression to {@code value}.
	 *
	 * @param expressionGenerator it generates expression dynamically.
	 * @param value               various type of value including {@link Supplier}, {@link Arbitrary},
	 *                            {@link ArbitraryBuilder}, {@code NOT_NULL}, {@code null}.
	 * @param limit               the count of affected properties referenced by {@code expression}
	 * @return an {@link ArbitraryBuilder} whose properties referenced by an {@code expressionGenerator} are set as
	 * {@code value}.
	 * @see ArbitraryBuilder#set(String, Object, int)
	 */
	ArbitraryBuilder<T> set(ExpressionGenerator expressionGenerator, @Nullable Object value, int limit);

	/**
	 * Set the {@link ArbitraryBuilder} sampling given {@code value}.
	 * It is same as {@code set("$", value)}
	 *
	 * @param value various type of value including {@link Supplier}, {@link Arbitrary},
	 *              {@link ArbitraryBuilder}, {@code NOT_NULL}, {@code null}.
	 * @return ArbitraryBuilder is set as {@code value}.
	 * @see ArbitraryBuilder#set(String, Object)
	 */
	ArbitraryBuilder<T> set(@Nullable Object value);

	/**
	 * Apply one or more manipulations defined in {@link InnerSpec}.
	 *
	 * @param innerSpec a type-independent specification of manipulators
	 * @return an {@link ArbitraryBuilder} applied the manipulators in {@link InnerSpec}
	 */
	ArbitraryBuilder<T> setInner(InnerSpec innerSpec);

	/**
	 * Set one or more properties referenced by expression to a result of {@link Supplier}.
	 * The {@link Supplier} gets the result when manipulation is executed.
	 * It might be used when to set the latest value or set unique id generated sequentially.
	 *
	 * @param expression expression similar to Java syntax, including {@code .}, {@code []}
	 *                   common expression including {@code *} referencing properties.
	 * @param supplier   a supplier of result. It might be a value or method.
	 * @return an {@link ArbitraryBuilder} whose properties referenced by an {@code expressionGenerator} are set as
	 * {@code value}.
	 * @see ArbitraryBuilder#set(String, Object, int)
	 */
	ArbitraryBuilder<T> setLazy(String expression, Supplier<?> supplier);

	/**
	 * Set the number of {@code limit} properties referenced by expression to a result of {@link Supplier}.
	 * The {@link Supplier} gets the result when manipulation is executed.
	 * It might be used when to set the latest value or set unique id generated sequentially.
	 *
	 * @param expression expression similar to Java syntax, including {@code .}, {@code []}
	 *                   common expression including {@code *} referencing properties.
	 * @param supplier   a supplier of result. It might be a value or method.
	 * @param limit      the count of affected properties referenced by {@code expression}
	 * @return an {@link ArbitraryBuilder} whose properties referenced by an {@code expression} are set as
	 * {@code value}.
	 * @see ArbitraryBuilder#set(String, Object, int)
	 */
	ArbitraryBuilder<T> setLazy(String expression, Supplier<?> supplier, int limit);

	/**
	 * Set one or more properties referenced by expression to a result of {@link Supplier}.
	 * The {@link Supplier} gets the result when manipulation is executed.
	 * It might be used when to set the latest value or set unique id generated sequentially.
	 *
	 * @param expressionGenerator it generates expression dynamically.
	 * @param supplier            a supplier of result. It might be a value or method.
	 * @return an {@link ArbitraryBuilder} whose properties referenced by an {@code expressionGenerator} are set as
	 * {@code value}.
	 * @see ArbitraryBuilder#set(String, Object, int)
	 */
	ArbitraryBuilder<T> setLazy(ExpressionGenerator expressionGenerator, Supplier<?> supplier);

	/**
	 * Set the number of {@code limit} properties referenced by expression to a result of {@link Supplier}.
	 * The {@link Supplier} gets the result when manipulation is executed.
	 * It might be used when to set the latest value or set unique id generated sequentially.
	 *
	 * @param expressionGenerator it generates expression dynamically.
	 * @param supplier            a supplier of result. It might be a value or method.
	 * @param limit               the count of affected properties referenced by {@code expression}
	 * @return an {@link ArbitraryBuilder} whose properties referenced by an {@code expressionGenerator} are set as
	 * {@code value}.
	 * @see ArbitraryBuilder#set(String, Object, int)
	 */
	ArbitraryBuilder<T> setLazy(ExpressionGenerator expressionGenerator, Supplier<?> supplier, int limit);

	/**
	 * Set one or more properties referenced by expression to null.
	 * If a manipulation you've already declared contains an {@code expression}, this manipulation would be omitted.
	 * It is same as {@code set(expression, null)}.
	 *
	 * @param expression expression similar to Java syntax, including {@code .}, {@code []}
	 *                   common expression including {@code *} referencing properties.
	 * @return an {@link ArbitraryBuilder} whose properties referenced by an {@code expression} are set as null.
	 */
	ArbitraryBuilder<T> setNull(String expression);

	/**
	 * Set one or more properties referenced by expression to null.
	 * If a manipulation you've already declared contains an {@code expression}, this manipulation would be omitted.
	 * It is same as {@code set(expression, null)}.
	 *
	 * @param expressionGenerator it generates expression dynamically.
	 * @return an {@link ArbitraryBuilder} whose properties referenced by an {@code expressionGenerator}
	 * are set as null.
	 */
	ArbitraryBuilder<T> setNull(ExpressionGenerator expressionGenerator);

	/**
	 * Set one or more properties referenced by expression to not null.
	 * If the properties are null, they are generated randomly for their type.
	 * It is same as {@code set(expression, NOT_NULL)}.
	 *
	 * @param expression expression similar to Java syntax, including {@code .}, {@code []}
	 *                   common expression including {@code *} referencing properties.
	 * @return an {@link ArbitraryBuilder} whose properties referenced by an {@code expression} are set as not null.
	 */
	ArbitraryBuilder<T> setNotNull(String expression);

	/**
	 * Set one or more properties referenced by expression to not null.
	 * If the properties are null, they are generated randomly for their type.
	 * It is same as {@code set(expression, NOT_NULL)}.
	 *
	 * @param expressionGenerator it generates expression dynamically.
	 * @return an {@link ArbitraryBuilder} whose properties referenced by an {@code expressionGenerator} are set as
	 * not null.
	 */
	ArbitraryBuilder<T> setNotNull(ExpressionGenerator expressionGenerator);

	/**
	 * Set one or more properties referenced by expression post-condition.
	 *
	 * @param <U>        the type of property.
	 * @param expression expression similar to Java syntax, including {@code .}, {@code []}
	 *                   common expression including {@code *} referencing properties.
	 * @param type       the type of property.
	 * @param predicate  determines the post-condition of properties.
	 * @return an {@link ArbitraryBuilder} whose properties referenced by an {@code expression}
	 * satisfy the post-condition.
	 */
	<U> ArbitraryBuilder<T> setPostCondition(String expression, Class<U> type, Predicate<U> predicate);

	/**
	 * Set one or more properties referenced by expression post-condition.
	 *
	 * @param <U>                 the type of property.
	 * @param expressionGenerator it generates expression dynamically.
	 * @param type                the type of property.
	 * @param predicate           determines the post-condition of properties.
	 * @return an {@link ArbitraryBuilder} whose properties referenced by an {@code expressionGenerator}
	 * satisfy the post-condition.
	 */
	<U> ArbitraryBuilder<T> setPostCondition(
		ExpressionGenerator expressionGenerator,
		Class<U> type,
		Predicate<U> predicate
	);

	/**
	 * Set the number of {@code limit} properties referenced by expression post-condition.
	 *
	 * @param <U>        the type of property.
	 * @param expression expression similar to Java syntax, including {@code .}, {@code []}
	 *                   common expression including {@code *} referencing properties.
	 * @param type       the type of property.
	 * @param predicate  determines the post-condition of properties.
	 * @param limit      the count of affected properties referenced by {@code expression}
	 * @return an {@link ArbitraryBuilder} whose properties referenced by an {@code expression} satisfy
	 * the post-condition.
	 */
	<U> ArbitraryBuilder<T> setPostCondition(
		String expression,
		Class<U> type,
		Predicate<U> predicate,
		int limit
	);

	/**
	 * Set the number of {@code limit} properties referenced by expression post-condition.
	 *
	 * @param <U>                 the type of property.
	 * @param expressionGenerator it generates expression dynamically.
	 * @param type                the type of property.
	 * @param predicate           determines the post-condition of properties.
	 * @param limit               the count of affected properties referenced by {@code expressionGenerator}
	 * @return an {@link ArbitraryBuilder} whose properties referenced by an {@code expressionGenerator}
	 * satisfy the post-condition.
	 */
	<U> ArbitraryBuilder<T> setPostCondition(
		ExpressionGenerator expressionGenerator,
		Class<U> type,
		Predicate<U> predicate,
		int limit
	);

	/**
	 * Set the {@link ArbitraryBuilder} post-condition.
	 * It is same as {@code setPostCondition("$", Class<T>, Predicate<T>)}
	 *
	 * @param predicate determines the post-condition of the {@link ArbitraryBuilder}.
	 * @return an {@link ArbitraryBuilder} whose properties referenced by an {@code expressionGenerator}
	 * satisfy the post-condition.
	 * @see ArbitraryBuilder#setPostCondition(String, Class, Predicate)
	 */
	ArbitraryBuilder<T> setPostCondition(Predicate<T> predicate);

	/**
	 * Set the {@code size} of one or more container properties referenced by expression.
	 *
	 * @param expression expression similar to Java syntax, including {@code .}, {@code []}
	 *                   common expression including {@code *} referencing properties.
	 * @param size       of the container to generate
	 * @return {@link ArbitraryBuilder} whose size of container properties referenced by an {@code expression} is
	 * {@code size}
	 */
	ArbitraryBuilder<T> size(String expression, int size);

	/**
	 * Set the {@code size} of one or more container properties referenced by expression.
	 *
	 * @param expressionGenerator it generates expression dynamically.
	 * @param size                of the container to generate
	 * @return {@link ArbitraryBuilder} whose size of container properties referenced by an {@code expression} is
	 * {@code size}
	 */
	ArbitraryBuilder<T> size(ExpressionGenerator expressionGenerator, int size);

	/**
	 * Set the size of one or more container properties referenced by expression.
	 * {@code minSize} should be less than or equal to {@code maxSize}.
	 *
	 * @param expression expression similar to Java syntax, including {@code .}, {@code []}
	 *                   common expression including {@code *} referencing properties.
	 * @param minSize    of the container to generate
	 * @param maxSize    of the container to generate
	 * @return an {@link ArbitraryBuilder} whose size of container properties referenced by an {@code expression} is
	 * between {@code minSize} and {@code maxSize}.
	 */
	ArbitraryBuilder<T> size(String expression, int minSize, int maxSize);

	/**
	 * Set the size of one or more container properties referenced by expression.
	 * {@code minSize} should be less than or equal to {@code maxSize}.
	 *
	 * @param expressionGenerator it generates expression dynamically.
	 * @param minSize             of the container to generate
	 * @param maxSize             of the container to generate
	 * @return an {@link ArbitraryBuilder} whose size of container properties referenced by an {@code expression} is
	 * between {@code minSize} and {@code maxSize}.
	 */
	ArbitraryBuilder<T> size(ExpressionGenerator expressionGenerator, int minSize, int maxSize);

	/**
	 * Set the size of one or more container properties referenced by expression.
	 * The size of container properties would be between {@code minSize} and
	 * {@code minSize} + {@link Constants#DEFAULT_ELEMENT_MAX_SIZE}
	 * <p>
	 * It would conflict with {@link ArbitraryBuilder#maxSize(String, int)}.
	 * The last executed manipulation would set the size of properties.
	 * <p>
	 * It is same as {@code size(minSize, minSize + DEFAULT_ELEMENT_MAX_SIZE)}.
	 *
	 * @param expression expression similar to Java syntax, including {@code .}, {@code []}
	 *                   common expression including {@code *} referencing properties.
	 * @param minSize    of the container to generate
	 * @return an {@link ArbitraryBuilder} whose size of container properties referenced by an {@code expression}
	 * is between {@code minSize} and {@code minSize} + {@link Constants#DEFAULT_ELEMENT_MAX_SIZE}.
	 */
	ArbitraryBuilder<T> minSize(String expression, int minSize);

	/**
	 * Set the size of one or more container properties referenced by expression.
	 * The size of container properties would be between {@code minSize} and
	 * {@code minSize} + {@link Constants#DEFAULT_ELEMENT_MAX_SIZE}
	 * <p>
	 * It would conflict with {@link ArbitraryBuilder#maxSize(String, int)}.
	 * The last executed manipulation would set the size of properties.
	 * <p>
	 * It is same as {@code size(minSize, minSize + DEFAULT_ELEMENT_MAX_SIZE)}.
	 *
	 * @param expressionGenerator it generates expression dynamically.
	 * @param minSize             of the container to generate
	 * @return an {@link ArbitraryBuilder} whose size of container properties referenced by
	 * an {@code expressionGenerator} is between {@code minSize}
	 * and {@code minSize} + {@link Constants#DEFAULT_ELEMENT_MAX_SIZE}.
	 */
	ArbitraryBuilder<T> minSize(ExpressionGenerator expressionGenerator, int minSize);

	/**
	 * Set the size of one or more container properties referenced by expression.
	 * The size of container properties would be between
	 * max(0, {@code maxSize} - {@link Constants#DEFAULT_ELEMENT_MAX_SIZE})
	 * and {@code maxSize}
	 * <p>
	 * It would conflict with {@link ArbitraryBuilder#minSize(String, int)}.
	 * The last executed manipulation would set the size of properties.
	 * <p>
	 * It is same as {@code size(expression, Math.max(0, maxSize - DEFAULT_ELEMENT_MAX_SIZE), maxSize)}.
	 *
	 * @param expression expression similar to Java syntax, including {@code .}, {@code []}
	 *                   common expression including {@code *} referencing properties.
	 * @param maxSize    of the container to generate
	 * @return an {@link ArbitraryBuilder} whose size of container properties referenced by an {@code expression} is
	 * max(0, {@code maxSize} - {@link Constants#DEFAULT_ELEMENT_MAX_SIZE})
	 * and {@code maxSize}
	 */
	ArbitraryBuilder<T> maxSize(String expression, int maxSize);

	/**
	 * Set the size of one or more container properties referenced by expression.
	 * The size of container properties would be between
	 * max(0, {@code maxSize} - {@link Constants#DEFAULT_ELEMENT_MAX_SIZE}) and {@code maxSize}
	 * <p>
	 * It would conflict with {@link ArbitraryBuilder#minSize(String, int)}.
	 * The last executed manipulation would set the size of properties.
	 * <p>
	 * It is same as {@code size(expression, Math.max(0, maxSize - DEFAULT_ELEMENT_MAX_SIZE), maxSize)}
	 *
	 * @param expressionGenerator it generates expression dynamically.
	 * @param maxSize             of the container to generate
	 * @return {@link ArbitraryBuilder} whose size of container properties referenced by
	 * an {@code expressionGenerator} is max(0, {@code maxSize} - {@link Constants#DEFAULT_ELEMENT_MAX_SIZE})
	 * and {@code maxSize}
	 */
	ArbitraryBuilder<T> maxSize(ExpressionGenerator expressionGenerator, int maxSize);

	/**
	 * It is deprecated. Use {@link #thenApply} instead.
	 */
	@Deprecated
	ArbitraryBuilder<T> apply(BiConsumer<T, ArbitraryBuilder<T>> biConsumer);

	ArbitraryBuilder<T> thenApply(BiConsumer<T, ArbitraryBuilder<T>> biConsumer);

	ArbitraryBuilder<T> acceptIf(Predicate<T> predicate, Consumer<ArbitraryBuilder<T>> consumer);

	/**
	 * Makes {@link ArbitraryBuilder} always sample an equivalent instance.
	 * All manipulations after {@link #fixed()} would work.
	 *
	 * @return a new {@link ArbitraryBuilder} always samples an equivalent instance
	 */
	ArbitraryBuilder<T> fixed();

	/**
	 * Transforms an instance by applying function to it.
	 *
	 * @param mapper the transforming {@link Function}
	 * @param <U>    type to transform
	 * @return a new {@link ArbitraryBuilder}
	 */
	<U> ArbitraryBuilder<U> map(Function<T, U> mapper);

	/**
	 * Combine the result from this {@link ArbitraryBuilder} and another into a new {@link ArbitraryBuilder}
	 *
	 * @param other      the {@link ArbitraryBuilder} to combine with
	 * @param combinator a {@link BiFunction} combinator function
	 * @param <U>        the element type of other {@link ArbitraryBuilder}
	 * @param <R>        the element type of the combination
	 * @return a new combined {@link ArbitraryBuilder}
	 */
	<U, R> ArbitraryBuilder<R> zipWith(ArbitraryBuilder<U> other, BiFunction<T, U, R> combinator);

	/**
	 * Combine the result from this {@link ArbitraryBuilder} and other and another into a new {@link ArbitraryBuilder}
	 *
	 * @param other      the {@link ArbitraryBuilder} to combine with
	 * @param combinator a {@link BiFunction} combinator function
	 * @param <U>        the element type of other {@link ArbitraryBuilder}
	 * @param <V>        the element type of another {@link ArbitraryBuilder}
	 * @param <R>        the element type of the combination
	 * @return a new combined {@link ArbitraryBuilder}
	 */
	<U, V, R> ArbitraryBuilder<R> zipWith(
		ArbitraryBuilder<U> other,
		ArbitraryBuilder<V> another,
		F3<T, U, V, R> combinator
	);

	/**
	 * Combine the result from this {@link ArbitraryBuilder} and other and another and the other into
	 * a new {@link ArbitraryBuilder}
	 *
	 * @param other      the {@link ArbitraryBuilder} to combine with
	 * @param combinator a {@link BiFunction} combinator function
	 * @param <U>        the element type of other {@link ArbitraryBuilder}
	 * @param <V>        the element type of another {@link ArbitraryBuilder}
	 * @param <W>        the element type of the other {@link ArbitraryBuilder}
	 * @param <R>        the element type of the combination
	 * @return a new combined {@link ArbitraryBuilder}
	 */
	<U, V, W, R> ArbitraryBuilder<R> zipWith(
		ArbitraryBuilder<U> other,
		ArbitraryBuilder<V> another,
		ArbitraryBuilder<W> theOther,
		F4<T, U, V, W, R> combinator
	);

	/**
	 * Combine the result from this {@link ArbitraryBuilder} and others into
	 * a new {@link ArbitraryBuilder}
	 *
	 * @param others     the list of {@link ArbitraryBuilder} to combine with
	 * @param combinator a {@link BiFunction} combinator function
	 * @param <R>        the element type of the combination
	 * @return a new combined {@link ArbitraryBuilder}
	 */
	<R> ArbitraryBuilder<R> zipWith(
		List<ArbitraryBuilder<?>> others,
		Function<List<?>, R> combinator
	);

	/**
	 * Build {@link ArbitraryBuilder} into {@link Arbitrary} for using various support methods
	 * provided in {@link Arbitrary}
	 *
	 * @return an {@link Arbitrary} built by {@link ArbitraryBuilder}
	 */
	Arbitrary<T> build();

	/**
	 * Generate a single sample value using this {@link ArbitraryBuilder}.
	 * It might generate differently per sample unless executing {@link #fixed()}.
	 *
	 * @return a generated instance
	 */
	T sample();

	/**
	 * Generate sample value list using this {@link ArbitraryBuilder}.
	 * All elements might generate differently per sample unless executing {@link #fixed()}.
	 *
	 * @return a list of generated instances
	 */
	List<T> sampleList(int size);

	/**
	 * Generate sample value stream using this {@link ArbitraryBuilder}.
	 * All elements might generate differently per sample unless executing {@link #fixed()}.
	 *
	 * @return a stream of generated instances
	 */
	Stream<T> sampleStream();

	/**
	 * Copy an {@link ArbitraryBuilder} instance. All manipulations would be copied.
	 *
	 * @return a copied {@link ArbitraryBuilder}
	 */
	ArbitraryBuilder<T> copy();

	/**
	 * Determine if only samples a valid instance or not.
	 * Default {@code validOnly} is true.
	 *
	 * @param validOnly determines generating only valid instance
	 * @return If true, returned {@link ArbitraryBuilder} would sample a valid instance validated by
	 * the {@link ArbitraryValidator}.
	 * If false, returned {@link ArbitraryBuilder} would sample an instance regardless of
	 * the {@link ArbitraryValidator}.
	 */
	ArbitraryBuilder<T> validOnly(boolean validOnly);
}
