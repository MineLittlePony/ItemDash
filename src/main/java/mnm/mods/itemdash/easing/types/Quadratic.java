package mnm.mods.itemdash.easing.types;

import mnm.mods.itemdash.easing.Easing;
import mnm.mods.itemdash.easing.EasingType;

public class Quadratic implements EasingType {

    @Override
    public Easing in() {
        return (t, b, c, d) -> {
            t /= d;
            return c * t * t + b;
        };
    }

    @Override
    public Easing out() {
        return (t, b, c, d) -> {
            t /= d;
            return -c * t * (t - 2) + b;
        };
    }

    @Override
    public Easing inOut() {
        return (t, b, c, d) -> {
            t /= d / 2;
            if (t < 1)
                return c / 2 * t * t + b;
            t--;
            return -c / 2 * (t * (t - 2) - 1) + b;
        };
    }

}
