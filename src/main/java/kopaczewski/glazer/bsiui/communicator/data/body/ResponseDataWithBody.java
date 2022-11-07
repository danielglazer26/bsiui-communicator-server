package kopaczewski.glazer.bsiui.communicator.data.body;

import kopaczewski.glazer.bsiui.communicator.data.ResponseData;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
public class ResponseDataWithBody extends ResponseData {

    protected Body body;

    public ResponseDataWithBody(HttpStatus status, String response, Body body) {
        super(status, response);
        this.body = body;
    }
}
