package todo.demo.Models.ChatGpt;


import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class Choice {
    private MessageGpt message;

    public MessageGpt getMessage() {
        return message;
    }
}
