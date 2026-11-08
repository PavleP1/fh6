import javax.microedition.lcdui.*;

final class Menus {
    static final String[] TITLE = {"Circuit race", "Time attack", "Options", "Exit"};
    static final String[] OPT = {"Music", "Sound effects", "Auto gas", "Quality", "FPS counter", "Reset records"};
    static final String[] QN = {"Auto", "High", "Medium", "Low"};
    static final int TC = Game.TC, TL = Game.TL, TR = Game.TR;
    final Game G;
    int tSel, oSel, pSel;

    Menus(Game g) { G = g; }

    void txt(Graphics g, Font f, String s, int x, int y, int anc, int col) {
        g.setFont(f);
        g.setColor(0x000000);
        g.drawString(s, x + 1, y + 1, anc);
        g.setColor(col);
        g.drawString(s, x, y, anc);
    }

    void big(Graphics g, String s, int x, int y, int col) {
        g.setFont(G.fl);
        g.setColor(0x000000);
        g.drawString(s, x - 1, y, TC);
        g.drawString(s, x + 1, y, TC);
        g.drawString(s, x, y - 1, TC);
        g.drawString(s, x, y + 2, TC);
        g.setColor(col);
        g.drawString(s, x, y, TC);
    }

    void panel(Graphics g, int x, int y, int pw, int ph, boolean sel) {
        g.setColor(sel ? 0xFFD60A : 0x3A4060);
        g.fillRoundRect(x, y, pw, ph, 12, 12);
        g.setColor(sel ? 0x2A1E00 : 0x14172A);
        g.fillRoundRect(x + 2, y + 2, pw - 4, ph - 4, 10, 10);
    }

    static String fmt(int ms) {
        int s = ms / 1000;
        return (s / 60) + ":" + Hub2.two(s % 60) + "." + ((ms / 100) % 10);
    }

    // ---------- screens
    void title(Graphics g) {
        int w = G.w, h = G.h;
        g.setColor(0xFF2D55);
        g.fillRect(0, 20, w, 4);
        g.setColor(0xFFD60A);
        g.fillRect(0, 26, w, 2);
        g.setFont(G.fl);
        g.setColor(0xB01030);
        g.drawString("TURBO", w / 2 + 3, 36, TC);
        g.drawString("RUSH", w / 2 + 3, 36 + G.fl.getHeight(), TC);
        big(g, "TURBO", w / 2, 33, 0xFFD60A);
        big(g, "RUSH", w / 2, 33 + G.fl.getHeight(), 0xFF6A00);
        int y0 = h * 52 / 100;
        for (int i = 0; i < 4; i++) {
            panel(g, 30, y0 + i * 34, w - 60, 28, i == tSel);
            txt(g, G.fb, TITLE[i], w / 2, y0 + i * 34 + 7, TC, 0xFFFFFF);
        }
    }

    void env(Graphics g) {
        int w = G.w, h = G.h, e = G.envSel;
        big(g, G.mode == 0 ? "CIRCUIT RACE" : "TIME ATTACK", w / 2, 12, 0xFFFFFF);
        int py = h - 96;
        panel(g, 12, py, w - 24, 84, true);
        txt(g, G.fb, "<  " + Env.NAME[e] + "  >", w / 2, py + 10, TC, 0xFFD60A);
        txt(g, G.fs, Env.DESC[e], w / 2, py + 12 + G.fb.getHeight(), TC, 0xDDDDDD);
        String rec = G.mode == 0 ? (G.bestLap[e] > 0 ? "Best lap " + fmt(G.bestLap[e]) : "No lap record") : (G.bestDist[e] > 0 ? "Best " + G.bestDist[e] + " m" : "No record yet");
        txt(g, G.fs, rec, w / 2, py + 16 + G.fb.getHeight() + G.fs.getHeight(), TC, 0x80E0FF);
        txt(g, G.fs, "FIRE: choose car   Back", w / 2, py + 62, TC, 0xAAAAAA);
    }

