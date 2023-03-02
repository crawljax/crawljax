package com.crawljax.test;

import org.junit.Assert;
import org.junit.Test;

public class WarchiveServerTest {

    WarchiveServer server;

    @Test
    public void testPort() {
        int port = 8088;
        server = new WarchiveServer("src/test/resources/warchives/petclinic", port);
        new Thread(server).start();

        int timeout = 0;
        while (!server.isStarted() && timeout < 10000) {
            try {
                Thread.sleep(100);
                timeout += 100;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        Assert.assertTrue(
                "server not started properly",
                server.getSiteUrl().toString().equalsIgnoreCase("http://localhost:" + port + "/"));
        server.stop();
    }
}
