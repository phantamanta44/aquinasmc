package xyz.phanta.aquinasmc.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Map;

interface DXModelMultiSkin {

    boolean visible(int state);

    float getAlpha(int state);

    boolean ignoresLight(int state);

    default int getStateCount() {
        return 1;
    }

    int getDuration(int state);

    default int getNext(int state) {
        return 0;
    }

    ResourceLocation getTexture(int state, Map<String, ResourceLocation> textures);

    class Single implements DXModelMultiSkin {

        private final boolean visible;
        @Nullable
        private final String texture;
        private final float alpha;
        private final boolean ignoreLight;
        private final int duration;

        Single(JsonObject dto) {
            if (dto.has("hidden") && dto.get("hidden").getAsBoolean()) {
                this.visible = false;
                this.texture = null;
            } else {
                this.visible = true;
                this.texture = dto.get("texture").getAsString();
            }
            this.alpha = dto.has("alpha") ? dto.get("alpha").getAsFloat() : 1F;
            this.ignoreLight = dto.has("ignore_light") && dto.get("ignore_light").getAsBoolean();
            this.duration = dto.has("duration") ? dto.get("duration").getAsInt() : -1;
        }

        @Override
        public boolean visible(int state) {
            return visible;
        }

        @Override
        public float getAlpha(int state) {
            return alpha;
        }

        @Override
        public boolean ignoresLight(int state) {
            return ignoreLight;
        }

        @Override
        public int getDuration(int state) {
            return duration;
        }

        @Override
        public ResourceLocation getTexture(int state, Map<String, ResourceLocation> textures) {
            return textures.get(texture);
        }

    }

    class Multi implements DXModelMultiSkin {

        private final State[] states;

        Multi(JsonArray stateListDto) {
            states = new State[stateListDto.size()];
            for (int i = 0; i < states.length; i++) {
                states[i] = new State(stateListDto.get(i).getAsJsonObject());
            }
        }

        @Override
        public int getStateCount() {
            return states.length;
        }

        @Override
        public boolean visible(int state) {
            return states[state].visible(0);
        }

        @Override
        public float getAlpha(int state) {
            return states[state].getAlpha(0);
        }

        @Override
        public boolean ignoresLight(int state) {
            return states[state].ignoresLight(0);
        }

        @Override
        public int getDuration(int state) {
            return states[state].getDuration(0);
        }

        @Override
        public int getNext(int state) {
            return states[state].getNext();
        }

        @Override
        public ResourceLocation getTexture(int state, Map<String, ResourceLocation> textures) {
            return states[state].getTexture(0, textures);
        }

        private static class State extends Single {

            private final int next;

            State(JsonObject dto) {
                super(dto);
                this.next = dto.has("next") ? dto.get("next").getAsInt() : 0;
            }

            int getNext() {
                return next;
            }

        }

    }

}
