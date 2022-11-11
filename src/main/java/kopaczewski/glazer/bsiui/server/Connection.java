package kopaczewski.glazer.bsiui.server;

import com.google.common.hash.Hashing;
import kopaczewski.glazer.bsiui.communicator.actions.CommunicatorActions;
import kopaczewski.glazer.bsiui.communicator.actions.LoginAction;
import kopaczewski.glazer.bsiui.communicator.data.ResponseData;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static kopaczewski.glazer.bsiui.ConstStorage.KEY_LOGIN;
import static kopaczewski.glazer.bsiui.server.Server.authorizationOptions;
import static kopaczewski.glazer.bsiui.server.Server.communicatorOptions;
import static kopaczewski.glazer.bsiui.server.encryption.Encryption.*;

public class Connection {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    public static final String KEY_ACTION = "action";
    public static final int SESSION_TIMEOUT_10_MIN = 600000;
    public static final int WAITING_TIME_FOR_GREETINGS = 15000;
    private final Socket clientSocket;
    private PublicKey sessionPublicKey;
    private PrivateKey sessionPrivateKey;
    private BufferedReader socketReader;
    private PrintWriter socketWriter;
    private final boolean debugOn = true;
    private final int port;
    public static final long NotSignInUser = -1L;
    private SecretKey clientPublicKeyAES;

    public Connection(Socket socket, int port) {
        clientSocket = socket;
        this.port = port;
    }

    interface LoopLambdaActionValidator {
        Long checkActionValidation(String message, String actionName);
    }

    private Long makeUserAuthorization() throws IOException {
        LoopLambdaActionValidator authorizationActionLambda = this::checkActionAuthorizationValidation;
        return runCommunicationLoop(authorizationActionLambda);
    }

    private void makeClientAction(Long accountId) throws IOException {
        LoopLambdaActionValidator communicationActionLambda = (mess, action) -> checkActionValidation(mess, action, accountId);
        runCommunicationLoop(communicationActionLambda);
    }

    public void startConnection() {
        try {
            generateKeys();
            startCommunicationWithClient();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("CONNECTION START ERROR");
        }
    }

