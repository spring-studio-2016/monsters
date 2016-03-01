package edu.bsu.storygame.core.view;

import edu.bsu.storygame.core.model.GameContext;
import edu.bsu.storygame.core.model.Phase;
import playn.scene.GroupLayer;
import playn.scene.Layer;
import react.Slot;
import tripleplay.ui.*;
import tripleplay.ui.layout.AxisLayout;

import static com.google.common.base.Preconditions.checkNotNull;

public class HandoffDialogFactory {

    private final GameContext context;

    public HandoffDialogFactory(GameContext context) {
        this.context = checkNotNull(context);
    }

    public Layer create(Interface iface) {
        final GroupLayer layer = new GroupLayer(context.game.bounds.width() / 2f, context.game.bounds.height() / 2f);
        layer.setVisible(false);

        final HandoffDialog dialog = new HandoffDialog();
        dialog.setConstraint(Constraints.fixedSize(layer.width(), layer.height()));

        iface.createRoot(AxisLayout.vertical(), GameStyle.newSheet(context.game), layer)
                .setSize(layer.width(), layer.height())
                .add(dialog);
        context.phase.connect(new Slot<Phase>() {
            @Override
            public void onEmit(Phase phase) {
                if (phase.equals(Phase.HANDOFF)) {
                    final String otherPlayerName = context.otherPlayer().name;
                    dialog.label.text.update("Hand the device to " + otherPlayerName);
                }
                layer.setVisible(phase.equals(Phase.HANDOFF));
            }
        });
        return layer;
    }

    final class HandoffDialog extends Group {
        final Label label = new StyledLabel();

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

    static final class StyledLabel extends Label {
        @Override
        protected Class<?> getStyleClass() {
            return PlayerCreationGroup.StyledLabel.class;
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