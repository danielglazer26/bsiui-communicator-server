package kopaczewski.glazer.bsiui.communicator.actions.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseData {
    protected int status;
    protected String response;

    public ResponseData(int status, String response) {
        this.status = status;
        this.response = response;
    }
}
