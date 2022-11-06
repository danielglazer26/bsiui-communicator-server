package kopaczewski.glazer.bsiui.communicator.actions.data;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ResponseDataWithBody extends ResponseData{

    protected Body body;
    public ResponseDataWithBody(HttpStatus status, String response, Body body) {
        super(status, response);
        this.body = body;
    }
}
