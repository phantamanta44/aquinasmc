package xyz.phanta.aquinasmc.client.model;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntLongMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntLongHashMap;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nullable;
import java.util.*;

class DXModelState {

    private static final long CACHE_DURATION = 15000L;
    private static final LinkedHashMap<UUID, DXModelState> cache = new LinkedHashMap<>(16, 0.75F, true);

    static DXModelState lookup(UUID id, long currentTime) {
        Iterator<Map.Entry<UUID, DXModelState>> iter = cache.entrySet().iterator();
        while (iter.hasNext() && iter.next().getValue().isExpired(currentTime)) {
            iter.remove();
        }
        DXModelState state = cache.computeIfAbsent(id, k -> new DXModelState(currentTime));
        state.lastAccessTime = currentTime;
        return state;
    }

    private boolean actionIndexInit = false;
    private byte actionIndex = 0;
    private boolean skinStateIndexInit = false;
    private byte skinStateIndex = 0;
    @Nullable
    private DXModelSeq sequence = null;
    private final TIntIntMap skinStates;
    private final TIntLongMap skinStateTimes;
    private int frame = 0;
    private long lastFrameTime;
    private long lastAccessTime;

    private DXModelState(long creationTime) {
        this.lastFrameTime = creationTime;
        this.skinStates = new TIntIntHashMap(16, 0.75F, 0, 0);
        this.skinStateTimes = new TIntLongHashMap(16, 0.75F, 0, -1L);
    }

    boolean checkActionIndex(byte actionIndex) {
        if (!actionIndexInit) {
            actionIndexInit = true;
        } else if (actionIndex == this.actionIndex) {
            return false;
        }
        this.actionIndex = actionIndex;
        return true;
    }

    void updateSequence(DXModel.DXModelItem state, @Nullable EntityPlayer player, long currentTime, Map<String, DXModelSeq> sequences) {
        String initialSequence = state.getSequence(player);
        if (initialSequence != null) {
            DXModelSeq newSeq = sequences.get(initialSequence);
            if (newSeq != null) {
                sequence = newSeq;
                frame = sequence.getStartFrame();
                lastFrameTime = currentTime;
            }
        }
    }

    boolean checkSkinStateIndex(byte skinStateIndex) {
        if (!skinStateIndexInit) {
            skinStateIndexInit = true;
        } else if (skinStateIndex == this.skinStateIndex) {
            return false;
        }
        this.skinStateIndex = skinStateIndex;
        return true;
    }

    void updateSkinState(DXModel.DXModelItem state, @Nullable EntityPlayer player, long currentTime, int skinCount) {
        for (int i = 0; i < skinCount; i++) {
            skinStates.put(i, state.getSkinState(player, i));
            skinStateTimes.put(i, currentTime);
        }
    }

    float advance(long currentTime, Map<String, DXModelSeq> sequences) {
        long diff = currentTime - lastFrameTime;
        if (sequence != null) {
            if (diff >= sequence.getFrameDuration()) {
                lastFrameTime = currentTime;
                if (++frame == sequence.getEndFrame()) {
                    String nextSeqName = sequence.getNextSequence();
                    if (nextSeqName != null) {
                        DXModelSeq nextSeq = sequences.get(nextSeqName);
                        if (nextSeq != null) {
                            sequence = nextSeq;
                        }
                    }
                    frame = sequence.getStartFrame();
                }
                return 0F;
            }
            return diff / (float)sequence.getFrameDuration();
        }
        return 0F;
    }

    int getFrame() {
        return frame;
    }

    int getNextFrame(Map<String, DXModelSeq> sequences) {
        if (sequence != null) {
            if (frame + 1 == sequence.getEndFrame()) {
                String next = sequence.getNextSequence();
                if (next != null) {
                    DXModelSeq nextSeq = sequences.get(next);
                    if (nextSeq != null) {
                        return nextSeq.getStartFrame();
                    }
                }
                return sequence.getStartFrame();
            }
            return frame + 1;
        }
        return 0;
    }

    float getPartialFrame(long currentTime) {
        return sequence == null ? 0F : ((currentTime - lastFrameTime) / (float)sequence.getFrameDuration());
    }

    void advanceSkinStates(long currentTime, List<DXModelMultiSkin> skins) {
        for (int i = 0; i < skins.size(); i++) {
            int state = skinStates.get(i);
            int duration = skins.get(i).getDuration(state);
            if (duration < 0 || currentTime - skinStateTimes.get(i) > duration) {
                skinStates.put(i, skins.get(i).getNext(state));
                skinStateTimes.put(i, currentTime);
            }
        }
    }

    int getSkinState(int skin) {
        return skinStates.get(skin);
    }

    private boolean isExpired(long currentTime) {
        return currentTime - lastAccessTime > CACHE_DURATION;
    }

}
