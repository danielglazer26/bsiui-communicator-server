package kopaczewski.glazer.bsiui.database.services;

import kopaczewski.glazer.bsiui.database.entities.Conversation;
import kopaczewski.glazer.bsiui.database.entities.Message;
import kopaczewski.glazer.bsiui.database.entities.Person;
import kopaczewski.glazer.bsiui.database.repositories.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;

    @Autowired
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message createNewMessage(Conversation conversation, Person person, String message, String dateTime) {
        String whoDoesntGetMessage = conversation.getConversationParticipants().stream()
                .filter(conPer -> !Objects.equals(conPer.getPersonId(), person.getPersonId()))
                .map(Person::getLogin)
                .collect(Collectors.joining(","));
        return messageRepository.save(new Message(0L, conversation, person, message, dateTime, whoDoesntGetMessage));
    }

    public List<Message> getAllMessagesForConversation(String conversationName, Long personId) {
        return messageRepository.findAllByConversation_NameAndPerson_personId(conversationName, personId);
    }

    public List<Message> getAllDoesntReadMessages(String login) {
        return messageRepository.findAllByPerson_personIdAndWhoDoesntGetMessageContains(login);
    }

    public int updateReadMessageStatus(String login, List<Long> messageIds) {
        return messageRepository.updateWhoDoesntGetMessageByMessageId(login, messageIds);
    }
}
