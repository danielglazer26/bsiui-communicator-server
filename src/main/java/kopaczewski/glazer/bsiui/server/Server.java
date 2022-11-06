package kopaczewski.glazer.bsiui.server;

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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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
        while (true) {
            try {
                serverSocket = new ServerSocket(this.port);

                Socket clientSocket = serverSocket.accept();

                receiveClientGreetings(clientSocket);

                clientSocket.close();
            } catch (IOException e) {
                LOGGER.error("Server stop working", e);
            } finally {
                try {
                    serverSocket.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void receiveClientGreetings(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        if (!in.readLine().isEmpty()) {
            moveClientToAnotherPort(clientSocket);
        }
        in.close();
    }

    private void moveClientToAnotherPort(Socket clientSocket) throws IOException {
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        int newClientPort = generateNewPortForClient();
        out.println(newClientPort);
        out.close();
        runClientThread(newClientPort);

    }

    private void runClientThread(int clientPort) {
        new Thread(() -> startConnectionOnNewPort(clientPort)).start();
    }

    private void startConnectionOnNewPort(int clientPort) {
        try (ServerSocket clientServerSocket = new ServerSocket(clientPort)) {
            try (Socket clientSocket = clientServerSocket.accept()) {
                startCommunicationWithClient(clientSocket);
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