    void bar(Graphics g, String label, int x, int y, int bw, int pct) {
        txt(g, G.fs, label, x, y, TL, 0xFFFFFF);
        int bx = x + 62;
        g.setColor(0x000000);
        g.fillRect(bx, y + 2, bw, G.fs.getHeight() - 4);
        g.setColor(Cars.mix(0xFF3B30, 0x30D158, pct * 255 / 100));
        g.fillRect(bx, y + 2, bw * pct / 100, G.fs.getHeight() - 4);
    }

    void car(Graphics g) {
        int w = G.w, h = G.h, c = G.carSel;
        big(g, "SELECT CAR", w / 2, 12, 0xFFFFFF);
        g.setColor(0x101010);
        g.fillRoundRect(w / 2 - w * 28 / 100, h * 44 / 100, w * 56 / 100, 10, 8, 8);
        Cars.car(g, 0, w / 2, h * 44 / 100 + 6, w * 46 / 100, Sim.CAR_COL[c], Sim.CAR_ACC_COL[c], 0, false);
        int py = h - 100;
        panel(g, 12, py, w - 24, 90, true);
        txt(g, G.fb, "<  " + Sim.CAR_NAME[c] + "  >", w / 2, py + 8, TC, 0xFFD60A);
        int by = py + 12 + G.fb.getHeight();
        bar(g, "Speed", 24, by, w - 110, (Sim.CAR_MAX[c] - 6000) * 100 / 3000);
        bar(g, "Accel", 24, by + G.fs.getHeight() + 3, w - 110, Sim.CAR_ACC[c] * 100 / 130);
        bar(g, "Handling", 24, by + 2 * (G.fs.getHeight() + 3), w - 110, Sim.CAR_HND[c] * 100 / 125);
        txt(g, G.fs, "FIRE: start race", w / 2, py + 72, TC, 0xAAAAAA);
    }

    void count(Graphics g) {
        int st = G.stage;
        String s = st < 0 ? "" : (st == 0 ? "3" : (st == 1 ? "2" : "1"));
        if (s.length() > 0) big(g, s, G.w / 2, G.h / 3, 0xFFD60A);
    }

    void options(Graphics g) {
        int w = G.w, h = G.h;
        g.setColor(0x10152A);
        g.fillRect(0, 0, w, h);
        big(g, "OPTIONS", w / 2, 12, 0xFFD60A);
        for (int i = 0; i < 6; i++) {
            int y = 56 + i * 38;
            panel(g, 14, y, w - 28, 32, i == oSel);
            txt(g, G.fb, OPT[i], 26, y + 9, TL, 0xFFFFFF);
            String v = "";
            if (i == 0) v = G.music ? "On" : "Off";
            else if (i == 1) v = G.sfx ? "On" : "Off";
            else if (i == 2) v = G.auto ? "On" : "Off";
            else if (i == 3) v = QN[G.quality];
            else if (i == 4) v = G.showFps ? "On" : "Off";
            else v = "FIRE";
            txt(g, G.fb, v, w - 26, y + 9, TR, 0xFFD60A);
        }
        txt(g, G.fs, "Back saves", w / 2, h - 18, TC, 0x888888);
    }

    void pause(Graphics g) {
        int w = G.w, h = G.h;
        String[] n = {"Resume", "Restart", "Quit"};
        panel(g, 30, h / 2 - 80, w - 60, 150, false);
        big(g, "PAUSED", w / 2, h / 2 - 72, 0xFFFFFF);
        for (int i = 0; i < 3; i++) {
            panel(g, 44, h / 2 - 36 + i * 36, w - 88, 30, i == pSel);
            txt(g, G.fb, n[i], w / 2, h / 2 - 36 + i * 36 + 8, TC, 0xFFFFFF);
        }
    }

