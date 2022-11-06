package kopaczewski.glazer.bsiui.server;

import com.google.common.hash.Hashing;
import kopaczewski.glazer.bsiui.communicator.actions.CommunicatorActions;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

@Component
public class Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private final Set<Integer> lockedSocketPorts = new HashSet<>();

    private ServerSocket serverSocket;

    @Autowired
    private Map<String, CommunicatorActions> communicatorOptions;

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
