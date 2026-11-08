import java.io.ByteArrayOutputStream;

final class Midi {
    static final int[] BPM = {118, 104, 124, 90};
    static final int[] LPROG = {80, 81, 81, 4};
    static final int[] BPROG = {38, 38, 39, 33};
    static final int STEP = 48;
    static final int[][] LEAD = {
        {72, -1, 76, -1, 79, -1, 76, 0, 74, -1, 77, -1, 81, -1, 77, 0, 72, -1, 76, -1, 79, -1, 84, -1, 83, -1, 79, -1, 76, -1, 72, 0},
        {69, -1, 70, -1, 69, -1, 65, 0, 67, -1, 69, -1, 67, -1, 64, 0, 69, -1, 72, -1, 74, -1, 72, -1, 70, -1, 69, -1, 67, -1, 0, 0},
        {64, -1, 0, 71, -1, 0, 76, -1, 74, -1, 0, 71, -1, 0, 67, -1, 64, -1, 0, 71, -1, 0, 76, -1, 79, -1, 76, -1, 74, -1, 71, -1},
        {76, -1, -1, 0, 74, -1, -1, 0, 72, -1, -1, 0, 67, -1, -1, 0, 69, -1, -1, 0, 72, -1, -1, 0, 71, -1, -1, 0, 67, -1, -1, 0}
    };
    static final int[][] BASS = {
        {48, -1, 48, 0, 55, -1, 55, 0, 50, -1, 50, 0, 57, -1, 57, 0, 48, -1, 48, 0, 55, -1, 55, 0, 55, -1, 55, 0, 52, -1, 52, 0},
        {45, -1, 0, 45, 52, -1, 0, 52, 43, -1, 0, 43, 50, -1, 0, 50, 45, -1, 0, 45, 52, -1, 0, 52, 46, -1, 0, 46, 43, -1, 0, 43},
        {40, 0, 40, 0, 40, 0, 47, 0, 38, 0, 38, 0, 38, 0, 45, 0, 36, 0, 36, 0, 36, 0, 43, 0, 43, 0, 43, 0, 40, 0, 40, 0},
        {48, -1, -1, -1, 43, -1, -1, -1, 45, -1, -1, -1, 40, -1, -1, -1, 41, -1, -1, -1, 48, -1, -1, -1, 43, -1, -1, -1, 36, -1, -1, -1}
    };

    static int[] ek, es, ed1, ed2;
    static int en;

    static void ev(int tick, int prio, int st, int a, int b) {
        ek[en] = tick * 4 + prio;
        es[en] = st;
        ed1[en] = a;
        ed2[en] = b;
        en++;
    }

    static void track(int[] p, int ch, int vel) {
        int i = 0;
        while (i < p.length) {
            int note = p[i];
            if (note > 0) {
                int len = 1;
                while (i + len < p.length && p[i + len] == -1) len++;
                ev(i * STEP, 2, 0x90 | ch, note, vel);
                ev((i + len) * STEP - 6, 1, 0x80 | ch, note, 0);
                i += len;
            } else {
                i++;
            }
        }
    }

    static void vlq(ByteArrayOutputStream o, int v) {
        int b = v & 0x7F;
        while ((v >>= 7) > 0) {
            b <<= 8;
            b |= 0x80 | (v & 0x7F);
        }
        while (true) {
            o.write(b & 255);
            if ((b & 0x80) != 0) b >>= 8; else break;
        }
    }

    static byte[] build(int t) {
        int[] lead = LEAD[t], bass = BASS[t];
        int max = (lead.length + bass.length) * 2 + 8;
        ek = new int[max]; es = new int[max]; ed1 = new int[max]; ed2 = new int[max];
        en = 0;
        ev(0, 0, 0xC0, LPROG[t], 0);
        ev(0, 0, 0xC1, BPROG[t], 0);
        track(lead, 0, 90);
        track(bass, 1, 70);
        for (int i = 1; i < en; i++) {
            int k = ek[i], s = es[i], a = ed1[i], b = ed2[i];
            int j = i - 1;
            while (j >= 0 && ek[j] > k) {
                ek[j + 1] = ek[j]; es[j + 1] = es[j]; ed1[j + 1] = ed1[j]; ed2[j + 1] = ed2[j];
                j--;
            }
            ek[j + 1] = k; es[j + 1] = s; ed1[j + 1] = a; ed2[j + 1] = b;
        }
        ByteArrayOutputStream tr = new ByteArrayOutputStream();
        int mpq = 60000000 / BPM[t];
        tr.write(0); tr.write(0xFF); tr.write(0x51); tr.write(3);
        tr.write((mpq >> 16) & 255); tr.write((mpq >> 8) & 255); tr.write(mpq & 255);
        int last = 0;
        for (int i = 0; i < en; i++) {
            int tk = ek[i] / 4;
            vlq(tr, tk - last);
            last = tk;
            tr.write(es[i]);
            tr.write(ed1[i]);
            if ((es[i] & 0xF0) != 0xC0) tr.write(ed2[i]);
        }
        int total = lead.length * STEP;
        vlq(tr, (total > last ? total : last) - last);
        tr.write(0xFF); tr.write(0x2F); tr.write(0);

        ByteArrayOutputStream o = new ByteArrayOutputStream();
        int[] hd = {0x4D, 0x54, 0x68, 0x64, 0, 0, 0, 6, 0, 0, 0, 1, 0, 96, 0x4D, 0x54, 0x72, 0x6B};
        for (int i = 0; i < hd.length; i++) o.write(hd[i]);
        int len = tr.size();
        o.write((len >> 24) & 255); o.write((len >> 16) & 255); o.write((len >> 8) & 255); o.write(len & 255);
        byte[] body = tr.toByteArray();
        o.write(body, 0, body.length);
        return o.toByteArray();
    }
}
