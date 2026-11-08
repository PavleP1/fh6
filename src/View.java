import javax.microedition.lcdui.*;

final class View {
    static final int SEG = Track.SEG, MAXD = 80, CAMH = 1000, ROADW = 2000, CD24 = 14092861, MINZ = 84;
    static final int TL = Graphics.TOP | Graphics.LEFT;
    int w, h, hw, hh, cy, draw = 64, step = 2, env = -1, skyW, bgx, seed;
    Image sky;
    final int[] sx = new int[MAXD + 2], sy = new int[MAXD + 2], sw = new int[MAXD + 2];
    final boolean[] ok = new boolean[MAXD + 2];
    final int[] gA = new int[MAXD + 1], gB = new int[MAXD + 1], rA = new int[MAXD + 1], rB = new int[MAXD + 1];
    final int[] bA = new int[MAXD + 1], bB = new int[MAXD + 1];
    final int[] cnI = new int[24], czI = new int[24];

    void init(int ww, int hh2) {
        w = ww; h = hh2; hw = w / 2; hh = h / 2; cy = h * 40 / 100;
        env = -1; sky = null;
    }

    int rnd(int k) { seed = seed * 1103515245 + 12345; return ((seed >>> 16) & 0x7fff) % k; }

    void setDraw(int d) {
        draw = Math.min(d, MAXD - 1);
        fogTables();
    }

    void setEnv(int e) {
        if (e == env && sky != null) return;
        env = e;
        fogTables();
        buildSky();
    }

    void fogTables() {
        if (env < 0) return;
        for (int n = 0; n <= MAXD; n++) {
            int p = n * 255 / draw;
            if (p > 255) p = 255;
            p = p * p / 255;
            gA[n] = Cars.mix(Env.GR_A[env], Env.FOG[env], p);
            gB[n] = Cars.mix(Env.GR_B[env], Env.FOG[env], p);
            rA[n] = Cars.mix(Env.RD_A[env], Env.FOG[env], p);
            rB[n] = Cars.mix(Env.RD_B[env], Env.FOG[env], p);
            bA[n] = Cars.mix(Env.RB_A[env], Env.FOG[env], p);
            bB[n] = Cars.mix(Env.RB_B[env], Env.FOG[env], p);
        }
    }

    void ridge(Graphics g, int col, int base, int minH, int maxH, int pw) {
        g.setColor(col);
        for (int x = -pw; x < skyW + pw; x += pw)
            g.fillTriangle(x, base, x + pw, base - minH - rnd(maxH - minH + 1), x + 2 * pw, base);
    }

    void buildSky() {
        sky = null;
        try {
            skyW = w * 2;
            int sh = cy + 2;
            Image im = Image.createImage(skyW, sh);
            Graphics g = im.getGraphics();
            int bands = 24;
            for (int i = 0; i < bands; i++) {
                g.setColor(Cars.mix(Env.SKY_T[env], Env.SKY_B[env], i * 255 / (bands - 1)));
                int y0 = sh * i / bands, y1 = sh * (i + 1) / bands;
                g.fillRect(0, y0, skyW, y1 - y0 + 1);
            }
            seed = 1234 + env;
            decor(g, sh);
            g.setColor(Env.FOG[env]);
            g.fillRect(0, sh - 3, skyW, 3);
            sky = im;
        } catch (Throwable e) {
            sky = null;
        }
    }

