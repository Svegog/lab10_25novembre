package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    
    private static final String SEP = File.separator;
    private static final String PATH = "src" + SEP + "main" + SEP + "resources" + SEP + "config.yml";

    /*
     * Adding start value to not get error from compiler.
     */
    private int min = 0;
    private int max = 100;
    private int attempts = 0;

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Read from files start.
         */
        try (BufferedReader br = new BufferedReader(
                new FileReader(PATH)
            );
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                final String[] component = line.split(":");

                if (component.length == 2) {
                    final String key = component[0].trim();
                    final int value = Integer.parseInt(component[1].trim());

                    // System.out.println("PRINT DI TEST: "+ key + " = " + value);
                    switch (key) {
                        case "minimum":
                            this.min = value;
                            break;
                        case "maximum":
                            this.max = value;
                            break;
                        case "attempts":
                            this.attempts = value;
                            break;
                        default:
                            throw new IllegalStateException("An error occured while reading the file");
                    }
                }

            } 
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }

        this.model = new DrawNumberImpl(min,max,attempts);
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl(), new DrawNumberViewImpl());
    }

}
