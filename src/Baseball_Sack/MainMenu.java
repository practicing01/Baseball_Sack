/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Baseball_Sack;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;


/**
 *
 * @author practicing01
 */
public class MainMenu extends AbstractAppState implements ScreenController{
    
    private SimpleApplication app;
    private Camera cam;
    private Node rootNode;
    private AssetManager assetManager;
    private AppStateManager stateManager;
    private AppSettings settings;
    private Nifty nifty;
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        //TODO: initialize your AppState, e.g. attach spatials to rootNode
        //this is called on the OpenGL thread after the AppState has been attached

        this.app = (SimpleApplication) app;
        this.stateManager = stateManager;
        this.cam = this.app.getCamera();
        this.rootNode = this.app.getRootNode();
        this.assetManager = this.app.getAssetManager();
        this.settings = app.getContext().getSettings();
        
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(
                assetManager, this.app.getInputManager(), this.app.getAudioRenderer(), this.app.getGuiViewPort());
   
        nifty = niftyDisplay.getNifty();
        
        nifty.fromXml("Interface/Gui/WindowMainMenu.xml", "ScreenMainMenu", this);
        
        this.app.getGuiViewPort().addProcessor(niftyDisplay);

    }
    
    @Override
    public void update(float tpf) {
        //TODO: implement behavior during runtime
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        //TODO: clean up what you initialized in the initialize method,
        //e.g. remove all spatials from rootNode
        //this is called on the OpenGL thread after the AppState has been detached
    }
    
    public void bind(Nifty nifty, Screen screen) {
        //System.out.println("bind( " + screen.getScreenId() + ")");
    }

    public void onStartScreen() {
        //System.out.println("onStartScreen");
    }

    public void onEndScreen() {
        //System.out.println("onEndScreen");
    }

    public void quit() {
        //nifty.gotoScreen("end");
    }
    
    public void play() {
        
        Main.Clear_Scene(app, rootNode, Main.bulletAppState);
        
        stateManager.detach(this);

        GameplayState gp = new GameplayState();

        stateManager.attach(gp);

        Main.appstate_delete(this);
        
    }

}
