package trendelenburtg.ChatLichessConnector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import trendelenburtg.ChatLichessConnector.LichessConnector.RESTFullConnector;
import trendelenburtg.ChatLichessConnector.TwitchChatConnector.TwitchChat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@SpringBootApplication
public class ChatLichessConnectorApplication {

	static String username;
	static String chatBotName;
	static String twitchChanelName;
	static String token;
	static int guessTime;

	public static void main(String[] args) {

		username = "UltimaRatio4";
		chatBotName = "fettarmqp";
		twitchChanelName = "robertyaa";
		token = "";
		guessTime = 30;

		SpringApplication.run(ChatLichessConnectorApplication.class, args);

		String gameID;

		TwitchChat tc= new TwitchChat(chatBotName, twitchChanelName);

		try {

			tc.connect();
			gameID = RESTFullConnector.makeNewGame(false, 10, 5, 0, "random", "standard", "", username, token);
			while(true){
				Runnable r1 = new Runnable() {
					@Override
					public void run() {
						try {
							tc.start();
						}catch (IOException ioe){
							ioe.printStackTrace();
						}
					}
				};
				while (!isChatsTurn(gameID)){
					Thread.sleep(1000);
				}
				System.out.println("ITS CHATS TURN!");
				Thread t1 = new Thread(r1);
				t1.start();
				tc.writeInTheChat("started poll");

				Thread.sleep(guessTime * 1000);
				t1.interrupt();
				tc.stop();
				tc.writeInTheChat("stopped poll");
				HashMap<String, Integer> results = tc.count();

				Iterator i = results.entrySet().iterator();
				boolean foundMove = false;
				for (int j = 0; j < results.size(); j++) {
					String move = getBestMove(results);
					if(RESTFullConnector.makeMove(gameID, move, token)){
						foundMove = true;
						break;
					}
					results.remove(move);
					i.remove();
				}

				if(!foundMove){
					System.out.println("Keinen valieden Move gefunden. ich breche ab");
					RESTFullConnector.resigne(gameID, token);
					RESTFullConnector.abort(gameID, token);
				}


			}
		}catch (IOException e2){

		}catch (InterruptedException ire){

		}

	}

	private static String getBestMove(HashMap<String, Integer> moves) {
		HashMap<String, Integer> clone = (HashMap<String, Integer>) moves.clone();
		int bestAmmound = 0;
		String bestMove = "";

		Iterator i = clone.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry pair = (Map.Entry) i.next();
			if((Integer)pair.getValue()>=bestAmmound){
				bestAmmound = (Integer)pair.getValue();
				bestMove = (String) pair.getKey();
			}
			i.remove();
		}
		return bestMove;
	}

	public static boolean isChatsTurn(String gameID) {

		return RESTFullConnector.isChatTurn(gameID, token);

	}

}
