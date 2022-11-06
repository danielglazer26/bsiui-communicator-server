package kopaczewski.glazer.bsiui.communicator.actions;

import kopaczewski.glazer.bsiui.database.entities.Message;
import kopaczewski.glazer.bsiui.database.entities.Person;
import kopaczewski.glazer.bsiui.database.services.ConversationService;
import kopaczewski.glazer.bsiui.database.services.MessageService;
import kopaczewski.glazer.bsiui.database.services.PersonService;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("getMessages")
public class GetUnreadMessagesAction extends CommunicatorActions {

    public GetUnreadMessagesAction(PersonService personService, MessageService messageService, ConversationService conversationService) {
        super(personService, messageService, conversationService);
    }

    @Override
    public JSONObject runAction(String body, Long accountId) {
        Person person = personService.getSignInPersonById(accountId);

        List<Message> messages = messageService.getAllUnreadMessages(person.getLogin());
        int numberOfRowUpdated = messageService.updateReadMessageStatus(
                person.getLogin(),
                messages);

        // TODO Response
        return new JSONObject();
    }


}
