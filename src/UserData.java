/**
 * Created by Chris on 12/6/2016.
 */
public class UserData {

    private byte[] salt;
    private String hashedPassword;
    private int numWins;

    public UserData(byte[] salt, String hashedPassword, int numWins){
        this.salt = salt;
        this.hashedPassword = hashedPassword;
        this.numWins = numWins;
    }

    public byte[] getSalt(){
        return this.salt;
    }

    public String getHashedPassword(){
        return this.hashedPassword;
    }

    public int getNumWins(){
        return this.numWins;
    }

    public void incrementNumWins(){
        this.numWins++;
    }
}
