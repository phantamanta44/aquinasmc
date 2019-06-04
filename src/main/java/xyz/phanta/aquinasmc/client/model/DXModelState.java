package xyz.phanta.aquinasmc.client.model;

import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nullable;
import java.util.*;

class DXModelState {

    private static final long CACHE_DURATION = 15000L;
    private static final LinkedHashMap<UUID, DXModelState> cache = new LinkedHashMap<>(16, 0.75F, true);

    static DXModelState lookup(UUID id, long currentTime, int skinCount) {
        Iterator<Map.Entry<UUID, DXModelState>> iter = cache.entrySet().iterator();
        while (iter.hasNext() && iter.next().getValue().isExpired(currentTime)) {
            iter.remove();
        }
        DXModelState state = cache.computeIfAbsent(id, k -> new DXModelState(currentTime, skinCount));
        state.lastAccessTime = currentTime;
        return state;
    }

    private boolean initialized = false;
    private byte actionIndex = 0;
    @Nullable
    private DXModelSeq sequence = null;
    private final int[] skinStates;
    private final long[] skinStateTimes;
    private int frame = 0;
    private long lastFrameTime;
    private long lastAccessTime;

    private DXModelState(long creationTime, int skinCount) {
        this.lastFrameTime = creationTime;
        this.skinStates = new int[skinCount];
        this.skinStateTimes = new long[skinCount];
    }

    boolean checkActionIndex(byte actionIndex) {
        if (!initialized) {
            initialized = true;
        } else if (actionIndex == this.actionIndex) {
            return false;
        }
        this.actionIndex = actionIndex;
        return true;
    }

    void update(DXModel.DXModelItem state, @Nullable EntityPlayer player, long currentTime, Map<String, DXModelSeq> sequences) {
        String initialSequence = state.getSequence(player);
        if (initialSequence != null) {
            sequence = Objects.requireNonNull(sequences.get(initialSequence));
            frame = sequence.getStartFrame();
            lastFrameTime = currentTime;
        }
        for (int i = 0; i < skinStates.length; i++) {
            skinStates[i] = state.getSkinState(player, i);
            skinStateTimes[i] = currentTime;
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
                        sequence = Objects.requireNonNull(sequences.get(nextSeqName));
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
                    return sequences.get(next).getStartFrame();
                }
                return sequence.getStartFrame();
            }
            return frame + 1;
        }
        return 0;
    }

    void advanceSkinStates(long currentTime, List<DXModelMultiSkin> skins) {
        for (int i = 0; i < skinStates.length; i++) {
            int duration = skins.get(i).getDuration(skinStates[i]);
            if (duration < 0 || currentTime - skinStateTimes[i] > duration) {
                skinStates[i] = skins.get(i).getNext(skinStates[i]);
                skinStateTimes[i] = currentTime;
            }
        }
    }

    int getSkinState(int skin) {
        return skinStates[skin];
    }

    private boolean isExpired(long currentTime) {
        return currentTime - lastAccessTime > CACHE_DURATION;
    }

}