    void decor(Graphics g, int sh) {
        if (env == 0) {
            int sx0 = w * 7 / 10, sy0 = sh * 35 / 100;
            for (int k = 3; k >= 0; k--) {
                g.setColor(Cars.mix(Env.SKY_B[0], 0xFFF6C8, 70 + k * 45));
                int r = 12 + k * 6;
                g.fillArc(sx0 - r, sy0 - r, r * 2, r * 2, 0, 360);
            }
            g.setColor(0xFFF3A0);
            g.fillArc(sx0 - 11, sy0 - 11, 22, 22, 0, 360);
            g.setColor(0xFFFFFF);
            for (int i = 0; i < 10; i++) {
                int cx0 = rnd(skyW), cy0 = 8 + rnd(sh / 3), cw = 30 + rnd(40);
                g.fillArc(cx0, cy0, cw, 10, 0, 360);
                g.fillArc(cx0 + cw / 4, cy0 - 5, cw / 2, 10, 0, 360);
            }
            ridge(g, 0x7F9DB8, sh - 3, 14, 40, 30);
            ridge(g, 0x4C7A56, sh - 3, 6, 20, 24);
        } else if (env == 1) {
            int sx0 = w * 35 / 100, sy0 = sh * 62 / 100;
            for (int k = 2; k >= 0; k--) {
                g.setColor(Cars.mix(Env.SKY_B[1], 0xFFB347, 90 + k * 60));
                int r = 16 + k * 9;
                g.fillArc(sx0 - r, sy0 - r, r * 2, r * 2, 0, 360);
            }
            g.setColor(0xFFE0A0);
            g.fillArc(sx0 - 15, sy0 - 15, 30, 30, 0, 360);
            g.setColor(0xB5562B);
            for (int i = 0; i < 9; i++) {
                int mx = rnd(skyW), mw = 24 + rnd(40), mh = 14 + rnd(26);
                g.fillRect(mx, sh - 3 - mh, mw, mh);
                g.fillTriangle(mx - 8, sh - 3, mx, sh - 3 - mh, mx, sh - 3);
                g.fillTriangle(mx + mw, sh - 3, mx + mw, sh - 3 - mh, mx + mw + 8, sh - 3);
            }
            ridge(g, 0xD9A860, sh - 3, 4, 14, 36);
            ridge(g, 0xC48F4A, sh - 3, 2, 8, 30);
        } else {
            for (int i = 0; i < 140; i++) {
                g.setColor(Cars.mix(0x505070, 0xFFFFFF, 60 + rnd(190)));
                int sz = rnd(8) == 0 ? 2 : 1;
                g.fillRect(rnd(skyW), rnd(sh * 7 / 10), sz, sz);
            }
            int mx = w * 3 / 10, my = sh * 28 / 100;
            g.setColor(0xF2F2E8);
            g.fillArc(mx - 11, my - 11, 22, 22, 0, 360);
            g.setColor(0x0D0620);
            g.fillArc(mx - 6, my - 13, 22, 22, 0, 360);
            for (int layer = 0; layer < 2; layer++) {
                int x = 0;
                while (x < skyW) {
                    int bw = 8 + rnd(14), bh = (layer == 0 ? 14 : 26) + rnd(layer == 0 ? 24 : 40);
                    g.setColor(layer == 0 ? 0x1A1236 : 0x0C0C22);
                    g.fillRect(x, sh - 3 - bh, bw, bh);
                    if (layer == 1) {
                        g.setColor(rnd(2) == 0 ? 0xFFE070 : 0x20E8FF);
                        for (int k = 0; k < bw * bh / 70; k++) g.fillRect(x + 1 + rnd(bw - 2), sh - 3 - bh + 2 + rnd(bh - 4), 1, 2);
                        if (rnd(4) == 0) { g.setColor(0xFF2BD6); g.fillRect(x, sh - 3 - bh, bw, 1); }
                    }
                    x += bw + rnd(3);
                }
            }
        }
    }

