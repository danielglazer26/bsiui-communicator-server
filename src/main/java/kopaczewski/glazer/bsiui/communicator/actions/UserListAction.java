package kopaczewski.glazer.bsiui.communicator.actions;

import kopaczewski.glazer.bsiui.database.services.ConversationService;
import kopaczewski.glazer.bsiui.database.services.MessageService;
import kopaczewski.glazer.bsiui.database.services.PersonService;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component("listUsers")
public class UserListAction extends CommunicatorActions {

    public UserListAction(PersonService personService, MessageService messageService, ConversationService conversationService) {
        super(personService, messageService, conversationService);
    }

    @Override
    public JSONObject runAction(String body, Long accountId) {
        personService.getAllPeople();

        //TODO Response
        return new JSONObject();
    }


}
