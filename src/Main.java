import gui.BoggleSwingApp;

public class Main {
    private static Main instance;


    private Main() {}

    public static Main getInstance() {
        if (instance == null) {
            instance = new Main();
        }
        return instance;
    }

    public void run(String[] args) {
        BoggleSwingApp.iniciarInterfaz(args);
    }

    public static void main(String[] args) {
        Main app = Main.getInstance();
        app.run(args);
    }
}
