package mnm.mods.itemdash.easing;

@FunctionalInterface
public interface Easing {

    /**
     * Eases to a position based on the time.
     * 
     * @param time The current time relative to the animation start
     * @param base The original position before move
     * @param change The amount to be moved
     * @param duration The total time the animation should take
     * @return The position for the animation
     */
    double ease(double time, double base, double change, double duration);
}
