package com.crawljax.plugins.testplugin;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.crawljax.core.CrawlerContext;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.HostInterface;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.PreCrawlingPlugin;
import com.crawljax.core.state.StateVertex;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Map;

public class TestPlugin implements OnNewStatePlugin,
    PreCrawlingPlugin {

  private HostInterface hostInterface;

  public TestPlugin(HostInterface hostInterface) {
    this.hostInterface = hostInterface;
  }

  @Override
  public void onNewState(CrawlerContext context, StateVertex newState) {
    try {
      String dom = context.getBrowser().getStrippedDom();
      File file = new File(hostInterface.getOutputDirectory(),
          context.getCurrentState().getName() + ".html");

      Writer fw = Files.newBufferedWriter(file.toPath(), UTF_8);
      fw.write(dom);
      fw.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void preCrawling(CrawljaxConfiguration config) throws RuntimeException {
    try {
      File file = new File(hostInterface.getOutputDirectory(), "parameters.txt");
      Writer fw = Files.newBufferedWriter(file.toPath(), UTF_8);
      for (Map.Entry<String, String> parameter : hostInterface.getParameters().entrySet()) {
        fw.write(parameter.getKey() + ": " + parameter.getValue() + System.getProperty(
            "line.separator"));
      }
      fw.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
