package com.crawljax.plugins.testcasegenerator;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.BrowserOptions;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.Plugin;
import com.crawljax.util.FSUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * @author mesbah
 * @version $Id: JavaTestGenerator.java 6234 2009-12-18 13:46:37Z mesbah $
 */
public class JavaTestGenerator {

    private static final String POSIX_RUNNER_SCRIPT_TEMPLATE_NAME = "RunnerScriptPosix.vm";
    private static final String WINDOWS_RUNNER_SCRIPT_TEMPLATE_NAME = "RunnerScriptWindows.vm";
    private static final String POSIX_RUNNER_SCRIPT_GENERATED_FILE_NAME = "run.sh";
    private static final String WINDOWS_RUNNER_SCRIPT_GENERATED_FILE_NAME = "run.bat";
    private static final String TESTNG_XML_TEMPLATE_NAME = "testng.xml.vm";
    private static final String TESTNG_XML_GENERATED_FILE_NAME = "testng.xml";
    private static final String POM_TEMPLATE = "pom_template.vm";
    private static final String POM_FILE_NAME = "pom.xml";
    private final VelocityEngine engine;
    private final VelocityContext context;
    private final String className;

    /**
     * @param className
     * @param url
     */
    //	public JavaTestGenerator(String className, String url, List<TestMethod> testMethods,
    //	        CrawljaxConfiguration config, String testSuitePath, String screenshotPath,
    //	        String diffPath, TestConfiguration testConfiguration) throws Exception {
    //		engine = new VelocityEngine();
    //		/* disable logging */
    //		engine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS,
    //		        "org.apache.velocity.runtime.log.NullLogChute");
    //		// tell Velocity to look in classpath for template file
    //		engine.setProperty("resource.loader", "file");
    //		engine.setProperty("file.resource.loader.class",
    //		        "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
    //
    //		engine.init();
    //		context = new VelocityContext();
    //		this.className = className;
    //		context.put("date", new Date().toString());
    //		context.put("classname", className);
    //		context.put("url", url);
    //		context.put("browserConfig",
    //		        getBrowserConfigString(testConfiguration.getBrowserConfig()));
    //
    //		context.put("waitAfterEvent", config.getCrawlRules().getWaitAfterEvent());
    //		context.put("waitAfterReloadUrl", config.getCrawlRules().getWaitAfterReloadUrl());
    //
    //		/*
    //		 * boolean usePropertiesFile = PropertyHelper.getPropertiesFileName() != null &&
    //		 * !PropertyHelper.getPropertiesFileName().equals(""); context.put("usePropertiesFile",
    //		 * usePropertiesFile); context.put("propertiesfile",
    //		 * PropertyHelper.getPropertiesFileName());
    //		 */
    //		context.put("methodList", testMethods);
    //		context.put("database", true);
    //		context.put("testSuitePath", escapePath(testSuitePath));
    //
    //		context.put("crawl", escapePath(screenshotPath));
    //		context.put("diffScreenshots", escapePath(diffPath));
    //		context.put("assertionMode", testConfiguration.getAssertionMode());
    //
    //		List<String> plugins = new ArrayList<String>();
    //		if(!config.getPlugins().isEmpty()) {
    //			for(Plugin plugin: config.getPlugins()) {
    //				if(plugin.getClass().getSimpleName().contains("Cleanup")){
    //					plugins.add(plugin.getClass().getSimpleName());
    //				}
    //			}
    //		}
    //		if(!plugins.isEmpty()) {
    //			context.put("plugins", plugins);
    //		}
    //	}
    public JavaTestGenerator(
            String className,
            String url,
            List<TestMethod> testMethods,
            CrawljaxConfiguration config,
            String absPath,
            TestConfiguration testConfiguration) {
        engine = new VelocityEngine();
        // tell Velocity to look in classpath for template file
        engine.setProperty("resource.loader", "file");
        engine.setProperty(
                "file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        engine.init();
        context = new VelocityContext();
        this.className = className;
        context.put("date", new Date().toString());
        context.put("classname", className);
        context.put("url", url);
        context.put("browserConfig", getBrowserConfigString(testConfiguration.getBrowserConfig()));

        context.put("waitAfterEvent", config.getCrawlRules().getWaitAfterEvent());
        context.put("waitAfterReloadUrl", config.getCrawlRules().getWaitAfterReloadUrl());

        /*
         * boolean usePropertiesFile = PropertyHelper.getPropertiesFileName() != null &&
         * !PropertyHelper.getPropertiesFileName().equals(""); context.put("usePropertiesFile",
         * usePropertiesFile); context.put("propertiesfile",
         * PropertyHelper.getPropertiesFileName());
         */
        context.put("methodList", testMethods);
        context.put("database", true);
        //		context.put("testSuitePath", escapePath(testSuitePath));

        context.put("crawlPath", escapePath(absPath));
        //		context.put("diffScreenshots", escapePath(diffPath));
        context.put("assertionMode", testConfiguration.getAssertionMode());

        List<String> plugins = new ArrayList<String>();
        if (!config.getPlugins().isEmpty()) {
            for (Plugin plugin : config.getPlugins()) {
                if (plugin.getClass().getSimpleName().contains("Cleanup")) {
                    plugins.add(plugin.getClass().getSimpleName());
                }
            }
        }
        if (!plugins.isEmpty()) {
            context.put("plugins", plugins);
        }
    }

    static String getBrowserOptionsString(BrowserOptions browserOptions) {
        StringBuilder builder = new StringBuilder();
        builder.append(browserOptions.getClass().getSimpleName() + "(");
        builder.append(browserOptions.getPixelDensity() + ", ");
        builder.append(browserOptions.isUSE_CDP());
        builder.append(")");
        return builder.toString();
    }

    static Object getBrowserConfigString(BrowserConfiguration browserConfig) {
        StringBuilder builder = new StringBuilder();
        if (browserConfig.getBrowserType() == EmbeddedBrowser.BrowserType.REMOTE) {
            builder.append("BrowserConfiguration.remoteConfig(");
            builder.append(browserConfig.getNumberOfBrowsers() + ", ");
            builder.append("\"" + browserConfig.getRemoteHubUrl() + "\")");
        } else {
            builder.append("new BrowserConfiguration(");
            builder.append("BrowserType." + browserConfig.getBrowserType());
            builder.append(", ");
            builder.append(browserConfig.getNumberOfBrowsers());
            builder.append(", ");
            builder.append("new " + getBrowserOptionsString(browserConfig.getBrowserOptions()));
            builder.append(")");
        }
        return builder.toString();
    }

    public void useJsonInsteadOfDB(String jsonStates, String jsonEventables) {
        context.put("jsonstates", escapePath(jsonStates));
        context.put("jsoneventables", escapePath(jsonEventables));
        context.put("database", false);
    }

    private String escapePath(String path) {
        return new File(path).getAbsolutePath().replace("\\", "\\\\");
    }

    /**
     * @param outputFolder
     * @param fileNameTemplate
     * @return filename of generates class
     * @throws Exception
     */
    public String generate(String outputFolder, String fileNameTemplate) throws Exception {

        Template template = engine.getTemplate(fileNameTemplate);
        FSUtils.directoryCheck(outputFolder);
        File f = new File(outputFolder + className + ".java");
        FileWriter writer = new FileWriter(f);
        template.merge(context, writer);
        writer.flush();
        writer.close();
        return f.getAbsolutePath();
    }

    public void copyExecutionScripts(
            String outputFolder, String testSuiteSrcFolder, String testSuitePackageName, String testSuiteClassName)
            throws IOException {
        FSUtils.directoryCheck(outputFolder);
        VelocityContext runnerScriptContext = new VelocityContext();
        //		runnerScriptContext.put("libsClassPath", "../../../libs");
        runnerScriptContext.put(
                "libsClassPath", "../../../target/crawljax-examples-5.0-beta-jar-with-dependencies.jar");
        runnerScriptContext.put(
                "generatedTestsJavaFilePath",
                testSuiteSrcFolder + File.separator + testSuitePackageName.replace(".", File.separator)
                        + File.separator + testSuiteClassName
                        + ".java");
        runnerScriptContext.put("generatedTestsSourceFolder", testSuiteSrcFolder);
        runnerScriptContext.put("testngXMLFile", TESTNG_XML_GENERATED_FILE_NAME);

        // Generate runner script for posix
        Template posixRunnerScriptTemplate = engine.getTemplate(POSIX_RUNNER_SCRIPT_TEMPLATE_NAME);
        File posixRunnerScriptFile = new File(outputFolder + File.separator + POSIX_RUNNER_SCRIPT_GENERATED_FILE_NAME);
        FileWriter posixRunnerScriptFileWriter = new FileWriter(posixRunnerScriptFile);
        posixRunnerScriptTemplate.merge(runnerScriptContext, posixRunnerScriptFileWriter);
        posixRunnerScriptFileWriter.flush();
        posixRunnerScriptFileWriter.close();

        // Generate runner script for windows
        Template windowsRunnerScriptTemplate = engine.getTemplate(WINDOWS_RUNNER_SCRIPT_TEMPLATE_NAME);
        File windowsRunnerScriptFile =
                new File(outputFolder + File.separator + WINDOWS_RUNNER_SCRIPT_GENERATED_FILE_NAME);
        FileWriter windowsRunnerScriptFileWriter = new FileWriter(windowsRunnerScriptFile);
        windowsRunnerScriptTemplate.merge(runnerScriptContext, windowsRunnerScriptFileWriter);
        windowsRunnerScriptFileWriter.flush();
        windowsRunnerScriptFileWriter.close();

        // Generate testng.xml
        VelocityContext testngContext = new VelocityContext();
        testngContext.put("generatedTestsQualifiedClassName", testSuitePackageName + "." + className);
        Template testngTemplate = engine.getTemplate(TESTNG_XML_TEMPLATE_NAME);
        File testngXMLFile = new File(outputFolder + File.separator + TESTNG_XML_GENERATED_FILE_NAME);
        FileWriter testngFileWriter = new FileWriter(testngXMLFile);
        testngTemplate.merge(testngContext, testngFileWriter);
        testngFileWriter.flush();
        testngFileWriter.close();

        // Generate pom.xml
        Template pomTemplate = engine.getTemplate(POM_TEMPLATE);
        File pomFile = new File(outputFolder + File.separator + POM_FILE_NAME);
        FileWriter pomWriter = new FileWriter(pomFile);
        pomTemplate.merge(runnerScriptContext, pomWriter);
        pomWriter.flush();
        pomWriter.close();
    }
}
