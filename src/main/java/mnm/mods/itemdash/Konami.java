package mnm.mods.itemdash;

import static org.lwjgl.input.Keyboard.*;

public class Konami {

    private static final int[] CODE = { KEY_UP, KEY_UP, KEY_DOWN, KEY_DOWN, KEY_LEFT, KEY_RIGHT, KEY_LEFT, KEY_RIGHT, KEY_B, KEY_A, KEY_RETURN };

    private final Runnable toRun;
    private int current;

    public Konami(Runnable onComplete) {
        this.toRun = onComplete;
    }

    public void onKey(int code) {
        if (code == CODE[current]) {
            current++;
        } else {
            current = 0;
        }
        if (current == CODE.length) {
            toRun.run();
            current = 0;
        }
    }
}
