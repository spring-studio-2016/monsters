package edu.bsu.storygame.core.model;

import com.google.common.collect.ImmutableList;
import edu.bsu.storygame.core.assets.ImageCache;
import playn.core.Image;

public class Encounter {

    public final Image image;
    public final String name = "Angry Cockatrice";
    public final ImmutableList<Reaction> reactions = ImmutableList.of(
            new Reaction("Fight",
                    new Story("You encounter an angry beast and charge forward to fight it like a silly English person.",
                            ImmutableList.of(
                                    new SkillTrigger(Skill.WEAPON_USE, "You pound the tar out of that chicken thing."),
                                    new SkillTrigger(Skill.LOGIC, "You make peace with the ugly bird lizard.")))),
            new Reaction("Hide",
                    new Story("You hear a terrifying sound and hide like a girly man",
                            ImmutableList.of(
                                    new SkillTrigger(Skill.WEAPON_USE, "You spend the rest of the day sharpening your sword, not that you have the guts to use it."),
                                    new SkillTrigger(Skill.LOGIC, "You relish the mustard of this dinner time.")))));

    public Encounter(GameContext context) {
        this.image = context.game.imageCache.image(ImageCache.Key.COCKATRICE);
    }
}