    private void generateKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();
        sessionPublicKey = kp.getPublic();
        sessionPrivateKey = kp.getPrivate();
    }

    private Long checkActionValidation(String message, String actionName, Long accountId) {
        if (communicatorOptions.containsKey(actionName)) {
            LOGGER.info("USER " + accountId + " PERFORMED " + actionName + " WITH " + message);
            JSONObject returnedData = communicatorOptions.get(actionName).runAction(message, accountId);
            sendResponse(returnedData);
        }
        return NotSignInUser;
    }

    private void startCommunicationWithClient() {
        try {
            clientSocket.setSoTimeout(WAITING_TIME_FOR_GREETINGS);
            socketReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            socketWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            if (makeHandshake()) {
                if (getAesKey()) {
                    clientSocket.setSoTimeout(SESSION_TIMEOUT_10_MIN);// 10 min
                    Long accountId = makeUserAuthorization();
                    makeClientAction(accountId);
                }
            }
        } catch (SocketTimeoutException e) {
            LOGGER.error("CLIENT SOCKET TIMEOUT EXCEPTION");
        } catch (Exception e) {
            LOGGER.error("CLIENT COMMUNICATION ERROR", e);
        }

    }

    private boolean getAesKey() {
        try {
            String aesKeys = socketReader.readLine();
            String encryptedAESKey = decryptHugeTextRSA(aesKeys, sessionPrivateKey);
            clientPublicKeyAES = secretKeyFromStringAES(encryptedAESKey);
            return true;
        } catch (Exception e) {
            LOGGER.error("AN ERROR OCCUR WHILE FORMATTING AES KEY", e);
            return false;
        }
    }

    private Long
    runCommunicationLoop(LoopLambdaActionValidator loopLambdaActionValidator) throws IOException {
        while (true) {
            String message = socketReader.readLine();

            if (message != null && message.isEmpty()) {
                continue;
            }

            try {
                message = decryptAES(message, clientPublicKeyAES);
            } catch (Exception e) {
                LOGGER.error("AES FORMATTING ERROR");
            }

            JSONObject json = new JSONObject(message);
            String actionName = json.getString(KEY_ACTION);

            Long id = loopLambdaActionValidator.checkActionValidation(message, actionName);

            if (id != null) {
                if (isUserSignIn(id)) {
                    LOGGER.info("USER " + id + " LOGGED IN ON PORT " + port);
                    return id;
                }
            }
        }
    }

    private static boolean isUserSignIn(Long id) {
        return id != NotSignInUser;
    }


    private Long checkActionAuthorizationValidation(String message, String actionName) {
        JSONObject returnedData;
        if (authorizationOptions.containsKey(actionName)) {
            CommunicatorActions communicatorActions = authorizationOptions.get(actionName);
            returnedData = communicatorActions.runAction(message, NotSignInUser);
            sendResponse(returnedData);
            if (actionName.equals(KEY_LOGIN)) {
                return ((LoginAction) communicatorActions).getAccountId();
            }
        } else {
            returnedData = new JSONObject(new ResponseData(HttpStatus.BAD_REQUEST, "You have to be sign in"));
            sendResponse(returnedData);
        }
        return NotSignInUser;
    }

    private void sendResponse(JSONObject returnedData) {
        try {
            socketWriter.println(encryptAES(returnedData.toString(), clientPublicKeyAES));
        } catch (Exception e) {
            LOGGER.error("ERROR OCCURRED WHILE ENCRYPTING RESPONSE DATA", e);
        }
    }

    private boolean makeHandshake() {
        try {
            String clientPublicKeyMessage = socketReader.readLine();
            try {
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(clientPublicKeyMessage));
                PublicKey clientPublicKey = keyFactory.generatePublic(keySpecX509);
                if (debugOn) LOGGER.info("CLIENT PUBLIC KEY = " + clientPublicKey.toString());

                try {
                    String sessionKey = Base64.getEncoder().encodeToString(sessionPublicKey.getEncoded());
                    if (debugOn) LOGGER.info("SERVER SESSION PUBLIC KEY = " + sessionKey);
                    String encodedSessionKey = encryptHugeTextRSA(sessionKey, clientPublicKey);
                    socketWriter.println(encodedSessionKey);

                    try {
                        String encodedControlMessage = socketReader.readLine();
                        if (debugOn) LOGGER.info("ENCODED CONTROL MESSAGE = " + encodedControlMessage);

                        try {
                            String controlMessage = decryptHugeTextRSA(encodedControlMessage, sessionPrivateKey);
                            if (debugOn) LOGGER.info("DECODED CONTROL MESSAGE = " + controlMessage);

                            String controlHash = Hashing.sha256().hashString(controlMessage + clientPublicKeyMessage, StandardCharsets.UTF_8).toString();
                            if (debugOn) LOGGER.info("CONTROL HASH = " + controlHash);

                            try {
                                String encodedControlHash = encryptHugeTextRSA(controlHash, clientPublicKey);
                                if (debugOn) LOGGER.info("ENCODED CONTROL HASH = " + encodedControlHash);
                                socketWriter.println(encodedControlHash);
                                LOGGER.info("CLIENT VERIFIED ON PORT = " + port);
                                return true;
                            } catch (Exception e) {
                                LOGGER.error("CONTROL HASH CODE ERROR");
                            }
                        } catch (Exception e) {
                            LOGGER.error("CONTROL MESSAGE DECODE ERROR");
                        }
                    } catch (IOException e) {
                        LOGGER.error("CLIENT DOESN'T SEND CONTROL MESSAGE");
                    }
                } catch (Exception e) {
                    LOGGER.error("SESSION KEY ENCRYPTION ERROR");
                }
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                LOGGER.error("CAN'T CREATE KEY FROM STRING");
            }
        } catch (IOException e) {
            LOGGER.error("CLIENT DOESN'T SEND KEY");
        }
        return false;
    }
}
