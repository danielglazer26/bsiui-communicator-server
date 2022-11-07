package kopaczewski.glazer.bsiui.communicator.actions.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConversationWithMessagesDTO {
    private String conversation;
    private List<MessageDTO> messages;
    public ConversationWithMessagesDTO(String conversation, List<MessageDTO> messages){
        this.conversation = conversation;
        this.messages = messages;
    }
}
