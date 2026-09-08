package com.skytrace;

import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/** Optional local proxy verification using the exact deployed server configuration. */
final class NginxProxy implements AutoCloseable {
    private Process process;
    private String executable;
    private Path prefix;
    final String url;

    NginxProxy(int appPort) throws Exception {
        executable = System.getenv("NGINX_BINARY");
        if (executable == null || executable.isBlank()) {
            url = "http://127.0.0.1:" + appPort;
            return;
        }
        int proxyPort;
        try (ServerSocket socket = new ServerSocket(0)) { proxyPort = socket.getLocalPort(); }
        url = "http://127.0.0.1:" + proxyPort;
        prefix = Files.createTempDirectory(Path.of("target"), "nginx-e2e-").toAbsolutePath();
        Files.createDirectories(prefix.resolve("logs"));
        Files.createDirectories(prefix.resolve("temp"));
        String server = Files.readString(Path.of("../deploy/nginx.conf"))
                .replace("listen 80;", "listen 127.0.0.1:" + proxyPort + ";")
                .replace("http://app:8080", "http://127.0.0.1:" + appPort);
        Files.writeString(prefix.resolve("nginx.conf"), "daemon off;\nevents {}\nhttp {\n" + server + "\n}\n");
        process = new ProcessBuilder(executable, "-p", prefix.toString() + "/", "-c", "nginx.conf")
                .redirectErrorStream(true).redirectOutput(prefix.resolve("process.log").toFile()).start();
        try (HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build()) {
            for (int attempt = 0; attempt < 50; attempt++) {
                try {
                    var response = client.send(HttpRequest.newBuilder(URI.create(url + "/actuator/health"))
                            .timeout(Duration.ofSeconds(1)).build(), HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200) return;
                } catch (java.io.IOException ignored) { }
                Thread.sleep(100);
            }
        } catch (Exception failure) { close(); throw failure; }
        close();
        throw new IllegalStateException("Nginx did not become healthy: " + prefix);
    }

    @Override public void close() throws Exception {
        if (process == null) return;
        new ProcessBuilder(executable, "-p", prefix.toString() + "/", "-c", "nginx.conf", "-s", "quit")
                .redirectErrorStream(true).redirectOutput(prefix.resolve("shutdown.log").toFile())
                .start().waitFor(5, TimeUnit.SECONDS);
        if (!process.waitFor(5, TimeUnit.SECONDS)) process.destroy();
    }
}
