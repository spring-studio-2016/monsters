package edu.bsu.storygame.core;

import edu.bsu.storygame.core.assets.ImageCache;
import edu.bsu.storygame.core.assets.TileCache;
import edu.bsu.storygame.core.view.SampleGameScreen;
import playn.core.Platform;
import playn.scene.SceneGame;
import tripleplay.game.ScreenStack;

public class MonsterGame extends SceneGame {

    private static final int UPDATE_RATE_MS = 33; // 30 times per second

    public final ImageCache imageCache;
    public final TileCache tileCache;

    public MonsterGame(Platform plat) {
        super(plat, UPDATE_RATE_MS);
        imageCache = new ImageCache(plat.assets());
        tileCache = new TileCache(plat.assets());
        ScreenStack screenStack = new ScreenStack(this, rootLayer);
        screenStack.push(new LoadingScreen(this, screenStack));
    }
}
