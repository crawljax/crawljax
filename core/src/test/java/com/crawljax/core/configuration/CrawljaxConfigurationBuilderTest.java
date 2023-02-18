package com.crawljax.core.configuration;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import java.io.File;
import java.util.concurrent.TimeUnit;
import org.hamcrest.core.Is;
import org.junit.Ignore;
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

}
