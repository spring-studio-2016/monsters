package edu.bsu.storygame.core.view;

import edu.bsu.storygame.core.assets.ImageCache;
import edu.bsu.storygame.core.model.*;
import edu.bsu.storygame.core.util.IconScaler;
import playn.scene.GroupLayer;
import playn.scene.Layer;
import pythagoras.f.Dimension;
import pythagoras.f.IDimension;
import react.*;
import tripleplay.anim.AnimGroup;
import tripleplay.anim.Animation;
import tripleplay.game.ScreenStack;
import tripleplay.ui.*;
import tripleplay.ui.layout.AxisLayout;

import static com.google.common.base.Preconditions.checkNotNull;

public final class NotebookLayer extends GroupLayer {

    public static final float OPEN_CLOSE_ANIM_DURATION = 400f;
    private static final float TOP = 100;

    private final IDimension closedSize;
    private final Stylesheet stylesheet;
    private final GameContext context;
    private final PageLayer cover;
    private final PageLayer encounterPage;
    private final PageLayer reactionPage;
    private final PageLayer storyPage;
    private final PageLayer skillsPage;
    private final PageLayer conclusionPage;
    private final PageLayer endPage;
    private final Player player;
    private final Interface iface;
    private final PageLayer[] pages;
    private float depthCounter = 0;


    public final UnitSignal onDone = new UnitSignal();

    public NotebookLayer(final Player player, IDimension closedSize, final GameContext context) {
        super(closedSize.width() * 2, closedSize.height());
        this.iface = ((ScreenStack.UIScreen) context.game.screenStack.top()).iface;

        this.closedSize = new Dimension(closedSize);
        this.stylesheet = GameStyle.newSheet(context.game);
        this.context = checkNotNull(context);
        this.player = checkNotNull(player);

        setOrigin(Origin.TC);

        this.cover = new CoverPage();
        this.encounterPage = new EncounterPage();
        this.reactionPage = new ReactionPage();
        this.storyPage = new StoryPage();
        this.skillsPage = new SkillsPage();
        this.conclusionPage = new ConclusionPage();
        this.endPage = new EndPage();

        pages = new PageLayer[]{
                cover,
                encounterPage,
                reactionPage,
                storyPage,
                skillsPage,
                conclusionPage,
                endPage
        };
        addPages(pages);

        context.phase.connect(new Slot<Phase>() {
            @Override
            public void onEmit(Phase phase) {
                if (player == context.currentPlayer.get()) {
                    if (phase == Phase.STORY) {
                        turnToStory();
                    } else if (phase == Phase.CONCLUSION) {
                        turnToConclusion();
                    }
                }
            }
        });

    }

    private void addPages(Layer... layers) {
        for (int i = layers.length - 1; i >= 0; i--) {
            addAt(layers[i], closedSize.width(), 0);
            layers[i].setDepth(layers.length - i);
        }
    }

    private abstract class PageLayer extends GroupLayer {

        protected final int color;
        protected final Interface iface;
        protected final Root root;

        protected PageLayer(Layout layout) {
            super(closedSize.width(), closedSize.height());
            color = player.color;
            iface = ((ScreenStack.UIScreen) context.game.screenStack.top()).iface;
            root = iface.createRoot(layout, stylesheet, this)
                    .setSize(closedSize)
                    .addStyles(Style.BACKGROUND.is(Background.solid(color)));
        }
    }


    private final class CoverPage extends PageLayer {

        private CoverPage() {
            super(AxisLayout.vertical().offStretch());
            root.add(new Label(player.name + "'s Story")
                            .addStyles(Style.HALIGN.left),
                    new ScoreLabel()
                            .addStyles(Style.HALIGN.left),
                    new SkillGroup().addStyles(Style.HALIGN.left),
                    new Shim(0, 0).setConstraint(AxisLayout.stretched()));
        }

        private final class ScoreLabel extends Label {
            private ScoreLabel() {
                super("Story Points: 0");
                player.storyPoints.connect(new Slot<Integer>() {
                    @Override
                    public void onEmit(Integer integer) {
                        text.update("Score: " + integer);
                    }
                });
            }
        }

        private final class SkillGroup extends Group {

            private SkillGroup() {
                super(AxisLayout.horizontal().offStretch());
                updatePlayerSkills();
                player.skills.connect(new RList.Listener<Skill>() {
                    @Override
                    public void onAdd(Skill skill) {
                        SkillGroup.this.updatePlayerSkills();
                    }

                    @Override
                    public void onRemove(Skill skill) {
                        SkillGroup.this.updatePlayerSkills();
                    }
                });
            }

