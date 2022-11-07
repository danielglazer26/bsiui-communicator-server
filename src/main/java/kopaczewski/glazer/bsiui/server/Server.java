package kopaczewski.glazer.bsiui.server;

import kopaczewski.glazer.bsiui.communicator.actions.CommunicatorActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static kopaczewski.glazer.bsiui.ConstStorage.QUALIFIER_AUTHORIZATION;

@Component
public class Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private final Set<Integer> lockedSocketPorts = new HashSet<>();

    public static Map<String, CommunicatorActions> communicatorOptions;
    public static Map<String, CommunicatorActions> authorizationOptions;

    private static final int ONE_HOUR_MS = 3600000;

    @Value("${socket.port}")
    private int port;

    @PostConstruct
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            //serverSocket.setSoTimeout(ONE_HOUR_MS); // timeout po godzinie bez połączenia
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    receiveClientGreetings(clientSocket);
                    clientSocket.close();
                } catch (SocketTimeoutException e) {
                    LOGGER.error("SERVER SOCKET TIMEOUT EXCEPTION", e);
                    break;
                } catch (IOException e) {
                    LOGGER.error("Server stop working", e);
                }
            }
        } catch (IOException e) {
            LOGGER.error("CAN'T START SERVER", e);
        }
    }

    private void receiveClientGreetings(Socket clientSocket) throws IOException {
        LOGGER.info("NEW CLIENT TRY TO CONNECT");
        clientSocket.setSoTimeout(2000);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        try {
            String greetings = in.readLine();
            if (greetings != null && !greetings.isEmpty()) {
                moveClientToAnotherPort(clientSocket);
            }
        } catch (NullPointerException n) {
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
        Thread thread = new Thread(() -> startConnectionOnNewPort(clientPort));
        thread.setName("Port: " + clientPort);
        thread.start();
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

    private int generateNewPortForClient() {
        while (true) {
            int randomNumber = new Random().nextInt(30000) + 15000;
            if (!lockedSocketPorts.contains(randomNumber)) {
                lockedSocketPorts.add(randomNumber);
                return randomNumber;
            }
        }
    }

    @Autowired
    public void setCommunicatorOptions(Map<String, CommunicatorActions> communicatorOptions) {
        Server.communicatorOptions = communicatorOptions;
    }

    @Autowired
    @Qualifier(QUALIFIER_AUTHORIZATION)
    public void setAuthorizationOptions(Map<String, CommunicatorActions> authorizationOptions) {
        Server.authorizationOptions = authorizationOptions;
    }
}
