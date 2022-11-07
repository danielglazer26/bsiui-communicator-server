package kopaczewski.glazer.bsiui.communicator.data.body;

import kopaczewski.glazer.bsiui.communicator.dto.ConversationWithMessagesDTO;
import lombok.Data;

import java.util.List;

@Data
public class UnreadMessagesResponseBody extends Body {
    private List<ConversationWithMessagesDTO> updatedConversations;

    public UnreadMessagesResponseBody(List<ConversationWithMessagesDTO> updatedConversations) {
        this.updatedConversations = updatedConversations;
    }
}
