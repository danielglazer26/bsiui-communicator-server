package kopaczewski.glazer.bsiui.communicator.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConversationDTO {
    private final String name;
    private final List<String> users;

    public ConversationDTO(String name, List<String> users) {
        this.name = name;
        this.users = users;
    }
}
