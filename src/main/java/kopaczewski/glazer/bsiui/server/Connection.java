package kopaczewski.glazer.bsiui.server;

import com.google.common.hash.Hashing;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static kopaczewski.glazer.bsiui.encryption.Encryption.decryptHugeText;
import static kopaczewski.glazer.bsiui.encryption.Encryption.encryptHugeText;

public class Connection {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    public static final String KEY_ACTION = "action";
    private final Socket clientSocket;
    private PublicKey sessionPublicKey;
    private PrivateKey sessionPrivateKey;
    private BufferedReader socketReader;
    private PrintWriter socketWriter;
    private final boolean debugOn = false;
    private final int port;


    public Connection(Socket socket, int port) {
        clientSocket = socket;
        this.port = port;
    }

    public void startConnection() {
        try {
            generateKeys();
            startCommunicationWithClient();
        } catch (IOException | NoSuchAlgorithmException e) {
            LOGGER.info("CONNECTION START ERROR");
        }
    }

    private void generateKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();
        sessionPublicKey = kp.getPublic();
        sessionPrivateKey = kp.getPrivate();
    }

    private void makeClientAction() throws IOException {
        // TODO Obsługa komunikacji z klientem, czytanie requestów i generowanie responsów
        while (true) {
            String message = socketReader.readLine();
            if (message.isEmpty()) {
                continue;
            }
            JSONObject json = new JSONObject(message);
            String actionName = json.getString(KEY_ACTION);
            LOGGER.info("ACTION: " + actionName + " FROM PORT: " + port);
            //checkActionValidation(out, message, actionName);
        }
    }

//    private void checkActionValidation(PrintWriter out, String message, String actionName) {
//        if (communicatorOptions.containsKey(actionName)) {
//            JSONObject returnedData = communicatorOptions.get(actionName).runAction(message);
//            out.println(returnedData.toString());
//        }
//    }

    private void startCommunicationWithClient() throws IOException {
        try {
            socketReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            socketWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            if (makeHandshake()) {
                clientSocket.setSoTimeout(600000);// 10 min timeout
                makeClientAction();
            }
        } catch (IOException e) {
            LOGGER.info("CAN'T INITIALIZE I/O");
        }

    }

    private boolean makeHandshake() {
        try {
            String clientPublicKeyMessage = socketReader.readLine();
            try {
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(clientPublicKeyMessage));
                PublicKey clientPublicKey = keyFactory.generatePublic(keySpecX509);
                if(debugOn) LOGGER.info("CLIENT PUBLIC KEY = " + clientPublicKey.toString());

                try {
                    String sessionKey = Base64.getEncoder().encodeToString(sessionPublicKey.getEncoded());
                    if(debugOn)  LOGGER.info("SERVER SESSION PUBLIC KEY = " + sessionKey);
                    String encodedSessionKey = encryptHugeText(sessionKey, clientPublicKey);
                    socketWriter.println(encodedSessionKey);

                    try {
                        String encodedControlMessage = socketReader.readLine();
                        if(debugOn)  LOGGER.info("ENCODED CONTROL MESSAGE = " + encodedControlMessage);

                        try {
                            String controlMessage = decryptHugeText(encodedControlMessage, sessionPrivateKey);
                            if(debugOn) LOGGER.info("DECODED CONTROL MESSAGE = " + controlMessage);

                            String controlHash = Hashing.sha256().hashString(controlMessage + clientPublicKeyMessage, StandardCharsets.UTF_8).toString();
                            if(debugOn) LOGGER.info("CONTROL HASH = " + controlHash);

                            try {
                                String encodedControlHash = encryptHugeText(controlHash, clientPublicKey);
                                if(debugOn)  LOGGER.info("ENCODED CONTROL HASH = " + encodedControlHash);
                                socketWriter.println(encodedControlHash);
                                LOGGER.info("CLIENT VERIFIED ON PORT = " + port);
                                return true;
                            } catch (Exception e) {
                                LOGGER.info("CONTROL HASH CODE ERROR");
                            }
                        } catch (Exception e) {
                            LOGGER.info("CONTROL MESSAGE DECODE ERROR");
                        }
                    } catch (IOException e) {
                        LOGGER.info("CLIENT DOESN'T SEND CONTROL MESSAGE");
                    }
                } catch (Exception e) {
                    LOGGER.info("SESSION KEY ENCRYPTION ERROR");
                }
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                LOGGER.info("CAN'T CREATE KEY FROM STRING");
            }
        } catch (IOException e) {
            LOGGER.info("CLIENT DOESN'T SEND KEY");
        }
        return false;
    }
}
