package com.gjy.domain.openai.annotation;

import com.gjy.domain.openai.service.rule.factory.DefaultLogicFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface LogicStrategy {
    DefaultLogicFactory.LogicModel logicMode();
}
