final class Env {
    static final int COUNT = 3;
    static final String[] NAME = {"Coast Highway", "Desert Run", "Neon City"};
    static final String[] DESC = {"Sunny seaside road", "Hot desert dunes", "City lights at night"};
    static final int[] SKY_T = {0x2D7DD2, 0xE8743B, 0x04041A};
    static final int[] SKY_B = {0xBDE3FF, 0xFFD9A0, 0x3A0F5C};
    static final int[] GR_A = {0x20A820, 0xE0BC72, 0x0E1420};
    static final int[] GR_B = {0x189418, 0xD0AA60, 0x0A101A};
    static final int[] RD_A = {0x707070, 0x5E544C, 0x3A3A44};
    static final int[] RD_B = {0x686868, 0x564C44, 0x34343E};
    static final int[] RB_A = {0xE02020, 0xE05A20, 0xFF2BD6};
    static final int[] RB_B = {0xFFFFFF, 0xFFE9C4, 0x20E8FF};
    static final int[] LANE = {0xFFFFFF, 0xFFF0C0, 0xFFD60A};
    static final int[] FOG = {0xBDE3FF, 0xF2C98C, 0x2A0C48};
    static final int TREE = 1, PALM = 2, SIGN = 3, BILL = 4, CACT = 5, ROCK = 6, LAMP = 7, BUILD = 8, NEON = 9, GATE = 20, START = 21;
    static final int[][] SCEN = {
        {TREE, PALM, TREE, SIGN, BILL, PALM},
        {CACT, ROCK, CACT, SIGN, BILL, ROCK},
        {LAMP, BUILD, LAMP, NEON, BILL, BUILD}
    };
}

final class Track {
    static final int SEG = 200, MAXN = 1800;
    final int[] curve = new int[MAXN], y = new int[MAXN + 1];
    final int[] sl = new int[MAXN], sr = new int[MAXN], ofl = new int[MAXN], ofr = new int[MAXN];
    int n, len, env, seed, lastY, cnt;

    int rnd(int k) { seed = seed * 1103515245 + 12345; return ((seed >>> 16) & 0x7fff) % k; }
    static int sm(int p) { return (p * p * (768 - 2 * p)) >> 16; }

    void put(int cv, int yEnd) {
        if (cnt >= MAXN) return;
        curve[cnt] = cv;
        cnt++;
        y[cnt] = yEnd;
    }

    void section(int enter, int hold, int leave, int cv, int dy) {
        int startY = lastY, endY = lastY + dy * SEG, total = enter + hold + leave;
        for (int k = 0; k < enter; k++) {
            int p = k * 256 / enter;
            put(cv * ((p * p) >> 8) / 256, startY + (endY - startY) * sm(k * 256 / total) / 256);
        }
        for (int k = 0; k < hold; k++) put(cv, startY + (endY - startY) * sm((enter + k) * 256 / total) / 256);
        for (int k = 0; k < leave; k++)
            put(cv * (256 - sm(k * 256 / leave)) / 256, startY + (endY - startY) * sm((enter + hold + k) * 256 / total) / 256);
        lastY = endY;
    }

    void build(int e, int sd) {
        env = e; seed = sd; cnt = 0; lastY = 0; y[0] = 0;
        section(0, 30, 0, 0, 0);
        while (cnt < 1250) {
            int t = rnd(10), ln = 14 + rnd(20), dir = rnd(2) == 0 ? -1 : 1;
            int up = rnd(2) == 0 ? 1 : -1;
            if (lastY > 36 * SEG) up = -1;
            if (lastY < -6 * SEG) up = 1;
            if (t < 2) section(8, 20 + rnd(40), 8, 0, 0);
            else if (t < 6) section(ln, ln + rnd(16), ln, dir * (1 + rnd(3)) * 20, 0);
            else if (t < 8) section(ln, ln, ln, 0, up * (6 + rnd(3) * 5));
            else section(ln, ln, ln, dir * (1 + rnd(2)) * 20, up * (5 + rnd(2) * 5));
        }
        section(30, 20, 30, 0, -lastY / SEG);
        n = cnt;
        y[n] = 0;
        len = n * SEG;
        decorate();
    }

    int off(int t) {
        switch (t) {
            case Env.TREE: return 1250 + rnd(1700);
            case Env.PALM: return 1250 + rnd(1200);
            case Env.SIGN: return 1130 + rnd(80);
            case Env.BILL: return 1500 + rnd(900);
            case Env.CACT: return 1250 + rnd(1500);
            case Env.ROCK: return 1150 + rnd(1800);
            case Env.LAMP: return 1130 + rnd(60);
            case Env.BUILD: return 2300 + rnd(1500);
            default: return 0;
        }
    }

    void decorate() {
        for (int i = 0; i < n; i++) { sl[i] = 0; sr[i] = 0; ofl[i] = 0; ofr[i] = 0; }
        int[] sc = Env.SCEN[env];
        for (int i = 8; i < n - 4; i += 3 + rnd(4)) { int t = sc[rnd(sc.length)]; sl[i] = t; ofl[i] = -off(t); }
        for (int i = 9; i < n - 4; i += 3 + rnd(4)) { int t = sc[rnd(sc.length)]; sr[i] = t; ofr[i] = off(t); }
        if (env == 2) for (int i = 30; i < n - 10; i += 40 + rnd(30)) { sl[i] = Env.NEON; ofl[i] = 0; }
        for (int i = 300; i < n - 50; i += 320) { sl[i] = Env.GATE; ofl[i] = 0; }
        sl[4] = Env.START; ofl[4] = 0;
    }
}
