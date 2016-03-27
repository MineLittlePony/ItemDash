package mnm.mods.itemdash.easing;

import mnm.mods.itemdash.easing.types.*;

public class EasingsFactory implements Easings {

    public static Easings getInstance() {
        return new EasingsFactory();
    }

    private EasingsFactory() {}

    @Override
    public EasingType linear() {
        return new Linear();
    }

    @Override
    public EasingType quadratic() {
        return new Quadratic();
    }

    @Override
    public EasingType cubic() {
        return new Cubic();
    }

    @Override
    public EasingType quartic() {
        return new Quartic();
    }

    @Override
    public EasingType quintic() {
        return new Quintic();
    }

    @Override
    public EasingType sinusoidal() {
        return new Sinusoidal();
    }

    @Override
    public EasingType circular() {
        return new Circular();
    }
}
