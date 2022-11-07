package kopaczewski.glazer.bsiui.communicator.actions.data;

import kopaczewski.glazer.bsiui.communicator.actions.dto.ConversationWithMessagesDTO;
import lombok.Data;

import java.util.List;

@Data
public class UnreadMessagesResponseBody extends Body {
    private List<ConversationWithMessagesDTO> updatedConversations;

    public UnreadMessagesResponseBody(List<ConversationWithMessagesDTO> updatedConversations) {
        this.updatedConversations = updatedConversations;
    }
}
