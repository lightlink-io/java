package io.lightlink.test;

/*
 * #%L
 * lightlink-core
 * %%
 * Copyright (C) 2015 Vitaliy Shevchuk
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */


import io.lightlink.utils.ClasspathScanUtils;
import junit.framework.AssertionFailedError;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LightLinkTestRunner extends Runner {

    private Class testClass;
    private final List<File> fileList;
    private String rootPackage;

    public LightLinkTestRunner(Class<?> testClass) throws InitializationError, IOException {
        this.testClass = testClass;
        fileList = getFilesList(testClass);

    }

    public Class getTestClass() {
        return testClass;
    }

    public List<File> getFilesList(Class testClass) throws IOException {

        rootPackage = testClass.getPackage().getName();

        System.out.println("******************************");
        System.out.println("Test cases package = " + rootPackage);
        System.out.println("******************************");
        final List<File> fileList = new ArrayList<File>();
        List<String> resourcesFromPackage = ClasspathScanUtils.getAllResources(rootPackage,null);

        for (Iterator<String> iterator = resourcesFromPackage.iterator(); iterator.hasNext(); ) {
            String resource = iterator.next();
            if (resource.matches("^.*\\.js\\.sql$") && !resource.matches(".*[\\\\/]config\\.js\\.sql")){
                fileList.add(ClasspathScanUtils.getFileFromResource(rootPackage,resource));
            }
        }

        return fileList;
    }

    @Override
    public Description getDescription() {
        return Description.createSuiteDescription(("LightLink unit tests : " + rootPackage ).replaceAll("\\.", "[dot]"));
    }

    protected void doTestFile(File file) throws Exception {
        new LightLinkFileTester(rootPackage, file).doTestFile();
    }

    @Override
    public void run(RunNotifier notifier) {

        String filePattern = System.getProperty("file");

        for (File file : fileList) {
            String fName = file.getName();
            if (filePattern == null || fName.matches(filePattern)) {
                System.out.println("Processing file : " + file);
                notifier.fireTestStarted(Description.createTestDescription(testClass, fName));
                try {
                    doTestFile(file);
                    notifier.fireTestFinished(Description.createTestDescription(testClass, fName));
                } catch (AssertionFailedError e) {
                    notifier.fireTestFailure(new Failure(Description.createTestDescription(testClass, fName), e));
                } catch (Throwable t) {
                    notifier.fireTestFailure(new Failure(Description.createTestDescription(testClass, fName), t));
                }
            } else {
                System.out.println("Skipping file : " + file);
                notifier.fireTestIgnored(Description.createTestDescription(testClass, fName));
            }
        }

    }

    @Override
    public int testCount() {
        return fileList.size();
    }
}
