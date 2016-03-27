package mnm.mods.itemdash.easing.types;

import mnm.mods.itemdash.easing.Easing;
import mnm.mods.itemdash.easing.EasingType;

public class Sinusoidal implements EasingType {

    @Override
    public Easing in() {
        return (t, b, c, d) -> -c * Math.cos(t / d * (Math.PI / 2)) + c + b;
    }

    @Override
    public Easing out() {
        return (t, b, c, d) -> c * Math.sin(t / d * (Math.PI / 2)) + b;
    }

    @Override
    public Easing inOut() {
        return (t, b, c, d) -> -c / 2 * (Math.cos(Math.PI * t / d) - 1) + b;
    }

}
