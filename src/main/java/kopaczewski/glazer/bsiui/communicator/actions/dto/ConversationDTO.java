package kopaczewski.glazer.bsiui.communicator.actions.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConversationDTO {
    private final String name;
    private final List<String> names;

    public ConversationDTO(String name, List<String> names) {
        this.name = name;
        this.names = names;
    }
}
