package xyz.phanta.aquinasmc.client.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.phantamanta44.libnine.capability.StatelessCapabilitySerializer;
import io.github.phantamanta44.libnine.client.model.L9Models;
import io.github.phantamanta44.libnine.util.helper.ResourceUtils;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.capabilities.CapabilityManager;
import xyz.phanta.aquinasmc.Aquinas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DXModelLoader implements ICustomModelLoader {

    private int renderLight = 0;

    public DXModelLoader() {
        CapabilityManager.INSTANCE.register(
                DXModel.DXModelItem.class, new StatelessCapabilitySerializer<>(), DXModel.DXModelItem.Impl::new);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        // NO-OP
    }

    @Override
    public boolean accepts(ResourceLocation resource) {
        try {
            JsonObject model = ResourceUtils.getAsJson(L9Models.getRealModelLocation(resource)).getAsJsonObject();
            return model.has("deus") && model.get("deus").getAsString().equals("ex");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public IModel loadModel(ResourceLocation resource) throws Exception {
        JsonObject modelDto = ResourceUtils.getAsJson(L9Models.getRealModelLocation(resource)).getAsJsonObject();
        List<ResourceLocation> deps = new ArrayList<>();

        DXModelVertData vert;
        DXModelAnivData aniv;
        if (modelDto.has("lodmesh_file")) {
            ResourceLocation lodMeshFile = Aquinas.INSTANCE.newResourceLocation(
                    "models/unreal/" + modelDto.get("lodmesh_file").getAsString());
            DXLodMesh mesh = new DXLodMesh(ResourceUtils.getAsBytes(lodMeshFile));
            vert = mesh;
            aniv = mesh;
            deps.add(lodMeshFile);
        } else {
            ResourceLocation vertFile = Aquinas.INSTANCE.newResourceLocation(
                    "models/unreal/" + modelDto.get("vert_file").getAsString());
            vert = new DXModelVertData.VertFile(ResourceUtils.getAsBytes(vertFile));
            deps.add(vertFile);
            ResourceLocation anivFile = Aquinas.INSTANCE.newResourceLocation(
                    "models/unreal/" + modelDto.get("aniv_file").getAsString());
            aniv = new DXModelAnivData.AnivFile(vert.getVertexCount(), ResourceUtils.getAsBytes(anivFile));
            deps.add(anivFile);
        }

        boolean stateless = modelDto.has("stateless") && modelDto.get("stateless").getAsBoolean();

        JsonObject poseDto = modelDto.get("pose").getAsJsonObject();
        Vec3d posePos = new Vec3d(
                poseDto.get("x").getAsDouble(),
                poseDto.get("y").getAsDouble(),
                poseDto.get("z").getAsDouble());
        Vec3d poseRot = new Vec3d(
                poseDto.get("pitch").getAsDouble(),
                poseDto.get("yaw").getAsDouble(),
                poseDto.get("roll").getAsDouble());

        Vec3d scale;
        if (modelDto.has("scale")) {
            JsonObject scaleDto = modelDto.get("scale").getAsJsonObject();
            scale = new Vec3d(
                    scaleDto.get("x").getAsDouble(),
                    scaleDto.get("y").getAsDouble(),
                    scaleDto.get("z").getAsDouble());
        } else {
            scale = new Vec3d(1D, 1D, 1D);
        }

        Map<String, ResourceLocation> textures = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : modelDto.get("textures").getAsJsonObject().entrySet()) {
            textures.put(entry.getKey(),
                    Aquinas.INSTANCE.newResourceLocation("unreal/" + entry.getValue().getAsString()));
        }
        ResourceLocation particleTexture = textures.get(modelDto.get("particleTexture").getAsString());

        List<DXModelMultiSkin> skins = new ArrayList<>();
        for (JsonElement skinDto : modelDto.get("skins").getAsJsonArray()) {
            if (skinDto.isJsonArray()) {
                skins.add(new DXModelMultiSkin.Multi(skinDto.getAsJsonArray()));
            } else {
                skins.add(new DXModelMultiSkin.Single(skinDto.getAsJsonObject()));
            }
        }

        Map<String, DXModelSeq> sequences = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : modelDto.get("sequences").getAsJsonObject().entrySet()) {
            JsonObject seqDto = entry.getValue().getAsJsonObject();
            sequences.put(entry.getKey(), new DXModelSeq(
                    entry.getKey(),
                    seqDto.get("startFrame").getAsInt(),
                    seqDto.get("frames").getAsInt(),
                    seqDto.has("rate") ? seqDto.get("rate").getAsDouble() : 1D,
                    seqDto.has("next") ? seqDto.get("next").getAsString() : null));
        }

        return new DXModel(vert, aniv, posePos, poseRot, scale,
                textures, particleTexture, skins, sequences, deps, this, stateless);
    }

    void setLighting(int light) {
        this.renderLight = light;
    }

    int getLighting() {
        return renderLight;
    }

}
