package kopaczewski.glazer.bsiui.communicator.actions;

import kopaczewski.glazer.bsiui.communicator.data.ResponseData;
import kopaczewski.glazer.bsiui.database.entities.Person;
import kopaczewski.glazer.bsiui.database.services.ConversationService;
import kopaczewski.glazer.bsiui.database.services.MessageService;
import kopaczewski.glazer.bsiui.database.services.PersonService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static kopaczewski.glazer.bsiui.ConstStorage.*;

@Component("register")
@Qualifier(QUALIFIER_AUTHORIZATION)
public class RegisterAction extends CommunicatorActions {

    @Autowired
    public RegisterAction(PersonService personService, MessageService messageService, ConversationService conversationService) {
        super(personService, messageService, conversationService);
    }

    @Override
    public JSONObject runAction(String body, Long accountId) {
        JSONObject jsonObject = getBodyFromJson(body);

        Person person = personService.createNewPerson(
                jsonObject.getString(KEY_LOGIN),
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
