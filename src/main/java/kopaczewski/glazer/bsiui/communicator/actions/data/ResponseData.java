package kopaczewski.glazer.bsiui.communicator.actions.data;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ResponseData {
    protected int status;
    protected String response;

    public ResponseData(HttpStatus status, String response) {
        this.status = status.value();
        this.response = response;
    }
}
