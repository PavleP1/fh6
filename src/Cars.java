import javax.microedition.lcdui.*;

final class Cars {
    static final int TL = Graphics.TOP | Graphics.LEFT;
    static final Font FS = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);

    static int mix(int a, int b, int p) {
        if (p < 0) p = 0;
        if (p > 255) p = 255;
        int r = (((a >> 16) & 255) * (255 - p) + ((b >> 16) & 255) * p) / 255;
        int g = (((a >> 8) & 255) * (255 - p) + ((b >> 8) & 255) * p) / 255;
        int bl = ((a & 255) * (255 - p) + (b & 255) * p) / 255;
        return (r << 16) | (g << 8) | bl;
    }
    static int dk(int c, int p) { return mix(c, 0x000000, p); }
    static int lt(int c, int p) { return mix(c, 0xFFFFFF, p); }

    // rear view of a car. kind 0 sports, 1 sedan, 2 truck. by = ground line, w = width in pixels
    static void car(Graphics g, int kind, int cx, int by, int w, int body, int accent, int lean, boolean brake) {
        int u = w / 32;
        if (u < 1) {
            g.setColor(body);
            g.fillRect(cx - w / 2, by - w / 2, w, w / 2 + 1);
            return;
        }
        g.setColor(0x151515);
        g.fillRoundRect(cx - 17 * u, by - 2 * u, 34 * u, 3 * u, 2 * u, 2 * u);
        g.fillRect(cx - 16 * u, by - 6 * u, 4 * u, 6 * u);
        g.fillRect(cx + 12 * u, by - 6 * u, 4 * u, 6 * u);
        if (kind == 2) {
            g.setColor(body);
            g.fillRect(cx - 14 * u + lean, by - 22 * u, 28 * u, 18 * u);
            g.setColor(lt(body, 60));
            g.fillRect(cx - 14 * u + lean, by - 22 * u, 28 * u, 2 * u);
            g.setColor(dk(body, 110));
            g.fillRect(cx - u + lean, by - 20 * u, 2 * u, 14 * u);
            g.setColor(dk(body, 70));
            g.fillRect(cx - 14 * u, by - 6 * u, 28 * u, 3 * u);
        } else {
            int ch = kind == 0 ? 7 : 9;
            g.setColor(body);
            g.fillRoundRect(cx - 15 * u, by - 11 * u, 30 * u, 8 * u, 3 * u, 3 * u);
            g.setColor(dk(body, 80));
            g.fillRect(cx - 14 * u, by - 5 * u, 28 * u, 3 * u);
            g.setColor(dk(body, 40));
            g.fillRoundRect(cx - 10 * u + lean, by - 11 * u - ch * u, 20 * u, (ch + 2) * u, 3 * u, 3 * u);
            g.setColor(0x1A2A3C);
            g.fillRect(cx - 8 * u + lean, by - 10 * u - ch * u, 16 * u, (ch - 2) * u);
            g.setColor(0x4A6A88);
            g.fillRect(cx - 8 * u + lean, by - 10 * u - ch * u, 16 * u, u);
            if (kind == 0) {
                g.setColor(accent);
                g.fillRect(cx - 15 * u + lean / 2, by - 15 * u, 30 * u, 2 * u);
                g.fillRect(cx - 13 * u + lean / 2, by - 13 * u, 2 * u, 3 * u);
                g.fillRect(cx + 11 * u + lean / 2, by - 13 * u, 2 * u, 3 * u);
            }
        }
        g.setColor(brake ? 0xFF5040 : 0xA01818);
        g.fillRect(cx - 14 * u, by - 9 * u, 6 * u, 3 * u);
        g.fillRect(cx + 8 * u, by - 9 * u, 6 * u, 3 * u);
        g.setColor(0xE8E8E8);
        g.fillRect(cx - 3 * u, by - 7 * u, 6 * u, 3 * u);
        g.setColor(0x333333);
        g.fillArc(cx - 9 * u, by - 4 * u, 3 * u, 3 * u, 0, 360);
        g.fillArc(cx + 6 * u, by - 4 * u, 3 * u, 3 * u, 0, 360);
    }

    static void scenery(Graphics g, int t, int x, int y, int u, int env, int tk, int sw, int dir, int idx) {
        if (x + 9 * u < 0 || x - 9 * u > sw) return;
        switch (t) {
            case Env.TREE: tree(g, x, y, u); break;
            case Env.PALM: palm(g, x, y, u); break;
            case Env.SIGN: sign(g, x, y, u); break;
            case Env.BILL: bill(g, x, y, u, env); break;
            case Env.CACT: cact(g, x, y, u); break;
            case Env.ROCK: rock(g, x, y, u); break;
            case Env.LAMP: lamp(g, x, y, u, dir); break;
            case Env.BUILD: building(g, x, y, u, idx); break;
            case Env.NEON: neon(g, x, y, u, tk); break;
            case Env.GATE: gate(g, t, x, y, u); break;
            case Env.START: gate(g, t, x, y, u); break;
        }
    }

    static void tree(Graphics g, int x, int y, int u) {
        g.setColor(0x5A3A1E);
        g.fillRect(x - u / 2, y - 3 * u, Math.max(1, u), 3 * u);
        g.setColor(0x1B6E24);
        g.fillTriangle(x - 3 * u, y - 3 * u, x + 3 * u, y - 3 * u, x, y - 8 * u);
        g.setColor(0x238A2C);
        g.fillTriangle(x - 5 * u / 2, y - 6 * u, x + 5 * u / 2, y - 6 * u, x, y - 11 * u);
        g.setColor(0x2BA336);
        g.fillTriangle(x - 2 * u, y - 9 * u, x + 2 * u, y - 9 * u, x, y - 13 * u);
    }

    static void palm(Graphics g, int x, int y, int u) {
        int tx = x + 2 * u, ty = y - 11 * u;
        g.setColor(0x8A6232);
        g.fillTriangle(x - u / 2, y, x + u / 2 + 1, y, tx, ty);
        g.setColor(0x1F8A2E);
        g.fillTriangle(tx, ty, tx - 5 * u, ty + 2 * u, tx - 2 * u, ty - u);
        g.fillTriangle(tx, ty, tx + 5 * u, ty + 2 * u, tx + 2 * u, ty - u);
        g.setColor(0x2AA83C);
        g.fillTriangle(tx, ty, tx - 4 * u, ty - 2 * u, tx - u, ty + u);
        g.fillTriangle(tx, ty, tx + 4 * u, ty - 2 * u, tx + u, ty + u);
        g.fillTriangle(tx - u, ty - 3 * u, tx + u, ty - 3 * u, tx, ty + u);
        g.setColor(0x6A4A22);
        g.fillArc(tx - u / 2, ty + u / 2, u, u, 0, 360);
    }

    static void sign(Graphics g, int x, int y, int u) {
        int pw = Math.max(1, u / 3);
        g.setColor(0x777777);
        g.fillRect(x - pw / 2, y - 5 * u, pw, 5 * u);
        g.setColor(0xFFD60A);
        g.fillRect(x - 2 * u, y - 7 * u, 4 * u, 3 * u);
        g.setColor(0x111111);
        g.fillRect(x - u, y - 6 * u, 2 * u, Math.max(1, u / 2));
        g.fillTriangle(x + u, y - 7 * u + u / 2, x + u, y - 4 * u - u / 2, x + 2 * u - u / 2, y - 5 * u - u / 2);
    }

    static void bill(Graphics g, int x, int y, int u, int env) {
        int pw = Math.max(1, u / 2);
        g.setColor(0x555555);
        g.fillRect(x - 3 * u, y - 5 * u, pw, 5 * u);
        g.fillRect(x + 3 * u - pw, y - 5 * u, pw, 5 * u);
        g.setColor(0x222233);
        g.fillRect(x - 4 * u, y - 11 * u, 8 * u, 5 * u);
        g.setColor(env == 2 ? 0xFF2BD6 : (env == 1 ? 0xE0521E : 0x2D7DD2));
        g.fillRect(x - 4 * u + u / 2, y - 11 * u + u / 2, 3 * u, 4 * u);
        g.setColor(env == 2 ? 0x20E8FF : 0xFFFFFF);
        g.fillRect(x - u, y - 10 * u, 4 * u, u);
        g.fillRect(x - u, y - 8 * u, 3 * u, u);
    }

    static void cact(Graphics g, int x, int y, int u) {
        int tw = Math.max(1, u), a = Math.max(1, u * 2 / 3);
        g.setColor(0x2E7D32);
        g.fillRoundRect(x - tw / 2, y - 7 * u, tw, 7 * u, tw, tw);
        g.fillRect(x - 3 * u, y - 4 * u, 3 * u, a);
        g.fillRect(x - 3 * u, y - 6 * u, a, 2 * u + a);
        g.fillRect(x, y - 3 * u, 3 * u, a);
        g.fillRect(x + 3 * u - a, y - 5 * u, a, 2 * u + a);
        g.setColor(0x4CAF50);
        g.fillRect(x - tw / 4, y - 7 * u + 1, Math.max(1, tw / 3), 6 * u);
    }

    static void rock(Graphics g, int x, int y, int u) {
        g.setColor(0x6E6258);
        g.fillArc(x - 3 * u, y - 4 * u, 6 * u, 5 * u, 0, 360);
        g.setColor(0x8A7C6E);
        g.fillArc(x - 2 * u, y - 4 * u, 3 * u, 3 * u, 0, 360);
    }

    static void lamp(Graphics g, int x, int y, int u, int dir) {
        int pw = Math.max(1, u / 3);
        g.setColor(0x55596A);
        g.fillRect(x - pw / 2, y - 9 * u, pw, 9 * u);
        g.fillRect(dir > 0 ? x : x - 2 * u, y - 9 * u, 2 * u, pw);
        int lx = x + dir * 2 * u, ly = y - 9 * u + pw;
        g.setColor(0x5A4A20);
        g.fillArc(lx - 2 * u, ly - u, 4 * u, 4 * u, 0, 360);
        g.setColor(0xFFE890);
        g.fillArc(lx - u, ly, 2 * u, 2 * u, 0, 360);
    }

    static void building(Graphics g, int x, int y, int u, int idx) {
        int bw = 6 * u, bh = 16 * u + (idx & 3) * 2 * u;
        g.setColor(0x0D1022);
        g.fillRect(x - bw / 2, y - bh, bw, bh);
        g.setColor(0x1C2140);
        g.fillRect(x - bw / 2, y - bh, u / 2 + 1, bh);
        if (u >= 3) {
            for (int r = 1; r < bh / (2 * u); r++)
                for (int c = 0; c < 3; c++) {
                    int hs = (r * 7 + c * 13 + idx) & 7;
                    if (hs < 4) {
                        g.setColor(hs == 0 ? 0x20E8FF : (hs == 1 ? 0xFF2BD6 : 0xFFE070));
                        g.fillRect(x - bw / 2 + u / 2 + c * 2 * u, y - bh + r * 2 * u, u, u);
                    }
                }
        }
        g.setColor(0x20E8FF);
        g.fillRect(x - bw / 2, y - bh, bw, Math.max(1, u / 3));
    }

    static void neon(Graphics g, int x, int y, int u, int tk) {
        int hw = 8 * u + u / 2, th = Math.max(1, u / 2);
        g.setColor(0x20E8FF);
        g.fillRect(x - hw - th, y - 11 * u, th, 11 * u);
        g.fillRect(x + hw, y - 11 * u, th, 11 * u);
        g.setColor(((tk >> 3) & 1) == 0 ? 0xFF2BD6 : 0xC81CA8);
        g.fillRect(x - hw - th, y - 12 * u, 2 * hw + 2 * th, th + u / 2);
        g.setColor(0xFFFFFF);
        g.fillRect(x - hw, y - 12 * u + th / 2, 2 * hw, Math.max(1, th / 2));
    }

    static void gate(Graphics g, int t, int x, int y, int u) {
        int hw = 8 * u + u / 2, th = Math.max(1, u / 2);
        g.setColor(0x888890);
        g.fillRect(x - hw - th, y - 10 * u, th * 2, 10 * u);
        g.fillRect(x + hw - th, y - 10 * u, th * 2, 10 * u);
        int cells = 14, cw = (2 * hw) / cells;
        for (int i = 0; i < cells; i++) {
            boolean a = (i & 1) == 0;
            g.setColor(t == Env.START ? (a ? 0xFFFFFF : 0x111111) : (a ? 0xFFD60A : 0x222222));
            g.fillRect(x - hw + i * cw, y - 10 * u, cw, 2 * u);
        }
        if (u >= 5) {
            g.setFont(FS);
            g.setColor(0x000000);
            g.drawString(t == Env.START ? "START" : "CHECKPOINT", x + 1, y - 10 * u - FS.getHeight() + 1, Graphics.TOP | Graphics.HCENTER);
            g.setColor(0xFFFFFF);
            g.drawString(t == Env.START ? "START" : "CHECKPOINT", x, y - 10 * u - FS.getHeight(), Graphics.TOP | Graphics.HCENTER);
        }
    }
}