    void results(Graphics g) {
        int w = G.w, h = G.h, lh = G.fs.getHeight() + 6;
        Sim s = G.sim;
        panel(g, 14, h / 2 - 96, w - 28, 192, true);
        String head, l1, l2, l3;
        if (s.mode == 0) {
            head = resultHead(G.resRank);
            l1 = "Position " + G.resRank + " of 6";
            l2 = "Total " + fmt(s.ticks * 33);
            l3 = "Best lap " + (s.bestLap > 0 ? fmt(s.bestLap) : "-");
        } else {
            head = "TIME UP";
            l1 = "Distance " + (s.prog / 103) + " m";
            l2 = "Best " + G.bestDist[s.env] + " m";
            l3 = "Checkpoints reached";
        }
        big(g, head, w / 2, h / 2 - 88, 0xFFD60A);
        int y = h / 2 - 88 + G.fl.getHeight() + 10;
        txt(g, G.fs, l1, w / 2, y, TC, 0xFFFFFF);
        txt(g, G.fs, l2, w / 2, y + lh, TC, 0xFFFFFF);
        txt(g, G.fs, l3, w / 2, y + 2 * lh, TC, 0xFFFFFF);
        if (G.newRec) txt(g, G.fb, "NEW RECORD!", w / 2, y + 3 * lh + 2, TC, 0x30D158);
        txt(g, G.fs, "Left: retry   FIRE: menu", w / 2, h / 2 + 76, TC, 0xAAAAAA);
    }

    String resultHead(int r) { return r == 1 ? "1ST PLACE!" : (r == 2 ? "2ND PLACE" : (r == 3 ? "3RD PLACE" : r + "TH PLACE")); }

    // ---------- HUD
    void hud(Graphics g) {
        Sim s = G.sim;
        int w = G.w, h = G.h, len = s.tk.len;
        int kmh = s.speed * 280 / 8000;
        txt(g, G.fl, "" + kmh, w - 8, h - 14 - G.fl.getHeight() - G.fs.getHeight() + 2, TR, 0xFFFFFF);
        txt(g, G.fs, "km/h", w - 8, h - 14 - G.fs.getHeight(), TR, 0xFFD60A);
        int pct = s.speed * 100 / (s.maxSpd * 125 / 100);
        if (pct > 100) pct = 100;
        g.setColor(0x101010);
        g.fillRect(8, h - 8, w - 16, 5);
        g.setColor(Cars.mix(0x30D158, 0xFF3B30, pct * 255 / 100));
        g.fillRect(8, h - 8, (w - 16) * pct / 100, 5);
        int nh = 50, nl = nh * s.nitro / 1000;
        g.setColor(0x101010);
        g.fillRect(6, h - 76, 8, nh + 2);
        g.setColor(G.nitroOn ? 0xFFFFFF : 0x28C8FF);
        g.fillRect(7, h - 75 + nh - nl, 6, nl);
        if (s.mode == 0) {
            txt(g, G.fb, "POS " + s.rankNow() + "/6", 6, 4, TL, 0xFFFFFF);
            txt(g, G.fb, "LAP " + Math.min(s.lapsDone + 1, s.laps) + "/" + s.laps, w - 6, 4, TR, 0xFFFFFF);
            txt(g, G.fs, Menus.fmt(s.ticks * 33), w / 2, 4, TC, 0xFFD60A);
            int y = G.fb.getHeight() + 9;
            g.setColor(0x202020);
            g.fillRect(10, y, w - 20, 3);
            for (int c = 0; c < s.nc; c++)
                if (s.crace[c] != 0) { g.setColor(s.ccol[c]); g.fillRect(10 + (s.cp[c] % len) * (w - 20) / len - 1, y - 1, 3, 5); }
            g.setColor(0xFFFFFF);
            g.fillRect(10 + ((s.prog + Sim.PZ) % len) * (w - 20) / len - 2, y - 2, 5, 7);
        } else {
            int sec = (s.tleft + 999) / 1000;
            txt(g, G.fl, "" + sec, w / 2, 2, TC, sec < 10 ? 0xFF3B30 : 0xFFD60A);
            txt(g, G.fb, (s.prog / 103) + " m", 6, 4, TL, 0xFFFFFF);
        }
        if (G.goT > 0) big(g, "GO!", w / 2, h / 3, 0x30D158);
        if (G.showFps) txt(g, G.fs, "FPS " + G.fps + " Q" + G.qLevel, 6, G.fb.getHeight() + 16, TL, 0x80FF80);
    }

