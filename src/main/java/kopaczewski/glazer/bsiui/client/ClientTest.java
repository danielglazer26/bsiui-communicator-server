package kopaczewski.glazer.bsiui.client;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import kopaczewski.glazer.bsiui.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.KeyGenerator;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Random;

import static kopaczewski.glazer.bsiui.server.encryption.Encryption.*;

public class ClientTest {
    public static final String connectionMessage = "Hi!";
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private static final String host = "127.0.0.1";
    private static final int controlMessageLength = 100;

    public static void main(String[] args) throws Exception {

        // generate client keys
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();
        PublicKey clientPublicKey = kp.getPublic();
        PrivateKey clientPrivateKey = kp.getPrivate();
        String clientPublicKeyString = Base64.getEncoder().encodeToString(clientPublicKey.getEncoded());

        // connect to server
        Socket clientSocket = new Socket(host, 16123);
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out.println(connectionMessage);
        int currentPort = Integer.parseInt(in.readLine());
        System.out.println("port =" + currentPort);
        clientSocket.close();

        // connection on new port
        Socket connectedSocket = new Socket(host, currentPort);
        out = new PrintWriter(connectedSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(connectedSocket.getInputStream()));

        // send user public key
        out.println(clientPublicKeyString);

        // get encoded server key
        String serverPublicKeyMessage = in.readLine();
        String decodedServerKey = decryptHugeTextRSA(serverPublicKeyMessage, clientPrivateKey);
        LOGGER.info("SERVER PUBLIC KEY = " + decodedServerKey);

        // get server public key as object
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(decodedServerKey));
        PublicKey serverPublicKey = keyFactory.generatePublic(keySpecX509);

        // send control message
        String controlMessage = controlMessageGenerator();
        LOGGER.info("CONTROL MESSAGE = " + controlMessage);
        String encodedControlMessage = encryptHugeTextRSA(controlMessage, serverPublicKey);
        LOGGER.info("ENCODED CONTROL MESSAGE = " + encodedControlMessage);
        out.println(encodedControlMessage);

        // get control message hash from server
        String controlHashFromServer = in.readLine();
        LOGGER.info("CONTROL HASH = " + controlHashFromServer);

        // decode hash
        String decodedHashFromServer = decryptHugeTextRSA(controlHashFromServer, clientPrivateKey);
        LOGGER.info("ENCODED CONTROL HASH = " + decodedHashFromServer);

        // hash control message in client
        String controlHashTest = Hashing
                .sha256()
                .hashString(controlMessage + clientPublicKeyString, StandardCharsets.UTF_8)
                .toString();
        LOGGER.info("CLIENT CODED HASH = " + controlHashTest);

        // check is hash the same
        if (controlHashTest.equals(decodedHashFromServer)) {
            LOGGER.info("SERVER VERIFIED");

            // send AES key
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            Key aesKey = keyGenerator.generateKey();
            String aesKeyString = Base64.getEncoder().encodeToString(aesKey.getEncoded());
            out.println(encryptHugeTextRSA(aesKeyString, serverPublicKey));

            System.out.println(sendGetMessage("register_json.txt", out, in, aesKey));
            System.out.println(sendGetMessage("login_json.txt", out, in, aesKey));
            System.out.println(sendGetMessage("create_conversation_json.txt", out, in, aesKey));
            System.out.println(sendGetMessage("user_list_json.txt", out, in, aesKey));
            sendTestMessages(1000, 5, out, in, aesKey);
            System.out.println(sendGetMessage("get_messages_json.txt", out, in, aesKey));
            System.out.println(sendGetMessage("get_unread_json.txt", out, in, aesKey));
            System.out.println(sendGetMessage("get_unread_json.txt", out, in, aesKey));
            sendTestMessages(5000, 100, out, in, aesKey);
        } else {
            LOGGER.info("SERVER IS NOT TRUSTED");
        }
        clientSocket.close();
    }

    private static void sendTestMessages(
            int sleepTime,
            int messageNumber,
            PrintWriter out,
            BufferedReader in,
            Key aesKey) throws Exception {
        for (int i = 0; i < messageNumber; i++) {
            Thread.sleep(sleepTime);
            System.out.println(sendGetMessage("send_message_json.txt", out, in, aesKey));
        }
    }

    private static String controlMessageGenerator() {
        byte[] array = new byte[controlMessageLength];
        new Random().nextBytes(array);
        return Base64.getEncoder().encodeToString(array);
    }

    private static String sendGetMessage(String fileName, PrintWriter out, BufferedReader in, Key key) throws Exception {
        File file = new File(fileName);
        CharSource source = Files.asCharSource(file, Charsets.UTF_8);
        String messageJson = source.read();
        String encodedMsg = encryptAES(messageJson, key);
        out.println(encodedMsg);
        String read = in.readLine();
        System.out.println(read);
        return decryptAES(read, key);
    }
}
