package me.eddiep.ghost.client.core;

import com.badlogic.gdx.graphics.g2d.Batch;

public class Blend {
    private int srcFunc;
    private int dstFunc;

    public Blend(int srcFunc, int dstFunc) {
        this.srcFunc = srcFunc;
        this.dstFunc = dstFunc;
    }

    public static Blend fromBatch(Batch batch) {
        return new Blend(batch.getBlendSrcFunc(), batch.getBlendDstFunc());
    }

    public int getSrcFunc() {
        return srcFunc;
    }

    public int getDstFunc() {
        return dstFunc;
    }

    public void apply(Batch batch) {
        batch.setBlendFunction(srcFunc, dstFunc);
    }

    public boolean isDifferent(Batch batch) {
        return srcFunc != batch.getBlendSrcFunc() || dstFunc != batch.getBlendDstFunc();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Blend blend = (Blend) o;

        return srcFunc == blend.srcFunc && dstFunc == blend.dstFunc;

    }

    @Override
    public int hashCode() {
        int result = srcFunc;
        result = 31 * result + dstFunc;
        return result;
    }
}
