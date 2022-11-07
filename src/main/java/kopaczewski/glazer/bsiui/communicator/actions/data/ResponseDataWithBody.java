package kopaczewski.glazer.bsiui.communicator.actions.data;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ResponseDataWithBody extends ResponseData {

    protected Body body;

    public ResponseDataWithBody(HttpStatus status, String response, Body body) {
        super(status, response);
        this.body = body;
    }
}
