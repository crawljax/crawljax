package com.crawljax.plugins.testcasegenerator;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.BrowserOptions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class JavaTestGeneratorTests {
    private static class InMemSource extends SimpleJavaFileObject {
        final String javaSource;

        InMemSource(String className, String javaSource) {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.javaSource = javaSource;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return javaSource;
        }
    }

    private static class InMemSink extends SimpleJavaFileObject {
        private ByteArrayOutputStream byteCodeStream = new ByteArrayOutputStream();

        InMemSink(String className) {
            super(URI.create("mem:///" + className + Kind.CLASS.extension), Kind.CLASS);
        }

        public byte[] getBytes() {
            return byteCodeStream.toByteArray();
        }

        @Override
        public OutputStream openOutputStream() {
            return byteCodeStream;
        }
    }

    private static class InMemFileManager extends ForwardingJavaFileManager {
        private final Map<String, InMemSink> classes = new HashMap<>();

        InMemFileManager() {
            super(ToolProvider.getSystemJavaCompiler().getStandardFileManager(null, null, null));
        }

        @Override
        public ClassLoader getClassLoader(Location location) {
            return new SecureClassLoader() {
                @Override
                protected Class<?> findClass(String name) {
                    byte[] byteCode = classes.get(name).getBytes();
                    return super.defineClass(name, byteCode, 0, byteCode.length);
                }
            };
        }
    }

    private interface CompilationListener<T> {
        T compiled(Boolean success, JavaFileManager manager, List<Diagnostic<? extends JavaFileObject>> diagnostics);
    }

    private <T> T compile(String className, String source, CompilationListener<T> listener) {
        JavaCompiler systemCompiler = ToolProvider.getSystemJavaCompiler();
        JavaFileManager manager = new InMemFileManager();
        DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<>();
        Iterable<? extends JavaFileObject> sources = Collections.singletonList(new InMemSource(className, source));
        CompilationTask task = systemCompiler.getTask(null, manager, diagnosticsCollector, null, null, sources);
        Boolean success = task.call();
        return listener.compiled(success, manager, diagnosticsCollector.getDiagnostics());
    }

    private void assertCompiles(final String className, String source) {
        compile(className, source, (success, manager, diagnostics) -> {
            assertSuccessfullyCompiled(success, diagnostics, className);
            return true;
        });
    }

    private void assertSuccessfullyCompiled(
            Boolean success, List<Diagnostic<? extends JavaFileObject>> diagnostics, String className) {
        if (success == null || !success) {
            StringBuilder builder = new StringBuilder();
            builder.append("Failed to compile: ");
            builder.append(className);
            builder.append("");
            for (Diagnostic<?> diagnostic : diagnostics) {
                builder.append(diagnostic.toString());
                builder.append("");
            }
            throw new AssertionError(builder.toString());
        }
    }

    @Test
    public void generatedBrowserConfigurationCompiles() throws IOException {
        BrowserConfiguration configuration =
                new BrowserConfiguration(BrowserType.CHROME, 1, new BrowserOptions(-1, true));
        StringBuilder output = new StringBuilder();
        output.append("package generated;");
        output.append("import java.util.concurrent.TimeUnit;"
                + "import com.crawljax.browser.EmbeddedBrowser.BrowserType;"
                + "import com.crawljax.core.configuration.BrowserConfiguration;"
                + "import com.crawljax.core.configuration.BrowserOptions;"
                + "import com.crawljax.core.configuration.CrawljaxConfiguration;"
                + "import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;");

        output.append("public class GeneratedTests {");
        output.append("    private final String URL = \"https://www.dictionary.com\";");
        output.append("private CrawljaxConfiguration getTestConfiguration() {"
                + "CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(URL);"
                + "builder.crawlRules().waitAfterEvent(200, TimeUnit.MILLISECONDS);"
                + "builder.crawlRules().waitAfterReloadUrl(200, TimeUnit.MILLISECONDS);"
                + "builder.setBrowserConfig(" + JavaTestGenerator.getBrowserConfigString(configuration) + ");"
                + "return builder.build();"
                + "}"
                + "}");
        String classNameWithPackage = "generated.GeneratedTests";

        System.out.println(JavaTestGenerator.getBrowserConfigString(configuration));
        // THEN
        assertCompiles(classNameWithPackage, output.toString());
        Files.deleteIfExists(Path.of("GeneratedTests.class").toAbsolutePath());
        Files.deleteIfExists(
                Path.of("plugins/testcasegenerator/GeneratedTests.class").toAbsolutePath());
    }
}
