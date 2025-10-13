package typingNinja.ai;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Uses a local Ollama server (free) at http://localhost:11434/api/generate */
public class OllamaTextService implements AITextService {
    private static final String BASE =
            System.getenv("OLLAMA_BASE_URL") != null ? System.getenv("OLLAMA_BASE_URL") : "http://localhost:11434";
    private static final String MODEL =
            System.getenv("OLLAMA_MODEL") != null ? System.getenv("OLLAMA_MODEL") : "gemma3:1b"; // small, fast

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final Gson gson = new Gson();

    @Override
    public String generatePassage(String topic, int targetWords,
                                  boolean includeUpper, boolean includeNumbers,
                                  boolean includePunct, boolean includeSpecial) throws Exception {
        String instructions = buildInstructions(topic, targetWords, includeUpper, includeNumbers, includePunct, includeSpecial);

        JsonObject body = new JsonObject();
        body.addProperty("model", MODEL);
        body.addProperty("prompt", instructions);
        body.addProperty("stream", false);
        // optional generation options
        JsonObject options = new JsonObject();
        options.addProperty("temperature", 0.7);
        options.addProperty("num_predict", Math.max(120, targetWords * 2)); // rough budget
        body.add("options", options);

        String url = BASE + "/api/generate";
        System.out.println("[AI] Ollama request → " + url + " model=" + MODEL);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(40))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("[AI] Ollama response status = " + resp.statusCode());
        if (resp.statusCode() / 100 != 2) {
            throw new RuntimeException("Ollama error " + resp.statusCode() + ": " + resp.body());
        }

        JsonObject json = gson.fromJson(resp.body(), JsonObject.class);
        if (json.has("response")) {
            String out = json.get("response").getAsString().trim();
            if (!out.isBlank()) {
                return PassageConstraintEnforcer.enforce(
                        out, includeUpper, includeNumbers, includePunct, includeSpecial);
            }
        }
        return PassageConstraintEnforcer.enforce(
                resp.body(), includeUpper, includeNumbers, includePunct, includeSpecial);
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
        sb.append("Write as a concise instructional overview that teaches the reader about the topic while they type.\n");
        sb.append("Explain key concepts, practical applications, and interesting facts to keep the passage educational.\n");
        if (includeUpper) {
            sb.append("Include a mix of UPPERCASE words where natural.\n");
        }
        else {
            sb.append("Do NOT include ANY uppercase characters in the passage. Every letter must be lowercase.\n");
        }
        if (includeNumbers) {
            sb.append("Include some numerals naturally.\n");
        }
        else {
            sb.append("Do NOT include any number digits (0-9) anywhere in the passage.\n");
        }
        if (includePunct) {
            sb.append("Use varied punctuation naturally.\n");
        }
        else {
            sb.append("Do NOT use any punctuation characters (.,;:!?\"'-).\n");
        }
        if (includeSpecial) {
            sb.append("Include occasional special characters like #, @, %, &, (), +, = when it feels natural.\n");
        }
        else {
            sb.append("Do NOT use any special characters such as #, @, %, &, (), +, =, /, _.\n");
        }
        sb.append("Return only the passage text (no quotes or labels).");
        return sb.toString();
    }
}
