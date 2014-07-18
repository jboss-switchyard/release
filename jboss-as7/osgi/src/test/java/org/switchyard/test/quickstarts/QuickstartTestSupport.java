/*
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.switchyard.test.quickstarts;

import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.*;
import static org.ops4j.pax.exam.CoreOptions.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.tooling.exam.options.LogLevelOption;
import org.ops4j.pax.exam.MavenUtils;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ProbeBuilder;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.osgi.framework.Constants;


/**
 * Quickstart Test support.
 */
public class QuickstartTestSupport {

    private static final Long DEFAULT_TIMEOUT = 70000L;
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    @Inject 
    CommandProcessor commandProcessor;

    @Configuration
    public Option[] config() {
        return new Option[]{ karafDistributionConfiguration().frameworkUrl(
            maven().groupId("org.apache.karaf").artifactId("apache-karaf").versionAsInProject().type("zip"))
            .karafVersion(MavenUtils.getArtifactVersion("org.apache.karaf", "apache-karaf")).name("Apache Karaf").unpackDirectory(new File("target/karaf")).useDeployFolder(false),
            editConfigurationFileExtend("etc/org.apache.karaf.features.cfg", "featuresRepositories", "," + featureRepository()),
            keepRuntimeFolder(),
            logLevel(LogLevelOption.LogLevel.INFO)};
    }

    protected String featureRepository() {
        return "mvn:org.switchyard/switchyard-deploy-karaf/" + MavenUtils.getArtifactVersion("org.switchyard", "switchyard-deploy-karaf") + "/xml/features";
    }

    public String executeCommand(final String command) {
        return executeCommands(command);
    }

    public String executeCommands(final String ...commands) {
        String response = null;
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(byteArrayOutputStream);
        final CommandSession commandSession = commandProcessor.createSession(System.in, printStream, printStream);
        commandSession.put("APPLICATION", System.getProperty("karaf.name", "root"));
        commandSession.put("USER", "karaf");
        FutureTask<String> commandFuture = new FutureTask<String>(
            new Callable<String>() {
                public String call() {
                    try {
                        for(String command: commands) {
                         commandSession.execute(command);
                        }
                    } catch (Exception e) {
                        new RuntimeException(e);
                    }
                    return byteArrayOutputStream.toString();
                }
        });

        try {
            executor.submit(commandFuture);
            response = commandFuture.get(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return response;
    }

    /**
     * This is used to customize the Probe that will contain the test.
     * We need to enable dynamic import of provisional bundles, to use the Console.
     */
    @ProbeBuilder
    public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
        probe.setHeader(Constants.DYNAMICIMPORT_PACKAGE, "*,org.apache.felix.service.*;status=provisional");
        return probe;
    }
}
