package kopaczewski.glazer.bsiui.communicator.actions;

import kopaczewski.glazer.bsiui.database.services.ConversationService;
import kopaczewski.glazer.bsiui.database.services.MessageService;
import kopaczewski.glazer.bsiui.database.services.PersonService;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component("listConversations")
public class ConversationListAction extends CommunicatorActions {

    public ConversationListAction(PersonService personService, MessageService messageService, ConversationService conversationService) {
        super(personService, messageService, conversationService);
    }

    @Override
    public JSONObject runAction(String body, Long accountId) {
        conversationService.getAllConversations();

        //TODO Response
        return new JSONObject();
    }


}
