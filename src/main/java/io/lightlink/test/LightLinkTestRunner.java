package io.lightlink.test;

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
