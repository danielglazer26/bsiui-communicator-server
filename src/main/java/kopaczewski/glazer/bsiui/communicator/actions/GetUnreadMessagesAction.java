package kopaczewski.glazer.bsiui.communicator.actions;

import kopaczewski.glazer.bsiui.communicator.actions.data.ResponseData;
import kopaczewski.glazer.bsiui.database.entities.Message;
import kopaczewski.glazer.bsiui.database.entities.Person;
import kopaczewski.glazer.bsiui.database.services.ConversationService;
import kopaczewski.glazer.bsiui.database.services.MessageService;
import kopaczewski.glazer.bsiui.database.services.PersonService;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component("getMessages")
public class GetUnreadMessagesAction extends CommunicatorActions {

    public GetUnreadMessagesAction(PersonService personService, MessageService messageService, ConversationService conversationService) {
        super(personService, messageService, conversationService);
    }

    @Override
    public JSONObject runAction(String body, Long accountId) {

        Optional<Person> person = personService.getPersonById(accountId);
        if (person.isEmpty()) {
            return new JSONObject(new ResponseData(HttpStatus.BAD_REQUEST, "You have to be sign in"));
        }

        List<Message> messages = messageService.getAllUnreadMessages(person.get().getLogin());
        int numberOfRowUpdated = messageService.updateReadMessageStatus(
                person.get().getLogin(),
                messages.stream().map(Message::getMessageId).collect(Collectors.toList()));

        //TODO Response
        return new JSONObject();
    }


}
