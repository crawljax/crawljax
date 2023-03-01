package com.crawljax.plugins.testplugin;

import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.CrawlRules.FormFillMode;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.plugin.HostInterfaceImpl;
import com.crawljax.core.plugin.descriptor.Parameter;
import com.crawljax.core.plugin.descriptor.PluginDescriptor;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.forms.FormInput.InputType;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Use the sample plugin in combination with Crawljax.
 */
public class Runner {

    private static final String URL = "http://www.google.com";
    private static final int MAX_DEPTH = 1;
    private static final int MAX_NUMBER_STATES = 3;

    private Runner() {
        // Utility class
    }

    /**
     * Entry point
     */
    public static void main(String[] args) {
        CrawljaxConfiguration.CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(URL);
        builder.crawlRules().setFormFillMode(FormFillMode.NORMAL);

        builder.crawlRules().click("a");
        builder.crawlRules().click("button");

        // except these
        builder.crawlRules().dontClick("a").underXPath("//DIV[@id='guser']");
        builder.crawlRules().dontClick("a").withText("Language Tools");

        // limit the crawling scope
        builder.setMaximumStates(MAX_NUMBER_STATES);
        builder.setMaximumDepth(MAX_DEPTH);

        PluginDescriptor descriptor = PluginDescriptor.forPlugin(TestPlugin.class);
        Map<String, String> parameters = new HashMap<>();
        for (Parameter parameter : descriptor.getParameters()) {
            parameters.put(parameter.getId(), "value");
        }
        builder.addPlugin(new TestPlugin(new HostInterfaceImpl(new File("out"), parameters)));

        builder.crawlRules().setInputSpec(getInputSpecification());

        CrawljaxRunner crawljax = new CrawljaxRunner(builder.build());
        crawljax.call();
    }

    private static InputSpecification getInputSpecification() {
        InputSpecification input = new InputSpecification();
        input.inputField(InputType.TEXT, new Identification(How.id, "lst-ib")).inputValues("Crawljax");

        return input;
    }
}
