package com.provinceofmusic.recorder;

import com.provinceofmusic.ProvinceOfMusicClient;
import com.provinceofmusic.jukebox.InstrumentSound;
import com.provinceofmusic.listeners.NoteListener;
import com.provinceofmusic.listeners.NoteListenerHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundInstanceListener;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.text.Text;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MusicYoinker implements NoteListener {

    private static int time_passed = 0;
    private static boolean is_writing_to_file = false;
    private static String file_to_write;

    public static KeyBinding recordBinding;
    @Override
    public void onNotePlayed(InstrumentSound instrument, int ticksPassed, float pitch, int volume) {
        if (!is_writing_to_file) return;

        float startingPitch = pitch;
        float endPitch = (float) Math.pow(2, (((((startingPitch - instrument.transpose) - 0.5f) + 1) - 66.5f) / 12f));

        try {
            FileWriter myWriter = new FileWriter(file_to_write, true);
            myWriter.append(instrument.registeredName + "," + time_passed + "," + endPitch + "," + volume / 100f + "\n");
            myWriter.close();
        } catch (IOException e) {
            ProvinceOfMusicClient.LOGGER.error("Error writing to file " + file_to_write + ".");
            e.printStackTrace();
        }
        time_passed = 0;
    }

    public void PassTime(){
        if(is_writing_to_file){
            time_passed++;
        }
    }

    public void main(){
        NoteListenerHelper.addListener(this);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (recordBinding.wasPressed()) {
                is_writing_to_file = !is_writing_to_file;
                if (is_writing_to_file) {
                    file_to_write = ProvinceOfMusicClient.recordedmusicdir + "\\" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".csv";
                    ProvinceOfMusicClient.LOGGER.info("Started recording to file " + file_to_write + ".");
                    client.player.sendMessage(Text.of("Started recording to file " + file_to_write + "."), false);
                } else {
                    time_passed = 0;
                    ProvinceOfMusicClient.LOGGER.info("Stopped recording to file " + file_to_write + ".");
                    client.player.sendMessage(Text.of("Stopped recording to file " + file_to_write + "."), false);

                }
            }
        });
    }
}
