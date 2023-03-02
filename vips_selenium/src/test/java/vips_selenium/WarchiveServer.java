package vips_selenium;

import com.google.common.base.Preconditions;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.netpreserve.jwarc.net.WarcServer;

public class WarchiveServer implements Runnable {

    private final String resource;

    private int port;
    ServerSocket socket;
    private URI demoSite;
    private WarcServer server;
    private boolean started;

    /**
     * @param directory The directory containing web archives
     */
    public WarchiveServer(String directory, int port) {
        resource = directory;
        this.port = port;
    }

    public void start() throws Exception {
        List<Path> warcs = Files.list(Paths.get(resource)).collect(Collectors.toList());
        socket = new ServerSocket(port);
        server = new WarcServer(socket, warcs);
        System.err.println("Listening on port " + port);
        this.started = true;
        this.demoSite = URI.create("http://localhost:" + port + "/");
        server.listen();
    }

    public URI getSiteUrl() {
        checkServerStarted();
        return demoSite;
    }

    public int getPort() {
        checkServerStarted();
        return port;
    }

    public synchronized void stop() {
        checkServerStarted();
        try {
            socket.close();
        } catch (Exception e) {
            throw new RuntimeException("Could not stop the server", e);
        }
    }

    private void checkServerStarted() {
        Preconditions.checkState(started, "Server not started");
    }

    @Override
    public void run() {
        try {
            start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
