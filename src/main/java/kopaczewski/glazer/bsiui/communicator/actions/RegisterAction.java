package kopaczewski.glazer.bsiui.communicator.actions;

import kopaczewski.glazer.bsiui.communicator.actions.data.ResponseData;
import kopaczewski.glazer.bsiui.database.entities.Person;
import kopaczewski.glazer.bsiui.database.services.ConversationService;
import kopaczewski.glazer.bsiui.database.services.MessageService;
import kopaczewski.glazer.bsiui.database.services.PersonService;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

import static kopaczewski.glazer.bsiui.ConstStorage.KEY_LOGIN;
import static kopaczewski.glazer.bsiui.ConstStorage.KEY_PASSWORD;

@Component("register")
public class RegisterAction extends CommunicatorActions {

    public RegisterAction(PersonService personService, MessageService messageService, ConversationService conversationService) {
        super(personService, messageService, conversationService);
    }

    @Override
    public JSONObject runAction(String body, Long accountId) {
        JSONObject jsonObject = getBodyFromJson(body);

        String login = jsonObject.getString(KEY_LOGIN);

        Optional<Person> optionalPerson = personService.getPersonByLogin(login);
        if (optionalPerson.isPresent()) {
            return new JSONObject(HttpStatus.BAD_REQUEST, "This login already exists");
        }

        Person person = personService.createNewPerson(
                login,
                jsonObject.getString(KEY_PASSWORD));

        boolean status = !Objects.isNull(person);

        return new JSONObject(
                new ResponseData(
                        status ? HttpStatus.OK : HttpStatus.BAD_REQUEST,
                        status ? "Account was created" : "Login is occupied"
                )
        );
    }


}
