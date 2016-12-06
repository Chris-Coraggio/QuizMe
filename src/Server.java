import javafx.util.Pair;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Chris on 11/14/2016.
 */

public class Server {

    static final int PORT = 50000;
    static ServerSocket socket = null;
    private static HashMap<String, User> usersHashmap; //keys: username values: Users
    private static HashMap<String, Pair<byte[], String>> database; //keys: username, values: salt and hashedPassword
    private static String pathToDatafile = "data.txt";
    private static int numUsers = 0;
    private static HashMap<Integer, Game> games = new HashMap<Integer, Game>(26 * 26); //hash based on first two letters

    public static void main(String[] args) {
        initDatabase();
        initSocket();

        Thread clientListen = new Thread(){
            public void run(){
            while(true){
                try{
                    usersHashmap.put(String.format("%d", numUsers++), new User(socket.accept()));
                    System.out.println("Adding User");
                }catch(Exception e){
                    System.out.println("Failed to connect to client");
                }
            }
            }
        };
        clientListen.start();

        while (true) {
            try{
                Thread.sleep(10);   //not sure why it didn't work without this
            }catch(Exception e){
                System.out.println("Main server thread interrupted");
                e.printStackTrace();    //major problem
            }
            if(usersHashmap.size()>0) {
                Iterator<Map.Entry<String, User>> it = usersHashmap.entrySet().iterator();
                try {
                    while (it.hasNext()) {
                        User temp = it.next().getValue();
                        try {
                            System.out.println("data waiting");
                            processInput(temp.readObject(), temp);
                        } catch (Exception e) {
                            System.out.println("there may be a problem here");
                        }
                    }
                }catch(Exception e){}
            }
        }
    }

    public static void initDatabase() {
        File datafile = new File(pathToDatafile);
        try {
            if (!datafile.exists()) {
                datafile.createNewFile();
            } else {
                try {
                    ObjectInputStream readFromDatabase = new ObjectInputStream(new FileInputStream(pathToDatafile));
                    database = (HashMap<String, Pair<byte[], String>>) readFromDatabase.readObject();
                } catch (EOFException ex) {
                    database = new HashMap<String, Pair<byte[], String>>();
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

    public static boolean updateHashmap(String username, User user) { //returns true if success
        if (!usersHashmap.containsKey(username)) {
            if(removeByUser(user)) {
                usersHashmap.put(username, user);
            }else{
                System.out.println("Failed to remove user");
            }
        }
        return true;
    }

    public static boolean removeByUser(User user){
        Iterator<Map.Entry<String, User>> it = usersHashmap.entrySet().iterator();
        for(int i=0;i<usersHashmap.size();i++){
            Map.Entry<String,User> temp = it.next();
            if(temp.getValue().equals(user)){
                usersHashmap.remove(temp.getKey());
                return true;
            }
        }
        return false;
    }

    public static boolean addUserToDatabase(String username, String password) { //returns true if success
        if (!database.containsKey(username)) {
            byte[] salt = generateSalt();
            database.put(username, new Pair(salt, hashPassword(password, salt)));
            return true;
            //TODO: write in new user
        } else {
            return false;
        }
    }

    public static String getRandomGameKey(){
        //random string of lowercase length 3
        final String choices = "abcdefghijklmnopqrstuvwxyz";
        String key = "";
        for(int i = 0; i < 3; i++){
            key += choices.charAt((int)(Math.random() * 26));
        }
        return key;
    }

    public static int hashGameKey(String gameKey){
        char[] letters = gameKey.toCharArray();
        return 26 * ((int)letters[0] - 97) + ((int)letters[1] - 97);
    }

    public static String hashPassword(String origPassword, byte[] salt){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            return convertBytesToString(md.digest(origPassword.getBytes()));
        }catch(NoSuchAlgorithmException e){
            return origPassword;
        }
    }

    public static byte[] generateSalt(){
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");     //pseudo-random number generator algorithm
            byte[] salt = new byte[16];
            sr.nextBytes(salt);                                         //get a random salt
            return salt;
        }catch(NoSuchAlgorithmException e){
            return null;
        }
    }

    public static String convertBytesToString(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for(int i=0; i < bytes.length; i++)
        {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    private static String[] processInput(String[] clientMessage, User user){
        switch (clientMessage[0]) {
            case "REGISTER":
                sendToClient(register(clientMessage[1], clientMessage[2]), user);
            case "LOGIN":
                sendToClient(login(clientMessage[1], clientMessage[2], user), user);
            default:
                System.out.println("Message sent with invalid first keyword");
                return null;
        }
    }


    public static void sendToClient(String [] message, User client){
        try {
            client.writeObject(message);
            System.out.println(String.format("Sent to %s: %s", client.getUsername(), String.join(" ", message)));
        }catch(IOException e){
            System.out.println("Error writing to client.");
        }
    }

    public static String[] register(String username, String unhashedPassword){
        if(database.containsKey(username)){
            return new String[]{"REGISTERERROR", username + " is already taken."};
        }else{
            if(addUserToDatabase(username, unhashedPassword)){
                return new String[]{"REGISTERSUCCESS", username + " registered successfully!"};
            }else{
                return new String[]{"REGISTERERROR", "There was a problem writing to the database. Please try again."};
            }
        }
    }

    public static String[] login(String username, String password, User user){

        if(database.containsKey(username) &&
                hashPassword(password, database.get(username).getKey()).equals(database.get(username).getValue())){
            updateHashmap(username, user);
            return(new String[]{"LOGINSUCCESS", "Logged in as " + username});
        }else if(usersHashmap.containsKey(username)) {
            return (new String[]{"LOGINERROR", String.format("%s is already logged in.", username)});
        }else{
            return (new String[]{"LOGINERROR", "Incorrect username or password."});
        }
    }

    public static String[] startNewGame(User user){
        try {
            Game newGame = new Game(games.size());
            newGame.addParticipant(user);
            String newGameKey = getRandomGameKey();
            games.put(hashGameKey(newGameKey), newGame);
            return new String[]{"NEWGAMESUCCESS", "Created new game!", newGame.getGameNumber()};
        }catch(Exception e){
            return new String[]{"NEWGAMEFAILURE", "Failed to create new game."};
        }
    }

    public static String[] joinGame(User user, String gameKey){
        User gameLeader = games.get(gameKey).getLeader();
        if(gameLeader != null && gameLeader.equals(user)){
            games.get(gameKey).addParticipant(usersHashmap.get(user.getUsername()));
            return(new String[]{"JOINGAMESUCCESS", String.format("Joined %s's game", gameLeader)});
        }else{
            return(new String[]{"JOINGAMEFAILURE", String.format("Failed to join %s's game", gameLeader)});
        }
    }
}
