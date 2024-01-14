import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/*
 * Class that contains the logic of the authentication :
 * It gets the username and the password, then verify if the password matches the username in data.json
 * If its a new username, the client's password and username will be added to this file
 */
public class Authenticator {

    public JSONObject readJSONDataFromFile() {
        JSONObject data = null;
        try (FileReader fileReader = new FileReader("./data.json")) {
            JSONParser jsonParser = new JSONParser();
            data = (JSONObject) jsonParser.parse(fileReader);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return data;
    }

    public boolean authenticate(String username, String password, JSONObject data) {
        boolean usernameExists = false;
        JSONArray users = (JSONArray) data.get("users");
        JSONObject user = null;
        for (Object userObject : users) {
            user = (JSONObject) userObject;
            if (user.get("username").equals(username)) {
                usernameExists = true;
                break;
            }
        }
        if (user != null && user.get("password").equals(password)) {
            return true;
        } else if (!usernameExists) {
            HashMap<String, String> map = new HashMap<>();
            map.put("username", username);
            map.put("password", password);
            JSONObject newUser = new JSONObject(map);
            users.add(newUser);
            try (FileWriter fileWriter = new FileWriter("data.json")) {
                fileWriter.write(data.toJSONString());
            } catch (IOException e) {
                System.out.println("Erreur survenue lors de l'ajout d'un nouveau user au fichier data.json" + e);
            }
            return true;
        }
        return false;
    }
}
