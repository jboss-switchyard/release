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

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

/**
 * CamelBinding Test.
 */
@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class CamelBindingQuickstartTest extends QuickstartTestSupport {

    private static String SOURCE_FILE = "../../test-classes/test.txt";
    private static String DEST_FILE = "target/input/test.txt";

    @Test
    public void testFeatures() throws Exception {
        executeCommand("features:install switchyard-quickstart-camel-binding");
        // Move our test file into position
        File srcFile = new File(SOURCE_FILE);
        File destFile = new File(DEST_FILE);
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }
        srcFile.renameTo(destFile);

        assertTrue(destFile.exists());
        // Wait a spell so that the file component polls and picks up the file

        // File should have been picked up
        while (destFile.exists()) {
            Thread.sleep(50);
        }
        assertFalse(destFile.exists());
    }
}
