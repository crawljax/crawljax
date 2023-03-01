package com.crawljax.cli;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.test.util.CaptureSystemStreams;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.LoggerFactory;

public class LogUtilTest {

    @Rule
    public final CaptureSystemStreams system = new CaptureSystemStreams();

    @Rule
    public final TemporaryFolder tmpFolder = new TemporaryFolder();

    @Before
    public void beforeResetLogging() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(loggerContext);
            loggerContext.reset();
            configurator.doConfigure(getClass().getResource("/logback-test.xml"));
        } catch (JoranException je) {
            // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);
    }

    @Test
    public void noChangePrintsWarnLogLevel() {
        LogUtil.setCrawljaxLogLevel(Level.WARN);
        LoggerFactory.getLogger(CrawljaxRunner.class).warn("Test123");
        LoggerFactory.getLogger(CrawljaxRunner.class).info("IAmNotPrinted");
        assertThat(system.getConsoleOutput(), containsString("Test123"));
        assertThat(system.getConsoleOutput(), not(containsString("IAmNotPrinted")));
    }

    @Test
    public void whenLogLevelChangesConsoleAdepts() {
        LogUtil.setCrawljaxLogLevel(Level.INFO);
        LoggerFactory.getLogger(CrawljaxRunner.class).info("Test123");
        assertThat(system.getConsoleOutput(), containsString("Test123"));
    }

    @Test
    public void whenPrintToFileTheFileExistsAndIsPrintedTo() throws IOException {
        File file = new File(tmpFolder.getRoot(), "test.log");
        assertThat(file.exists(), is(false));

        LogUtil.logToFile(file.getPath());

        LoggerFactory.getLogger(CrawljaxRunner.class).warn("Test123");

        assertThat(file.exists(), is(true));
        assertThat(Files.toString(file, Charsets.UTF_8), containsString("Test123"));
        assertThat(system.getConsoleOutput(), not(containsString("Test123")));
    }
}
