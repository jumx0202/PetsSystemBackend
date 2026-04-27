package ynu.pet.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiChatRequestDTO {
    private Long sessionId;
    private List<Message> messages = new ArrayList<>();

    @Data
    public static class Message {
        private String role;
        private String content;
    }
}
