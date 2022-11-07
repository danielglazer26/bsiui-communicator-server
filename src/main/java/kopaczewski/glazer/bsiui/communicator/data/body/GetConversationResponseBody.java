package kopaczewski.glazer.bsiui.communicator.data.body;

import kopaczewski.glazer.bsiui.communicator.dto.MessageDTO;
import lombok.Data;

import java.util.List;

@Data
public class GetConversationResponseBody extends Body {
    private List<MessageDTO> messages;

    public GetConversationResponseBody(List<MessageDTO> messages) {
        this.messages = messages;
    }
}
