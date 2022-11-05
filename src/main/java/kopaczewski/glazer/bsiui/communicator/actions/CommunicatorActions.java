package kopaczewski.glazer.bsiui.communicator.actions;

import kopaczewski.glazer.bsiui.database.services.ConversationService;
import kopaczewski.glazer.bsiui.database.services.MessageService;
import kopaczewski.glazer.bsiui.database.services.PersonService;
import org.json.JSONObject;


public abstract class CommunicatorActions {
    protected final PersonService personService;
    protected final MessageService messageService;
    protected final ConversationService conversationService;

    public CommunicatorActions(PersonService personService, MessageService messageService, ConversationService conversationService) {
        this.personService = personService;
        this.messageService = messageService;
        this.conversationService = conversationService;
    }

    public abstract JSONObject runAction(String body, Long accountId);

    protected JSONObject getBodyFromJson(String body) {
        JSONObject jsonObject = new JSONObject(body);
        return jsonObject.getJSONObject("body");
    }
}
