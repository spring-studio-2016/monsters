/*
 * Copyright 2016 Traveler's Notebook: Monster Tales project authors
 *
 * This file is part of monsters
 *
 * monsters is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * monsters is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with monsters.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.bsu.storygame.core.view;

import edu.bsu.storygame.core.assets.AudioCache;
import edu.bsu.storygame.core.assets.AudioRandomizer;
import edu.bsu.storygame.core.assets.ImageCache;
import edu.bsu.storygame.core.model.GameContext;
import edu.bsu.storygame.core.model.Phase;
import playn.scene.GroupLayer;
import playn.scene.ImageLayer;
import playn.scene.Layer;
import react.Slot;
import tripleplay.ui.*;
import tripleplay.ui.layout.AxisLayout;

import static com.google.common.base.Preconditions.checkNotNull;

public class HandoffDialogFactory {

    private static final float ANIMATION_DURATION = 500f;
    private final GameContext context;
    private final AudioRandomizer audioRandomizer = new AudioRandomizer();

    public HandoffDialogFactory(GameContext context) {
        this.context = checkNotNull(context);
    }

    public Layer create(final Interface iface) {
        final GroupLayer layer = new GroupLayer(context.game.bounds.width(), context.game.bounds.height());
        layer.setVisible(false);

        final HandoffDialog dialog = new HandoffDialog();
        dialog.setConstraint(Constraints.fixedSize(layer.width() / 2f, layer.height() / 2f));

        iface.createRoot(AxisLayout.vertical(), GameStyle.newSheet(context.game), layer)
                .setSize(layer.width(), layer.height())
                .add(dialog);
        context.phase.connect(new Slot<Phase>() {
            @Override
            public void onEmit(Phase phase) {
                if (phase.equals(Phase.HANDOFF)) {
                    final String otherPlayerName = context.otherPlayer().name;
                    dialog.label.text.update("Please pass to " + otherPlayerName + ", who will read you your story!");
                    layer.setVisible(true);
                    iface.anim.play(context.game.audioCache.getSound(audioRandomizer.getKey(AudioRandomizer.Event.HANDOFF_SLIDE)))
                            .then()
                            .tweenY(layer)
                            .from(context.game.bounds.height())
                            .to(0)
                            .in(ANIMATION_DURATION)
                            .easeIn();
                } else if (phase.equals(Phase.STORY)) {
                    iface.anim.play(context.game.audioCache.getSound(AudioCache.Key.HANDOFF_SLIDE_4))
                            .then()
                            .tweenY(layer)
                            .from(0)
                            .to(context.game.bounds.height())
                            .in(ANIMATION_DURATION)
                            .easeOut();
                }
                ensureLayerIsOnTop();
            }

            private void ensureLayerIsOnTop() {
                layer.setDepth(100000);
            }
        });

        createMonsterHandIn(layer);
        return layer;
    }

    private void createMonsterHandIn(GroupLayer layer) {
        final ImageLayer monsterHand = new ImageLayer(context.game.imageCache.image(ImageCache.Key.MONSTER_HAND));
        final float handX = context.game.bounds.width() * 3 / 5;
        final float handY = context.game.bounds.height() * 3 / 5;
        final float scale = (context.game.bounds.height() - handY) / monsterHand.height();
        monsterHand.setScale(scale);
        layer.addAt(monsterHand, handX, handY);
    }

    final class HandoffDialog extends Group {
        final Label label = new Label("")
                .addStyles(Style.TEXT_WRAP.on,
                        Style.BACKGROUND.is(Background.blank().inset(
                                context.game.bounds.percentOfHeight(0.08f), 0)));

        private HandoffDialog() {
            super(AxisLayout.vertical().stretchByDefault());
            add(new Shim(0, 0),
                    label.setConstraint(AxisLayout.stretched()),
                    new OkButton().setConstraint(Constraints.fixedWidth(context.game.bounds.width() * 0.20f)),
                    new Shim(0, 0));
        }

        @Override
        protected Class<?> getStyleClass() {
            return HandoffDialog.class;
        }
    }

    final class OkButton extends Button {
        private OkButton() {
            super("Okay");
            onClick(new Slot<Button>() {
                @Override
                public void onEmit(Button button) {
                    context.phase.update(Phase.STORY);
                }
            });
        }

        @Override
        protected Class<?> getStyleClass() {
            return OkButton.class;
        }
    }
}
