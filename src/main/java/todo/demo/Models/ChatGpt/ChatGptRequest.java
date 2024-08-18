package todo.demo.Models.ChatGpt;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Data
@NoArgsConstructor
public class ChatGptRequest {
    private String model;
    private MessageGpt[] messages;

    public ChatGptRequest(String model, MessageGpt[] messages) {
        this.model = model;
        this.messages = messages;
    }

    public String getModel() {
        return model;
    }

    public MessageGpt[] getMessages() {
        return messages;
    }
}