    void render(Graphics g, Sim s, int tick) {
        Track t = s.tk;
        int N = t.n, len = t.len, base = s.pos / SEG, bp = s.pos % SEG;
        int off = (bgx >> 8) % skyW;
        if (off < 0) off += skyW;
        if (sky != null) {
            g.drawImage(sky, -off, 0, TL);
            if (off > skyW - w) g.drawImage(sky, skyW - off, 0, TL);
        } else {
            g.setColor(Env.SKY_B[env]);
            g.fillRect(0, 0, w, cy + 2);
        }
        g.setColor(gB[draw - 1]);
        g.fillRect(0, cy, w, h - cy);

        int camY = CAMH + s.py, px2 = s.px * 2;
        int x10 = 0, dx10 = -(t.curve[base] * bp / SEG);
        for (int n = 0; n <= draw; n++) {
            int idx = (base + n) % N, camZ = n * SEG - bp;
            if (camZ < MINZ) {
                ok[n] = false;
            } else {
                ok[n] = true;
                int sc = CD24 / camZ;
                int cxw = px2 - x10 / 10;
                sx[n] = hw + ((((sc * cxw) >> 8) * hw) >> 16);
                sy[n] = cy + ((((sc * (camY - t.y[idx])) >> 8) * hh) >> 16);
                sw[n] = (((sc * ROADW) >> 8) * hw) >> 16;
            }
            x10 += dx10;
            dx10 += t.curve[idx];
        }
        for (int c = 0; c < s.nc; c++) {
            int dz = ((s.cp[c] - s.pos) % len + len) % len + bp;
            cnI[c] = dz < (draw - 1) * SEG ? dz / SEG : -1;
            czI[c] = dz;
        }
        int lane = Env.LANE[env];
        for (int n = draw - 1; n >= 0; n--) {
            if (!ok[n]) continue;
            int idx = (base + n) % N, y1 = sy[n], y2 = sy[n + 1];
            if (y2 >= y1 || y1 < 0 || y2 >= h) continue;
            int x1 = sx[n], w1 = sw[n], x2 = sx[n + 1], w2 = sw[n + 1];
            int stripe = (idx >> 2) & 1;
            int top = y2 < 0 ? 0 : y2, bot = y1 > h ? h : y1;
            g.setColor(stripe == 0 ? gA[n] : gB[n]);
            g.fillRect(0, top, w, bot - top);
            int hgt = y1 - y2, rcol = stripe == 0 ? rA[n] : rB[n], bcol = stripe == 0 ? bA[n] : bB[n];
            int yy = top;
            while (yy < bot) {
                int hr = bot - yy < step ? bot - yy : step;
                int tt = (yy + hr / 2 - y2) * 256 / hgt;
                int xc = x2 + (x1 - x2) * tt / 256, wd = w2 + (w1 - w2) * tt / 256;
                int rb = wd / 6 + 1;
                g.setColor(bcol);
                g.fillRect(xc - wd - rb, yy, 2 * (wd + rb), hr);
                g.setColor(rcol);
                g.fillRect(xc - wd, yy, 2 * wd, hr);
                if (stripe == 0 && wd > 5) {
                    int lw = wd / 32 + 1;
                    g.setColor(lane);
                    g.fillRect(xc - wd / 3 - lw / 2, yy, lw, hr);
                    g.fillRect(xc + wd / 3 - lw / 2, yy, lw, hr);
                }
                yy += hr;
            }
            int su = w1 / 8;
            if (su > 0 && y1 - 13 * su < h) {
                int tl = t.sl[idx], tr = t.sr[idx];
                if (tl != 0) Cars.scenery(g, tl, x1 + w1 * t.ofl[idx] / 1000, y1, su, env, tick, w, 1, idx);
                if (tr != 0) Cars.scenery(g, tr, x1 + w1 * t.ofr[idx] / 1000, y1, su, env, tick, w, -1, idx);
            }
            for (int c = 0; c < s.nc; c++) {
                if (cnI[c] != n) continue;
                int fr = czI[c] % SEG;
                int ys = y1 + (y2 - y1) * fr / SEG;
                int ws = w1 + (w2 - w1) * fr / SEG;
                int xs = x1 + (x2 - x1) * fr / SEG + ws * s.cxo[c] / 1000;
                int cw = ws * 3 / 10;
                if (cw >= 3 && ys > 0 && ys - cw < h + 10) Cars.car(g, s.ckind[c], xs, ys, cw, s.ccol[c], s.cacc[c], 0, false);
            }
        }
    }
}
