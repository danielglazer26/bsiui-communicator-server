package kopaczewski.glazer.bsiui.communicator.actions.data;

import kopaczewski.glazer.bsiui.database.entities.Conversation;
import kopaczewski.glazer.bsiui.database.entities.Person;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ConversationDTO {
    private String name;
    private List<String> names;

    public ConversationDTO(String name, List<String> names) {
        this.name = name;
        this.names = names;
    }

    public static List<ConversationDTO> conversationParser(List<Conversation> conversations) {
        return conversations.stream()
                .map(e -> new ConversationDTO(e.getName(), e.getConversationParticipants().stream()
                        .map(Person::getLogin).collect(Collectors.toList()))).collect(Collectors.toList());
    }
}
