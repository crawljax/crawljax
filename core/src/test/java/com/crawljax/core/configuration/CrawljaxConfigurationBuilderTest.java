package com.crawljax.core.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import java.io.File;
import java.util.concurrent.TimeUnit;
import org.hamcrest.core.Is;
import org.junit.Test;

public class CrawljaxConfigurationBuilderTest {

  @Test(expected = IllegalArgumentException.class)
  public void negativeMaximumStatesIsNotAllowed() {
    testBuilder().setMaximumStates(-1).build();
  }

  private CrawljaxConfigurationBuilder testBuilder() {
    return CrawljaxConfiguration.builderFor("http://localhost");
  }

  @Test(expected = IllegalArgumentException.class)
  public void negativeDepthIsNotAllowed() {
    testBuilder().setMaximumDepth(-1).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void negativeRuntimeIsNotAllowed() {
    testBuilder().setMaximumRunTime(-1L, TimeUnit.SECONDS).build();
  }

  @Test
  public void noArgsBuilderWorksFine() {
    testBuilder().build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void ifOutputIsFileNotFolderReject() throws Exception {
    File file = File.createTempFile(getClass().getSimpleName(), "tmp");
    file.deleteOnExit();
    assertThat(file.exists(), is(true));
    testBuilder().setOutputDirectory(file).build();
  }

  @Test(expected = IllegalStateException.class)
  public void ifCannotCreateOutputFolderReject() {
    File file = new File("/this/should/not/be/writable");
    testBuilder().setOutputDirectory(file).build();
  }

  @Test
  public void whenSpecifyingBasicAuthTheUrlShouldBePreserved() {
    String url = "https://example.com/test/?a=b#anchor";
    CrawljaxConfiguration conf =
        CrawljaxConfiguration.builderFor(url).setBasicAuth("username", "password")
            .build();
    assertThat(conf.getBasicAuthUrl().toString(),
        Is.is("https://username:password@example.com/test/?a=b#anchor"));
  }

  @Test
  public void shouldReturnDefaultCrawlScopeIfNoneSet() throws Exception {
    CrawlScope crawlScope = testBuilder().build().getCrawlScope();
    assertThat(crawlScope, is(instanceOf(DefaultCrawlScope.class)));
    assertThat(((DefaultCrawlScope) crawlScope).getUrl().toString(), is("http://localhost"));
  }

  @Test
  public void shouldReturnCrawlScopeSet() throws Exception {
    CrawlScope crawlScope = url -> true;
    CrawljaxConfiguration conf = testBuilder().setCrawlScope(crawlScope).build();
    assertThat(conf.getCrawlScope(), is(crawlScope));
  }

}
