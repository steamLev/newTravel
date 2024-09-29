package todo.demo.Services;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import todo.demo.Models.ChatGpt.ChatGptRequest;
import todo.demo.Models.ChatGpt.ChatGptResponse;
import todo.demo.Models.ChatGpt.MessageGpt;
import todo.demo.Models.Cred;
import todo.demo.Repositories.CredRepository;

import java.io.IOException;
import java.util.List;

@Service
@NoArgsConstructor
public class OpenAiService {

    @Autowired
    CredRepository credRepository;


    public Mono<String> getChatGptResponse(String prompt) {
        ChatGptRequest request = new ChatGptRequest("gpt-4o-mini", new MessageGpt[]{new MessageGpt("user", prompt)});

        return WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", credRepository.findAll().getFirst().getCred())
                .defaultHeader("Content-Type", "application/json")
                .build().post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatGptResponse.class)
                .map(response -> response.getChoices()[0].getMessage().getContent())
                .doOnError(e -> System.err.println("Error: " + e.getMessage()));
    }
}
