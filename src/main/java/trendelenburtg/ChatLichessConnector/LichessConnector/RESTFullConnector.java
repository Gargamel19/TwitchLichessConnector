package trendelenburtg.ChatLichessConnector.LichessConnector;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class RESTFullConnector {

    static String URLRoot = "https://lichess.org/api";

    public static String makeNewGame(Boolean rated, int limit, int increment, int days, String color, String variant, String fen, String username, String token) throws IOException {

        String newGameURL =  URLRoot + "/challenge/" +username;

        String body = "rated=" + rated + "&color=" + color+ "&variant=" + variant;

        if(limit<=10800){
            body = body + "&clock_limit=" + limit;
        }
        if(increment<=60){
            body = body + "&clock_increment=" + increment;
        }
        if(days<=15 && days>0){
            body = body + "&days=" + days;
        }
        if(!fen.isEmpty()){
            body = body + "&fen=" + fen;
        }


        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(newGameURL);

        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setHeader("Authorization", "Bearer " + token);

        HttpEntity sendEntity = new ByteArrayEntity(body.getBytes("UTF-8"));
        httpPost.setEntity(sendEntity);

        CloseableHttpResponse response = client.execute(httpPost);
        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity);
        try {
            JSONObject jo = new JSONObject(result);

            return jo.getJSONObject("challenge").getString("id");
        }catch (JSONException je){
            je.printStackTrace();
        }
        return "";
    }

    public static boolean makeMove(String gameID, String move, String token) throws IOException {
        String newGameURL =  URLRoot + "/bot/game/" +gameID + "/move/" + move;

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(newGameURL);

        httpPost.setHeader("Authorization", "Bearer " + token);

        CloseableHttpResponse response = client.execute(httpPost);
        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity);

        JSONObject jo = new JSONObject(result);
        if(jo.has("error")){
            return false;
        }
        return jo.getBoolean("ok");
    }

    public static boolean isChatTurn(String gameiID, String token) {

        String newGameURL =  URLRoot + "/account/playing?nb=50";

        System.out.println(newGameURL);

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(newGameURL);

        httpGet.setHeader("Content-Type", "application/json");
        httpGet.setHeader("Authorization", "Bearer " + token);
        try {
            CloseableHttpResponse response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);

            if(response.getStatusLine().getStatusCode()==200){

                JSONObject jo = new JSONObject(result);
                JSONArray nowPlayingGames = jo.getJSONArray("nowPlaying");
                for (int i = 0; i < nowPlayingGames.length(); i++) {
                    if(nowPlayingGames.getJSONObject(i).getString("gameId").equals(gameiID)){
                        return nowPlayingGames.getJSONObject(i).getBoolean("isMyTurn");
                    }
                }
                return false;
            }else if(response.getStatusLine().getStatusCode()==404){
                return false;
            }




        }catch (JSONException je){
            je.printStackTrace();
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
        return false;
    }

    public static boolean resigne(String gameID, String token) {
        String newGameURL =  URLRoot + "/bot/game/" + gameID + "/resign";

        System.out.println(newGameURL);

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(newGameURL);

        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + token);
        try {
            CloseableHttpResponse response = client.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);

            System.out.println(response.getStatusLine().getStatusCode());
            System.out.println(result);
            if(response.getStatusLine().getStatusCode()==200){
                return true;
            }else if(response.getStatusLine().getStatusCode()==404){
                return false;
            }
        }catch (JSONException je){
            je.printStackTrace();
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
        return false;
    }

    public static boolean abort(String gameID, String token) {
        String newGameURL =  URLRoot + "/bot/game/" + gameID + "/abort";

        System.out.println(newGameURL);

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(newGameURL);

        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + token);
        try {
            CloseableHttpResponse response = client.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);

            System.out.println(response.getStatusLine().getStatusCode());
            System.out.println(result);
            if(response.getStatusLine().getStatusCode()==200){
                return true;
            }else if(response.getStatusLine().getStatusCode()==404){
                return false;
            }
        }catch (JSONException je){
            je.printStackTrace();
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
        return false;
    }
}
