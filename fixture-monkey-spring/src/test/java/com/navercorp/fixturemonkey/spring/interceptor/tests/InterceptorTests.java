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

package com.navercorp.fixturemonkey.spring.interceptor.tests;

import static org.assertj.core.api.BDDAssertions.then;

import java.net.URI;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.random.Randoms;
import com.navercorp.fixturemonkey.spring.interceptor.FixtureMonkeyInterceptorConfiguration;
import com.navercorp.fixturemonkey.spring.interceptor.MethodInterceptorContext;
import com.navercorp.fixturemonkey.spring.interceptor.tests.InterceptorTests.SpringTestApplication;
import com.navercorp.fixturemonkey.spring.interceptor.tests.InterceptorTests.SpringTestApplication.TestApiClient;

@SpringBootTest(
	classes = SpringTestApplication.class,
	webEnvironment = WebEnvironment.NONE
)
@ActiveProfiles("test")
@ImportAutoConfiguration(FixtureMonkeyInterceptorConfiguration.class)
class InterceptorTests {
	@Autowired
	private TestApiClient testApiClient;

	@AfterEach
	void tearDown() {
		MethodInterceptorContext.clear();
	}

	@Test
	void returnTypeIsMonoTypeReturns() {
		String expected = "test";

		String actual = testApiClient.getMonoString()
			.blockOptional().orElseThrow();

		then(actual).isEqualTo(expected);
	}

	@Test
	void returnTypeIsSimpleTypeReturns() {
		String expected = "test";

		String actual = testApiClient.getRawString();

		then(actual).isEqualTo(expected);
	}

	@Test
	void staticMethodReturns() {
		String actual = TestApiClient.getStaticString();

		then(actual).isEqualTo("test");
	}

	@Test
	void fixReturnTypeIsSimpleTypeReturnsFixed() {
		String expected = "expected";
		MethodInterceptorContext.bean(TestApiClient.class)
			.methodReturnType(String.class)
			.fix("$", expected);

		String actual = testApiClient.getRawString();

		then(actual).isEqualTo(expected);
	}

	@Test
	void fixMethodReturnsFixed() {
		String expected = "expected";
		MethodInterceptorContext.bean(TestApiClient.class)
			.method(String.class, "getRawString")
			.fix("$", expected);

		String actual = testApiClient.getRawString();

		then(actual).isEqualTo(expected);
	}

	@Test
	void fixReturnTypeIsMonoReturnsFixed() {
		String expected = "expected";
		MethodInterceptorContext.bean(TestApiClient.class)
			.methodReturnType(String.class)
			.fix("$", expected);

		String actual = testApiClient.getMonoString()
			.blockOptional().orElseThrow();

		then(actual).isEqualTo(expected);
	}

	@Test
	void fixByMethodNameReturnsFixed() {
		String expected = "expected";
		MethodInterceptorContext.bean(TestApiClient.class).methodName("getMonoString")
			.fix("$", expected);

		String actual = testApiClient.getMonoString()
			.blockOptional().orElseThrow();

		then(actual).isEqualTo(expected);
	}

	@Test
	void fixMonoEmptyReturnsFixed() {
		String expected = "expected";
		MethodInterceptorContext.bean(TestApiClient.class)
			.methodReturnType(String.class)
			.fix("$", expected);

		String actual = testApiClient.getMonoEmpty()
			.blockOptional().orElseThrow();

		then(actual).isEqualTo(expected);
	}

	@Test
	void fixNullReturnsFixed() {
		String expected = "expected";
		MethodInterceptorContext.bean(TestApiClient.class)
			.methodReturnType(String.class)
			.fix("$", expected);

		String actual = testApiClient.getNull();

		then(actual).isEqualTo(expected);
	}

	@Test
	void fixExceptionReturnsFixed() {
		String actual = testApiClient.fetch()
			.blockOptional().orElseThrow();

		then(actual).isNotNull();
	}

	@Test
	void returnTypeIsComplexTypeReturns() {
		String actual = testApiClient.getComplexType().stringValue();

		then(actual).isEqualTo("test");
	}

	@Test
	void fixComplexTypePropertyReturnsFixed() {
		String expected = "expected";
		MethodInterceptorContext.bean(TestApiClient.class).methodReturnType(ComplexType.class)
			.fix("stringValue", expected);
		String actual = testApiClient.getComplexType().stringValue();

		then(actual).isEqualTo(expected);
	}

	@Test
	void withInitialReturnsSameIfNoChanged() {
		String expected = "expected";
		MethodInterceptorContext.bean(TestApiClient.class).methodReturnType(ComplexType.class)
			.withInitial("stringValue", expected);
		testApiClient.getComplexType().stringValue();

		String actual = testApiClient.getComplexType().stringValue();

		then(actual).isEqualTo(expected);
	}

	@Test
	void withInitialReturnsDiff() {
		String expected = "expected";
		MethodInterceptorContext.bean(TestApiClient.class).methodReturnType(ComplexType.class)
			.withInitial("stringValue", expected);
		testApiClient.getRandomComplexType().stringValue();

		String actual = testApiClient.getRandomComplexType().stringValue();

		then(actual).isNotEqualTo(expected);
	}

	@Test
	void manipulateInOrderReturnsWithed() {
		String notExpected = "notExpected";
		String expected = "expected";
		MethodInterceptorContext.bean(TestApiClient.class).methodReturnType(ComplexType.class)
			.fix("stringValue", notExpected)
			.withInitial("stringValue", expected);

		String actual = testApiClient.getComplexType().stringValue();

		then(actual).isEqualTo(expected);
	}

	@Test
	void manipulateInOrderReturnsFixed() {
		String notExpected = "notExpected";
		String expected = "expected";
		MethodInterceptorContext.bean(TestApiClient.class).methodReturnType(ComplexType.class)
			.withInitial("stringValue", notExpected)
			.fix("stringValue", expected);

		String actual = testApiClient.getComplexType().stringValue();

		then(actual).isEqualTo(expected);
	}

	@Test
	void withInitialWouldNotAffectedByManipulatingOtherProperty() {
		String expected = "expected";
		MethodInterceptorContext.bean(TestApiClient.class).methodReturnType(ComplexType.class)
			.withInitial("stringValue", expected)
			.fix("intValue", 9);

		String actual = testApiClient.getRandomComplexType().stringValue();

		then(actual).isEqualTo(expected);
	}

	public record ComplexType(int intValue, String stringValue) {
	}

	@SpringBootApplication
	public static class SpringTestApplication {
		@Bean
		public WebClient webClient() {
			return WebClient.builder().build();
		}

		@Bean
		public FixtureMonkey fixtureMonkey() {
			return FixtureMonkey.builder()
				.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
				.build();
		}

		@Component
		@RequiredArgsConstructor
		public static class TestApiClient {
			private final WebClient webClient;

			public Mono<String> getMonoString() {
				return Mono.just("test");
			}

			public Mono<String> getMonoEmpty() {
				return Mono.empty();
			}

			public Mono<String> fetch() {
				return webClient.get()
					.uri(URI.create("localhost:8080"))
					.retrieve()
					.bodyToMono(String.class);
			}

			public String getRawString() {
				return "test";
			}

			public String getNull() {
				return null;
			}

			public static String getStaticString() {
				return "test";
			}

			public ComplexType getComplexType() {
				return new ComplexType(1, "test");
			}

			public ComplexType getRandomComplexType() {
				return new ComplexType(Randoms.nextInt(10), UUID.randomUUID().toString());
			}
		}
	}
}
