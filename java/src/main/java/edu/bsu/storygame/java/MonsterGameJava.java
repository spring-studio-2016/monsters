package edu.bsu.storygame.java;

import edu.bsu.storygame.core.MonsterGame;
import edu.bsu.storygame.core.assets.FontConstants;
import org.apache.commons.cli.*;
import playn.java.JavaPlatform;
import playn.java.LWJGLPlatform;
import pythagoras.i.Dimension;
import pythagoras.i.IDimension;

public class MonsterGameJava {

    private static final String[] FONT_SPECS = {
            "fonts/Oxygen-Light.ttf", FontConstants.OXYGEN_LIGHT_NAME,
            "fonts/Oxygen-Regular.ttf", FontConstants.OXYGEN_NAME,
            "fonts/PassionOne-Regular.ttf", FontConstants.PASSION_ONE_NAME
    };

    private static final IDimension DEFAULT_SIZE = new Dimension(960, 640);
    private static Dimension size = new Dimension(DEFAULT_SIZE.width(), DEFAULT_SIZE.height());

    public static void main(String[] args) {
        new CommandLineParser().process(args);

        LWJGLPlatform.Config config = new LWJGLPlatform.Config();
        config.width = size.width;
        config.height = size.height;

        LWJGLPlatform plat = new LWJGLPlatform(config);
        registerFonts(plat);

        MonsterGame.Config gameConf = new MonsterGame.Config(plat);
        gameConf.debugMode = true;
        new MonsterGame(gameConf);
        plat.start();
    }

    private static void registerFonts(JavaPlatform plat) {
        try {
            for (int i=0; i<FONT_SPECS.length; i+=2) {
                plat.graphics().registerFont(
                        FONT_SPECS[i+1],
                        plat.assets().getFont(FONT_SPECS[i])
                );
            }
        } catch (Exception e) {
            plat.log().error("Failed to load font", e);
        }
    }

    private static final class CommandLineParser extends BasicParser {
        private static final String SIZE_OPTION = "size";

        // Static access required by Apache Commons CLI API.
        @SuppressWarnings("AccessStaticViaInstance")
        private static Options createOptions() {
            Options options = new Options();
            options.addOption(
                    OptionBuilder.withLongOpt(SIZE_OPTION)
                    .withDescription("window size")
                    .hasArg()
                    .withArgName("<w>x<h>")
                    .create());
            return options;
        }

        public void process(String[] args) {
            CommandLine line;
            try {
                line = parse(createOptions(), args);
            } catch (ParseException e) {
                System.err.println("Parsing failed. Reason: " + e.getMessage());
                return;
            }
            if (line.hasOption(SIZE_OPTION)) {
                processSizeOption(line);
            }
        }

        private static void processSizeOption(CommandLine line) {
            String sizeString = line.getOptionValue(SIZE_OPTION);
            String[] separated = sizeString.split("x");
            if (separated.length != 2) {
                System.err.println("Illegal size expression, must be in form <w>x<h>: " + sizeString);
                System.err.println("Using defaults");
            } else {
                int w = Integer.valueOf(separated[0]);
                int h = Integer.valueOf(separated[1]);
                size.width = w;
                size.height = h;
            }
        }
    }
}
