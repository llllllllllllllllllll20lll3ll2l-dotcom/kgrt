package com.manyl.mod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

/**
 * Управляет воспроизведением голосовой озвучки стражей на клиенте:
 * гарантирует, что новая реплика сразу останавливает предыдущую и голоса не накладываются.
 */
public final class ClientVoiceManager {
    private static SoundInstance activeVoice;

    private ClientVoiceManager() {}

    public static void playExclusive(int entityId, Identifier soundId, float volume, float pitch, double x, double y, double z) {
        Minecraft client = Minecraft.getInstance();
        SoundManager manager = client.getSoundManager();

        if (activeVoice != null) {
            manager.stop(activeVoice);
            activeVoice = null;
        }

        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getValue(soundId);
        if (sound == null) return;

        SoundInstance next;
        Entity entity = client.level != null ? client.level.getEntity(entityId) : null;
        if (entity != null) {
            next = new EntityBoundSoundInstance(
                    sound,
                    SoundSource.NEUTRAL,
                    volume,
                    pitch,
                    entity,
                    entity.level().random.nextLong()
            );
        } else {
            next = new SimpleSoundInstance(
                    sound,
                    SoundSource.NEUTRAL,
                    volume,
                    pitch,
                    RandomSource.create(),
                    x, y, z
            );
        }

        activeVoice = next;
        manager.play(next);
    }
}
