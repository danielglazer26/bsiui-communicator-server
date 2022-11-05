package kopaczewski.glazer.bsiui.communicator.actions.data;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ResponseData {
    protected int status;
    protected String response;

    public ResponseData(HttpStatus status, String response) {
        this.status = status.value();
        this.response = response;
    }
}