            private void updatePlayerSkills() {
                this.removeAll();
                int skillCounter = 1;
                this.add(new Label("Skills: ")
                        .addStyles(Style.HALIGN.left));
                for (Skill skill : player.skills) {
                    if (!(player.skills.size() == skillCounter)) {
                        this.add(new Label(skill.name), new Label(", "));
                    } else {
                        this.add(new Label(skill.name));
                    }
                    skillCounter++;
                }
            }
        }
    }

    private final class EncounterPage extends PageLayer {
        private EncounterPage() {
            super(AxisLayout.vertical());
            context.encounter.connect(new ValueView.Listener<Encounter>() {
                @Override
                public void onChange(Encounter encounter, Encounter t1) {
                    if(encounter == null){
                        root.removeAll();
                    } else if (context.currentPlayer.get() == player){
                        root.add(new Label("I encountered a "));
                        root.add(new EncounterImage(encounter));
                        root.add(new Label(encounter.name));
                    }
                }
            });
        }

        private class EncounterImage extends Label {
            private IconScaler scaler;
            final float IMAGE_SIZE = 0.8f;

            private EncounterImage(Encounter encounter) {
                this.scaler = new IconScaler(context.game);
                ImageCache.Key imageKey;
                try {
                    imageKey = ImageCache.Key.valueOf(encounter.imageKey.toUpperCase());
                } catch (IllegalArgumentException e) {
                    imageKey = ImageCache.Key.MISSING_IMAGE;
                }
                final float desiredWidth = IMAGE_SIZE * encounterPage.width();
                Icon scaledIcon = scaler.scale(imageKey, desiredWidth);
                icon.update(scaledIcon);
            }
        }
    }

    private final class ReactionPage extends PageLayer {
        private ReactionPage() {
            super(AxisLayout.vertical());
            root.add(new Label("I decided to"));
            root.add(new ReactionGroup());
        }

        private final class ReactionGroup extends Group {

            public ReactionGroup() {
                super(AxisLayout.vertical());
                context.encounter.connect(new Slot<Encounter>() {
                    @Override
                    public void onEmit(Encounter encounter) {
                        removeAll();
                        if (encounter != null) {
                            add(makeReactionButtonAreaFor(encounter));
                        }
                    }
                });

            }

            private Group makeReactionButtonAreaFor(Encounter encounter) {
                Group group = new Group(new AxisLayout.Vertical());
                for (Reaction reaction : encounter.reactions) {
                    group.add(new ReactionButton(reaction));
                }
                return group;
            }

            final class ReactionButton extends Button {
                private ReactionButton(final Reaction reaction) {
                    super(reaction.name);
                    onClick(new Slot<Button>() {
                        @Override
                        public void onEmit(Button button) {
                            context.reaction.update(reaction);
                            context.phase.update(Phase.HANDOFF);
                        }
                    });
                    context.phase.connect(new Slot<Phase>() {
                        @Override
                        public void onEmit(Phase phase) {
                            setEnabled(phase.equals(Phase.ENCOUNTER));
                        }
                    });
                }
            }
        }
    }

    private final class StoryPage extends PageLayer {
        protected StoryPage() {
            super(AxisLayout.vertical());
            context.reaction.connect(new ValueView.Listener<Reaction>() {
                @Override
                public void onChange(Reaction reaction, Reaction t1) {
                    if(reaction == null){
                        root.removeAll();
                    } else if (context.currentPlayer.get() == player){
                        root.add(new Label(reaction.story.text).addStyles(Style.TEXT_WRAP.is(true)));
                    }
                }
            });
        }
    }

    private final class SkillsPage extends PageLayer {
        protected SkillsPage() {
            super(AxisLayout.vertical());
            context.reaction.connect(new Slot<Reaction>() {
                @Override
                public void onEmit(Reaction reaction) {
                    if (reaction == null) {
                        root.removeAll();
                    } else if (context.currentPlayer.get() == player) {
                        root.add(new Label("You used:"));
                        for (SkillTrigger skillTrigger : reaction.story.triggers) {
                            SkillButton button = new SkillButton(skillTrigger);
                            button.setEnabled(context.currentPlayer.get().skills.contains(skillTrigger.skill));
                            root.add(button);
                        }
                        Button noSkill = new Button("No Skill").onClick(new Slot<Button>() {
                            @Override
                            public void onEmit(Button button) {
                                context.conclusion.update(context.reaction.get().story.noSkill.conclusion);
                                context.phase.update(Phase.CONCLUSION);
                            }
                        });
                        root.add(noSkill);
                    }
                }
            });
        }

