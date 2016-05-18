import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by Fanchao Zhou on 5/15/2016.
 */
public class Main {

    private static final int JOIN = 0;
    private static final int SEND = 1;
    private static final int FWD = 2;
    private static final int NAK = 3;
    private static final int OFFLINE = 4;
    private static final int ACK = 5;
    private static final int ONLINE = 6;

    private static final String TYPEFIELD = "type";
    private static final String CONTENTFIELD = "content";
    private static final String USERNAMEFIELD = "username";

    public static void main(String[] args){
        if(args.length != 3){
            System.out.println("Command Format: ClientAppName ServerIP ServerPort ClientName");
        } else {
            final String SERVER_IP = args[ 0 ];
            final int SERVER_PORT = Integer.parseInt(args[ 1 ]);
            final String clientName = args[ 2 ];

            try{
                // Start a new thread to receive messages from the server
                final Socket server = new Socket(SERVER_IP, SERVER_PORT);

                // The main thread is for reading local user input and sending it to the server
                final ObjectOutputStream objOS = new ObjectOutputStream(server.getOutputStream());
                final ObjectInputStream objIS = new ObjectInputStream(server.getInputStream());
                final Scanner reader = new Scanner(System.in);

                // Register a shutdown hook
                Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Exiting......");
                        try{
                            if(!server.isClosed()){
                                objOS.writeObject("");//Notify the server that the user is exiting with a string of zero length
                                server.close();       //Close the TCP Socket
                            }

                            reader.close();       //Close the standard input reader
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }));

                //Send a JOIN packet to the server and wait for reply
                String joinPacket = msgWrapper(JOIN, clientName, null);
                String reply;
                ServerMsg serverMsg;

                System.out.println("JOIN packet: "+joinPacket);
                objOS.writeObject(joinPacket);      //Send the JOIN packet
                reply = (String)objIS.readObject(); //Receive an ACK or an NAK
                serverMsg = msgUnwrapper(reply);
                System.out.println("Message in the reply:\n"+serverMsg.message);//Print the message
                if(serverMsg.msgType == ACK){ //If an ACK is received, start a ServerMsgReceiver Thread
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                while(true){
                                    String msg = (String)objIS.readObject();   //Read the string from the server
                                    ServerMsg serverMsg = msgUnwrapper(msg);   //Unwrap the packet and retrieve the content
                                    if(serverMsg.msgType==ONLINE || serverMsg.msgType==OFFLINE || serverMsg.msgType==FWD){
                                        System.out.println(serverMsg.message);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();

                    while(true){
                        if(reader.hasNextLine()){ //Check if the user has any input
                            String msg = reader.nextLine(); //Read the user's input

                            if(msg.length() > 0){ //If there is a valid input(a string of non-zero length)
                                msg = msgWrapper(SEND, clientName, msg);
                                objOS.writeObject(msg); //Send the message to the server
                            }
                        }
                    }
                } else if(serverMsg.msgType == NAK) { //If an NAK is received, print the error message and exit
                    try{
                        server.close();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class ServerMsg{
        public int msgType;
        public String message;
    }

    private static String msgWrapper(int msgType, String userName, String message){
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty(TYPEFIELD, msgType);
        jsonObject.addProperty(USERNAMEFIELD, userName);
        switch(msgType){
            case JOIN:
                jsonObject.addProperty(CONTENTFIELD, "");
                break;
            case SEND:
                jsonObject.addProperty(CONTENTFIELD, message);
                break;
        }

        return jsonObject.toString();
    }

    private static ServerMsg msgUnwrapper(String jsonData){
        ServerMsg serverMsg = new ServerMsg();
        Gson gson = new GsonBuilder().create();
        JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);
        String userName;
        String content;

        serverMsg.msgType = jsonObject.get(TYPEFIELD).getAsInt();
        switch(serverMsg.msgType){
            case FWD:
                userName = jsonObject.get(USERNAMEFIELD).getAsString();
                content = jsonObject.get(CONTENTFIELD).getAsString();
                serverMsg.message = userName+": "+content;
                break;
            case ACK:
                JsonArray jsonNameList = jsonObject.get(CONTENTFIELD).getAsJsonArray();
                final int clientNum = jsonNameList.size();
                content = "There are currently "+clientNum+" clients in the chatting room:\n";
                for(int cnt = 0; cnt < clientNum; cnt++){
                    content += jsonNameList.get(cnt).getAsString()+"\n";
                }
                serverMsg.message = content;

                break;
            case NAK:
                content = jsonObject.get(CONTENTFIELD).getAsString();
                serverMsg.message = "JOIN REQ Denied. Reason: "+content;
                break;
            case ONLINE:
                userName = jsonObject.get(USERNAMEFIELD).getAsString();
                serverMsg.message = userName+" is online.";
                break;
            case OFFLINE:
                userName = jsonObject.get(USERNAMEFIELD).getAsString();
                serverMsg.message = userName+" is offline.";
                break;
        }

        return serverMsg;
    }
}
