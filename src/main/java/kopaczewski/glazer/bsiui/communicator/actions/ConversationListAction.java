package kopaczewski.glazer.bsiui.communicator.actions;

import kopaczewski.glazer.bsiui.communicator.actions.data.ConversationListResponseBody;
import kopaczewski.glazer.bsiui.communicator.actions.data.GetConversationResponseBody;
import kopaczewski.glazer.bsiui.communicator.actions.data.ResponseData;
import kopaczewski.glazer.bsiui.communicator.actions.data.ResponseDataWithBody;
import kopaczewski.glazer.bsiui.database.entities.Message;
import kopaczewski.glazer.bsiui.database.services.ConversationService;
import kopaczewski.glazer.bsiui.database.services.MessageService;
import kopaczewski.glazer.bsiui.database.services.PersonService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

import static kopaczewski.glazer.bsiui.ConstStorage.KEY_CONVERSATION;
import static kopaczewski.glazer.bsiui.communicator.actions.data.ConversationDTO.conversationParser;
import static kopaczewski.glazer.bsiui.communicator.actions.data.MessageDTO.messageParser;

@Component("listConversations")
public class ConversationListAction extends CommunicatorActions {

    public ConversationListAction(PersonService personService, MessageService messageService, ConversationService conversationService) {
        super(personService, messageService, conversationService);
    }

    @Override
    public JSONObject runAction(String body, Long accountId) {
        var conversations = conversationService.getAllConversations();

        return new JSONObject(
                new ResponseDataWithBody(
                        HttpStatus.OK,
                        "List of conversations send",
                        new ConversationListResponseBody(conversationParser(conversations))
                )
        );
    }


}
