import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class JGossamer {
    public static final String VERSION = "1.0";
    public static JG_DB db;

    JGossamer() throws IOException, ParseException {}

    static {
        try {
            String start_db_name = Files.readString(Path.of("cfg/usage.cfg"));
            db = new JG_DB(start_db_name);
        } catch (Exception exc) {}
    }

    public static void main(String[] args) throws IOException, ParseException {
        Menu menu = new Menu(true);
        menu.drawMenu();
    }
}

class Menu {
    public String state;
    private final Scanner sc;
    private String root_path;

    public void setRoot(String root) {
        this.root_path = root;
    }

    public String getRoot() {
        return this.root_path;
    }

    Menu(boolean intro) {
        this.state = "start";
        this.sc = new Scanner(System.in);

        if (intro) {
            System.out.println("════════════ JG v" + JGossamer.VERSION + " ════════════");
        }
    }

    public void drawMenu() throws IOException, ParseException {
        switch (this.state) {
            case "start" -> {
                System.out.println("\n1. enter");
                System.out.println("2. settings");
                System.out.println("3. quit");
                this.readAnswer();
            }
            case "settings" -> {
                System.out.println("\n1. select root dir");
                System.out.println("2. change DB");
                System.out.println("3. clone DB");
                System.out.println("4. back");
                this.readAnswer();
            }
            case "quit" -> {
                System.exit(0);
            }
            case "enter" -> {
                Units root = new Units(this.root_path).createRoot();

                this.readAnswer();
            }
        }
    }

    public void readAnswer() throws IOException, ParseException {
        switch (this.state) {
            case "start" -> {
                switch (sc.nextLine()) {
                    case "1" -> {
                        if (JGossamer.db.get("root") != null) {
                            this.state = "enter";
                            this.setRoot(JGossamer.db.get("root"));
                            this.drawMenu();
                        } else {
                            System.out.println("no root! Go to settings");
                            this.drawMenu();
                        }
                    }
                    case "2" -> {
                        this.state = "settings";
                        this.drawMenu();
                    }
                    case "3" -> {
                        this.state = "quit";
                        this.drawMenu();
                    }
                    default -> {
                        System.out.println("invalid option!");
                        this.readAnswer();
                    }
                }
            }
            case "settings" -> {
                switch (sc.nextLine()) {
                    case "1" -> {
                        System.out.println("enter path");
                        String path = sc.nextLine();
                        if (Files.exists(Path.of(path))) {
                            JGossamer.db.put("root", path);
                            this.drawMenu();
                        } else {
                            System.out.println("invalid path!");
                            this.drawMenu();
                        }

                    }
                    case "2" -> {
                        System.out.println("enter name");
                        String name = sc.nextLine();

                        if (!Files.exists(Path.of(name + ".json"))) {
                            System.out.println("invalid name!");
                            this.drawMenu();
                        } else {
                            String old_name = JGossamer.db.getName();
                            JGossamer.db = new JG_DB(name);
                            Files.writeString(Path.of("cfg/usage.cfg"), name);
                        }

                        this.state = "settings";
                        this.drawMenu();
                    }
                    case "3" -> {
                        System.out.println("enter base db name");
                        String old_name = sc.nextLine();
                        System.out.println("enter cloned db name");
                        String new_name = sc.nextLine();

                        if (!Files.exists(Path.of(old_name + ".json")) || !Files.exists(Path.of(new_name + ".json"))) {
                            System.out.println("one of these names is invalid!");
                            this.drawMenu();
                        } else {
                            CloneDB cl = new CloneDB(old_name, new_name);
                            cl.cloneDB();

                            this.state = "settings";
                            this.drawMenu();
                        }
                    }
                    case "4" -> {
                        this.state = "start";
                        this.drawMenu();
                    }
                    default -> {
                        System.out.println("invalid option!");
                        this.readAnswer();
                    }
                }
            }
        }
    }
}
