package com.crawljax.test;

import org.junit.Assert;
import org.junit.Test;

public class WarchiveServerTest {

  WarchiveServer server;


  @Test
  public void testPort() {
    server = new WarchiveServer("src/test/resources/warchives/petclinic", 8080);
    new Thread(server).start();
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    Assert.assertTrue("server not started properly",
        server.getSiteUrl().toString().equalsIgnoreCase("http://localhost:8080/"));
    server.stop();
  }
}
