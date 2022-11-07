package kopaczewski.glazer.bsiui.server;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import static kopaczewski.glazer.bsiui.encryption.Encryption.decryptHugeText;
import static kopaczewski.glazer.bsiui.encryption.Encryption.encryptHugeText;

public class Client {
    public static final String connectionMessage = "Hi!";
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws Exception {

        // generate client keys
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();
        PublicKey clientPublicKey = kp.getPublic();
        PrivateKey clientPrivateKey = kp.getPrivate();
        String clientPublicKeyString = Base64.getEncoder().encodeToString(clientPublicKey.getEncoded());

        // connect to server
        Socket clientSocket = new Socket("127.0.0.1", 16123);
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out.println(connectionMessage);
        int currentPort = Integer.parseInt(in.readLine());
        System.out.println("port =" + currentPort);
        clientSocket.close();

        // connection on new port
        Socket connectedSocket = new Socket("127.0.0.1", currentPort);
        out = new PrintWriter(connectedSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(connectedSocket.getInputStream()));

        // send user public key
        out.println(clientPublicKeyString);

        // get encoded server key
        String serverPublicKeyMessage = in.readLine();
        String decodedServerKey = decryptHugeText(serverPublicKeyMessage, clientPrivateKey);
        LOGGER.info("SERVER PUBLIC KEY = " + decodedServerKey);

        // get server public key as object
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(decodedServerKey));
        PublicKey serverPublicKey = keyFactory.generatePublic(keySpecX509);

        // send control message
        String controlMessage = controlMessageGenerator(100);
        LOGGER.info("CONTROL MESSAGE = " + controlMessage);
        String encodedControlMessage = encryptHugeText(controlMessage, serverPublicKey);
        LOGGER.info("ENCODED CONTROL MESSAGE = " + encodedControlMessage);
        out.println(encodedControlMessage);

        // get control message hash from server
        String controlHashFromServer = in.readLine();
        LOGGER.info("CONTROL HASH = " + controlHashFromServer);

        // decode hash
        String decodedHashFromServer = decryptHugeText(controlHashFromServer, clientPrivateKey);
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
            out.println("{\"action\": \"login\", \"body\":{\"login\":\"admin\",\"password\":\"admin\"}}");
            String status = in.readLine();
            LOGGER.info("STATUS: " + status);

            // create conversation
            File file = new File("create_conversation_json.txt");
            CharSource source = Files.asCharSource(file, Charsets.UTF_8);
            String createConversationMessage = source.read();
            System.out.println(createConversationMessage);

            out.println(createConversationMessage);

            File file2 = new File("message_json.txt");
            CharSource source2 = Files.asCharSource(file2, Charsets.UTF_8);
            String messageJson = source2.read();

            File file3 = new File("user_list_json.txt");
            CharSource source3 = Files.asCharSource(file3, Charsets.UTF_8);
            String messageJson3 = source3.read();

            out.println(messageJson3);
            String listUsers = in.readLine();
            System.out.println(listUsers);

            for(int i = 0; i < 5; i++){
                Thread.sleep(1000);
                String currentMessage = messageJson
                        .replace("[1]", "testConversation")
                        .replace("[2]", "test test test");
                out.println(currentMessage);
                String response = in.readLine();
                LOGGER.info("MESSAGE: " + response);
            }

            File file4 = new File("get_messages.txt");
            CharSource source4 = Files.asCharSource(file4, Charsets.UTF_8);
            String messageJson4 = source4.read();

            File file5 = new File("get_all_conversations.txt");
            CharSource source5 = Files.asCharSource(file5, Charsets.UTF_8);
            String messageJson5 = source5.read();

            out.println(messageJson5);
            String messagesList = in.readLine();
            System.out.println(messagesList);

            File file6 = new File("get_unreaded_json.txt");
            CharSource source6 = Files.asCharSource(file6, Charsets.UTF_8);
            String messageJson6 = source6.read();

            out.println(messageJson6);
            String unreadMessages = in.readLine();
            System.out.println(unreadMessages);

            out.println(messageJson6);
            unreadMessages = in.readLine();
            System.out.println(unreadMessages);


            while(true){
                Thread.sleep(5000);
                out.println(messageJson4);
                String listMessages = in.readLine();
                System.out.println(listMessages);
            }

        } else {
            LOGGER.info("SERVER IS NOT TRUSTED");
        }
    }

    private static String controlMessageGenerator(int length) {
        byte[] array = new byte[length];
        new Random().nextBytes(array);
        return Base64.getEncoder().encodeToString(array);
    }
}
