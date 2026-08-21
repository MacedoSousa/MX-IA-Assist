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
import java.time.Duration;
import java.util.List;

import com.macedxs.mx.conversation.application.port.ModelGateway.ModelImage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    void shouldSendMultimodalPayloadWithPerformanceOptions() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/generate", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            assertThat(body).contains("\"model\":\"llava:7b\"");
            assertThat(body).contains("\"keep_alive\":\"10m\"");
            assertThat(body).contains("\"num_ctx\":8192");
            assertThat(body).contains("\"num_thread\":4");
            assertThat(body).contains("\"images\":[\"aGVsbG8=\"]");
            byte[] response = "{\"response\":\"imagem analisada\"}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });
        server.start();

        try {
            OllamaService service = new OllamaService(
                    "http://localhost:" + server.getAddress().getPort(),
                    Duration.ofSeconds(5),
                    "qwen3:8b",
                    "10m",
                    8192,
                    4
            );
            String reply = service.generateText(
                    "descreva",
                    List.of(new ModelImage("image/png", "aGVsbG8=")),
                    "llava:7b"
            );
            assertThat(reply).isEqualTo("imagem analisada");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldFailWhenOllamaDoesNotRespondBeforeTheConfiguredTimeout() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/generate", exchange -> {
            try {
                Thread.sleep(300);
                byte[] response = "{\"response\":\"late\"}".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        });
        server.start();

        try {
            String url = "http://localhost:" + server.getAddress().getPort();

            assertThatThrownBy(() -> new OllamaService(url, Duration.ofMillis(50)).generateText("hello"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Failed to reach Ollama");
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
