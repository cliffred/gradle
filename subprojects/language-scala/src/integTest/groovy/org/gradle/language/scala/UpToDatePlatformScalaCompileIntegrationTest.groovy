/*
 * Copyright 2018 the original author or authors.
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

package org.gradle.language.scala

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.integtests.fixtures.AvailableJavaHomes
import org.gradle.util.Requires
import org.junit.Ignore
import spock.lang.Unroll

import static org.gradle.api.JavaVersion.VERSION_1_7
import static org.gradle.api.JavaVersion.VERSION_1_8

@Unroll
class UpToDatePlatformScalaCompileIntegrationTest extends AbstractIntegrationSpec {

    def setup() {
        file('app/controller/Person.scala') << "class Person(name: String)"
    }

    @Ignore("TODO upgrade Scala to be Java 9+ compatible to enable this test again")
    @Requires(adhoc = { AvailableJavaHomes.getJdk(VERSION_1_7) && AvailableJavaHomes.getJdk(VERSION_1_8) })
    def "compile is out of date when changing the java version"() {
        def jdk7 = AvailableJavaHomes.getJdk(VERSION_1_7)
        def jdk8 = AvailableJavaHomes.getJdk(VERSION_1_8)

        def scalaFixture = new LanuageScalaCompilationFixture(temporaryFolder.testDirectory)
        scalaFixture.baseline()
        buildFile << scalaFixture.buildScript()
        when:
        executer.withJavaHome(jdk7.javaHome)
        run 'compileMainJarMainScala'

        then:
        executedAndNotSkipped(':compileMainJarMainScala')

        when:
        executer.withJavaHome(jdk7.javaHome)
        run 'compileMainJarMainScala'
        then:
        skipped ':compileMainJarMainScala'

        when:
        executer.withJavaHome(jdk8.javaHome)
        run 'compileMainJarMainScala'
        then:
        executedAndNotSkipped(':compileMainJarMainScala')
    }
}