        private final class SkillButton extends Button {

            public SkillButton(final SkillTrigger skillTrigger) {
                super(skillTrigger.skill.name);
                onClick(new Slot<Button>() {
                    @Override
                    public void onEmit(Button button) {
                        context.conclusion.update(skillTrigger.conclusion);
                        context.phase.update(Phase.CONCLUSION);
                    }
                });
            }
        }
    }

    private final class ConclusionPage extends PageLayer {
        private final Label label = new Label().addStyles(Style.TEXT_WRAP.on);

        protected ConclusionPage() {
            super(AxisLayout.vertical());
            root.add(label);
            context.conclusion.connect(new SignalView.Listener<Conclusion>() {
                @Override
                public void onEmit(Conclusion conclusion) {
                    if (conclusion != null) {
                        label.text.update(conclusion.text);
                    }
                }
            });
        }
    }

    private final class EndPage extends PageLayer {
        protected EndPage() {
            super(AxisLayout.vertical());
            root.add(new Button("Close notebook").onClick(new Slot<Button>() {
                @Override
                public void onEmit(Button button) {
                    onDone.emit();
                }
            }));
        }
    }

    /**
     * Open the book.
     * <p/>
     * There is not currently a real "flip" animation, and so this does a page shuffle animation instead,
     * like a stack of loose pages.
     */
    public void open() {
        depthCounter = 0;
        iface.anim.add(movePageLeft(cover))
                .then()
                .add(movePageLeft(encounterPage));
    }

    private Animation movePageLeft(final PageLayer layer) {
        AnimGroup group = new AnimGroup();
        group.action(new SetDepthToTop(layer))
                .then()
                .tweenX(layer)
                .to(0)
                .in(OPEN_CLOSE_ANIM_DURATION)
                .easeIn()
                .then()
                .action(new SetDepthAndUpdateCounter(layer));
        return group.toAnim();
    }

    private void turnToStory() {
        context.game.plat.log().debug("Turning to story");
        iface.anim.add(movePageLeft(reactionPage))
                .then()
                .add(movePageLeft(storyPage));
    }

    private void turnToConclusion() {
        context.game.plat.log().debug("Turning to conclusion");
        iface.anim.add(movePageLeft(skillsPage))
                .then()
                .add(movePageLeft(conclusionPage));
    }

    public void close() {
        depthCounter = 0;
        iface.anim.add(movePageRight(conclusionPage))
                .then()
                .add(movePageRight(skillsPage))
                .then()
                .add(movePageRight(storyPage))
                .then()
                .add(movePageRight(reactionPage))
                .then()
                .add(movePageRight(encounterPage))
                .then()
                .add(movePageRight(cover))
                .then()
                .action(new Runnable() {
                    @Override
                    public void run() {
                        // Reset depths to fix the problem that 'endPage' never moves, which
                        // breaks my silly depthCounter approach
                        for (int i = 0; i < pages.length; i++) {
                            pages[i].setDepth(pages.length - i);
                        }
                    }
                });
    }

    private Animation movePageRight(final PageLayer layer) {
        final float centerX = width() / 2f;
        AnimGroup group = new AnimGroup();
        group.action(new SetDepthToTop(layer))
                .then()
                .tweenX(layer)
                .to(centerX)
                .in(OPEN_CLOSE_ANIM_DURATION / 6)
                .easeIn()
                .then()
                .action(new SetDepthAndUpdateCounter(layer));
        return group.toAnim();
    }

    private final class SetDepthToTop implements Runnable {
        private final Layer layer;

        private SetDepthToTop(Layer layer) {
            this.layer = checkNotNull(layer);
        }

        @Override
        public void run() {
            layer.setDepth(TOP);
        }
    }

    private final class SetDepthAndUpdateCounter implements Runnable {
        private final Layer layer;

        private SetDepthAndUpdateCounter(Layer layer) {
            this.layer = checkNotNull(layer);
        }

        @Override
        public void run() {
            layer.setDepth(depthCounter);
            depthCounter++;
        }
    }
}