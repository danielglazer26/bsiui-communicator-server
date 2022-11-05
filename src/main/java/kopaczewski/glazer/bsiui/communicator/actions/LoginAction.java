package kopaczewski.glazer.bsiui.communicator.actions;

import kopaczewski.glazer.bsiui.communicator.actions.data.ResponseData;
import kopaczewski.glazer.bsiui.database.services.ConversationService;
import kopaczewski.glazer.bsiui.database.services.MessageService;
import kopaczewski.glazer.bsiui.database.services.PersonService;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import static kopaczewski.glazer.bsiui.ConstStorage.KEY_LOGIN;
import static kopaczewski.glazer.bsiui.ConstStorage.KEY_PASSWORD;

@Component("login")
public class LoginAction extends CommunicatorActions {

    public LoginAction(PersonService personService, MessageService messageService, ConversationService conversationService) {
        super(personService, messageService, conversationService);
    }

    @Override
    public JSONObject runAction(String body, Long accountId) {
        JSONObject jsonObject = getBodyFromJson(body);
        boolean authorizationStatus = personService.makeAuthorization(
                jsonObject.getString(KEY_LOGIN),
                jsonObject.getString(KEY_PASSWORD));

        return new JSONObject(
                new ResponseData(
                        authorizationStatus ? HttpStatus.OK : HttpStatus.BAD_REQUEST,
                        authorizationStatus ? "The authorization has been successful" : "The authorization has not been successful"
                )
        );
    }


}
