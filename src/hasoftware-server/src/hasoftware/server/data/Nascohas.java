package hasoftware.server.data;

import hasoftware.server.data.auto._Nascohas;

public class Nascohas extends _Nascohas {

    private static Nascohas instance;

    private Nascohas() {}

    public static Nascohas getInstance() {
        if(instance == null) {
            instance = new Nascohas();
        }

        return instance;
    }
}
