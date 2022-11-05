package kopaczewski.glazer.bsiui.server;

import kopaczewski.glazer.bsiui.communicator.actions.CommunicatorActions;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

@Component
public class Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    public static final String KEY_ACTION = "action";

    private final Set<Integer> lockedSocketPorts = new HashSet<>();

    private ServerSocket serverSocket;

    @Autowired
    private Map<String, CommunicatorActions> communicatorOptions;

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
                makeClientAction(in, out);
            }
        }
    }

    private void makeHandshake(BufferedReader in, PrintWriter out) {

    }

    private void makeClientAction(BufferedReader in, PrintWriter out) throws IOException {
        Long accountId = -1L;
        while (true) {
            String message = in.readLine();
            if (message.isEmpty()) {
                continue;
            }

            JSONObject json = new JSONObject(message);

            String actionName = json.getString(KEY_ACTION);

            checkActionValidation(out, message, actionName, accountId);
        }
    }

    private void checkActionValidation(PrintWriter out, String message, String actionName, Long accountId) {
        if (communicatorOptions.containsKey(actionName)) {
            JSONObject returnedData = communicatorOptions.get(actionName).runAction(message, accountId);
            out.println(returnedData.toString());
        }
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


}