    // ---------- keys
    void key(int k, int a, boolean lf, boolean rt) {
        switch (G.state) {
            case Game.S_TITLE:
                if (a == Canvas.UP && tSel > 0) tSel--;
                else if (a == Canvas.DOWN && tSel < 3) tSel++;
                else if (rt) G.midlet.exit();
                else if (a == Canvas.FIRE || lf) {
                    Snd.sfx(Snd.SELECT);
                    if (tSel < 2) { G.mode = tSel; G.startDemo(G.envSel); G.state = Game.S_ENV; }
                    else if (tSel == 2) { oSel = 0; G.state = Game.S_OPT; }
                    else G.midlet.exit();
                }
                break;
            case Game.S_ENV:
                if (a == Canvas.LEFT || a == Canvas.RIGHT) { G.envSel = (G.envSel + (a == Canvas.RIGHT ? 1 : 2)) % 3; G.startDemo(G.envSel); Snd.sfx(Snd.SELECT); }
                else if (a == Canvas.FIRE || lf) { G.state = Game.S_CAR; Snd.sfx(Snd.SELECT); }
                else if (rt) { G.state = Game.S_TITLE; Snd.sfx(Snd.BACK); }
                break;
            case Game.S_CAR:
                if (a == Canvas.LEFT || a == Canvas.RIGHT) { G.carSel = (G.carSel + (a == Canvas.RIGHT ? 1 : 2)) % 3; Snd.sfx(Snd.SELECT); }
                else if (a == Canvas.FIRE || lf) G.startRace();
                else if (rt) { G.state = Game.S_ENV; Snd.sfx(Snd.BACK); }
                break;
            case Game.S_PAUSE:
                if (a == Canvas.UP && pSel > 0) pSel--;
                else if (a == Canvas.DOWN && pSel < 2) pSel++;
                else if (rt) G.state = Game.S_RACE;
                else if (a == Canvas.FIRE || lf) {
                    if (pSel == 0) G.state = Game.S_RACE;
                    else if (pSel == 1) G.startRace();
                    else { G.startDemo(G.envSel); G.state = Game.S_TITLE; }
                }
                break;
            case Game.S_RES:
                if (lf) G.startRace();
                else if (a == Canvas.FIRE || rt) { G.startDemo(G.envSel); G.state = Game.S_TITLE; }
                break;
            case Game.S_OPT:
                if (rt) { G.save(); G.state = Game.S_TITLE; break; }
                if (a == Canvas.UP && oSel > 0) oSel--;
                else if (a == Canvas.DOWN && oSel < 5) oSel++;
                else if (a == Canvas.LEFT || a == Canvas.RIGHT || a == Canvas.FIRE || lf) opt(a == Canvas.LEFT ? -1 : 1);
                break;
        }
    }

    void opt(int d) {
        if (oSel == 0) { G.music = !G.music; Snd.wake(); }
        else if (oSel == 1) { G.sfx = !G.sfx; Snd.sfx(Snd.SELECT); }
        else if (oSel == 2) G.auto = !G.auto;
        else if (oSel == 3) { G.quality = (G.quality + d + 4) % 4; G.applyQ(); }
        else if (oSel == 4) G.showFps = !G.showFps;
        else { for (int i = 0; i < 3; i++) { G.bestLap[i] = 0; G.bestDist[i] = 0; } G.say("Records reset"); }
        G.save();
    }
}

final class Hub2 {
    static String two(int v) { return v < 10 ? "0" + v : "" + v; }
}
