package edu.bsu.storygame.core.view;

import edu.bsu.storygame.core.MonsterGame;
import edu.bsu.storygame.core.assets.Typeface;
import playn.core.Font;
import playn.core.Game;
import react.Slot;
import tripleplay.game.ScreenStack;
import tripleplay.ui.*;
import tripleplay.ui.layout.AxisLayout;
import tripleplay.util.Colors;

public class PlayerCreationScreen extends ScreenStack.UIScreen {

    private final MonsterGame game;

    public PlayerCreationScreen(final MonsterGame game) {
        super(game.plat);
        this.game = game;
        Root root = iface.createRoot(AxisLayout.vertical(), GameStyle.newSheet(game), layer);
        root.setSize(game.bounds.width(), game.bounds.height());
        root.addStyles(Style.BACKGROUND.is(Background.solid(Colors.ORANGE)));
        root.add(new Group(AxisLayout.vertical()).add(
                new Label("Nightmare Defenders!")
        ));
        root.add(new Button("Start")
                .addStyles(Style.FONT.is(Typeface.OXYGEN.in(game).atSize(0.05f)),
                        Style.HALIGN.center)
                .onClick(new Slot<Button>() {
                    @Override
                    public void onEmit(Button button) {
                        game.screenStack.push(new SampleGameScreen(game), game.screenStack.slide());
                    }
                }));
    }

    @Override
    public Game game() {
        return game;
    }
}
