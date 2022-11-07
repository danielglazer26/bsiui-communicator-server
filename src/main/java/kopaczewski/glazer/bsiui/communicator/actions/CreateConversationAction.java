package kopaczewski.glazer.bsiui.communicator.actions;

import kopaczewski.glazer.bsiui.communicator.data.ResponseData;
import kopaczewski.glazer.bsiui.database.entities.Conversation;
import kopaczewski.glazer.bsiui.database.entities.Person;
import kopaczewski.glazer.bsiui.database.services.ConversationService;
import kopaczewski.glazer.bsiui.database.services.MessageService;
import kopaczewski.glazer.bsiui.database.services.PersonService;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static kopaczewski.glazer.bsiui.ConstStorage.KEY_NAME;
import static kopaczewski.glazer.bsiui.ConstStorage.KEY_USERS;

@Component("createConversation")
public class CreateConversationAction extends CommunicatorActions {

    public CreateConversationAction(PersonService personService, MessageService messageService, ConversationService conversationService) {
        super(personService, messageService, conversationService);
    }

    @Override
    public JSONObject runAction(String body, Long accountId) {
        JSONObject jsonObject = getBodyFromJson(body);

        String conversationName = jsonObject.getString(KEY_NAME);

        Optional<Conversation> optionalConversation = conversationService.getConversationsByName(conversationName);
        if (optionalConversation.isPresent()) {
            return new JSONObject(new ResponseData(HttpStatus.BAD_REQUEST, "This conversation already exists"));
        }

        List<String> users = jsonObject.getJSONArray(KEY_USERS).toList().stream().map(o -> (String) o).collect(Collectors.toList());
        List<Person> people = personService.getPeopleByLogins(users);

        Conversation conversation = conversationService.createNewConversation(conversationName, people);

        boolean status = !Objects.isNull(conversation);

        return new JSONObject(
                new ResponseData(
                        status ? HttpStatus.OK : HttpStatus.BAD_REQUEST,
                        status ? "Conversation was created" : "An error occurred while creating conversation"
                )
        );
    }


}

