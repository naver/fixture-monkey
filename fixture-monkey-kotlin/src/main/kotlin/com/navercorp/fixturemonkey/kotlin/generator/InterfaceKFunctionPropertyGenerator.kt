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

package com.navercorp.fixturemonkey.kotlin.generator

import com.navercorp.fixturemonkey.api.generator.PropertyGenerator
import com.navercorp.fixturemonkey.api.property.InterfaceJavaMethodProperty
import com.navercorp.fixturemonkey.api.property.Property
import com.navercorp.fixturemonkey.api.type.Types
import com.navercorp.fixturemonkey.kotlin.property.InterfaceKFunctionProperty
import net.jqwik.kotlin.internal.isKotlinClass
import org.apiguardian.api.API
import java.lang.reflect.AnnotatedType
import kotlin.reflect.full.declaredMemberFunctions

@API(since = "0.5.3", status = API.Status.EXPERIMENTAL)
class InterfaceKFunctionPropertyGenerator : PropertyGenerator by KotlinPropertyGenerator(){
    override fun generateObjectChildProperties(annotatedType: AnnotatedType): List<Property>{
        val type = Types.getActualType(annotatedType.type)

        if(type.isKotlinClass()){
            return type.kotlin.declaredMemberFunctions.map{ InterfaceKFunctionProperty(it) }
        }

        return type.methods.map{ InterfaceJavaMethodProperty(it) }
    }
}
