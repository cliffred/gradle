/*
 * Copyright 2012 the original author or authors.
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
package org.gradle.integtests.fixtures;

import org.gradle.api.Action;
import org.gradle.integtests.fixtures.executer.ArtifactBuilder;
import org.gradle.integtests.fixtures.executer.GradleBackedArtifactBuilder;
import org.gradle.integtests.fixtures.executer.GradleContextualExecuter;
import org.gradle.integtests.fixtures.executer.GradleDistribution;
import org.gradle.integtests.fixtures.executer.GradleExecuter;
import org.gradle.integtests.fixtures.executer.IntegrationTestBuildContext;
import org.gradle.integtests.fixtures.executer.UnderDevelopmentGradleDistribution;
import org.gradle.test.fixtures.file.TestFile;
import org.gradle.test.fixtures.file.TestNameTestDirectoryProvider;
import org.gradle.test.fixtures.ivy.IvyFileRepository;
import org.gradle.test.fixtures.maven.M2Installation;
import org.gradle.test.fixtures.maven.MavenFileRepository;
import org.junit.After;
import org.junit.Rule;

import java.io.File;

import static org.gradle.api.internal.artifacts.BaseRepositoryFactory.PLUGIN_PORTAL_OVERRIDE_URL_PROPERTY;
import static org.gradle.integtests.fixtures.RepoScriptBlockUtil.gradlePluginRepositoryMirrorUrl;

public abstract class AbstractIntegrationTest {
    @Rule
    public final TestNameTestDirectoryProvider testDirectoryProvider = new TestNameTestDirectoryProvider();

    public final GradleDistribution distribution = new UnderDevelopmentGradleDistribution(getBuildContext());
    public final GradleContextualExecuter executer = new GradleContextualExecuter(distribution, testDirectoryProvider, getBuildContext());

    public IntegrationTestBuildContext getBuildContext() {
        return IntegrationTestBuildContext.INSTANCE;
    }

//    @Rule
    public final M2Installation m2 = new M2Installation(testDirectoryProvider);

    private MavenFileRepository mavenRepo;
    private IvyFileRepository ivyRepo;

    @After
    public void cleanup() {
        executer.cleanup();
    }

    protected GradleDistribution getDistribution() {
        return distribution;
    }

    protected GradleExecuter getExecuter() {
        return executer;
    }

    protected TestNameTestDirectoryProvider getTestDirectoryProvider() {
        return testDirectoryProvider;
    }

    public TestFile getTestDirectory() {
        return getTestDirectoryProvider().getTestDirectory();
    }

    public TestFile file(Object... path) {
        return getTestDirectory().file(path);
    }

    public TestFile testFile(String name) {
        return file(name);
    }

    protected GradleExecuter inTestDirectory() {
        return inDirectory(getTestDirectory());
    }

    protected GradleExecuter inDirectory(File directory) {
        return getExecuter().inDirectory(directory);
    }

    protected GradleExecuter usingBuildFile(File file) {
        return getExecuter().usingBuildScript(file);
    }

    protected GradleExecuter usingProjectDir(File projectDir) {
        return getExecuter().usingProjectDirectory(projectDir);
    }

    protected ArtifactBuilder artifactBuilder() {
        GradleExecuter gradleExecuter = getDistribution().executer(testDirectoryProvider, getBuildContext());
        gradleExecuter.withGradleUserHomeDir(getExecuter().getGradleUserHomeDir());
        return new GradleBackedArtifactBuilder(gradleExecuter, getTestDirectory().file("artifacts"));
    }

    public MavenFileRepository maven(TestFile repo) {
        return new MavenFileRepository(repo);
    }

    public MavenFileRepository maven(Object repo) {
        return new MavenFileRepository(file(repo));
    }

    public MavenFileRepository getMavenRepo() {
        if (mavenRepo == null) {
            mavenRepo = new MavenFileRepository(file("maven-repo"));
        }
        return mavenRepo;
    }

    public IvyFileRepository ivy(TestFile repo) {
        return new IvyFileRepository(repo);
    }

    public IvyFileRepository ivy(Object repo) {
        return new IvyFileRepository(file(repo));
    }

    public IvyFileRepository getIvyRepo() {
        if (ivyRepo == null) {
            ivyRepo = new IvyFileRepository(file("ivy-repo"));
        }
        return ivyRepo;
    }


    public GradleExecuter using(Action<GradleExecuter> action) {
        action.execute(executer);
        return executer;
    }

    public static String mavenCentralRepository() {
        return RepoScriptBlockUtil.mavenCentralRepository();
    }

    public void useRepositoryMirrors() {
        useRepositoryMirrors(false);
    }

    public void useRepositoryMirrors(final boolean global) {
        if (System.getenv("CI") == null) {
            return;
        }
        executer.beforeExecute(new Action<GradleExecuter>() {
            @Override
            public void execute(GradleExecuter gradleExecuter) {
                if (global) {
                    File userHome = file("user-home");
                    executer.withGradleUserHomeDir(userHome);
                    file("user-home/init.gradle").write(RepoScriptBlockUtil.mirrorInitScript());
                } else {
                    executer.usingInitScript(RepoScriptBlockUtil.createMirrorInitScript());
                }
            }
        });
    }

    public void usePluginRepositoryMirror() {
        if (System.getenv("CI") == null) {
            return;
        }
        executer.beforeExecute(new Action<GradleExecuter>() {
            @Override
            public void execute(GradleExecuter gradleExecuter) {
                executer.withArgument("-D" + PLUGIN_PORTAL_OVERRIDE_URL_PROPERTY + "=" + gradlePluginRepositoryMirrorUrl());
            }
        });
    }
}
