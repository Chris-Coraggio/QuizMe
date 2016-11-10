import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chris on 11/9/2016.
 */
public class Question {

    private String question;
    private String answer;
    private int difficulty;
    private String category;

    public Question(String question, String answer, int difficulty, String category){

        this.question = question;
        this.answer = answer;
        this.difficulty = difficulty;
        this.category = category;
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
}
