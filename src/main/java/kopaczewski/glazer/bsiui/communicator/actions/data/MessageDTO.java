package kopaczewski.glazer.bsiui.communicator.actions.data;

import kopaczewski.glazer.bsiui.database.entities.Message;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
public class MessageDTO {
    private final String author;
    private final String dateTime;
    private final String content;

    public MessageDTO(String author, String dateTime, String content) {
        this.author = author;
        this.dateTime = dateTime;
        this.content = content;
    }

    public void print(){
        System.out.println(author + " " + dateTime + " " + content);
    }

    public static List<MessageDTO> messageParser(List<Message> messages) {
        return messages.stream().map(e -> new MessageDTO(e.getPerson().getLogin(), e.getDateTime(), e.getMessage())).collect(Collectors.toList());
    }
}
