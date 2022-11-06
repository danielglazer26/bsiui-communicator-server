package kopaczewski.glazer.bsiui.communicator.actions.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ConversationListResponseBody extends Body{
    private List<ConversationDTO> conversations;
    public ConversationListResponseBody(List<ConversationDTO> conversations) {
        this.conversations = conversations;
    }
}
