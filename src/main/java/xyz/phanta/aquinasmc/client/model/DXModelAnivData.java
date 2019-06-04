package xyz.phanta.aquinasmc.client.model;

import net.minecraft.util.math.Vec3d;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

interface DXModelAnivData {

    int getFrameCount();

    Vec3d getVertex(int frame, int vertexIndex);

    class AnivFile implements DXModelAnivData {

        private final Vec3d[][] vertices;

        AnivFile(int vertexCount, byte[] data) {
            ByteBuffer in = ByteBuffer.wrap(data);
            in.order(ByteOrder.LITTLE_ENDIAN);
            int frameCount = Short.toUnsignedInt(in.getShort());
            this.vertices = new Vec3d[frameCount][vertexCount];
            int frameSize = Short.toUnsignedInt(in.getShort());
            if (frameSize == vertexCount * 4) {
                for (int i = 0; i < frameCount; i++) {
                    for (int j = 0; j < vertexCount; j++) {
                        if (in.remaining() >= 4) {
                            int encoded = in.getInt();
                            double x = (encoded & 0x7FF) / 8D;
                            double y = ((encoded >> 11) & 0x7FF) / 8D;
                            double z = ((encoded >> 22) & 0x3FF) / 4D;
                            vertices[i][j] = new Vec3d(
                                    -(x > 128 ? x - 256 : x),
                                    y > 128 ? y - 256 : y,
                                    z > 128 ? z - 256 : z);
                        } else {
                            vertices[i][j] = Vec3d.ZERO;
                        }
                    }
                }
            } else if (frameSize == vertexCount * 8) {
                for (int i = 0; i < frameCount; i++) {
                    for (int j = 0; j < vertexCount; j++) {
                        if (in.remaining() >= 8) {
                            vertices[i][j] = new Vec3d(in.getShort(), -in.getShort(), in.getShort());
                            in.position(in.position() + 2);
                        } else {
                            vertices[i][j] = Vec3d.ZERO;
                        }
                    }
                }
            } else {
                throw new IllegalStateException("Bad Unreal ANIV file!");
            }
        }

        @Override
        public int getFrameCount() {
            return vertices.length;
        }

        @Override
        public Vec3d getVertex(int frame, int vertexIndex) {
            return vertices[frame][vertexIndex];
        }

    }

}
