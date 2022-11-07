package kopaczewski.glazer.bsiui.communicator.actions.data;

import kopaczewski.glazer.bsiui.communicator.actions.dto.ConversationDTO;
import lombok.Data;

import java.util.List;

@Data
public class ConversationListResponseBody extends Body {
    private List<ConversationDTO> conversations;

    public ConversationListResponseBody(List<ConversationDTO> conversations) {
        this.conversations = conversations;
    }
}
