package Baseball_Sack;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * test
 *
 * @author normenhansen
 * @author practicing01
 */
public class Main extends SimpleApplication {

    public static BulletAppState bulletAppState;
    public static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    public static void main(String[] args) {

        AppSettings settings = new AppSettings(true);
        settings.setTitle("JME Android Template");
        settings.setResolution(800, 480);
        settings.setSettingsDialogImage("Interface/Backgrounds/Splash_Configure.png");
        settings.setVSync(true);
        settings.setFrequency(60);
        settings.setFrameRate(30);
        settings.setSamples(0);
        settings.useInput();
        try {
            settings.setIcons(new BufferedImage[]{
                ImageIO.read(Main.class.getResourceAsStream("/Interface/Icons/Icon-16x16.png")),
                ImageIO.read(Main.class.getResourceAsStream("/Interface/Icons/Icon-32x32.png")),
                ImageIO.read(Main.class.getResourceAsStream("/Interface/Icons/Icon-128x128.png")),
                ImageIO.read(Main.class.getResourceAsStream("/Interface/Icons/Icon-256x256.png"))});
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.WARNING, "Unable to load program icons.", ex);
        }

        Main app = new Main();
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {

        setDisplayStatView(false);
        setDisplayFps(false);

        bulletAppState = new BulletAppState();
        //bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(bulletAppState);

        SplashesState state = new SplashesState();
        stateManager.attach(state);
        
        //Debug splash bypass.
        //GameplayState gp = new GameplayState();
        //stateManager.attach(gp);

    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    @Override
    public void destroy() {
        super.destroy();
        executor.shutdownNow();
    }

    public static void appstate_delete(AbstractAppState state) {

        state = null;

    }

    public static void Clear_Scene(SimpleApplication app, Node rootNode, BulletAppState bulletAppState) {

        app.getGuiViewPort().clearProcessors();

        app.getGuiNode().detachAllChildren();

        rootNode.detachAllChildren();

        Collection<PhysicsRigidBody> bodies = bulletAppState.getPhysicsSpace().getRigidBodyList();

        for (Iterator<PhysicsRigidBody> it = bodies.iterator(); it.hasNext();) {
            PhysicsRigidBody body = it.next();
            bulletAppState.getPhysicsSpace().remove(body);
        }

    }
}
