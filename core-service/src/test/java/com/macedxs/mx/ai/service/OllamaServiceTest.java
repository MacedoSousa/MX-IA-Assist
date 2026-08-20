package com.macedxs.mx.ai.service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OllamaServiceTest {

    @Test
    void shouldStreamOllamaNdjsonChunksAndReturnTheAccumulatedAnswer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/generate", exchange -> {
            byte[] first = "{\"model\":\"qwen3\",\"response\":\"Oi \" ,\"done\":false}\n".getBytes(StandardCharsets.UTF_8);
            byte[] second = "{\"model\":\"qwen3\",\"response\":\"mundo\",\"done\":false}\n".getBytes(StandardCharsets.UTF_8);
            byte[] done = "{\"model\":\"qwen3\",\"response\":\"\",\"done\":true}\n".getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().add("Content-Type", "application/x-ndjson");
            exchange.sendResponseHeaders(200, 0);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(first);
                os.flush();
                os.write(second);
                os.flush();
                os.write(done);
                os.flush();
            }
        });
        server.start();

        try {
            List<String> chunks = new ArrayList<>();
            OllamaService service = new OllamaService("http://localhost:" + server.getAddress().getPort());

            String reply = service.streamText("hello", chunks::add);

            assertThat(chunks).containsExactly("Oi ", "mundo");
            assertThat(reply).isEqualTo("Oi mundo");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldCallOllamaAndReturnPlainTextResponse() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/generate", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String body = "{\"model\":\"qwen3\",\"prompt\":\"hello\",\"stream\":false}";
                byte[] response = "{\"model\":\"qwen3\",\"response\":\"Resposta do MX\",\"done\":true}".getBytes(StandardCharsets.UTF_8);

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
                exchange.close();
            }
        });
        server.start();

        try {
            String url = "http://localhost:" + server.getAddress().getPort();
            OllamaService service = new OllamaService(url);

            String reply = service.generateText("hello");

            assertThat(reply).isEqualTo("Resposta do MX");
        } finally {
            server.stop(0);
        }
    }
}
