package kopaczewski.glazer.bsiui.server;

import com.google.common.hash.Hashing;
import kopaczewski.glazer.bsiui.communicator.actions.CommunicatorActions;
import kopaczewski.glazer.bsiui.communicator.actions.LoginAction;
import kopaczewski.glazer.bsiui.communicator.actions.data.ResponseData;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import static kopaczewski.glazer.bsiui.ConstStorage.KEY_LOGIN;
import static kopaczewski.glazer.bsiui.ConstStorage.QUALIFIER_AUTHORIZATION;

@Component
public class Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    public static final String KEY_ACTION = "action";
    public static final long NotSignInUser = -1L;

    private final Set<Integer> lockedSocketPorts = new HashSet<>();

    private ServerSocket serverSocket;

    private Map<String, CommunicatorActions> communicatorOptions;
    private Map<String, CommunicatorActions> authorizationOptions;

    @Value("${socket.port}")
    private int port;


    @PostConstruct
    public void start() {
        try {
            serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            LOGGER.error("CAN'T START SERVER", e);
        }

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                receiveClientGreetings(clientSocket);
                clientSocket.close();
            } catch (IOException e) {
                LOGGER.error("Server stop working", e);
            }
        }
    // TODO
    //        try {
    //            serverSocket.close();
    //        } catch (IOException ignored) {
    //        }
    }

    private void receiveClientGreetings(Socket clientSocket) throws IOException {
        LOGGER.info("NEW CLIENT TRY TO CONNECT");
        clientSocket.setSoTimeout(2000);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        try{
            String greetings = in.readLine();
            if (!greetings.isEmpty()) {
                moveClientToAnotherPort(clientSocket);
            }
        }catch(NullPointerException n){
            n.printStackTrace();
        }
        in.close();
    }

    private void moveClientToAnotherPort(Socket clientSocket) throws IOException {
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        int newClientPort = generateNewPortForClient();
        out.println(newClientPort);
        out.close();
        LOGGER.info("NEW CLIENT ON PORT = " + newClientPort);
        runClientThread(newClientPort);
    }

    private void runClientThread(int clientPort) {
        new Thread(() -> startConnectionOnNewPort(clientPort)).start();
    }

    private void startConnectionOnNewPort(int clientPort) {
        try (ServerSocket clientServerSocket = new ServerSocket(clientPort)) {
            try (Socket clientSocket = clientServerSocket.accept()) {
                Connection connection = new Connection(clientSocket, clientPort);
                connection.startConnection();
            }
        } catch (IOException e) {
            LOGGER.error("Client closed with port: " + clientPort, e);
        } finally {
            lockedSocketPorts.remove(clientPort);
        }
    }

    private void startCommunicationWithClient(Socket clientSocket) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                makeHandshake(in, out);
                Long accountId = makeUserAuthorization(in, out);
                makeClientAction(in, out, accountId);
            }
        }
    }

    private void makeHandshake(BufferedReader in, PrintWriter out) {

    }

    interface LoopLambdaActionValidator {
        Long checkActionValidation(String message, String actionName);
    }

    private Long makeUserAuthorization(BufferedReader in, PrintWriter out) throws IOException {
        LoopLambdaActionValidator authorizationActionLambda = (mess, action) -> checkActionAuthorizationValidation(out, mess, action);
        return runCommunicationLoop(in, authorizationActionLambda);
    }

    private void makeClientAction(BufferedReader in, PrintWriter out, Long accountId) throws IOException {
        LoopLambdaActionValidator communicationActionLambda = (mess, action) -> checkActionValidation(out, mess, action, accountId);
        runCommunicationLoop(in, communicationActionLambda);
    }

    private Long runCommunicationLoop(BufferedReader in, LoopLambdaActionValidator loopLambdaActionValidator) throws IOException {
        while (true) {
            String message = in.readLine();
            if (message.isEmpty()) {
                continue;
            }

            JSONObject json = new JSONObject(message);

            String actionName = json.getString(KEY_ACTION);

            Long id = loopLambdaActionValidator.checkActionValidation(message, actionName);

            if (isUserSignIn(id)) {
                return id;
            }
        }
    }

    private static boolean isUserSignIn(Long id) {
        return id != NotSignInUser;
    }

    private Long checkActionAuthorizationValidation(PrintWriter out, String message, String actionName) {
        JSONObject returnedData;
        if (authorizationOptions.containsKey(actionName)) {
            CommunicatorActions communicatorActions = communicatorOptions.get(actionName);
            returnedData = communicatorActions.runAction(message, NotSignInUser);
            out.println(returnedData.toString());

            if (actionName.equals(KEY_LOGIN)) {
                return ((LoginAction) communicatorActions).getAccountId();
            }
        } else {
            returnedData = new JSONObject(new ResponseData(HttpStatus.BAD_REQUEST, "You have to be sign in"));
            out.println(returnedData.toString());
        }
        return NotSignInUser;
    }

    private Long checkActionValidation(PrintWriter out, String message, String actionName, Long accountId) {
        if (communicatorOptions.containsKey(actionName)) {
            JSONObject returnedData = communicatorOptions.get(actionName).runAction(message, accountId);
            out.println(returnedData.toString());
        }
        return NotSignInUser;
    }

    private int generateNewPortForClient() {
        while (true) {
            int randomNumber = new Random().nextInt(30000) + 15000;
            if (!lockedSocketPorts.contains(randomNumber)) {
                lockedSocketPorts.add(randomNumber);
                return randomNumber;
            }
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

    @Autowired
    public void setCommunicatorOptions(Map<String, CommunicatorActions> communicatorOptions) {
        this.communicatorOptions = communicatorOptions;
    }

    @Autowired
    @Qualifier(QUALIFIER_AUTHORIZATION)
    public void setAuthorizationOptions(Map<String, CommunicatorActions> authorizationOptions) {
        this.authorizationOptions = authorizationOptions;
    }
}
