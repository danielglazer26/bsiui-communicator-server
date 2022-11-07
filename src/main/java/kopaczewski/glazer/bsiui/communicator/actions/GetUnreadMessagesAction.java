package kopaczewski.glazer.bsiui.communicator.actions;

import kopaczewski.glazer.bsiui.communicator.data.body.ResponseDataWithBody;
import kopaczewski.glazer.bsiui.communicator.data.body.UnreadMessagesResponseBody;
import kopaczewski.glazer.bsiui.database.entities.Message;
import kopaczewski.glazer.bsiui.database.entities.Person;
import kopaczewski.glazer.bsiui.database.services.ConversationService;
import kopaczewski.glazer.bsiui.database.services.MessageService;
import kopaczewski.glazer.bsiui.database.services.PersonService;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static kopaczewski.glazer.bsiui.communicator.dto.DataParser.parseConversationsWithMessages;

@Component("getMessages")
public class GetUnreadMessagesAction extends CommunicatorActions {

    public GetUnreadMessagesAction(PersonService personService, MessageService messageService, ConversationService conversationService) {
        super(personService, messageService, conversationService);
    }

    @Override
    public JSONObject runAction(String body, Long accountId) {
        Person person = personService.getSignInPersonById(accountId);
        List<Message> messages = messageService.getAllUnreadMessages(person.getLogin());
        int numberOfRowUpdated = messageService.updateReadMessageStatus(person.getLogin(), messages);
        var conversationNames = messages.stream().map(e -> e.getConversation().getName()).collect(Collectors.toSet());
        return new JSONObject(
                new ResponseDataWithBody(
                        HttpStatus.OK,
                        "List of conversations was send",
                        new UnreadMessagesResponseBody(parseConversationsWithMessages(conversationNames, messages))
                )
        );
    }
}
