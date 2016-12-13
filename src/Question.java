import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chris on 11/9/2016.
 */
public class Question implements Serializable {

    private String question;
    private String answer;
    private int difficulty;
    private String category;

    public Question(String question, String answer, int difficulty, String category){

        this.question = properCase(question);
        this.answer = clean(answer.toUpperCase());
        this.difficulty = difficulty;
        this.category = properCase(category);
    }

    public String properCase(String str){
        char [] properCased = str.trim().toLowerCase().toCharArray();
        boolean capitalizeNext = false;
        properCased[0] = Character.toUpperCase(properCased[0]);
        for(int i = 1; i < properCased.length; i++){
            if(capitalizeNext){
                properCased[i] = Character.toUpperCase(properCased[i]);
            }
            if(properCased[i] == ' ' || properCased[i] == '-'){
                capitalizeNext = true;
            }else{
                capitalizeNext = false;
            }
        }
        return new String(properCased);
    }

    public String clean(String str){
        str = str.replace("<I>", "");
        str = str.replace("</I>", "");
        return str;
    }

    public String getQuestion(){
        return this.question;
    }

    public String getAnswer(){
        return this.answer;
    }

    public int getDifficulty(){
        return this.difficulty;
    }

    public String getCategory(){
        return this.category;
    }

    public String getScrambledAnswer(){
        String scrambled = "";
        List<Character> charList = new ArrayList<Character>(this.answer.length());
        for(char c: this.answer.toCharArray()){
            charList.add(c);
        }
        while(charList.size() != 0){
            scrambled += charList.remove((int)(Math.random()*charList.size()));
        }
        return scrambled;
    }

    public String toString(){
        return String.format("Question: %s\nAnswer: %s\nDifficulty: %d\nCategory: %s\n",
                this.question, this.answer, this.difficulty, this.category);
    }

    public String serialize(){
        return String.join("---", new String[]{this.getQuestion(), this.getAnswer(),
                Integer.toString(this.getDifficulty()), this.getCategory()});
    }

    public static Question deSerialize(String str){
        String[] params = str.split("---");
        return new Question(params[0], params[1], Integer.parseInt(params[2]), params[3]);
    }
}
