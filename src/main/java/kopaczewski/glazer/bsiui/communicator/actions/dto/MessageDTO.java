package kopaczewski.glazer.bsiui.communicator.actions.dto;

import lombok.Data;

@Data
public class MessageDTO {
    private final String author;
    private final String dateTime;
    private final String content;

    public MessageDTO(String author, String dateTime, String content) {
        this.author = author;
        this.dateTime = dateTime;
        this.content = content;
    }
}
