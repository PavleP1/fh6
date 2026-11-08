import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.VolumeControl;
import java.io.ByteArrayInputStream;

final class Snd implements Runnable {
    static final int SELECT = 0, BACK = 1, BEEP = 2, GO = 3, CRASH = 4, GATE = 5, LAP = 6, FIN = 7, NITRO = 8;
    static final int[][] PAT = {
        {84, 25}, {72, 40}, {72, 110}, {84, 280}, {45, 70, 38, 130},
        {79, 60, 84, 100}, {76, 50, 79, 50, 84, 110}, {72, 90, 76, 90, 79, 90, 84, 320}, {70, 35, 77, 45}
    };
    static final String[] TYPES = {"audio/midi", "audio/mid", "audio/x-midi"};
    static final Object lock = new Object();
    static final int[][] q = new int[4][];
    static int qn, curKey = -9;
    static volatile int want = -1;
    static Game game;
    static Thread th;
    static Player mp;

    static void start(Game g) {
        if (th != null) return;
        game = g;
        th = new Thread(new Snd());
        th.start();
    }

    static void sfx(int id) {
        if (th == null || !game.sfx) return;
        synchronized (lock) {
            if (qn < 4) { q[qn++] = PAT[id]; lock.notify(); }
        }
    }

    static void wake() {
        synchronized (lock) { lock.notify(); }
    }

    public void run() {
        while (game.running) {
            int[] pat = null;
            synchronized (lock) {
                if (qn == 0) {
                    try { lock.wait(300); } catch (InterruptedException e) {}
                }
                if (qn > 0) {
                    pat = q[0];
                    qn--;
                    for (int i = 0; i < qn; i++) q[i] = q[i + 1];
                }
            }
            try { sync(); } catch (Throwable e) {}
            if (pat != null) play(pat);
        }
        stopMusic();
        th = null;
    }

    static void sync() {
        int w = (game.running && game.isShown() && game.music) ? want : -1;
        if (w == curKey) return;
        stopMusic();
        curKey = w;
        if (w >= 0) startMusic(w);
    }

    static void startMusic(int t) {
        byte[] mid = Midi.build(t);
        for (int i = 0; i < TYPES.length && mp == null; i++) {
            Player p = null;
            try {
                p = Manager.createPlayer(new ByteArrayInputStream(mid), TYPES[i]);
                p.realize();
                p.setLoopCount(-1);
                try {
                    VolumeControl v = (VolumeControl) p.getControl("VolumeControl");
                    if (v != null) v.setLevel(80);
                } catch (Throwable e) {}
                p.start();
                mp = p;
            } catch (Throwable e) {
                if (p != null) { try { p.close(); } catch (Throwable e2) {} }
            }
        }
    }

    static void stopMusic() {
        if (mp != null) {
            try { mp.stop(); } catch (Throwable e) {}
            try { mp.close(); } catch (Throwable e) {}
            mp = null;
        }
    }

    static void play(int[] pat) {
        for (int i = 0; i + 1 < pat.length; i += 2) {
            try { Manager.playTone(pat[i], pat[i + 1], 90); } catch (Throwable e) { return; }
            try { Thread.sleep(pat[i + 1]); } catch (InterruptedException e) {}
        }
    }
}
