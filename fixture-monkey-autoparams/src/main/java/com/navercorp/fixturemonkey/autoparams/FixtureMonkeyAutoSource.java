package com.navercorp.fixturemonkey.autoparams;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.javaunit.autoparams.AutoSource;
import org.javaunit.autoparams.customization.Customization;

import com.navercorp.fixturemonkey.autoparams.customization.FixtureMonkeyCustomizer;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@AutoSource
@Customization(FixtureMonkeyCustomizer.class)
public @interface FixtureMonkeyAutoSource {
}
