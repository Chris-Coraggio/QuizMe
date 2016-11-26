import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chris on 11/14/2016.
 */

//TODO: figure out why socket closed error happens
public class Server {

    static final int PORT = 50000;
    static ServerSocket socket = null;
    private static HashMap<String, String> database; //keys: username values: passwords TODO: add encryption
    private static String pathToDatafile = "data.txt";

    public static void main(String[] args) {
        initDatabase();
        initSocket();

        while (true) {
            try {
                new ServerWorker(socket.accept()).start();
            } catch (IOException e) {
                System.out.println("Failed to connect to client");
            }
        }
    }

    public static void initDatabase() {
        File datafile = new File(pathToDatafile);
        try {
            if(!datafile.exists()){
                datafile.createNewFile();
            }else {
                try {
                    ObjectInputStream readFromDatabase = new ObjectInputStream(new FileInputStream(pathToDatafile));
                    database = (HashMap<String, String>) readFromDatabase.readObject();
                }catch(EOFException ex){
                    database = new HashMap<String, String>();
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); //this should work, so if it doesn't just let me know
        }

    }

    public static void initSocket() {
        String ipAddress = "";
        try {
            ipAddress = InetAddress.getLocalHost().toString();
        } catch (UnknownHostException e) {
            System.out.println("Failed to discover own IP address");
        }

        try {
            socket = new ServerSocket(PORT);
            System.out.println("Server started on " + ipAddress);
        } catch (IOException err) {
            System.out.println("Failed to create server socket");
        }
    }

    public static boolean addUserToDatabase(String username, String password) { //returns true if success
        if(!database.containsKey(username)) {
            database.put(username, password);
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkUserInDatabase(String username, String password){
        return database.get(username).equals(password);
}

    static class ServerWorker extends Thread{

        private Socket client;
        private ObjectOutputStream out;
        private ObjectInputStream in;

        public ServerWorker(Socket client) {
            System.out.println("Created new ServerWorker");
            this.client = client;
            try {
                out = new ObjectOutputStream(this.client.getOutputStream());
                in = new ObjectInputStream(this.client.getInputStream());
            } catch (IOException e) {
                System.out.println("Failed to set up client I/O");
            }
        }

        public void run(){
            try {
                while(true) {
                    String[] clientResponse = (String[]) in.readObject();
                    switch (clientResponse[0]) {
                        case "REGISTER":
                            boolean userAlreadyExists = !Server.addUserToDatabase(clientResponse[1], clientResponse[2]); //username, password
                            if (userAlreadyExists) {
                                out.writeObject(new String[]{"REGISTERERROR", clientResponse[1] + " is already taken."});
                            } else {
                                out.writeObject(new String[]{"REGISTERSUCCESS", clientResponse[1] + " registered successfully!"});
                            }
                            break;
                        case "LOGIN":
                            if (Server.checkUserInDatabase(clientResponse[1], clientResponse[2])) { //username, password
                                out.writeObject(new String[]{"LOGINERROR", "Incorrect username or password."});
                            } else {
                                out.writeObject(new String[]{"LOGINSUCCESS", "Logged in as " + clientResponse[1]});
                            }
                            break;
                        default:
                            System.out.println("Message sent with invalid first keyword");
                            break;

                    }
                    out.flush();
                }
            }catch(Exception e){
                e.printStackTrace();
            }finally {
                close();
            }
        }
        
        public void close(){
            try {
                out.close();
                in.close();
            }catch(Exception e){
                System.out.println("Resources failed to close :(");
            }
        }
    }
}
