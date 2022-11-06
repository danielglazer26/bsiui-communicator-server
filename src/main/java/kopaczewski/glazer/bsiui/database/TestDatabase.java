package kopaczewski.glazer.bsiui.database;

import kopaczewski.glazer.bsiui.database.entities.Conversation;
import kopaczewski.glazer.bsiui.database.entities.Person;
import kopaczewski.glazer.bsiui.database.services.ConversationService;
import kopaczewski.glazer.bsiui.database.services.MessageService;
import kopaczewski.glazer.bsiui.database.services.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TestDatabase {

    private final ConversationService conversationService;
    private final PersonService personService;
    private final MessageService messageService;

    @Autowired
    public TestDatabase(ConversationService conversationService, PersonService personService, MessageService messageService) {
        this.conversationService = conversationService;
        this.personService = personService;
        this.messageService = messageService;
    }

    @PostConstruct
    public void start() {
        Person person = personService.createNewPerson("login", "haslo");
        Person person2 = personService.createNewPerson("login2", "haslo2");
        Conversation conversation = conversationService.createNewConversation("to jest konwersacja", List.of(person, person2));
        Conversation conversation2 = conversationService.createNewConversation("to jest konwersacja 2", List.of(person,
                person2));
        person = personService.getPersonById(1L).orElse(person);
        messageService.createNewMessage(conversation, person, "To jest message1", "213213");
        messageService.createNewMessage(conversation2, person, "To jest message2", "213213");
        messageService.createNewMessage(conversation2, person, "To jest message3", "213213");
        messageService.createNewMessage(conversation2, person, "To jest message4", "213213");
        messageService.createNewMessage(conversation, person, "To jest message5", "213213");
        messageService.createNewMessage(conversation, person, "To jest message6", "213213");
        messageService.getAllDoesntReadMessages(person2.getLogin()).forEach(message -> System.out.println(message.getConversation().getName() + " " + message.getMessage()));
    }
}
