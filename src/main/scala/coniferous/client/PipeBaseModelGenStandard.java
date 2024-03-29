/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

/*
 * Copyright (c) 2019 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package coniferous.client;

import coniferous.pipe.PipeEntity;
import coniferous.pipe.SidedPipe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class PipeBaseModelGenStandard {

    // Models
    private static final MutableQuad[][][] QUADS;
    private static final MutableQuad[][][] QUADS_COLOURED;

    static {
        QUADS = new MutableQuad[2][][];
        QUADS_COLOURED = new MutableQuad[2][][];
        final double colourOffset = 0.01;
        Vec3d[] faceOffset = new Vec3d[6];
        for (Direction face : Direction.values()) {
            faceOffset[face.ordinal()] = new Vec3d(face.getOpposite().getVector()).multiply(colourOffset);
        }

        // not connected
        QUADS[0] = new MutableQuad[6][2];
        QUADS_COLOURED[0] = new MutableQuad[6][2];
        Vec3d center = new Vec3d(0.5f, 0.5f, 0.5f);
        Vec3d radius = new Vec3d(0.25f, 0.25f, 0.25f);
        UvFaceData uvs = new UvFaceData();
        uvs.minU = uvs.minV = 4 / 16f;
        uvs.maxU = uvs.maxV = 12 / 16f;
        for (Direction face : Direction.values()) {
            MutableQuad quad = ModelUtil.createFace(face, center, radius, uvs);
            quad.setDiffuse(quad.normalvf());
            QUADS[0][face.ordinal()][0] = quad;
            dupDarker(QUADS[0][face.ordinal()]);

            MutableQuad[] colQuads = ModelUtil.createDoubleFace(face, center, radius, uvs);
            for (MutableQuad q : colQuads) {
                q.translatevd(faceOffset[face.ordinal()]);
            }
            QUADS_COLOURED[0][face.ordinal()] = colQuads;
        }

        int[][] uvsRot = { //
                {2, 0, 3, 3}, //
                {0, 2, 1, 1}, //
                {2, 0, 0, 2}, //
                {0, 2, 2, 0}, //
                {3, 3, 0, 2}, //
                {1, 1, 2, 0} //
        };

        UvFaceData[] types = { //
                UvFaceData.from16(4, 0, 12, 4), //
                UvFaceData.from16(4, 12, 12, 16), //
                UvFaceData.from16(0, 4, 4, 12), //
                UvFaceData.from16(12, 4, 16, 12) //
        };

        // connected
        QUADS[1] = new MutableQuad[6][8];
        QUADS_COLOURED[1] = new MutableQuad[6][8];
        for (Direction side : Direction.values()) {
            center = new Vec3d(//
                    0.5 + side.getOffsetX() * 0.375f, //
                    0.5 + side.getOffsetY() * 0.375f, //
                    0.5 + side.getOffsetZ() * 0.375f //
            );
            radius = new Vec3d(//
                    side.getAxis() == Axis.X ? 0.125f : 0.25f, //
                    side.getAxis() == Axis.Y ? 0.125f : 0.25f, //
                    side.getAxis() == Axis.Z ? 0.125f : 0.25f //
            );//

            int i = 0;
            for (Direction face : Direction.values()) {
                if (face.getAxis() == side.getAxis()) continue;
                MutableQuad quad = ModelUtil.createFace(face, center, radius, types[i]);
                quad.rotateTextureUp(uvsRot[side.ordinal()][i]);

                MutableQuad col = new MutableQuad(quad);

                quad.setDiffuse(quad.normalvf());
                QUADS[1][side.ordinal()][i] = quad;

                col.translatevd(faceOffset[face.ordinal()]);
                QUADS_COLOURED[1][side.ordinal()][i++] = col;
            }
            dupDarker(QUADS[1][side.ordinal()]);
            dupInverted(QUADS_COLOURED[1][side.ordinal()]);
        }
    }

    private static void dupDarker(MutableQuad[] quads) {
        int halfLength = quads.length / 2;
        float mult = 0.75f;
        for (int i = 0; i < halfLength; i++) {
            int n = i + halfLength;
            MutableQuad from = quads[i];
            if (from != null) {
                MutableQuad to = from.copyAndInvertNormal();
                to.setCalculatedDiffuse();
                to.multColourd(mult);
                quads[n] = to;
            }
        }
    }

    private static void dupInverted(MutableQuad[] quads) {
        int halfLength = quads.length / 2;
        for (int i = 0; i < halfLength; i++) {
            int n = i + halfLength;
            MutableQuad from = quads[i];
            if (from != null) {
                quads[n] = from.copyAndInvertNormal();
            }
        }
    }

    // Model Usage

    public static List<BakedQuad> generateCutout(PipeEntity.PipeRenderData key) {
        List<MutableQuad> quads = new ArrayList<>();

        for (Direction face : Direction.values()) {
            boolean connected = key.isConnected(face);
            Sprite sprite = connected ? getSprite(key, face) : getCenterSprite(key);
            int quadsIndex = connected ? 1 : 0;
            MutableQuad[] quadArray = QUADS[quadsIndex][face.ordinal()];
            addQuads(quadArray, quads, sprite);
        }
        List<BakedQuad> bakedQuads = new ArrayList<>();
        for (MutableQuad q : quads) {
            bakedQuads.add(q.toBakedBlock());
        }
        return bakedQuads;
    }

    private static Sprite getPipeSprite(String id) {
        return MinecraftClient.getInstance().getSpriteAtlas().getSprite("coniferous:pipe_" + id);
    }

    public static Sprite getCenterSprite(PipeEntity.PipeRenderData key) {
        return getPipeSprite(key.kind().toString());
    }

    private static Sprite getSprite(PipeEntity.PipeRenderData key, Direction face) {
        if (key.pipe() instanceof SidedPipe) {
            if (((SidedPipe) key.pipe()).mainSide() == face) {
                return getPipeSprite(key.kind().toString() + "_main");
            }
        }
        return getCenterSprite(key);
    }

    private static void addQuads(MutableQuad[] from, List<MutableQuad> to, Sprite sprite) {
        for (MutableQuad f : from) {
            if (f == null) {
                continue;
            }
            MutableQuad copy = new MutableQuad(f);
            copy.setSprite(sprite);
            copy.texFromSprite(sprite);
            to.add(copy);
        }
    }
}