package com.example.addressbook.ai;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class OpenAITextService implements AITextService {
    private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
    private static final String ENDPOINT = "https://api.openai.com/v1/responses";
    private static final String MODEL = "gpt-4o-mini";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();
    private final Gson gson = new Gson();

    @Override
    public String generatePassage(String topic, int targetWords,
                                  boolean includeUpper, boolean includeNumbers,
                                  boolean includePunct, boolean includeSpecial) throws Exception {
        if (OPENAI_API_KEY == null || OPENAI_API_KEY.isBlank()) {
            throw new IllegalStateException("Missing OPENAI_API_KEY");
        }

        String instructions = buildInstructions(topic, targetWords, includeUpper, includeNumbers, includePunct, includeSpecial);

        JsonObject bodyJson = new JsonObject();
        bodyJson.addProperty("model", MODEL);
        bodyJson.addProperty("input", instructions);

        String body = gson.toJson(bodyJson);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + OPENAI_API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() / 100 != 2) {
            throw new RuntimeException("OpenAI error: " + resp.statusCode() + " " + resp.body());
        }

        JsonObject json = JsonParser.parseString(resp.body()).getAsJsonObject();
        if (json.has("output_text")) return json.get("output_text").getAsString().trim();
        return json.toString();
    }

    private String buildInstructions(String topic, int targetWords,
                                     boolean includeUpper, boolean includeNumbers,
                                     boolean includePunct, boolean includeSpecial) {
        StringBuilder sb = new StringBuilder();
        sb.append("Generate a typing practice passage only. No headings.\n");
        if (topic != null && !topic.isBlank()) sb.append("Topic: ").append(topic).append(".\n");
        else sb.append("Topic: everyday life / general knowledge.\n");
        sb.append("Length: about ").append(targetWords).append(" words.\n");
        sb.append("Style: clear, readable, coherent. Avoid rare/unicode characters.\n");
        if (includeUpper) sb.append("Include a mix of UPPERCASE words where natural.\n");
        if (includeNumbers) sb.append("Include some numerals naturally.\n");
        if (includePunct) sb.append("Use varied punctuation naturally.\n");
        if (includeSpecial) sb.append("Include occasional safe special characters like #, @, %, &, ().\n");
        sb.append("Return only the passage text (no quotes or labels).");
        return sb.toString();
    }
}
