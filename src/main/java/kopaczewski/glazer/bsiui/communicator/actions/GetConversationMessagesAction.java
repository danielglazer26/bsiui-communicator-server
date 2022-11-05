package kopaczewski.glazer.bsiui.communicator.actions;

import kopaczewski.glazer.bsiui.database.entities.Message;
import kopaczewski.glazer.bsiui.database.services.ConversationService;
import kopaczewski.glazer.bsiui.database.services.MessageService;
import kopaczewski.glazer.bsiui.database.services.PersonService;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.List;

import static kopaczewski.glazer.bsiui.ConstStorage.KEY_CONVERSATION;

@Component("getConversationMessages")
public class GetConversationMessagesAction extends CommunicatorActions {

    public GetConversationMessagesAction(PersonService personService, MessageService messageService, ConversationService conversationService) {
        super(personService, messageService, conversationService);
    }

    @Override
    public JSONObject runAction(String body, Long accountId) {
        JSONObject jsonObject = getBodyFromJson(body);

        List<Message> messages = messageService.getAllMessagesForConversation(jsonObject.getString(KEY_CONVERSATION));


        //TODO Response
        return new JSONObject();
    }
}
