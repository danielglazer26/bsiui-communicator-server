package kopaczewski.glazer.bsiui.communicator.data.body;

import lombok.Data;

import java.util.List;

@Data
public class UsersListResponseBody extends Body {
    private List<String> users;

    public UsersListResponseBody(List<String> users) {
        this.users = users;
    }
}
