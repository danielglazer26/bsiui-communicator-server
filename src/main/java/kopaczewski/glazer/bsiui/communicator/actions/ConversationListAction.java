package kopaczewski.glazer.bsiui.communicator.actions;

import kopaczewski.glazer.bsiui.communicator.data.body.ConversationListResponseBody;
import kopaczewski.glazer.bsiui.communicator.data.body.ResponseDataWithBody;
import kopaczewski.glazer.bsiui.database.services.ConversationService;
import kopaczewski.glazer.bsiui.database.services.MessageService;
import kopaczewski.glazer.bsiui.database.services.PersonService;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import static kopaczewski.glazer.bsiui.communicator.dto.DataParser.parseConversations;

@Component("listConversations")
public class ConversationListAction extends CommunicatorActions {

    public ConversationListAction(PersonService personService, MessageService messageService, ConversationService conversationService) {
        super(personService, messageService, conversationService);
    }

    @Override
    public JSONObject runAction(String body, Long accountId) {
        var conversations = conversationService.getAllConversations();

        return new JSONObject(
                new ResponseDataWithBody(
                        HttpStatus.OK,
                        "List of conversations send",
                        new ConversationListResponseBody(parseConversations(conversations))
                )
        );
    }

}
