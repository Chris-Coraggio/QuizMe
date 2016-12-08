import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Chris on 11/14/2016.
 */

public class Server {

    static final int PORT = 50000;
    static ServerSocket socket = null;
    private static HashMap<String, User> usersHashmap = new HashMap<String, User>(); //keys: username values: Users
    private static HashMap<String, UserData> database; //keys: username, values: UserData instances
    private static String pathToDatafile = "data.txt";
    private static int numUsers = 0;
    private static HashMap<String, Game> games = new HashMap<String, Game>(); //key: game key value: Game instance

    public static HashMap<String, Game> getGames(){
        return games;
    }

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
    }

    public static void initDatabase() {
        File datafile = new File(pathToDatafile);
        try {
            if (!datafile.exists()) {
                datafile.createNewFile();
            } else {
                try {
                    ObjectInputStream readFromDatabase = new ObjectInputStream(new FileInputStream(pathToDatafile));
                    database = (HashMap<String, UserData>) readFromDatabase.readObject();
                    if(database == null){
                        database = new HashMap<String, UserData>();
                    }
                    readFromDatabase.close();
                } catch (EOFException ex) {
                    database = new HashMap<String, UserData>();
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); //this should work, so if it doesn't just let me know
        }
    }

    public static void updateDatabaseFile() {
        try {
            ObjectOutputStream writeToDatabase = new ObjectOutputStream(new FileOutputStream(pathToDatafile, false));
            writeToDatabase.writeObject(database);
            writeToDatabase.close();
        } catch (IOException e) {
            e.printStackTrace();
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
        System.out.println("UPDATING HASHMAP: " + username);
        if (!usersHashmap.containsKey(username)) {
            if(removeByUser(user)) {
                user.updateUserData(database.get(username));
                user.setUsername(username);
                usersHashmap.put(username, user);
            }else{
                System.out.println("Failed to remove user");
            }
        }
        return true;
    }

    public static void updateDatabase(User user){
        removeByUser(user);
        database.put(user.getUsername(), user.getUserData());
        updateDatabaseFile();
    }

    public static boolean removeByUser(User user){
        Iterator<Map.Entry<String, User>> it = usersHashmap.entrySet().iterator();
        for(int i=0; i<usersHashmap.size(); i++){
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
            database.put(username, new UserData(salt, hashPassword(password, salt), 0));
            updateDatabaseFile();
            return true;
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
        //return key;
        return "aaa";
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

    public static Game findGameByUser(User user){
        Iterator<Map.Entry<String, Game>> it = games.entrySet().iterator();
        for(int i=0; i < games.size(); i++){
            Game temp = it.next().getValue();
            if(temp.findParticipant(user)){
                return temp;
            }
        }
        return null;
    }

    public static void processInput(Object[] clientMessage, User user){
        switch ((String)clientMessage[0]) {
            case "REGISTER":
                sendToClient(register((String)clientMessage[1], (String)clientMessage[2]), user);
                break;
            case "LOGIN":
                sendToClient(login((String)clientMessage[1], (String)clientMessage[2], user), user);
                break;
            case("STARTNEWGAME"):
                sendToClient(startNewGame(user), user);
                break;
            case("GETAVAILABLEGAMES"):
                sendToClient(new HashMap[]{compileGames()}, user);
                break;
            case("JOINGAME"):
                sendToClient(joinGame(user, (String)clientMessage[1]), user); //gameKey
                break;
            case("LAUNCHGAME"):
                String[] launchGameResponse = launchGame(user, (String)clientMessage[1]);
                if(launchGameResponse[0].equals("LAUNCHGAMEFAILURE")){
                    sendToClient(launchGameResponse, user);
                }
                //sends success message to all clients in game at the start of the game
                break;
            case("GETNEXTQUESTION"):
                sendToClient(new Object[]{"NEXTQUESTION", findGameByUser(user).getNextQuestion(user)}, user);
                break;
            case("SCORE"):
                user.updateScore(Integer.parseInt((String)clientMessage[1]));
                findGameByUser(user).checkForResults();
                break;
            default:
                System.out.println("Message sent with invalid first keyword");
        }
    }


    public static void sendToClient(Object [] message, User client){
        try {
            client.writeObject(message);
                System.out.println(String.format(
                        "Sent to %s: %s", client.getUsername(), Arrays.toString(message)));
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
                hashPassword(password, database.get(username).getSalt()).equals(database.get(username).getHashedPassword())){
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
            String newGameKey = getRandomGameKey();
            Game newGame = new Game(newGameKey);
            newGame.addParticipant(user);
            games.put(newGameKey, newGame);
            return new String[]{"NEWGAMESUCCESS", "Created new game!", newGame.getGameKey()};
        }catch(Exception e){
            e.printStackTrace();
            return new String[]{"NEWGAMEFAILURE", "Failed to create new game."};
        }
    }

    public static HashMap<String, String> compileGames(){ //keys: leader usernames values: gameKeys
        HashMap<String, String> leadersAndKeys = new HashMap<String, String>();
        for(Game g: games.values()){
            leadersAndKeys.put(g.getLeader().getUsername(), g.getGameKey());
        }
        return leadersAndKeys;
    }

    public static String[] joinGame(User user, String gameKey){
        User gameLeader = games.get(gameKey).getLeader();
        if(gameLeader != null && !gameLeader.equals(user)){
            games.get(gameKey).addParticipant(usersHashmap.get(user.getUsername()));
            return(new String[]{"JOINGAMESUCCESS", String.format("Joined %s's game", gameLeader.getUsername())});
        }else{
            return(new String[]{"JOINGAMEFAILURE", String.format("Failed to join %s's game", gameLeader.getUsername())});
        }
    }

    public static String[] launchGame(User user, String gameKey){

        if(games.get(gameKey).getLeader().getUsername().equals(user.getUsername())){
            games.get(gameKey).start();
            return new String[]{"LAUNCHGAMESUCCESS"};
        }else{
            return new String[]{"LAUNCHGAMEFAILURE", "Failed to launch game. Please try again."};
        }
    }

}
