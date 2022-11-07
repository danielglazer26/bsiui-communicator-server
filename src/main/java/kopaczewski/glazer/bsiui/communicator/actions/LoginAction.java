package kopaczewski.glazer.bsiui.communicator.actions;

import kopaczewski.glazer.bsiui.communicator.data.ResponseData;
import kopaczewski.glazer.bsiui.database.services.ConversationService;
import kopaczewski.glazer.bsiui.database.services.MessageService;
import kopaczewski.glazer.bsiui.database.services.PersonService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import static kopaczewski.glazer.bsiui.ConstStorage.*;

@Component("login")
@Qualifier(QUALIFIER_AUTHORIZATION)
public class LoginAction extends CommunicatorActions {

    private Long accountId;

    public LoginAction(PersonService personService, MessageService messageService, ConversationService conversationService) {
        super(personService, messageService, conversationService);
    }

    @Override
    public JSONObject runAction(String body, Long accountId) {
        JSONObject jsonObject = getBodyFromJson(body);
        String login = jsonObject.getString(KEY_LOGIN);
        boolean authorizationStatus = personService.makeAuthorization(
                login,
                jsonObject.getString(KEY_PASSWORD));

        if (authorizationStatus) {
            personService.getPersonByLogin(login).ifPresent(person -> setAccountId(person.getPersonId()));
        }

        return new JSONObject(
                new ResponseData(
                        authorizationStatus ? HttpStatus.OK : HttpStatus.BAD_REQUEST,
                        authorizationStatus ? "The authorization has been successful" : "The authorization has not been successful"
                )
        );
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getAccountId() {
        return accountId;
    }
}
