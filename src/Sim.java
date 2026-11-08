final class Sim {
    static final int SEG = Track.SEG, PZ = 840, MAXC = 20;
    static final int EV_CRASH = 1, EV_GATE = 2, EV_LAP = 4, EV_FIN = 8, EV_TIME = 16;
    static final String[] CAR_NAME = {"Falcon", "Viper", "Rally"};
    static final int[] CAR_MAX = {8000, 8800, 7400};
    static final int[] CAR_ACC = {100, 86, 125};
    static final int[] CAR_HND = {100, 86, 120};
    static final int[] CAR_COL = {0xD62828, 0x2D6CDF, 0xE6B800};
    static final int[] CAR_ACC_COL = {0xFFFFFF, 0xFFD60A, 0x202020};
    static final int[] LANES = {-666, 0, 666};
    static final int[] TCOL = {0x2D9CDB, 0xE0E0E0, 0x2EAD4B, 0x8E44AD, 0xF2994A, 0x555555, 0xEB5757};
    static final int[] RCOL = {0x1ABC9C, 0xE91E63, 0x9C27B0, 0xFF9800, 0xFFFFFF};
    static final int[] SIN = new int[256];
    static {
        for (int i = 0; i < 256; i++) {
            int k = i & 127;
            int v = k * (128 - k) * 127 / 4096;
            SIN[i] = i < 128 ? v : -v;
        }
    }

    final Track tk = new Track();
    int mode, env, car, laps, maxSpd, accel, handl;
    int prog, pos, px, py, speed, nitro, lastAbs, crash, ev, bgd;
    int lapsDone, ticks, lapStart, bestLap, tleft, finished, seed = 99, nc;
    boolean boost, braking;
    final int[] cp = new int[MAXC], cxo = new int[MAXC], cs = new int[MAXC], ckind = new int[MAXC], ccol = new int[MAXC];
    final int[] cacc = new int[MAXC], crace = new int[MAXC], cbase = new int[MAXC], ctgt = new int[MAXC];

    int rnd(int k) { seed = seed * 1103515245 + 12345; return ((seed >>> 16) & 0x7fff) % k; }

    void start(int m, int e, int c, int sd) {
        mode = m; env = e; car = c;
        tk.build(e, sd);
        maxSpd = CAR_MAX[c]; accel = CAR_ACC[c]; handl = CAR_HND[c];
        prog = 0; px = 0; speed = 0; nitro = 700; crash = 0; ev = 0; ticks = 0;
        lapsDone = 0; lapStart = 0; bestLap = 0; finished = 0; laps = 3; tleft = 55000;
        seed = sd ^ 0x5bd1;
        lastAbs = PZ / SEG;
        update();
        spawn();
    }

    void update() {
        int len = tk.len, pp = (prog + PZ) % len;
        pos = prog % len;
        int sg = pp / SEG, fr = pp % SEG;
        py = tk.y[sg] + (tk.y[sg + 1] - tk.y[sg]) * fr / SEG;
    }

    void spawn() {
        int len = tk.len;
        nc = 0;
        int nt = mode == 1 ? 14 : 9;
        for (int i = 0; i < nt; i++) {
            int c = nc++;
            cp[c] = 4000 + (len - 6000) * i / nt + rnd(1500);
            cxo[c] = LANES[rnd(3)];
            ctgt[c] = cxo[c];
            cs[c] = 2200 + rnd(2400);
            ckind[c] = rnd(4) == 0 ? 2 : 1;
            ccol[c] = TCOL[rnd(TCOL.length)];
            cacc[c] = 0xFFFFFF;
            crace[c] = 0;
        }
        if (mode == 0) {
            for (int k = 0; k < 5; k++) {
                int c = nc++;
                cp[c] = PZ + 600 + k * 330;
                cxo[c] = LANES[k % 3];
                ctgt[c] = cxo[c];
                cs[c] = 0;
                ckind[c] = 0;
                ccol[c] = RCOL[k];
                cacc[c] = 0x202020;
                crace[c] = 1;
                cbase[c] = 80 + rnd(14);
            }
        }
    }

    int rankNow() {
        int pp = prog + PZ, r = 1;
        for (int c = 0; c < nc; c++) if (crace[c] != 0 && cp[c] > pp) r++;
        return r;
    }

    void demoTick() {
        ticks++;
        speed = 5200;
        int dp = speed * 33 / 1000;
        prog += dp;
        px = SIN[(ticks * 2) & 255] * 450 / 127;
        update();
        bgd = tk.curve[((prog + PZ) % tk.len) / SEG] * dp / 80;
        moveCars();
    }

    void tick(boolean left, boolean right, boolean gas, boolean brake, boolean nos) {
        ev = 0;
        int len = tk.len, pp = prog + PZ;
        int cv = tk.curve[(pp % len) / SEG];
        int top = maxSpd;
        boost = nos && nitro > 0 && speed > maxSpd / 4;
        if (boost) { top = top * 125 / 100; nitro -= 7; if (nitro < 0) nitro = 0; }
        else if (nitro < 1000) nitro++;
        int spct = speed * 100 / maxSpd;
        if (spct > 130) spct = 130;
        int dx = 66 * spct / 100;
        if (crash > 0) crash--;
        if (left) px -= dx * handl / 100;
        if (right) px += dx * handl / 100;
        px -= dx * spct * cv / 5000;
        int a = maxSpd * accel * 33 / 500000;
        braking = brake;
        if (brake) speed -= maxSpd * 33 / 1000;
        else if (gas) { if (speed < top) speed += boost ? a * 2 : a; else speed -= a; }
        else speed -= maxSpd * 33 / 5000;
        if ((px < -1000 || px > 1000) && speed > maxSpd / 4) speed -= maxSpd * 33 / 2000;
        if (speed < 0) speed = 0;
        if (speed > maxSpd * 125 / 100) speed = maxSpd * 125 / 100;
        if (px < -3200) px = -3200;
        if (px > 3200) px = 3200;
        int dp = speed * 33 / 1000;
        prog += dp;
        bgd = cv * dp / 80;
        update();
        collide(dp);
        moveCars();
        ticks++;
        if (mode == 0) {
            int ld = (prog + PZ + len - 4 * SEG) / len - 1;
            if (ld < 0) ld = 0;
            if (ld > lapsDone) {
                lapsDone = ld;
                int lt = (ticks - lapStart) * 33;
                if (bestLap == 0 || lt < bestLap) bestLap = lt;
                lapStart = ticks;
                if (ld >= laps) { finished = 1; ev |= EV_FIN; } else ev |= EV_LAP;
            }
        } else {
            tleft -= 33;
            if (tleft <= 0) { tleft = 0; finished = 1; ev |= EV_TIME; }
        }
    }

    boolean hit(int t, int off) {
        if (t == 0 || t == Env.NEON || t == Env.GATE || t == Env.START) return false;
        int hw;
        switch (t) {
            case Env.TREE: hw = 230; break;
            case Env.PALM: hw = 160; break;
            case Env.CACT: hw = 200; break;
            case Env.ROCK: hw = 260; break;
            case Env.LAMP: hw = 90; break;
            case Env.SIGN: hw = 90; break;
            case Env.BILL: hw = 420; break;
            default: hw = 700;
        }
        int d = px - off;
        if (d < 0) d = -d;
        return d < hw + 150;
    }

    void crashHit() {
        if (speed > 600) {
            speed = speed / 4;
            crash = 20;
            ev |= EV_CRASH;
            px = px > 0 ? 950 : -950;
        }
    }

    void collide(int dp) {
        int len = tk.len, abs = (prog + PZ) / SEG;
        for (int a = lastAbs + 1; a <= abs; a++) {
            int i = a % tk.n;
            if (hit(tk.sl[i], tk.ofl[i])) crashHit();
            if (hit(tk.sr[i], tk.ofr[i])) crashHit();
            if (tk.sl[i] == Env.GATE && mode == 1) { tleft += 22000; ev |= EV_GATE; }
        }
        lastAbs = abs;
        for (int c = 0; c < nc; c++) {
            int dz = ((cp[c] - (prog + PZ)) % len + len) % len;
            if (dz > len / 2) dz -= len;
            int d = cxo[c] - px;
            if (d < 0) d = -d;
            if (d < 300 && dz > -120 && dz < 260 + dp && speed > cs[c]) {
                prog += dz - 270;
                if (prog < 0) prog = 0;
                speed = cs[c] * 3 / 4;
                crash = 15;
                ev |= EV_CRASH;
                px += px >= cxo[c] ? 90 : -90;
                update();
            }
        }
    }

    void moveCars() {
        int len = tk.len;
        for (int c = 0; c < nc; c++) {
            if (crace[c] != 0 && mode == 0) racer(c, len);
            cp[c] += cs[c] * 33 / 1000;
            int d = ctgt[c] - cxo[c];
            if (d > 14) cxo[c] += 14; else if (d < -14) cxo[c] -= 14; else cxo[c] = ctgt[c];
        }
    }

    void racer(int c, int len) {
        int ahead = cp[c] - (prog + PZ), tg = cbase[c];
        if (ahead > 16000) tg -= 7; else if (ahead < -9000) tg += 6;
        int cv = tk.curve[(cp[c] % len) / SEG];
        if (cv < 0) cv = -cv;
        tg -= cv / 4;
        int target = 8000 * tg / 100;
        if (cs[c] < target) cs[c] += 36; else cs[c] -= 26;
        if (cs[c] < 0) cs[c] = 0;
        if (((ticks + c * 7) & 15) == 0) pickLane(c, len);
    }

    void pickLane(int c, int len) {
        int best = ctgt[c];
        for (int tries = 0; tries < 3; tries++) {
            boolean blocked = false;
            for (int j = 0; j < nc && !blocked; j++) {
                if (j == c) continue;
                int dz = ((cp[j] - cp[c]) % len + len) % len;
                int dl = ctgt[j] - best;
                if (dl < 0) dl = -dl;
                if (dz > 0 && dz < 2600 && dl < 400 && cs[j] < cs[c] + 400) blocked = true;
            }
            if (!blocked) break;
            best = LANES[rnd(3)];
        }
        ctgt[c] = best;
    }
}
