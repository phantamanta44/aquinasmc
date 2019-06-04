package xyz.phanta.aquinasmc.client.model;

import io.github.phantamanta44.libnine.util.math.LinAlUtils;
import io.github.phantamanta44.libnine.util.math.MathUtils;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

interface DXModelQuad {

    Vec3d getVertex1();

    Vec3d getVertex2();

    Vec3d getVertex3();

    void setRgb(float red, float green, float blue);

    DXModelQuad lerp(DXModelQuad dest, float grad);

    boolean isVisible();

    BakedQuad bake(VertexFormat format);

    class Visible implements DXModelQuad {

        private final Vec3d posePos, poseRot, scale;
        private final Vec3d vert1, vert2, vert3;
        private final TextureAtlasSprite sprite;
        private float red, green, blue;
        private final boolean colourLocked;
        private final float alpha;
        private final float u1, v1, u2, v2, u3, v3;

        Visible(Vec3d posePos, Vec3d poseRot, Vec3d scale,
                       Vec3d vert1, Vec3d vert2, Vec3d vert3,
                       TextureAtlasSprite sprite,
                       float red, float green, float blue, float alpha, boolean colourLocked,
                       float u1, float v1, float u2, float v2, float u3, float v3) {
            this.posePos = posePos;
            this.poseRot = poseRot;
            this.scale = scale;
            this.vert1 = vert1;
            this.vert2 = vert2;
            this.vert3 = vert3;
            this.sprite = sprite;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
            this.colourLocked = colourLocked;
            this.u1 = u1;
            this.v1 = v1;
            this.u2 = u2;
            this.v2 = v2;
            this.u3 = u3;
            this.v3 = v3;
        }

        @Override
        public Vec3d getVertex1() {
            return vert1;
        }

        @Override
        public Vec3d getVertex2() {
            return vert2;
        }

        @Override
        public Vec3d getVertex3() {
            return vert3;
        }

        @Override
        public void setRgb(float red, float green, float blue) {
            if (!colourLocked) {
                this.red = red;
                this.green = green;
                this.blue = blue;
            }
        }

        @Override
        public DXModelQuad lerp(DXModelQuad dest, float grad) {
            return new Visible(posePos, poseRot, scale,
                    lerp(vert1, dest.getVertex1(), grad),
                    lerp(vert2, dest.getVertex2(), grad),
                    lerp(vert3, dest.getVertex3(), grad),
                    sprite, red, green, blue, alpha, colourLocked,
                    u1, v1, u2, v2, u3, v3);
        }

        @Override
        public boolean isVisible() {
            return true;
        }

        @Override
        public BakedQuad bake(VertexFormat format) {
            Vec3d v1 = preprocessVertex(vert1);
            Vec3d v2 = preprocessVertex(vert2);
            Vec3d v3 = preprocessVertex(vert3);
            UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
            builder.setTexture(sprite);
            Vec3d norm = v2.subtract(v1).crossProduct(v3.subtract(v1)).normalize();
            builder.setQuadOrientation(EnumFacing.getFacingFromVector((float)norm.x, (float)norm.y, (float)norm.z));
            writeVertex(v3, this.u3, this.v3, norm, sprite, format, builder);
            writeVertex(v2, this.u2, this.v2, norm, sprite, format, builder);
            writeVertex(v1, this.u1, this.v1, norm, sprite, format, builder);
            writeVertex(v1, this.u1, this.v1, norm, sprite, format, builder); // tri -> quad
            return builder.build();
        }

        private Vec3d preprocessVertex(Vec3d vertex) {
            // model space has really weird axes
            return rotate(rotate(rotate(
                    new Vec3d(0.0625D * vertex.x * scale.x, 0.0625D * vertex.y * scale.y, 0.0625D * vertex.z * scale.z),
                    LinAlUtils.Z_POS, poseRot.y), LinAlUtils.X_POS, poseRot.x), LinAlUtils.Y_POS, poseRot.z)
                    .addVector(posePos.x, posePos.y, -posePos.z);
        }

        private void writeVertex(Vec3d vertex, float u, float v, Vec3d norm, TextureAtlasSprite sprite,
                                 VertexFormat format, UnpackedBakedQuad.Builder builder) {
            for (int i = 0; i < format.getElementCount(); i++) {
                switch (format.getElement(i).getUsage()) {
                    case POSITION:
                        builder.put(i, (float)vertex.x, (float)vertex.y, (float)vertex.z, 1F);
                        break;
                    case NORMAL:
                        builder.put(i, (float)norm.x, (float)norm.y, (float)norm.z, 0F);
                        break;
                    case COLOR:
                        builder.put(i, red, green, blue, alpha);
                        break;
                    case UV:
                        builder.put(i, sprite.getInterpolatedU(u), sprite.getInterpolatedV(v), 0F, 1F);
                        break;
                    default:
                        builder.put(i);
                        break;
                }
            }
        }

        private static Vec3d lerp(Vec3d from, Vec3d to, float grad) {
            return new Vec3d(from.x + (to.x - from.x) * grad, from.y + (to.y - from.y) * grad, from.z + (to.z - from.z) * grad);
        }

        // adapted from libnine 1.13
        // https://github.com/phantamanta44/libnine/blob/1.13/src/main/kotlin/xyz/phanta/libnine/util/math/LinAlgUtil.kt
        private static Vec3d rotate(Vec3d vec, Vec3d axis, double angle) {
            Vec3d parallel = LinAlUtils.project(vec, axis);
            vec = vec.subtract(parallel);
            double magn = axis.lengthVector();
            double i = axis.x / magn;
            double j = axis.y / magn;
            double k = axis.z / magn;
            angle = angle * MathUtils.D2R_D;
            double cos = Math.cos(angle);
            double ncs = 1 - cos;
            double sin = Math.sin(angle);
            return new Vec3d(
                    vec.x * (ncs * i * i + cos) + vec.y * (ncs * i * j - sin * k) + vec.z * (ncs * i * k + sin * j) + parallel.x,
                    vec.x * (ncs * i * j + sin * k) + vec.y * (ncs * j * j + cos) + vec.z * (ncs * j * k - sin * i) + parallel.y,
                    vec.x * (ncs * i * k - sin * j) + vec.y * (ncs * j * k + sin * i) + vec.z * (ncs * k * k + cos) + parallel.z);
        }

    }

    class Invisible implements DXModelQuad {

        private final Vec3d vert1, vert2, vert3;

        Invisible(Vec3d vert1, Vec3d vert2, Vec3d vert3) {
            this.vert1 = vert1;
            this.vert2 = vert2;
            this.vert3 = vert3;
        }

        @Override
        public Vec3d getVertex1() {
            return vert1;
        }

        @Override
        public Vec3d getVertex2() {
            return vert2;
        }

        @Override
        public Vec3d getVertex3() {
            return vert3;
        }

        @Override
        public void setRgb(float red, float green, float blue) {
            // NO-OP
        }

        @Override
        public DXModelQuad lerp(DXModelQuad dest, float grad) {
            return new Invisible(
                    Visible.lerp(vert1, dest.getVertex1(), grad),
                    Visible.lerp(vert2, dest.getVertex2(), grad),
                    Visible.lerp(vert3, dest.getVertex3(), grad));
        }

        @Override
        public boolean isVisible() {
            return false;
        }

        @Override
        public BakedQuad bake(VertexFormat format) {
            throw new UnsupportedOperationException();
        }

    }

}
