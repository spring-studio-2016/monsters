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

package edu.bsu.storygame.core.assets;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import playn.core.Assets;
import playn.core.Image;
import react.RFuture;
import react.RPromise;
import react.Slot;
import react.Try;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class ImageCache {

    public enum Key {
        MAIN_MENU_BG("main_menu_bg.png"),
        BACKGROUND("world_map.png"),
        MONSTER_HAND("monster_hand.png"),
        COCKATRICE("cockatrice.png"),
        LOGO("logo.png"),
        MISSING_IMAGE("missing_image.png"),
        BUNYIP("bunyip.png"),
        KAPPA("kappa.png"),
        KRAKEN("kraken.png"),
        NESSY("nessy.png"),
        WRAITH("wraith.png"),
        STAR("star.png"),
        CHUPACABRA("chupacabra.png"),
        CHINESE_DRAGON("chinese_dragon.png"),
        DEER_WOMEN("deer_woman.png"),
        IMPUNDULU("impundulu.png"),
        TANIWHA("taniwha.png"),
        ADZE("adze.png"),
        YOWIE("yowie.png"),
        WENDIGO("wendigo.png"),
        INTRO_SCENE_2("intro_scene_2.png"),
        INTRO_SCENE_1("intro_scene_1.png");

        private final String path;

        Key(String relativePath) {
            this.path = "images/" + relativePath;
        }
    }

    private final EnumMap<Key, Image> map = Maps.newEnumMap(Key.class);

    public ImageCache(Assets assets) {
        List<RFuture<Image>> futures = Lists.newArrayListWithCapacity(Key.values().length);
        for (final Key key : Key.values()) {
            final Image image = assets.getImage(key.path);
            image.state.onSuccess(new Slot<Image>() {
                @Override
                public void onEmit(Image image) {
                    map.put(key,image);
                }
            });
            futures.add(image.state);
        }
        RFuture.collect(futures).onComplete(new Slot<Try<Collection<Image>>>() {
            @Override
            public void onEmit(Try<Collection<Image>> collectionTry) {
                if (collectionTry.isSuccess()) {
                    ((RPromise<ImageCache>)state).succeed(ImageCache.this);
                } else {
                    ((RPromise<ImageCache>)state).fail(collectionTry.getFailure());
                }
            }
        });

    }

    public final RFuture<ImageCache> state = RPromise.create();

    public Image image(Key key) {
        checkNotNull(key);
        Image image = map.get(key);
        checkNotNull(image, "Image not cached: " + key.name());
        return image;
    }
}
