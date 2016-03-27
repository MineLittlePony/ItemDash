package mnm.mods.itemdash.easing.types;

import mnm.mods.itemdash.easing.Easing;
import mnm.mods.itemdash.easing.EasingType;

public class Circular implements EasingType {

    @Override
    public Easing in() {
        return (t, b, c, d) -> {
            t /= d;
            return -c * (Math.sqrt(1 - t * t) - 1) + b;
        };
    }

    @Override
    public Easing out() {
        return (t, b, c, d) -> {
            t /= d;
            t--;
            return c * Math.sqrt(1 - t * t) + b;
        };
    }

    @Override
    public Easing inOut() {
        return (t, b, c, d) -> {
            t /= d / 2;
            if (t < 1)
                return -c / 2 * (Math.sqrt(1 - t * t) - 1) + b;
            return c / 2 * (Math.sqrt(1 - t * t) + 1) + b;
        };
    }

}
