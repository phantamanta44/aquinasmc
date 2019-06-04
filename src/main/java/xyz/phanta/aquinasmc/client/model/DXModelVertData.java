package xyz.phanta.aquinasmc.client.model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

interface DXModelVertData {

    float UV_CONV = 0.0627451F;

    int getVertexCount();

    List<Poly> getPolygons();

    class VertFile implements DXModelVertData {

        private final int vertexCount;
        private final List<Poly> polygons;

        VertFile(byte[] data) {
            ByteBuffer in = ByteBuffer.wrap(data);
            in.order(ByteOrder.LITTLE_ENDIAN);
            Poly[] polyArr = new Poly[Short.toUnsignedInt(in.getShort())];
            this.vertexCount = Short.toUnsignedInt(in.getShort());
            in.position(in.position() + 44);
            for (int i = 0; i < polyArr.length; i++) {
                int i1 = Short.toUnsignedInt(in.getShort());
                int i2 = Short.toUnsignedInt(in.getShort());
                int i3 = Short.toUnsignedInt(in.getShort());
                MeshType type = MeshType.getFor(in.get());
                in.position(in.position() + 1);
                float u1 = Byte.toUnsignedInt(in.get()) * UV_CONV, v1 = Byte.toUnsignedInt(in.get()) * UV_CONV;
                float u2 = Byte.toUnsignedInt(in.get()) * UV_CONV, v2 = Byte.toUnsignedInt(in.get()) * UV_CONV;
                float u3 = Byte.toUnsignedInt(in.get()) * UV_CONV, v3 = Byte.toUnsignedInt(in.get()) * UV_CONV;
                int tex = Byte.toUnsignedInt(in.get());
                polyArr[i] = new Poly(i1, i2, i3, type, u1, v1, u2, v2, u3, v3, tex);
                in.position(in.position() + 1);
            }
            this.polygons = Collections.unmodifiableList(Arrays.asList(polyArr));
        }

        @Override
        public int getVertexCount() {
            return vertexCount;
        }

        @Override
        public List<Poly> getPolygons() {
            return polygons;
        }

    }

    class Poly {

        private final int vertIndex1;
        private final int vertIndex2;
        private final int vertIndex3;
        private final MeshType meshType;
        private final float u1, v1;
        private final float u2, v2;
        private final float u3, v3;
        private final int textureIndex;

        Poly(int vertIndex1, int vertIndex2, int vertIndex3, MeshType meshType,
             float u1, float v1, float u2, float v2, float u3, float v3, int textureIndex) {
            this.vertIndex1 = vertIndex1;
            this.vertIndex2 = vertIndex2;
            this.vertIndex3 = vertIndex3;
            this.meshType = meshType;
            this.u1 = u1;
            this.v1 = v1;
            this.u2 = u2;
            this.v2 = v2;
            this.u3 = u3;
            this.v3 = v3;
            this.textureIndex = textureIndex;
        }

        int getVertIndex1() {
            return vertIndex1;
        }

        int getVertIndex2() {
            return vertIndex2;
        }

        int getVertIndex3() {
            return vertIndex3;
        }

        MeshType getMeshType() {
            return meshType;
        }

        float getU1() {
            return u1;
        }

        float getV1() {
            return v1;
        }

        float getU2() {
            return u2;
        }

        float getV2() {
            return v2;
        }

        float getU3() {
            return u3;
        }

        float getV3() {
            return v3;
        }

        int getTextureIndex() {
            return textureIndex;
        }

    }

    enum MeshType {

        NORMAL_1SIDE,
        NORMAL_2SIDE,
        TRANSLUCENT,
        MASKED,
        MOD_BLENDED,
        INVISIBLE,
        UNKNOWN;

        public static MeshType getFor(byte meshType) {
            switch (meshType) {
                case 0:
                    return NORMAL_1SIDE;
                case 1:
                    return NORMAL_2SIDE;
                case 2:
                    return TRANSLUCENT;
                case 3:
                    return MASKED;
                case 4:
                    return MOD_BLENDED;
                case 8:
                    return INVISIBLE;
            }
            return UNKNOWN;
        }

    }

}
