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

package com.navercorp.fixturemonkey.spring.interceptor;

import java.util.List;
import java.util.Optional;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.aop.support.Pointcuts;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.spring.interceptor.properties.AspectJPointcutProperty;
import com.navercorp.fixturemonkey.spring.interceptor.properties.MethodNamePointcutProperty;

@SuppressWarnings("SpringFacetCodeInspection")
@AutoConfiguration
@EnableAspectJAutoProxy
@ConfigurationPropertiesScan
public class FixtureMonkeyInterceptorConfiguration {
	@Bean
	@ConditionalOnMissingBean
	public Pointcut fixtureMonkeyPointcut(
		AspectJPointcutProperty aspectJPointcutProperty,
		MethodNamePointcutProperty methodNamePointcutProperty
	) {
		Pointcut combinedPointcut = null;
		if (aspectJPointcutProperty.getExpression() != null) {
			AspectJExpressionPointcut aspectJPointcut = new AspectJExpressionPointcut();
			aspectJPointcut.setExpression(aspectJPointcutProperty.getExpression());
			combinedPointcut = aspectJPointcut;
		}

		if (methodNamePointcutProperty.getNames() != null) {
			List<String> methodNames = methodNamePointcutProperty.getNames();
			NameMatchMethodPointcut nameMatchMethodPointcut = new NameMatchMethodPointcut();
			for (String methodName : methodNames) {
				nameMatchMethodPointcut.addMethodName(methodName);
			}

			if (combinedPointcut == null) {
				combinedPointcut = nameMatchMethodPointcut;
			} else {
				combinedPointcut = Pointcuts.union(combinedPointcut, nameMatchMethodPointcut);
			}
		}

		if (combinedPointcut == null) {
			throw new IllegalArgumentException("Pointcut is not set.");
		}

		return combinedPointcut;
	}

	@Bean
	public Advice fixtureMonkeyMethodInterceptor(Optional<FixtureMonkey> fixtureMonkey) {
		return new FixtureMonkeyMethodInterceptor(fixtureMonkey.orElse(FixtureMonkey.create()));
	}

	@Bean
	public Advisor fixtureMonkeyAdvisor(
		@Qualifier("fixtureMonkeyPointcut") Pointcut pointcut,
		@Qualifier("fixtureMonkeyMethodInterceptor") Advice advice
	) {
		return new DefaultPointcutAdvisor(pointcut, advice);
	}
}
