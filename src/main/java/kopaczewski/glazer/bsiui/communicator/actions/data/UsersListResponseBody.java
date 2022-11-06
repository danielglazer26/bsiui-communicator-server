package kopaczewski.glazer.bsiui.communicator.actions.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UsersListResponseBody extends Body {
    private List<String> users;
    public UsersListResponseBody(List<String> users){
        this.users = users;
    }
}
