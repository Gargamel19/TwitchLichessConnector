package trendelenburtg.ChatLichessConnector.TwitchChatConnector;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class TwitchChat {

    HashMap<String, String> guesses = new HashMap<>();

    boolean running = true;
    String token = "oauth:tzm4sb6ue2saygc24rf3s2l6wkp8x6";
    String nick;
    String channel;
    Socket socket;
    PrintWriter out;

    public TwitchChat(String nick, String channel) {
        this.nick = nick;
        this.channel = "#" + channel;
    }

    public void connect() throws IOException {

        socket = new Socket("irc.twitch.tv", 6667);

        out = new PrintWriter(socket.getOutputStream(), true);

        writeInTheChat("PASS " + token);
        writeInTheChat("NICK " + nick);
        writeInTheChat("JOIN " + channel);
    }

    public void start() throws IOException {

        Scanner input = new Scanner(socket.getInputStream());

        while (input.hasNext()) {
            String line = input.nextLine();
            if (line.contains("PRIVMSG")) {
                String user = line.split("!")[0].substring(1);
                int index = line.indexOf(':', 1);
                String msg = line.substring(index + 1);

                System.out.println(user + " : " + msg);
                if (msg.matches("[a-h][0-8][a-h][0-8]")) {

                    printMap(guesses);
                    if (!guesses.containsKey(user)) {
                        guesses.put(user, msg);
                    }
                    printMap(guesses);

                }
            } else if (line.contains("PING")) {
                writeInTheChat("PONG tmi.twitch.tv");
            }
            if (!running) {
                input.close();
                out.close();
                socket.close();

            }
        }

        input.close();
        out.close();
        socket.close();
    }

    private void printMap(HashMap<String, String> hashmap){

        HashMap<String, String> clone = (HashMap<String, String>) hashmap.clone();

        Iterator i = clone.entrySet().iterator();
        System.out.println("HM: " + clone.size());
        while (i.hasNext()) {
            Map.Entry pair = (Map.Entry) i.next();
            System.out.println(pair.getKey() + ": " + pair.getValue());
            i.remove();
        }
        System.out.println(":HM");
    }

    public void writeInTheChat(String msg){
        out.print(msg + "\r\n");
        out.flush();
    }

    public HashMap<String, Integer> count() {
        HashMap<String, Integer> results = new HashMap<>();

        HashMap<String, String> clone = (HashMap<String, String>) guesses.clone();

        Iterator i = clone.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry pair = (Map.Entry) i.next();
            if (!results.containsKey(pair.getValue())) {
                results.put((String) pair.getValue(), 0);
            }
            results.put((String) pair.getValue(), results.get((String) pair.getValue()) + 1);
            i.remove();
        }
        guesses = new HashMap<>();
        running = true;
        return results;
    }

    public void stop(){
        running = false;
    }

}