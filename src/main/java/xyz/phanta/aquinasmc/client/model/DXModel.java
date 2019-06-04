package xyz.phanta.aquinasmc.client.model;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.model.IModelState;
import xyz.phanta.aquinasmc.client.DXClientConfig;
import xyz.phanta.aquinasmc.constant.NbtConst;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DXModel implements IModel {

    @SuppressWarnings("NullableProblems")
    @CapabilityInject(DXModelItem.class)
    public static Capability<DXModelItem> ANIM_CAP;

    private final DXModelVertData vertData;
    private final DXModelAnivData anivData;
    private final Vec3d posePos;
    private final Vec3d poseRot;
    private final Vec3d scale;
    private final Map<String, ResourceLocation> textures;
    private final ResourceLocation particleTexture;
    private final List<DXModelMultiSkin> skins;
    private final Map<String, DXModelSeq> sequences;
    private final Collection<ResourceLocation> deps;
    private final DXModelLoader loader;
    private final int[] skinStateProducts;

    DXModel(DXModelVertData vertData, DXModelAnivData anivData, Vec3d posePos, Vec3d poseRot, Vec3d scale,
                   Map<String, ResourceLocation> textures, ResourceLocation particleTexture,
                   List<DXModelMultiSkin> skins, Map<String, DXModelSeq> sequences,
                   Collection<ResourceLocation> deps, DXModelLoader loader) {
        this.vertData = vertData;
        this.anivData = anivData;
        this.posePos = posePos;
        this.poseRot = poseRot;
        this.scale = scale;
        this.textures = textures;
        this.particleTexture = particleTexture;
        this.skins = skins;
        this.sequences = sequences;
        this.deps = deps;
        this.loader = loader;
        this.skinStateProducts = new int[skins.size()];
        skinStateProducts[0] = skins.get(0).getStateCount();
        for (int i = 1; i < skinStateProducts.length; i++) {
            skinStateProducts[i] = skinStateProducts[i - 1] * skins.get(i).getStateCount();
        }
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> texBakery) {
        int totalSkinStateProduct = skinStateProducts[skinStateProducts.length - 1];
        //noinspection unchecked
        List<DXModelQuad>[][] frames = new List[totalSkinStateProduct][anivData.getFrameCount()];
        int[] skinStates = new int[skins.size()];
        List<DXModelVertData.Poly> polygons = vertData.getPolygons();
        for (int totalStateNum = 0; totalStateNum < totalSkinStateProduct; totalStateNum++) {
            for (int frameNum = 0; frameNum < anivData.getFrameCount(); frameNum++) {
                List<DXModelQuad> frame = new ArrayList<>();
                for (DXModelVertData.Poly poly : polygons) {
                    if (poly.getMeshType() != DXModelVertData.MeshType.UNKNOWN) {
                        DXModelMultiSkin polySkin = skins.get(poly.getTextureIndex());
                        int skinState = skinStates[poly.getTextureIndex()];
                        if (polySkin.visible(skinState)) { // TODO use polygon mesh type
                            frame.add(new DXModelQuad.Visible(
                                    posePos, poseRot, scale,
                                    anivData.getVertex(frameNum, poly.getVertIndex1()),
                                    anivData.getVertex(frameNum, poly.getVertIndex2()),
                                    anivData.getVertex(frameNum, poly.getVertIndex3()),
                                    texBakery.apply(polySkin.getTexture(skinState, textures)),
                                    1F, 1F, 1F, polySkin.getAlpha(skinState), polySkin.ignoresLight(skinState),
                                    poly.getU1(), poly.getV1(), poly.getU2(), poly.getV2(), poly.getU3(), poly.getV3()));
                        } else {
                            frame.add(new DXModelQuad.Invisible(
                                    anivData.getVertex(frameNum, poly.getVertIndex1()),
                                    anivData.getVertex(frameNum, poly.getVertIndex2()),
                                    anivData.getVertex(frameNum, poly.getVertIndex3())));
                        }
                    }
                }
                frames[totalStateNum][frameNum] = frame;
            }
            for (int i = skinStates.length - 1; i >= 0; i--) {
                if (++skinStates[i] == skins.get(i).getStateCount()) {
                    skinStates[i] = 0;
                } else {
                    break;
                }
            }
        }
        return new MasterBakedModel(frames, format, texBakery.apply(particleTexture));
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return deps;
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return textures.values();
    }

    public static void setSequence(EntityPlayer player, ItemStack stack, String sequence) {
        if (stack.hasCapability(ANIM_CAP, null)) {
            Objects.requireNonNull(stack.getCapability(ANIM_CAP, null)).setSequence(player, sequence);
        }
    }

    public static void setSkinState(EntityPlayer player, ItemStack stack, int skin, int state) {
        if (stack.hasCapability(ANIM_CAP, null)) {
            Objects.requireNonNull(stack.getCapability(ANIM_CAP, null)).setSkinState(player, skin, state);
        }
    }

    private class MasterBakedModel implements IBakedModel {

        private final List<DXModelQuad>[][] unbakedFrames;
        private final BakedAnimationFrame[][] frames;
        private final VertexFormat format;
        private final TextureAtlasSprite particleTextureSprite;
        private final ItemStackModelMapper stackModelMapper = new ItemStackModelMapper();
        private final List<DXModelQuad> dynFrameData;
        private final BakedAnimationFrame dynFrame;

        private MasterBakedModel(List<DXModelQuad>[][] frames, VertexFormat format, TextureAtlasSprite particleTextureSprite) {
            this.unbakedFrames = frames;
            this.frames = DXClientConfig.modelAnimDynRender
                    ? computeUninterpolatedFrames(frames, format)
                    : precomputeInterpolatedFrames(frames, format);
            this.format = format;
            this.particleTextureSprite = particleTextureSprite;
            this.dynFrameData = new ArrayList<>(frames[0][0]);
            this.dynFrame = new BakedAnimationFrame(dynFrameData, format);
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            return frames[0][0].getQuads(state, side, rand);
        }

        @Override
        public boolean isAmbientOcclusion() {
            return false;
        }

        @Override
        public boolean isGui3d() {
            return true;
        }

        @Override
        public boolean isBuiltInRenderer() {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            return particleTextureSprite;
        }

        @Override
        public ItemOverrideList getOverrides() {
            return stackModelMapper;
        }

        private class ItemStackModelMapper extends ItemOverrideList {

            ItemStackModelMapper() {
                super(Collections.emptyList());
            }

            @Override
            public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack,
                                               @Nullable World world, @Nullable EntityLivingBase entity) {
                EntityPlayer player = entity instanceof EntityPlayer ? (EntityPlayer)entity : null;
                DXModelItem modelItem = Objects.requireNonNull(stack.getCapability(ANIM_CAP, null));
                if (modelItem.requiresPlayer() && player == null) {
                    return frames[0][0];
                }
                long currentTime = System.currentTimeMillis();
                DXModelState state = DXModelState.lookup(modelItem.getIdentifier(player), currentTime, skins.size());
                state.advanceSkinStates(currentTime, skins);
                float grad = 0F;
                if (state.checkActionIndex(modelItem.getActionIndex(player))) {
                    state.update(modelItem, player, currentTime, sequences);
                } else {
                    grad = state.advance(currentTime, sequences);
                }
                int skinState = state.getSkinState(0);
                for (int i = 1; i < skins.size(); i++) {
                    skinState += Math.min(state.getSkinState(i), skins.get(i).getStateCount() - 1) * skinStateProducts[i - 1];
                }
                if (DXClientConfig.modelAnimDynRender) {
                    List<DXModelQuad> thisFrame = unbakedFrames[skinState][state.getFrame()];
                    List<DXModelQuad> nextFrame = unbakedFrames[skinState][
                            state.getNextFrame(sequences) % unbakedFrames[skinState].length];
                    int light = loader.getLighting();
                    float lr = ((light >> 16) & 0xFF) / 255F;
                    float lg = ((light >> 8) & 0xFF) / 255F;
                    float lb = (light & 0xFF) / 255F;
                    for (int i = 0; i < dynFrameData.size(); i++) {
                        DXModelQuad quad = thisFrame.get(i).lerp(nextFrame.get(i), grad);
                        quad.setRgb(lr, lg, lb);
                        dynFrameData.set(i, quad);
                    }
                    dynFrame.rebakeDynamic(dynFrameData);
                    return dynFrame;
                } else {
                    return frames[skinState][Math.min(
                            (int)Math.floor((state.getFrame() + grad) * (DXClientConfig.modelAnimAotLerpDensity + 1)),
                            frames[skinState].length - 1)];
                }
            }

        }

        private BakedAnimationFrame[][] computeUninterpolatedFrames(List<DXModelQuad>[][] frames, VertexFormat format) {
            return Arrays.stream(frames)
                    .map(s -> Arrays.stream(s)
                            .map(q -> new BakedAnimationFrame(q, format))
                            .toArray(BakedAnimationFrame[]::new))
                    .toArray(BakedAnimationFrame[][]::new);
        }

        private BakedAnimationFrame[][] precomputeInterpolatedFrames(List<DXModelQuad>[][] frames, VertexFormat format) {
            int iFrameCount = DXClientConfig.modelAnimAotLerpDensity;
            int frameCount = frames[0].length, quadCount = frames[0][0].size();
            BakedAnimationFrame[][] result = new BakedAnimationFrame[frames.length][frameCount * (iFrameCount + 1)];
            for (int stateNum = 0; stateNum < frames.length; stateNum++) {
                for (int startFrame = 0; startFrame < frameCount; startFrame++) {
                    int next = -1;
                    for (DXModelSeq seq : sequences.values()) {
                        if (startFrame == seq.getEndFrame() - 1) {
                            String nextSeq = seq.getNextSequence();
                            next = (nextSeq != null ? sequences.get(nextSeq) : seq).getStartFrame();
                            break;
                        }
                    }
                    if (next == -1) {
                        next = (startFrame + 1) % frameCount;
                    }
                    int offset = startFrame * (iFrameCount + 1);
                    result[stateNum][offset] = new BakedAnimationFrame(frames[stateNum][startFrame], format);
                    for (int iFrame = 1; iFrame <= iFrameCount; iFrame++) {
                        float grad = (float)iFrame / iFrameCount;
                        List<DXModelQuad> newFrame = new ArrayList<>();
                        for (int quad = 0; quad < quadCount; quad++) {
                            newFrame.add(frames[stateNum][startFrame].get(quad)
                                    .lerp(frames[stateNum][next].get(quad), grad));
                        }
                        result[stateNum][offset + iFrame] = new BakedAnimationFrame(newFrame, format);
                    }
                }
            }
            return result;
        }

        private class BakedAnimationFrame implements IBakedModel {

            private final List<BakedQuad> quads;

            BakedAnimationFrame(List<DXModelQuad> quads, VertexFormat format) {
                this.quads = quads.stream()
                        .filter(DXModelQuad::isVisible)
                        .map(q -> q.bake(format))
                        .collect(Collectors.toList());
            }

            void rebakeDynamic(List<DXModelQuad> quads) {
                this.quads.clear();
                for (DXModelQuad quad : quads) {
                    if (quad.isVisible()) {
                        this.quads.add(quad.bake(format));
                    }
                }
            }

            @Override
            public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
                return side == null ? quads : Collections.emptyList();
            }

            @Override
            public boolean isAmbientOcclusion() {
                return false;
            }

            @Override
            public boolean isGui3d() {
                return true;
            }

            @Override
            public boolean isBuiltInRenderer() {
                return false;
            }

            @Override
            public TextureAtlasSprite getParticleTexture() {
                return particleTextureSprite;
            }

            @Override
            public ItemOverrideList getOverrides() {
                return ItemOverrideList.NONE;
            }

        }

    }

    public interface DXModelItem {

        boolean requiresPlayer();

        UUID getIdentifier(@Nullable EntityPlayer player);

        @Nullable
        String getSequence(@Nullable EntityPlayer player);

        void setSequence(@Nullable EntityPlayer player, String sequence);

        int getSkinState(@Nullable EntityPlayer player, int skin);

        void setSkinState(@Nullable EntityPlayer player, int skin, int state);

        byte getActionIndex(@Nullable EntityPlayer player);

        class Impl implements DXModelItem {

            private final UUID id = UUID.randomUUID();

            @Override
            public boolean requiresPlayer() {
                return false;
            }

            @Override
            public UUID getIdentifier(@Nullable EntityPlayer player) {
                return id;
            }

            @Nullable
            @Override
            public String getSequence(@Nullable EntityPlayer player) {
                return null;
            }

            @Override
            public void setSequence(@Nullable EntityPlayer player, String sequence) {
                // NO-OP
            }

            @Override
            public int getSkinState(@Nullable EntityPlayer player, int skin) {
                return 0;
            }

            @Override
            public void setSkinState(@Nullable EntityPlayer player, int skin, int state) {
                // NO-OP
            }

            @Override
            public byte getActionIndex(@Nullable EntityPlayer player) {
                return 0;
            }

        }

        class ForStack implements DXModelItem {

            private final ItemStack stack;

            public ForStack(ItemStack stack) {
                this.stack = stack;
            }

            public ForStack(ItemStack stack, String initialSequence) {
                this(stack);
                setSequence(null, initialSequence);
            }

            @Override
            public boolean requiresPlayer() {
                return false;
            }

            @Override
            public UUID getIdentifier(@Nullable EntityPlayer player) {
                return UUID.fromString(getTag().getString(NbtConst.ANIM_STATE_ID));
            }

            @Nullable
            @Override
            public String getSequence(@Nullable EntityPlayer player) {
                NBTTagCompound tag = getTag();
                return tag.hasKey(NbtConst.ANIM_STATE_SEQ) ? tag.getString(NbtConst.ANIM_STATE_SEQ) : null;
            }

            @Override
            public void setSequence(@Nullable EntityPlayer player, String sequence) {
                NBTTagCompound tag = getTag();
                tag.setString(NbtConst.ANIM_STATE_SEQ, sequence);
                byte actionIndex = tag.getByte(NbtConst.ANIM_STATE_INDEX);
                tag.setByte(NbtConst.ANIM_STATE_INDEX, ++actionIndex);
            }

            @Override
            public int getSkinState(@Nullable EntityPlayer player, int skin) {
                NBTTagCompound states = getTag().getCompoundTag(NbtConst.ANIM_SKIN_STATES);
                String key = Integer.toString(skin);
                if (states.hasKey(key)) {
                    return states.getInteger(key);
                }
                return 0;
            }

            @Override
            public void setSkinState(@Nullable EntityPlayer player, int skin, int state) {
                getTag().getCompoundTag(NbtConst.ANIM_SKIN_STATES).setInteger(Integer.toString(skin), state);
            }

            @Override
            public byte getActionIndex(@Nullable EntityPlayer player) {
                return getTag().getByte(NbtConst.ANIM_STATE_INDEX);
            }

            private NBTTagCompound getTag() {
                NBTTagCompound tag = stack.getTagCompound();
                if (tag == null) {
                    tag = new NBTTagCompound();
                    stack.setTagCompound(tag);
                }
                if (!tag.hasKey(NbtConst.ANIM_STATE)) {
                    NBTTagCompound stateTag = new NBTTagCompound();
                    stateTag.setString(NbtConst.ANIM_STATE_ID, UUID.randomUUID().toString());
                    stateTag.setByte(NbtConst.ANIM_STATE_INDEX, (byte)0);
                    NBTTagCompound skinState = new NBTTagCompound();
                    skinState.setInteger("0", 0);
                    stateTag.setTag(NbtConst.ANIM_SKIN_STATES, skinState);
                    tag.setTag(NbtConst.ANIM_STATE, stateTag);
                    return stateTag;
                }
                return tag.getCompoundTag(NbtConst.ANIM_STATE);
            }

        }

    }

}
