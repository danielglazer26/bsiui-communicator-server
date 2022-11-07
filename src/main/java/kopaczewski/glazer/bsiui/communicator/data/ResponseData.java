package kopaczewski.glazer.bsiui.communicator.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
public class ResponseData {
    protected int status;
    protected String response;

    public ResponseData(HttpStatus status, String response) {
        this.status = status.value();
        this.response = response;
    }
}
