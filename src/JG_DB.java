import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JG_DB {
    public String name;
    private JSONObject jsonObj;

    JG_DB(String name) throws IOException, ParseException {
        this.name = name;
        fileCheck(this.name);
        createJSONObj();
        this.put("db_name", this.name);
    }

    public String getName() {
        return this.name;
    }

    private void createJSONObj() throws IOException, ParseException {
        String fileRead = Files.readString(Path.of(this.name + ".json"));
        try {
            this.jsonObj = (JSONObject) new JSONParser().parse(fileRead);
        } catch (Exception exc) {
            Files.writeString(Path.of(this.name + ".json"), "{}");
            fileRead = Files.readString(Path.of(this.name + ".json"));
            this.jsonObj = (JSONObject) new JSONParser().parse(fileRead);
        }

    }

    private void fileCheck(String name) throws IOException {
        if (!Files.exists(Path.of(name + ".json"))) {
            Files.createFile(Path.of(name + ".json"));
        }
    }

    public String get(String key) {
        return (String) jsonObj.get(key);
    }

    public void put(String key, String value) throws IOException, ParseException {
        jsonObj.put(key, value);
        Files.writeString(Path.of(this.name + ".json"), jsonObj.toJSONString());
        this.createJSONObj();
    }
}

class CloneDB {
    String old_name;
    String new_name;

    CloneDB(String old_name, String new_name) {
        this.old_name = old_name;
        this.new_name = new_name;
    }

    public void cloneDB() throws IOException {
        String old_read = Files.readString(Path.of(this.old_name + ".json"));
        if (!Files.exists(Path.of(this.new_name + ".json"))) {
            Files.createFile(Path.of(this.new_name + ".json"));
            Files.writeString(Path.of(this.new_name + ".json"), old_read);
        } else {
            Files.writeString(Path.of(this.new_name + ".json"), old_read);
        }
    }
}
