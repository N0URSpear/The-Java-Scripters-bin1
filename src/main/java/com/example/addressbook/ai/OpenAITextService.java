package com.example.addressbook.ai;

import com.google.gson.*;
import java.net.URI;
import java.net.http.*;
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

        System.out.println("[AI] OpenAI request → " + ENDPOINT + " model=" + MODEL);
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("[AI] OpenAI response status = " + resp.statusCode());
        if (resp.statusCode() / 100 != 2) {
            // Make sure we SEE why it failed (401/429/etc), so we know why fallback happens
            throw new RuntimeException("OpenAI error " + resp.statusCode() + ": " + resp.body());
        }
        JsonObject json = gson.fromJson(resp.body(), JsonObject.class);
        // The Responses API returns a helpful "output_text" — sometimes string, sometimes array.
        if (json.has("output_text")) {
            if (json.get("output_text").isJsonArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonElement el : json.getAsJsonArray("output_text")) {
                    sb.append(el.getAsString());
                }
                String out = sb.toString().trim();
                if (!out.isBlank()) return out;
            } else {
                String out = json.get("output_text").getAsString().trim();
                if (!out.isBlank()) return out;
            }
        }
        // Fallback parse if some variants put text in "output" → [... { content: [ { text: { value } } ] } ...]
        if (json.has("output") && json.get("output").isJsonArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonElement item : json.getAsJsonArray("output")) {
                JsonObject obj = item.getAsJsonObject();
                if (obj.has("content") && obj.get("content").isJsonArray()) {
                    for (JsonElement c : obj.getAsJsonArray("content")) {
                        JsonObject co = c.getAsJsonObject();
                        if (co.has("text")) {
                            JsonObject t = co.getAsJsonObject("text");
                            if (t.has("value")) sb.append(t.get("value").getAsString());
                        }
                    }
                }
            }
            String out = sb.toString().trim();
            if (!out.isBlank()) return out;
        }
        // Last resort: return raw body (shouldn't normally happen)
        return resp.body();
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
