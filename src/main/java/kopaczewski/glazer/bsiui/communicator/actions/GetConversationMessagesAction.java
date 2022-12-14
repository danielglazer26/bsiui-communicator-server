package kopaczewski.glazer.bsiui.communicator.actions;

import kopaczewski.glazer.bsiui.communicator.data.ResponseData;
import kopaczewski.glazer.bsiui.communicator.data.body.GetConversationResponseBody;
import kopaczewski.glazer.bsiui.communicator.data.body.ResponseDataWithBody;
import kopaczewski.glazer.bsiui.database.entities.Message;
import kopaczewski.glazer.bsiui.database.services.ConversationService;
import kopaczewski.glazer.bsiui.database.services.MessageService;
import kopaczewski.glazer.bsiui.database.services.PersonService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

import static kopaczewski.glazer.bsiui.ConstStorage.KEY_CONVERSATION;
import static kopaczewski.glazer.bsiui.communicator.dto.DataParser.parseMessages;

@Component("getConversationMessages")
public class GetConversationMessagesAction extends CommunicatorActions {

    public GetConversationMessagesAction(PersonService personService, MessageService messageService, ConversationService conversationService) {
        super(personService, messageService, conversationService);
    }

    @Override
    public JSONObject runAction(String body, Long accountId) {
        JSONObject jsonObject = getBodyFromJson(body);

        try {
            String conversationName = jsonObject.getString(KEY_CONVERSATION);
            List<Message> messages = messageService.getAllMessagesForConversation(conversationName);
            return new JSONObject(
                    new ResponseDataWithBody(
                            HttpStatus.OK,
                            "List of messages was send",
                            new GetConversationResponseBody(parseMessages(messages))
                    )
            );
        } catch (JSONException e) {
            return new JSONObject(
                    new ResponseData(
                            HttpStatus.BAD_REQUEST,
                            "There is no such conversation."
                    )
            );
        }
    }
}
