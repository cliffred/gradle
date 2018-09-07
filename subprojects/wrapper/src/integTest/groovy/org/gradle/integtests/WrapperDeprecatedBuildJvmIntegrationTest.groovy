/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.integtests

import org.gradle.integtests.fixtures.AvailableJavaHomes
import org.gradle.internal.jvm.UnsupportedJavaRuntimeException
import spock.lang.IgnoreIf

class WrapperDeprecatedBuildJvmIntegrationTest extends AbstractWrapperIntegrationSpec {
    @IgnoreIf({ AvailableJavaHomes.jdk7 == null })
    def "fails when running under java 7"() {
        def jdk = AvailableJavaHomes.jdk7

        given:
        prepareWrapper()
        wrapperExecuter.withJavaHome(jdk.javaHome)

        expect:
        def result = wrapperExecuter.withTasks("help").runWithFailure()
        result.assertOutputContains("requires Java 8 or later to run. You are currently using Java 7.")
    }
}
