import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.Display;

public class Rush extends MIDlet {
    private Game g;

    public void startApp() {
        if (g == null) {
            g = new Game(this);
            g.start();
        }
        Display.getDisplay(this).setCurrent(g);
    }
    public void pauseApp() { if (g != null) g.paused(); }
    public void destroyApp(boolean unconditional) { if (g != null) g.shutdown(); }
    void exit() { destroyApp(true); notifyDestroyed(); }
}
