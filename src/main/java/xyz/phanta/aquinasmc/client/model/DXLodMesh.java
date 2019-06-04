package xyz.phanta.aquinasmc.client.model;

import net.minecraft.util.math.Vec3d;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class DXLodMesh implements DXModelVertData, DXModelAnivData {

    private final int wedgeCount;
    private final Vec3d[][] compiledVertices;
    private final List<Poly> compiledFaces;

    DXLodMesh(byte[] data) {
        ByteBuffer in = ByteBuffer.wrap(data);
        in.order(ByteOrder.LITTLE_ENDIAN);
        // inherited from Mesh
        if (in.get() != 0) {
            throw new IllegalStateException("Cannot parse nonzero Unreal properties!");
        }
        in.position(in.position() + 45);
        Vec3d[] vertices = new Vec3d[readVarInt(in)];
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = new Vec3d(
                    in.getShort() * 0.00390625D,
                    in.getShort() * 0.00390625D,
                    in.getShort() * 0.00390625D);
            in.position(in.position() + 2);
        }
        in.position(in.position() + 4);
        if (in.get() != 0) {
            throw new IllegalStateException("LODMesh with nonzero Mesh poly count!");
        }
        int seqCount = readVarInt(in); // ignore anim seqs because the user defines them
        while (seqCount > 0) {
            readVarInt(in);
            readVarInt(in);
            in.position(in.position() + 8);
            int funcs = readVarInt(in);
            while (funcs > 0) {
                in.position(in.position() + 4);
                readVarInt(in);
                --funcs;
            }
            in.position(in.position() + 4);
            --seqCount;
        }
        in.position(in.position() + 4);
        if (in.get() != 0) {
            throw new IllegalStateException("LODMesh with nonzero connect count!");
        }
        in.position(in.position() + 45);
        int vertLinkCount = readVarInt(in);
        in.position(in.position() + vertLinkCount * 4);
        int texCount = readVarInt(in); // ignore textures because the user defines them
        while (texCount > 0) {
            readVarInt(in);
            --texCount;
        }
        int bBoxCount = readVarInt(in);
        in.position(in.position() + 25 * bBoxCount);
        int bSphereCount = readVarInt(in);
        in.position(in.position() + 16 * bSphereCount);
        int verticesPerFrame = in.getInt(); // hopefully doesn't matter that it's unsigned
        int frameCount = in.getInt();
        in.position(in.position() + 52); // ignore transforms because the user defines them
        int texLodCount = readVarInt(in);
        in.position(in.position() + texLodCount * 4);
        // LODMesh
        int collapsePointThus = readVarInt(in);
        in.position(in.position() + collapsePointThus * 2);
        int faceLevelCount = readVarInt(in);
        in.position(in.position() + faceLevelCount * 2);
        Face[] faces = new Face[readVarInt(in)];
        for (int i = 0; i < faces.length; i++) {
            faces[i] = new Face(in);
        }
        int collapseWedgeThus = readVarInt(in);
        in.position(in.position() + collapseWedgeThus * 2);
        wedgeCount = readVarInt(in);
        Wedge[] wedges = new Wedge[wedgeCount];
        for (int i = 0; i < wedges.length; i++) {
            wedges[i] = new Wedge(in);
        }
        int[] materials = new int[readVarInt(in)];
        for (int i = 0; i < materials.length; i++) {
            in.position(in.position() + 4);
            materials[i] = in.getInt();
        }
        in.position(in.position() + 4);
        int specialVerts = in.getInt();
        compiledVertices = IntStream.range(0, frameCount)
                .mapToObj(f -> Arrays.stream(wedges)
                        .map(w -> w.compile(f, vertices, verticesPerFrame, specialVerts))
                        .toArray(Vec3d[]::new))
                .toArray(Vec3d[][]::new);
        compiledFaces = Arrays.stream(faces).map(f -> f.compile(wedges, materials)).collect(Collectors.toList());
    }

    @Override
    public int getFrameCount() {
        return compiledVertices.length;
    }

    @Override
    public Vec3d getVertex(int frame, int vertexIndex) {
        return compiledVertices[frame][vertexIndex];
    }

    @Override
    public int getVertexCount() {
        return wedgeCount;
    }

    @Override
    public List<Poly> getPolygons() {
        return compiledFaces;
    }

    private static int readVarInt(ByteBuffer in) {
        byte first = in.get();
        int value = first & 0x3F;
        if ((first & 0x40) != 0) {
            byte next;
            int shift = 6;
            do {
                next = in.get();
                value |= (next & 0x7F) << shift;
                shift += 7;
            } while ((next & 0x80) != 0);
        }
        return (first & 0x80) != 0 ? -value : value;
    }

    private static class Face {

        private final int wedge1, wedge2, wedge3, material;

        Face(ByteBuffer in) {
            this.wedge1 = Short.toUnsignedInt(in.getShort());
            this.wedge2 = Short.toUnsignedInt(in.getShort());
            this.wedge3 = Short.toUnsignedInt(in.getShort());
            this.material = Short.toUnsignedInt(in.getShort());
        }

        Poly compile(Wedge[] wedges, int[] materials) {
            // TODO figure out mesh type
            Wedge w1 = wedges[wedge1], w2 = wedges[wedge2], w3 = wedges[wedge3];
            return new Poly(
                    wedge1, wedge2, wedge3,
                    MeshType.NORMAL_1SIDE,
                    w1.u, w1.v, w2.u, w2.v, w3.u, w3.v,
                    materials[material]);
        }

    }

    private static class Wedge {

        private final int vertex;
        private final float u, v;

        Wedge(ByteBuffer in) {
            this.vertex = Short.toUnsignedInt(in.getShort());
            this.u = Byte.toUnsignedInt(in.get()) * DXModelVertData.UV_CONV;
            this.v = Byte.toUnsignedInt(in.get()) * DXModelVertData.UV_CONV;
        }

        Vec3d compile(int frame, Vec3d[] vertices, int verticesPerFrame, int specialVerts) {
            return vertices[specialVerts + frame * verticesPerFrame + vertex];
        }

    }

}
