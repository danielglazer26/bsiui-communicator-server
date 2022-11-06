package kopaczewski.glazer.bsiui.communicator.actions;

import kopaczewski.glazer.bsiui.communicator.actions.data.UsersListResponseBody;
import kopaczewski.glazer.bsiui.communicator.actions.data.ResponseDataWithBody;
import kopaczewski.glazer.bsiui.database.entities.Person;
import kopaczewski.glazer.bsiui.database.services.ConversationService;
import kopaczewski.glazer.bsiui.database.services.MessageService;
import kopaczewski.glazer.bsiui.database.services.PersonService;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component("listUsers")
public class UserListAction extends CommunicatorActions {

    public UserListAction(PersonService personService, MessageService messageService, ConversationService conversationService) {
        super(personService, messageService, conversationService);
    }

    @Override
    public JSONObject runAction(String body, Long accountId) {
        var personList = personService.getAllPeople();
        UsersListResponseBody responseBody = new UsersListResponseBody(personList.stream().map(Person::getLogin).collect(Collectors.toList()));
        var status = !responseBody.getUsers().isEmpty();
        return new JSONObject(
                new ResponseDataWithBody(
                        status ? HttpStatus.OK : HttpStatus.BAD_REQUEST,
                        status ? "List of users was send" : "There is no users",
                        responseBody
                ));
    }


}
