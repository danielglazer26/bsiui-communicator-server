package kopaczewski.glazer.bsiui.communicator.actions.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetConversationResponseBody extends Body{
    private List<MessageDTO> messages;

    public GetConversationResponseBody(List<MessageDTO> messages){
        this.messages = messages;
    }
}
