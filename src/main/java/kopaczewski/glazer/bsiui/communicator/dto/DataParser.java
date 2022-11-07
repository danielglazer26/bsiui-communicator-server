package kopaczewski.glazer.bsiui.communicator.dto;

import kopaczewski.glazer.bsiui.database.entities.Conversation;
import kopaczewski.glazer.bsiui.database.entities.Message;
import kopaczewski.glazer.bsiui.database.entities.Person;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DataParser {
    public static List<ConversationDTO> parseConversations(List<Conversation> conversations) {
        return conversations.stream()
                .map(e -> new ConversationDTO(e.getName(), e.getConversationParticipants().stream()
                        .map(Person::getLogin).collect(Collectors.toList()))).collect(Collectors.toList());
    }

    public static List<MessageDTO> parseMessages(List<Message> messages) {
        return messages.stream().map(e -> new MessageDTO(e.getPerson().getLogin(), e.getDateTime(), e.getMessage())).collect(Collectors.toList());
    }

    public static List<ConversationWithMessagesDTO> parseConversationsWithMessages(Set<String> conversationNames, List<Message> messages) {
        return conversationNames.stream()
                .map(name -> new ConversationWithMessagesDTO(name, parseMessages(
                        messages.stream()
                                .filter(e -> e.getConversation().getName().equals(name))
                                .collect(Collectors.toList()))))
                .collect(Collectors.toList());
    }
}

