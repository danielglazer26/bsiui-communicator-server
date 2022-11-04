package kopaczewski.glazer.bsiui.communicator.actions.data;

import java.util.List;

public class UsersListResponseData extends ResponseData {


    List<String> users;

    public UsersListResponseData(int status, String response) {
        super(status, response);
    }
}
