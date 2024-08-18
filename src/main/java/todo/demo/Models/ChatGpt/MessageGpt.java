package todo.demo.Models.ChatGpt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;


public class MessageGpt {
    public MessageGpt(String role, String content) {
        this.role = role;
        this.content = content;
    }

    private String role;
    private String content;



    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }
}