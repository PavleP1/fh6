import javax.microedition.lcdui.*;
import javax.microedition.rms.RecordStore;
import java.io.*;

final class Game extends Canvas implements Runnable {
    static final int S_TITLE = 0, S_ENV = 1, S_CAR = 2, S_COUNT = 3, S_RACE = 4, S_PAUSE = 5, S_RES = 6, S_OPT = 7;
    static final int TL = Graphics.TOP | Graphics.LEFT, TC = Graphics.TOP | Graphics.HCENTER, TR = Graphics.TOP | Graphics.RIGHT;

    final Rush midlet;
    final View view = new View();
    final Sim sim = new Sim();
    final Menus ui;
    final Font fs = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    final Font fb = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
    final Font fl = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_LARGE);
    volatile boolean running = true;
    volatile int state;
    boolean music = true, sfx = true, auto = true, showFps, kL, kR, kGas, kBrake, nitroOn, newRec, started;
    int w, h, tick, fps, good, quality, qLevel, mode, envSel, carSel, lean, goT, stage, resRank, rs = 4321;
    final int[] bestLap = new int[3], bestDist = new int[3];
    String toast;
    long toastT, countT;
    Image back;

    Game(Rush m) {
        midlet = m;
        setFullScreenMode(true);
        w = getWidth();
        h = getHeight();
        view.init(w, h);
        ui = new Menus(this);
        load();
        applyQ();
        startDemo(0);
        Snd.start(this);
    }

    void start() {
        if (started) return;
        started = true;
        new Thread(this).start();
    }
    void shutdown() { running = false; save(); }
    void paused() { save(); if (state == S_RACE) { clearKeys(); state = S_PAUSE; } }
    protected void hideNotify() { paused(); }

    int rnd(int n) { rs = rs * 1103515245 + 12345; return ((rs >>> 16) & 0x7fff) % n; }
    int act(int k) { try { return getGameAction(k); } catch (Exception e) { return 0; } }
    void say(String s) { toast = s; toastT = System.currentTimeMillis(); }
    void clearKeys() { kL = false; kR = false; kGas = false; kBrake = false; }

    // ---------- save / load
    void load() {
        byte[] d = null;
        RecordStore r = null;
        try {
            r = RecordStore.openRecordStore("rush1", true);
            if (r.getNumRecords() > 0) d = r.getRecord(1);
        } catch (Exception e) {
        } finally {
            if (r != null) try { r.closeRecordStore(); } catch (Exception e) {}
        }
        if (d == null) return;
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(d));
            music = in.readBoolean(); sfx = in.readBoolean(); auto = in.readBoolean(); showFps = in.readBoolean();
            quality = in.readInt();
            for (int i = 0; i < 3; i++) { bestLap[i] = in.readInt(); bestDist[i] = in.readInt(); }
        } catch (Exception e) {}
        if (quality < 0 || quality > 3) quality = 0;
    }

    synchronized void save() {
        RecordStore r = null;
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            DataOutputStream o = new DataOutputStream(bo);
            o.writeBoolean(music); o.writeBoolean(sfx); o.writeBoolean(auto); o.writeBoolean(showFps);
            o.writeInt(quality);
            for (int i = 0; i < 3; i++) { o.writeInt(bestLap[i]); o.writeInt(bestDist[i]); }
            o.flush();
            byte[] d = bo.toByteArray();
            r = RecordStore.openRecordStore("rush1", true);
            if (r.getNumRecords() == 0) r.addRecord(d, 0, d.length); else r.setRecord(1, d, 0, d.length);
        } catch (Exception e) {
        } finally {
            if (r != null) try { r.closeRecordStore(); } catch (Exception e) {}
        }
    }

    // ---------- game flow
    void applyQ() {
        int lv = quality == 0 ? qLevel : quality - 1;
        view.step = lv == 0 ? 2 : (lv == 1 ? 3 : 4);
        view.setDraw(lv == 0 ? 66 : (lv == 1 ? 54 : 42));
    }

    void adapt() {
        if (quality != 0) return;
        if (fps < 22 && qLevel < 2) { qLevel++; good = 0; applyQ(); }
        else if (fps >= 29 && qLevel > 0) { if (++good >= 4) { qLevel--; good = 0; applyQ(); } }
        else good = 0;
    }

    void startDemo(int e) {
        sim.start(2, e, 0, 4000 + e * 31);
        view.setEnv(e);
        view.bgx = 0;
        Snd.want = 3;
    }

    void startRace() {
        sim.start(mode, envSel, carSel, 2000 + envSel * 137);
        view.setEnv(envSel);
        view.bgx = 0;
        lean = 0; nitroOn = false; newRec = false; goT = 0; stage = -1;
        clearKeys();
        countT = System.currentTimeMillis();
        state = S_COUNT;
        Snd.want = envSel;
    }

    void finishRace() {
        newRec = false;
        int e = sim.env;
        if (sim.mode == 0) {
            resRank = sim.rankNow();
            if (sim.bestLap > 0 && (bestLap[e] == 0 || sim.bestLap < bestLap[e])) { bestLap[e] = sim.bestLap; newRec = true; }
        } else {
            int d = sim.prog / 103;
            if (d > bestDist[e]) { bestDist[e] = d; newRec = true; }
        }
        save();
        clearKeys();
        state = S_RES;
        Snd.sfx(Snd.FIN);
    }

    void step() {
        tick++;
        switch (state) {
            case S_TITLE: case S_ENV: case S_CAR:
                sim.demoTick();
                view.bgx += sim.bgd;
                break;
            case S_COUNT: {
                int st = (int) ((System.currentTimeMillis() - countT) / 850);
                if (st != stage) {
                    stage = st;
                    if (st < 3) Snd.sfx(Snd.BEEP);
                    else { Snd.sfx(Snd.GO); goT = 24; state = S_RACE; }
                }
                break;
            }
            case S_RACE: stepRace(); break;
        }
    }

    void stepRace() {
        sim.tick(kL, kR, auto || kGas, kBrake, nitroOn);
        if (sim.nitro == 0) nitroOn = false;
        view.bgx += sim.bgd;
        int tl = kL ? -w / 36 : (kR ? w / 36 : 0);
        lean += (tl - lean) / 3;
        if (goT > 0) goT--;
        int ev = sim.ev;
        if ((ev & Sim.EV_CRASH) != 0) Snd.sfx(Snd.CRASH);
        if ((ev & Sim.EV_GATE) != 0) { say("+22 sec"); Snd.sfx(Snd.GATE); }
        if ((ev & Sim.EV_LAP) != 0) { say("Lap " + (sim.lapsDone + 1) + "/" + sim.laps); Snd.sfx(Snd.LAP); }
        if ((ev & (Sim.EV_FIN | Sim.EV_TIME)) != 0) finishRace();
    }

    public void run() {
        long last = System.currentTimeMillis(), acc = 0, fpsT = last;
        int fr = 0;
        while (running) {
            long now = System.currentTimeMillis();
            acc += now - last;
            last = now;
            if (state <= S_RACE) {
                int n = 0;
                while (acc >= 33 && n < 3) { step(); acc -= 33; n++; }
                if (acc > 99) acc = 0;
                repaint();
                serviceRepaints();
                fr++;
                if (now - fpsT >= 1000) { fps = fr; fr = 0; fpsT = now; adapt(); }
                long dt = System.currentTimeMillis() - now;
                if (dt < 28) { try { Thread.sleep(28 - dt); } catch (InterruptedException e) {} }
            } else {
                acc = 0;
                try { Thread.sleep(60); } catch (InterruptedException e) {}
            }
        }
    }

    // ---------- input
    protected void keyPressed(int k) { key(k, true); }
    protected void keyReleased(int k) { key(k, false); }

    void key(int k, boolean down) {
        int a = act(k);
        boolean lf = k == -6 || k == -21, rt = k == -7 || k == -22;
        if (state == S_RACE) {
            if (a == LEFT) kL = down;
            else if (a == RIGHT) kR = down;
            else if (a == UP) kGas = down;
            else if (a == DOWN) kBrake = down;
            else if (down && (a == FIRE || lf)) {
                if (sim.nitro > 80) { nitroOn = !nitroOn; if (nitroOn) Snd.sfx(Snd.NITRO); }
            } else if (down && (rt || k == KEY_POUND)) { clearKeys(); ui.pSel = 0; state = S_PAUSE; }
            return;
        }
        if (!down) return;
        ui.key(k, a, lf, rt);
        repaint();
    }

    // ---------- drawing
    protected void paint(Graphics g) {
        int nw = getWidth(), nh = getHeight();
        if (nw != w || nh != h) { w = nw; h = nh; view.init(w, h); applyQ(); view.setEnv(sim.env); }
        Graphics gg = g;
        if (!isDoubleBuffered()) {
            try {
                if (back == null || back.getWidth() != w || back.getHeight() != h) back = Image.createImage(w, h);
                gg = back.getGraphics();
            } catch (Throwable e) { gg = g; back = null; }
        }
        switch (state) {
            case S_TITLE: view.render(gg, sim, tick); ui.title(gg); break;
            case S_ENV: view.render(gg, sim, tick); ui.env(gg); break;
            case S_CAR: view.render(gg, sim, tick); ui.car(gg); break;
            case S_COUNT: drawWorld(gg); ui.count(gg); break;
            case S_RACE: drawWorld(gg); break;
            case S_PAUSE: drawWorld(gg); ui.pause(gg); break;
            case S_RES: drawWorld(gg); ui.results(gg); break;
            case S_OPT: ui.options(gg); break;
        }
        if (toast != null && System.currentTimeMillis() - toastT < 1600 && state != S_OPT) {
            int tw = fb.stringWidth(toast) + 20;
            g.setColor(0x14172A);
            gg.fillRoundRect((w - tw) / 2, h * 22 / 100, tw, fb.getHeight() + 8, 10, 10);
            ui.txt(gg, fb, toast, w / 2, h * 22 / 100 + 4, TC, 0xFFD60A);
        }
        if (gg != g) g.drawImage(back, 0, 0, TL);
    }

    void drawWorld(Graphics g) {
        int sk = sim.crash > 0 ? 1 + sim.crash / 7 : 0;
        int tx = (tick & 1) == 0 ? sk : -sk, ty = ((tick >> 1) & 1) == 0 ? sk : -sk;
        if (sk > 0) g.translate(tx, ty);
        view.render(g, sim, tick);
        drawPlayer(g);
        drawFx(g);
        if (sk > 0) g.translate(-tx, -ty);
        ui.hud(g);
    }

    void drawPlayer(Graphics g) {
        int pw = w * 30 / 100, cx = w / 2, by = h - 30 - (((tick & 1) != 0 && sim.speed > 2000) ? 1 : 0);
        int u = pw / 32;
        if (sim.boost && u > 0) {
            int f = 5 + rnd(5);
            g.setColor(0xFF8A00);
            g.fillTriangle(cx - 8 * u, by - 3 * u, cx - 4 * u, by - 3 * u, cx - 6 * u, by + f * u);
            g.fillTriangle(cx + 4 * u, by - 3 * u, cx + 8 * u, by - 3 * u, cx + 6 * u, by + f * u);
            g.setColor(0xFFE860);
            g.fillTriangle(cx - 7 * u, by - 3 * u, cx - 5 * u, by - 3 * u, cx - 6 * u, by + f * u / 2);
            g.fillTriangle(cx + 5 * u, by - 3 * u, cx + 7 * u, by - 3 * u, cx + 6 * u, by + f * u / 2);
        }
        Cars.car(g, 0, cx, by, pw, Sim.CAR_COL[sim.car], Sim.CAR_ACC_COL[sim.car], lean, sim.braking);
        if ((sim.px < -1000 || sim.px > 1000) && sim.speed > 500) {
            g.setColor(Cars.mix(Env.GR_A[sim.env], 0x6B4A2B, 140));
            for (int i = 0; i < 6; i++) g.fillRect(cx - pw / 2 + rnd(pw), by - rnd(8), 2 + rnd(4), 2 + rnd(3));
        }
    }

    void drawFx(Graphics g) {
        int n = sim.boost ? 10 : (sim.speed > sim.maxSpd * 3 / 4 ? 4 : 0);
        g.setColor(0xFFFFFF);
        for (int i = 0; i < n; i++) {
            int x = rnd(2) == 0 ? rnd(w / 5) : w - rnd(w / 5);
            int y = view.cy + 10 + rnd(h - view.cy - 40);
            int len = 8 + rnd(18);
            g.drawLine(x, y, x + (x - w / 2) * len / (w / 2), y + len / 3);
        }
    }
}
