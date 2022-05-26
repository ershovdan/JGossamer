import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Units {
    String root_path;
    DirUnit root;

    Units(String root) {
        this.root_path = root;
    }

    public Units createRoot() throws IOException, ParseException {
        this.root = new DirUnit(this.root_path);
        this.root.childTable();
        return null;
    }
}

abstract class FSUnit {
    String path;
    Date date;
    String parent;
    String type;

    public String getPath() {
        return this.path;
    }

    public Date getDate() {
        return this.date;
    }

    public String getParent() {
        return this.parent;
    }

    public String getFinalName(String full_name) {
        return full_name.substring(full_name.lastIndexOf("/") + 1);
    }
}

class FileUnit extends FSUnit {
    File file;

    FileUnit(String path) {
        this.path = path;
        file = new File(this.path);
        this.date = new Date(file.lastModified());
        this.parent = file.getParent();
        this.type = "file";
    }
}

class DirUnit extends FSUnit {
    ArrayList<String> children;
    ArrayList<String> children_dates;

    DirUnit(String path) {
        this.type = "dir";
        this.path = path;
        File[] children_file = new File(this.path).listFiles();

        this.children = new ArrayList<String>();
        this.children_dates = new ArrayList<String>();

        if (children_file != null) {
            for (File i: children_file) {
                if (this.getFinalName(i.toString()).length() <= 20) {
                    this.children.add(this.getFinalName(i.toString()) + " ".repeat(20 - this.getFinalName(i.toString()).length()));
                } else {
                    this.children.add(this.getFinalName(i.toString()).substring(0, 17) + "...");
                }
                this.children_dates.add(new Date(i.lastModified()).toString());
            }
        }

    }

    public void childTable() throws IOException, ParseException {
        this.childTable(this.children.size());
    }

    public void childTable(int numberOfRows) throws IOException, ParseException {
        HashMap<Integer, String> codes = new HashMap<Integer, String>();
        int numberOfRowsRight = numberOfRows;

        if (this.children.size() < numberOfRows) {
            numberOfRowsRight = this.children.size();
        }

        try {
            if (!(new File(this.path).getParentFile().getPath().toString().equals("/") && this.path.equals("/"))) {
                System.out.println(" 0 ║ ..");
            }
        } catch (NullPointerException exc) {}

        for (int i = 0; i < numberOfRowsRight; i++) {
            if (i <= 8) {
                System.out.print(" " + String.valueOf(i + 1) + " ║ ");
            } else {
                System.out.print(String.valueOf(i + 1) + " ║ ");
            }
            System.out.print(this.children.get(i) + " ║");
            System.out.println(" " + this.children_dates.get(i));
            codes.put(i + 1, this.children.get(i));
        }

        System.out.println(":q quit  :rn rename  :rm remove  :mv move  :c create  :cdir create dir");
        this.readCode(codes);
    }

    public void readCode(HashMap<Integer, String> codes) throws IOException, ParseException {
        Scanner sc = new Scanner(System.in);
        int ans = -1;
        String ans_str;
        boolean isCommand = false;

        ans_str = sc.nextLine();

        try {
            ans = Integer.parseInt(ans_str);
        } catch (NumberFormatException exc) {
            isCommand = true;
        }

        if (!isCommand) {
            if (codes.containsKey(ans)) {
                ChooseType chooser = new ChooseType(this.path + codes.get(ans).replace(" ", "") + "/");

                if (chooser.checkForDir()) {
                    DirUnit new_dir = new DirUnit(this.path + codes.get(ans).replace(" ", "") + "/");
                    new_dir.childTable();
                } else {
                    System.out.println("not a directory!");
                    this.readCode(codes);
                }
            } else {
                if (ans == 0) {
                    File parent_dir = new File(this.path).getParentFile();

                    try {
                        DirUnit new_dir = new DirUnit(parent_dir.getPath().toString());
                        new_dir.childTable();
                    } catch (NullPointerException exc) {
                        System.out.println("can't move upper!");
                        this.readCode(codes);
                    }

                } else {
                    System.out.println("invalid code!");
                    this.readCode(codes);
                }
            }
        } else {
            switch (ans_str) {
                case ":q" -> {
                    Menu menu = new Menu(false);
                    menu.drawMenu();
                }
                case ":rn" -> {
                    System.out.println("enter current name");
                    String current_name = sc.nextLine();
                    System.out.println("enter new name");
                    String new_name = sc.nextLine();

                    try {
                        Path source = Path.of(this.path + "/" + current_name);
                        Files.move(source, source.resolveSibling(new_name));
                    } catch (Exception exc) {
                        System.out.println("wrong file name!");
                        System.out.println();
                        this.childTable();;
                    }

                    System.out.println();
                    DirUnit root = new DirUnit(this.path);
                    root.childTable();
                }
                case ":mv" -> {
                    System.out.println("enter current name");
                    String current_name = sc.nextLine();
                    System.out.println("enter new location");
                    String new_location = sc.nextLine();

                    Path source = null;
                    Path dest = null;

                    try {
                        source = Path.of(this.path + "/" + current_name);
                    } catch (Exception exc) {
                        System.out.println("wrong file name!");
                        System.out.println();
                        this.childTable();
                    }

                    try {
                        dest = Path.of(new_location + "/" + current_name);
                    } catch (Exception exc) {
                        System.out.println("wrong location!");
                        System.out.println();
                        this.childTable();
                    }

                    try {
                        Files.move(source, dest);
                    } catch (Exception exc) {
                        System.out.println(exc);
                        System.out.println();
                        this.childTable();
                    }


                    System.out.println();
                    DirUnit root = new DirUnit(this.path);
                    root.childTable();
                }
                case ":rm" -> {
                    System.out.println("enter file name");
                    String name = sc.nextLine();

                    try {
                        Files.delete(Path.of(this.path + "/" + name));
                    } catch (Exception exc) {
                        System.out.println("wrong file name!");
                        System.out.println();
                        this.childTable();;
                    }

                    System.out.println();
                    DirUnit root = new DirUnit(this.path);
                    root.childTable();
                }
                case ":c" -> {
                    System.out.println("enter file name");
                    String name = sc.nextLine();

                    try {
                        Files.createFile(Path.of(this.path + "/" + name));
                    } catch (Exception exc) {
                        System.out.println("something went wrong!");
                        System.out.println();
                        this.childTable();;
                    }

                    System.out.println();
                    DirUnit root = new DirUnit(this.path);
                    root.childTable();
                }
                case ":cdir" -> {
                    System.out.println("enter dir name");
                    String name = sc.nextLine();

                    try {
                        Files.createDirectories(Path.of(this.path + "/" + name));
                    } catch (Exception exc) {
                        System.out.println("something went wrong!");
                        System.out.println();
                        this.childTable();;
                    }

                    System.out.println();
                    DirUnit root = new DirUnit(this.path);
                    root.childTable();
                }
                default -> {
                    System.out.println("invalid command!");
                    this.readCode(codes);
                }
            }

        }
    }
}

class ChooseType {
    String path;
    File file;

    ChooseType(String path) {
        this.path = path;
        this.file = new File(path);
    }

    public boolean checkForDir() {
        if (file.isDirectory()) {
            return true;
        }

        return false;
    }
}