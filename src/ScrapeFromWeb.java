import org.jsoup.Jsoup;
import org.json.*;
import java.io.IOException;

/**
 * Created by Chris on 11/9/2016.
 */
public class ScrapeFromWeb {

    private String resultsJSONString;                               //resulting page of a scrape
    private final String address = "http://jservice.io/api/random"; //the web site url
    private JSONObject json;                                        //the object to store the results

    public Question getRandomQuestion() throws IOException, JSONException {
        try {
            resultsJSONString = String.format(Jsoup.connect(address).ignoreContentType(true).execute().body());
            resultsJSONString = resultsJSONString.substring(1, resultsJSONString.length()); //remove array designations at either end
        }catch(IOException e){
            throw e;
        }
        try{
            json = new JSONObject(resultsJSONString);
        }catch(JSONException e){
            throw e;
        }
        return new Question( json.get("question").toString(),
                             json.get("answer").toString(),
                             Integer.parseInt(json.get("value").toString()),
                             new JSONObject(json.get("category").toString()).get("title").toString());
    }

    public static void main (String [] args) throws Exception{
        Question q = new ScrapeFromWeb().getRandomQuestion();
        System.out.println(q.getAnswer() + "\t" + q.getScrambledAnswer());
    }
}
