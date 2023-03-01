package com.crawljax.forms;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import com.crawljax.browser.BrowserProvider;
import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.configuration.CrawlRules;
import com.crawljax.core.configuration.CrawlRules.FormFillMode;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.Form;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.forms.FormInput.InputType;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.RunWithWebServer;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.WebElement;

@Category(BrowserTest.class)
public class FormHandlerTest {

    @ClassRule
    public static final RunWithWebServer DEMO_SITE_SERVER = new RunWithWebServer("/site/formhandler");

    @Rule
    public final BrowserProvider provider = new BrowserProvider();

    private EmbeddedBrowser browser;

    private FormHandler formHandler;

    @Before
    public void setup() throws Exception {
        browser = provider.newEmbeddedBrowser();
        browser.goToUrl(DEMO_SITE_SERVER.getSiteUrl());
        FormInputValueHelper.reset();
    }

    @Test
    public void shouldSetValueIntoTextField() throws Exception {
        // Given
        String fieldName = "name";
        String fieldValue = "Some Name";
        formHandler = new FormHandler(browser, crawlRulesWithField(fieldName, setOf(fieldValue)));
        WebElement nameField = getFieldById(browser, fieldName);
        assertThat(value(nameField), is(""));
        // When
        formHandler.handleFormElements(formHandler.getFormInputs());
        // Then
        assertThat(value(nameField), is(fieldValue));
    }

    @Test
    public void shouldSetFirstValueIntoTextFieldEvenIfManyValuesSpecified() throws Exception {
        // Given
        String fieldName = "name";
        Set<InputValue> values = setOf("Some Name", "another value", "...");
        String fieldValue = values.iterator().next().getValue();
        formHandler = new FormHandler(browser, crawlRulesWithField(fieldName, values));
        WebElement nameField = getFieldById(browser, fieldName);
        assertThat(value(nameField), is(""));
        // When
        formHandler.handleFormElements(formHandler.getFormInputs());
        // Then
        assertThat(value(nameField), is(fieldValue));
    }

    @Test
    public void shouldClearTextFieldBeforeSettingValue() throws Exception {
        // Given
        String fieldName = "name";
        String fieldValue = "Some Name";
        formHandler = new FormHandler(browser, crawlRulesWithField(fieldName, setOf(fieldValue)));
        WebElement nameField = getFieldById(browser, fieldName);
        nameField.sendKeys("Not Empty");
        assertThat(value(nameField), is("Not Empty"));
        // When
        formHandler.handleFormElements(formHandler.getFormInputs());
        // Then
        assertThat(value(nameField), is(fieldValue));
    }

    @Test
    public void shouldNotSetValueToTextFieldIfNotFoundAndRandomIsDisabled() throws Exception {
        // Given
        String fieldName = "fieldNotFound";
        formHandler = new FormHandler(browser, crawlRulesWithField(fieldName, setOf("value")));
        WebElement nameField = getFieldById(browser, "name");
        assertThat(value(nameField), is(""));
        // When
        formHandler.handleFormElements(formHandler.getFormInputs());
        // Then
        assertThat(value(nameField), is(""));
    }

    @Test
    public void shouldSetRandomValueToTextFieldIfNotFoundAndRandomIsEnabled() throws Exception {
        // Given
        String fieldName = "fieldNotFound";
        formHandler = new FormHandler(browser, crawlRulesWithFieldAndRandom(fieldName, setOf("value")));
        WebElement nameField = getFieldById(browser, "name");
        assertThat(value(nameField), is(""));
        // When
        formHandler.handleFormElements(formHandler.getFormInputs());
        // Then
        assertThat(value(nameField), is(not("")));
    }

    private static String value(WebElement field) {
        return field.getAttribute("value");
    }

    private static CrawlRules crawlRulesWithField(String name, Set<InputValue> values) {
        return crawlRulesWithField(false, name, values);
    }

    private static CrawlRules crawlRulesWithField(boolean random, String name, Set<InputValue> values) {
        CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(DEMO_SITE_SERVER.getSiteUrl());
        InputSpecification inputSpec = new InputSpecification();
        Form form = new Form();
        form.inputField(InputType.TEXT, new Identification(How.id, name)).inputValues(values);
        inputSpec.setValuesInForm(form);
        builder.crawlRules()
                .setFormFillMode(random ? FormFillMode.RANDOM : FormFillMode.NORMAL)
                .setInputSpec(inputSpec);
        return builder.build().getCrawlRules();
    }

    private static CrawlRules crawlRulesWithFieldAndRandom(String name, Set<InputValue> values) {
        return crawlRulesWithField(true, name, values);
    }

    private static WebElement getFieldById(EmbeddedBrowser browser, String id) {
        Identification fieldId = new Identification(Identification.How.id, id);
        return browser.getWebElement(fieldId);
    }

    private static Set<InputValue> setOf(String... values) {
        Set<InputValue> set = new HashSet<>();
        if (values != null && values.length != 0) {
            for (String value : values) {
                set.add(new InputValue(value));
            }
        }
        return set;
    }
}
