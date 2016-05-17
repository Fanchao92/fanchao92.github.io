import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Fanchao Zhou on 5/14/2016.
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

    private static final String NAK_DUP_NAME = "Your user name is duplicated";
    private static final String NAK_TOO_MANY_USERS = "The number of clients has reached the limit";

    private static ConcurrentHashMap<String, ClientInfo> clientInfoList = new ConcurrentHashMap<>();
    private static int MAX_CLIENT_NUM;
    private static int PORT_NUMBER;

    public static void main(String[] args){
        if(args.length != 2){
            System.out.println("Command Format: ServerAppName PortNumber QueueLength.");
        } else {

            PORT_NUMBER = Integer.parseInt(args[ 0 ]);                  //Get the TCP port number
            MAX_CLIENT_NUM = Integer.parseInt(args[ 1 ]);               //Get the maximum number of clients


            try{
                final ServerSocket serverSocket = new ServerSocket(PORT_NUMBER, MAX_CLIENT_NUM);//Create a server socket
                final ExecutorService clientThreadPool = Executors.newCachedThreadPool();       //Create a thread pool for clients
                Socket clientSocket;

                System.out.println("Server Port Number: "+PORT_NUMBER);
                System.out.println("Maximum Queue Length: "+MAX_CLIENT_NUM);
                while(true){                                                //Assume that the server is never shutdown before users exit the system
                    clientSocket = serverSocket.accept();                         //accept the TCP connection from a new client
                    clientThreadPool.execute(new ClientMsgReceiver(clientSocket));//Assign a thread to the new client
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    private static class ClientInfo{
        public Socket clientSocket;     //The socket of the client
        public ObjectOutputStream objOS;//The input stream of the client
        public String clientName;       //The name of the client

        public ClientInfo (Socket clientSocket){
            this.clientSocket = clientSocket;
        }
    }

    private static class ClientMsgReceiver implements Runnable{
        private ClientInfo clientInfo;

        public ClientMsgReceiver(Socket clientSocket){
            clientInfo = new ClientInfo(clientSocket);
        }

        @Override
        public void run() {
            try{
                ObjectInputStream objIS = new ObjectInputStream(clientInfo.clientSocket.getInputStream());
                clientInfo.objOS = new ObjectOutputStream(clientInfo.clientSocket.getOutputStream());
                ClientMsg clientMsg;
                String joinPacket;

                // Get the JOIN packet and read the user's name from it
                joinPacket = (String)objIS.readObject();
                clientMsg = msgUnwrapper(joinPacket);
                clientInfo.clientName = clientMsg.message;
                System.out.println("JOIN packet from "+clientMsg.message+": "+joinPacket);

                if(clientInfoList.size() >= MAX_CLIENT_NUM){ //The number of clients exceeds the limit
                    System.out.println("Join REQ from "+clientInfo.clientName+"(IP address: "+clientInfo.clientSocket.getInetAddress()+
                            ") is Denied. Reason: "+NAK_TOO_MANY_USERS);
                    //Send an NAK
                    String nakPacket = msgWrapper(NAK, null, NAK_TOO_MANY_USERS);
                    clientInfo.objOS.writeObject(nakPacket);
                } else if(clientInfoList.containsKey(clientInfo.clientName)){  //The user name already exists
                    System.out.println("Join REQ from "+clientInfo.clientName+"(IP address: "+clientInfo.clientSocket.getInetAddress()+
                            ") is Denied. Reason: "+NAK_DUP_NAME);
                    //Send an NAK
                    String nakPacket = msgWrapper(NAK, null, NAK_DUP_NAME);
                    clientInfo.objOS.writeObject(nakPacket);
                } else {
                    String ackPacket = msgWrapper(ACK, null, null);
                    String onlinePacket = msgWrapper(ONLINE, clientInfo.clientName, null);
                    Collection<ClientInfo> clientInfoCollection = null;

                    System.out.println("ACK Packet for "+clientInfo.clientName+": "+ackPacket);
                    System.out.println("ONLINE Packet of "+clientInfo.clientName+": "+onlinePacket);

                    clientInfo.objOS.writeObject(ackPacket);  //Send the ACK packet
                    //Send an ONLINE notification to all the other users
                    clientInfoCollection = clientInfoList.values();
                    for(ClientInfo otherClientInfo:clientInfoCollection){  //Forward the message to every other client
                        if(!otherClientInfo.clientName.equals(clientInfo.clientName)){//As long as it's not the user himself
                            otherClientInfo.objOS.writeObject(onlinePacket);
                        }
                    }

                    clientInfoList.put(clientInfo.clientName, clientInfo);  //Add the client to the name list

                    System.out.println("User "+
                            clientInfo.clientName+
                            " From "+
                            clientInfo.clientSocket.getInetAddress()+
                            " is Connected.");
                    while(true) {
                        String msg = (String)objIS.readObject();
                        System.out.println("Message Length: "+msg.length());
                        if(msg.length() == 0){  //The client is exiting the chatroom
                            System.out.println("A Client has dropped. Client Name: "+clientInfo.clientName+
                                    ". Client IP: "+clientInfo.clientSocket.getInetAddress());
                            String offlinePacket = msgWrapper(OFFLINE, clientInfo.clientName, null);
                            System.out.println("OFFLINE packet: "+offlinePacket);
                            //Send an ONLINE notification to all the other users
                            clientInfoCollection = clientInfoList.values();
                            for(ClientInfo otherClientInfo:clientInfoCollection){  //Forward the message to every other client
                                if(!otherClientInfo.clientName.equals(clientInfo.clientName)){//As long as it's not the user himself
                                    otherClientInfo.objOS.writeObject(offlinePacket);
                                }
                            }

                            break;
                        } else {                //The client is sending a message
                            System.out.println("SEND packet from "+clientInfo.clientName+": "+msg);
                            clientMsg = msgUnwrapper(msg);
                            msg = msgWrapper(FWD, clientInfo.clientName, clientMsg.message);
                            System.out.println("FWD packet: "+msg);
                            clientInfoCollection = clientInfoList.values();
                            for(ClientInfo otherClientInfo:clientInfoCollection){  //Forward the message to every other client
                                if(!otherClientInfo.clientName.equals(clientInfo.clientName)){//As long as it's not the user himself
                                    otherClientInfo.objOS.writeObject(msg);
                                }
                            }
                        }
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (clientInfo != null) {                 //Close the client socket and remove the user information from the list
                        clientInfo.clientSocket.close();
                        if (clientInfoList.remove(clientInfo.clientName, clientInfo)) {
                            System.out.println("Client Info. is Removed from the list!");
                        } else {
                            System.out.println("Client is rejected before his info is added to the list");
                        }
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private static String msgWrapper(int msgType, String userName, String message){
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty(TYPEFIELD, msgType);
        switch(msgType){
            case FWD:
                jsonObject.addProperty(USERNAMEFIELD, userName);
                jsonObject.addProperty(CONTENTFIELD, message);
                break;
            case NAK:
                jsonObject.addProperty(USERNAMEFIELD, "");
                jsonObject.addProperty(CONTENTFIELD, "JOIN REQ denied. Reason: "+message);
                break;
            case ONLINE:
            case OFFLINE:
                jsonObject.addProperty(USERNAMEFIELD, userName);
                jsonObject.addProperty(CONTENTFIELD, "");
                break;
            case ACK:
                JsonArray jsonNameList = new JsonArray();
                Collection<ClientInfo> clientInfoCollection = clientInfoList.values();

                jsonObject.addProperty(USERNAMEFIELD, "");
                for(ClientInfo curClientInfo:clientInfoCollection){
                    jsonNameList.add(curClientInfo.clientName);
                }
                jsonObject.add(CONTENTFIELD, jsonNameList);
                break;
        }

        return jsonObject.toString();
    }

    private static class ClientMsg{
        int msgType;
        String message;
    }

    private static ClientMsg msgUnwrapper(String jsonData){
        ClientMsg clientMsg = new ClientMsg();
        Gson gson = new GsonBuilder().create();
        JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);

        clientMsg.msgType = jsonObject.get(TYPEFIELD).getAsInt();
        switch (clientMsg.msgType){
            case JOIN:
                clientMsg.message = jsonObject.get(USERNAMEFIELD).getAsString();
                break;
            case SEND:
                clientMsg.message = jsonObject.get(CONTENTFIELD).getAsString();
                break;
        }

        return clientMsg;
    }
}
