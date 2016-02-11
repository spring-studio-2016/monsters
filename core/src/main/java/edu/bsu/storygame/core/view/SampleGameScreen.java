package edu.bsu.storygame.core.view;

import edu.bsu.storygame.core.assets.TileCache;
import edu.bsu.storygame.core.model.GameContext;
import edu.bsu.storygame.core.MonsterGame;
import edu.bsu.storygame.core.model.Encounter;
import edu.bsu.storygame.core.model.Phase;
import edu.bsu.storygame.core.model.Player;
import playn.core.Game;
import playn.scene.GroupLayer;
import playn.scene.Mouse;
import playn.scene.Pointer;
import pythagoras.f.Dimension;
import react.Connection;
import react.Slot;
import tripleplay.game.ScreenStack;
import tripleplay.ui.*;
import tripleplay.ui.layout.AxisLayout;
import tripleplay.util.Colors;

import static com.google.common.base.Preconditions.checkNotNull;

public class SampleGameScreen extends ScreenStack.UIScreen {

    private final MonsterGame game;
    private final GameContext context;
    private final GroupLayer boundedLayer;

    public SampleGameScreen(final MonsterGame game) {
        super(checkNotNull(game).plat);
        this.game = game;
        this.context = new GameContext(game, new Player("Abigail"), new Player("Bruce"));
        this.boundedLayer = new GroupLayer(game.bounds.width(), game.bounds.height());
        layer.addAt(boundedLayer,
                (game.plat.graphics().viewSize.width() - game.bounds.width()) / 2,
                (game.plat.graphics().viewSize.height() - game.bounds.height()) / 2);

        configurePointerInput();
        createUI();
        context.phase.connect(new Slot<Phase>() {
            private Connection connection;
            private final float SIZE_PERCENT = 0.9f;

            @Override
            public void onEmit(Phase phase) {
                if (phase.equals(Phase.ENCOUNTER)) {
                    popupEncounterDialog();
                }
            }

            private void popupEncounterDialog() {
                final Root dialog = iface.createRoot(AxisLayout.vertical(), SimpleStyles.newSheet(game.plat.graphics()), boundedLayer);
                dialog.setStyles(Style.BACKGROUND.is(Background.solid(Colors.LIGHT_GRAY)));
                dialog.setSize(boundedLayer.width() * SIZE_PERCENT, boundedLayer.height() * SIZE_PERCENT)
                        .setLocation(boundedLayer.width() * (1-SIZE_PERCENT)/2, boundedLayer.height());
                iface.anim.tweenY(dialog.layer)
                        .to(boundedLayer.height() * (1-SIZE_PERCENT)/2)
                        .in(200f)
                        .easeIn();
                dialog.add(new EncounterView(context, new Encounter(context)));

                connection = context.phase.connect(new Slot<Phase>() {
                    @Override
                    public void onEmit(Phase phase) {
                        if (phase.equals(Phase.END_OF_ROUND)) {
                            iface.removeRoot(dialog);
                            connection.close();
                        }
                    }
                });
            }
        });
        configurePlayerAdvancementAtEndOfRound();
    }

    private void configurePointerInput() {
        new Pointer(game().plat, layer, true);
        game().plat.input().mouseEvents.connect(new Mouse.Dispatcher(layer, true));
    }

    private void createUI() {
        iface.createRoot(AxisLayout.vertical().offStretch(), SimpleStyles.newSheet(game.plat.graphics()), boundedLayer)
                .setSize(boundedLayer.width(), boundedLayer.height())
                .setStyles(Style.BACKGROUND.is(Background.image(game.tileCache.tile(TileCache.Key.BACKGROUND))))
                .add(new Label() {
                    {
                        updateText();
                        context.currentPlayer.connect(new Slot<Player>() {
                            @Override
                            public void onEmit(Player player) {
                                updateText();
                            }

                        });
                    }

                    private void updateText() {
                        text.update(context.currentPlayer.get().name + "\'s turn");
                    }
                })
                .add(new Label() {
                         {
                             updateText();
                             SampleGameScreen.this.context.phase.connect(new Slot<Phase>() {
                                 @Override
                                 public void onEmit(Phase phase) {
                                     updateText();
                                 }
                             });
                         }

                         private void updateText() {
                             text.update("Current phase: " + context.phase.get().name());
                         }
                     },
                        new MapView(context, new Dimension(boundedLayer.width(), boundedLayer.height())));
    }

    private void configurePlayerAdvancementAtEndOfRound() {
        context.phase.connect(new Slot<Phase>() {
            @Override
            public void onEmit(Phase phase) {
                if (phase.equals(Phase.END_OF_ROUND)) {
                    advancePlayer();
                    context.phase.update(Phase.MOVEMENT);
                }
            }

            private void advancePlayer() {
                for (int i = 0, limit = context.players.size(); i < limit; i++) {
                    if (context.currentPlayer.get().equals(context.players.get(i))) {
                        context.currentPlayer.update(context.players.get((i + 1) % context.players.size()));
                        return;
                    }
                }
                throw new IllegalStateException("Cannot advance player");
            }
        });
    }


    @Override
    public Game game() {
        return game;
    }
}