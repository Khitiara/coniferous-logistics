package coniferous.client;

import net.minecraft.client.texture.Sprite;

public class UvFaceData {
    static final UvFaceData DEFAULT = new UvFaceData(0, 0, 1, 1);

    public float minU, maxU, minV, maxV;

    public UvFaceData() {
    }

    public UvFaceData(UvFaceData from) {
        this.minU = from.minU;
        this.maxU = from.maxU;
        this.minV = from.minV;
        this.maxV = from.maxV;
    }

    public UvFaceData(float uMin, float vMin, float uMax, float vMax) {
        this.minU = uMin;
        this.maxU = uMax;
        this.minV = vMin;
        this.maxV = vMax;
    }

    public UvFaceData(double minU, double minV, double maxU, double maxV) {
        this((float) minU, (float) minV, (float) maxU, (float) maxV);
    }

    public static UvFaceData from16(double minU, double minV, double maxU, double maxV) {
        return new UvFaceData(minU / 16.0, minV / 16.0, maxU / 16.0, maxV / 16.0);
    }

    public static UvFaceData from16(int minU, int minV, int maxU, int maxV) {
        return new UvFaceData(minU / 16f, minV / 16f, maxU / 16f, maxV / 16f);
    }

    public UvFaceData andSub(UvFaceData sub) {
        float size_u = maxU - minU;
        float size_v = maxV - minV;

        float min_u = minU + sub.minU * size_u;
        float min_v = minV + sub.minV * size_v;
        float max_u = minU + sub.maxU * size_u;
        float max_v = minV + sub.maxV * size_v;

        return new UvFaceData(min_u, min_v, max_u, max_v);
    }

    public UvFaceData inParent(UvFaceData parent) {
        return parent.andSub(this);
    }

    @Override
    public String toString() {
        return "[ " + minU * 16 + ", " + minV * 16 + ", " + maxU * 16 + ", " + maxV * 16 + " ]";
    }

    public void inSprite(Sprite sprite) {
        minU = sprite.getU(minU * 16);
        minV = sprite.getV(minV * 16);
        maxU = sprite.getU(maxU * 16);
        maxV = sprite.getV(maxV * 16);
    }

    // public void inSprite(ISprite sprite) {
    // minU = (float) sprite.getInterpU(minU);
    // minV = (float) sprite.getInterpV(minV);
    // maxU = (float) sprite.getInterpU(maxU);
    // maxV = (float) sprite.getInterpV(maxV);
    // }
}