package mnm.mods.itemdash.easing.types;

import mnm.mods.itemdash.easing.Easing;
import mnm.mods.itemdash.easing.EasingType;

public class Linear implements EasingType {

    @Override
    public Easing in() {
        return linear();
    }

    @Override
    public Easing out() {
        return linear();
    }

    @Override
    public Easing inOut() {
        return linear();
    }

    private Easing linear() {
        return (t, b, c, d) -> c * t / d + b;
    }

}
