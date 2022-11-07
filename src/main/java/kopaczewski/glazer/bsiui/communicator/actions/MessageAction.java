package kopaczewski.glazer.bsiui.communicator.actions;

import kopaczewski.glazer.bsiui.communicator.data.ResponseData;
import kopaczewski.glazer.bsiui.database.entities.Conversation;
import kopaczewski.glazer.bsiui.database.entities.Message;
import kopaczewski.glazer.bsiui.database.entities.Person;
import kopaczewski.glazer.bsiui.database.services.ConversationService;
import kopaczewski.glazer.bsiui.database.services.MessageService;
import kopaczewski.glazer.bsiui.database.services.PersonService;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

import static kopaczewski.glazer.bsiui.ConstStorage.KEY_CONTENT;
import static kopaczewski.glazer.bsiui.ConstStorage.KEY_CONVERSATION;

@Component("message")
public class MessageAction extends CommunicatorActions {

    public MessageAction(PersonService personService, MessageService messageService, ConversationService conversationService) {
        super(personService, messageService, conversationService);
    }

    @Override
    public JSONObject runAction(String body, Long accountId) {
        JSONObject jsonObject = getBodyFromJson(body);

        Person person = personService.getSignInPersonById(accountId);

        Optional<Conversation> conversation = conversationService.getConversationsByName(jsonObject.getString(KEY_CONVERSATION));
        if (conversation.isEmpty()) {
            return new JSONObject(new ResponseData(HttpStatus.BAD_REQUEST, "Incorrect conversation name"));
        }

        Message message = messageService.createNewMessage(
                conversation.get(),
                person,
                jsonObject.getString(KEY_CONTENT)
        );

        boolean status = !Objects.isNull(message);

        return new JSONObject(
                new ResponseData(
                        status ? HttpStatus.OK : HttpStatus.BAD_REQUEST,
                        status ? "Message was processed" : "An error occurred while processing the message"
                )
        );
    }


}
