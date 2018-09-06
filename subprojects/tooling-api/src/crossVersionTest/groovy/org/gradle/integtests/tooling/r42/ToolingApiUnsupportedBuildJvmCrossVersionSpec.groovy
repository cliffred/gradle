/*
 * Copyright 2017 the original author or authors.
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


package org.gradle.integtests.tooling.r42

import org.gradle.integtests.fixtures.AvailableJavaHomes
import org.gradle.integtests.tooling.fixture.TargetGradleVersion
import org.gradle.integtests.tooling.fixture.ToolingApiSpecification
import org.gradle.integtests.tooling.fixture.ToolingApiVersion
import org.gradle.integtests.tooling.r18.NullAction
import org.gradle.tooling.BuildException
import org.gradle.tooling.ProjectConnection
import org.gradle.tooling.model.GradleProject
import spock.lang.IgnoreIf

@TargetGradleVersion("current")
class ToolingApiUnsupportedBuildJvmCrossVersionSpec extends ToolingApiSpecification {

    def setup() {
        toolingApi.requireDaemons()
    }

    def configureJava7() {
        projectDir.file("gradle.properties").writeProperties(
            ["org.gradle.java.home": AvailableJavaHomes.jdk7.javaHome.absolutePath, 'org.gradle.warning.mode': 'all'])
    }

    @IgnoreIf({ AvailableJavaHomes.jdk7 == null })
    def "fails running a build when build is configured to use Java 7"() {
        configureJava7()
        def output = new ByteArrayOutputStream()

        when:
        toolingApi.withConnection { ProjectConnection connection ->
            def build = connection.newBuild()
            build.standardOutput = output
            build.run()
        }

        then:
        def e = thrown(BuildException)
        e.message.contains("requires Java 8 or later to run. You are currently using Java 7.")
    }

    @IgnoreIf({ AvailableJavaHomes.jdk7 == null })
    def "fails fetching model when build is configured to use Java 7"() {
        configureJava7()
        def output = new ByteArrayOutputStream()

        when:
        toolingApi.withConnection { ProjectConnection connection ->
            def model = connection.model(GradleProject)
            model.standardOutput = output
            model.get()
        }

        then:
        def e = thrown(BuildException)
        e.message.contains("requires Java 8 or later to run. You are currently using Java 7.")
    }

    @IgnoreIf({ AvailableJavaHomes.jdk7 == null })
    def "fails running action when build is configured to use Java 7"() {
        configureJava7()
        def output = new ByteArrayOutputStream()

        when:
        toolingApi.withConnection { ProjectConnection connection ->
            def action = connection.action(new NullAction())
            action.standardOutput = output
            action.run()
        }

        then:
        def e = thrown(BuildException)
        e.message.contains("requires Java 8 or later to run. You are currently using Java 7.")
    }

    @ToolingApiVersion(">=2.6")
    @IgnoreIf({ AvailableJavaHomes.jdk7 == null })
    def "fails running tests when build is configured to use Java 7"() {
        configureJava7()
        def output = new ByteArrayOutputStream()

        when:
        toolingApi.withConnection { ProjectConnection connection ->
            def launcher = connection.newTestLauncher().withJvmTestClasses("SomeTest")
            launcher.standardOutput = output
            launcher.run()
        }

        then:
        def e = thrown(BuildException)
        e.message.contains("requires Java 8 or later to run. You are currently using Java 7.")
    }

}
