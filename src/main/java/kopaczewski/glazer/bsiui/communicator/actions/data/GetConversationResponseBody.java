package kopaczewski.glazer.bsiui.communicator.actions.data;

import kopaczewski.glazer.bsiui.communicator.actions.dto.MessageDTO;
import lombok.Data;

import java.util.List;

@Data
public class GetConversationResponseBody extends Body {
    private List<MessageDTO> messages;

    public GetConversationResponseBody(List<MessageDTO> messages) {
        this.messages = messages;
    }
}
