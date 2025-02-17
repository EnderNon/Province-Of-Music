package com.provinceofmusic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.provinceofmusic.jukebox.*;
import com.provinceofmusic.listeners.NoteListenerHelper;
import com.provinceofmusic.recorder.DebugMode;
import com.provinceofmusic.recorder.MusicRecorder;
import com.provinceofmusic.screen.ConfigScreen;
import com.provinceofmusic.screen.SamplePackConfig;
import com.provinceofmusic.screen.SamplePackEditor;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class ProvinceOfMusicClient implements ClientModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("provinceofmusic");

	MusicRecorder musicRecorder = new MusicRecorder();
	NoteReplacer noteReplacer = new NoteReplacer();

	public static KeyBinding openConfigScreenBinding;
	public static KeyBinding openSamplePackConfigScreenBinding;



	DebugMode debugMode = new DebugMode(); //this many not be used but this class needs to be instantiated at least once for it to work

	public static NoteListenerHelper noteListenerHelper = new NoteListenerHelper();

	public static File recordedmusicdir;
	public static File exportedmusicdir;
	public static File playrulesheetsdir;
	public static File configsettingsdir;
	public static File samplepacksdir;

	public static POMConfigObject configSettings;

	public static int guiSize = 1;

	@Override
	public void onInitializeClient() {
		setupFiles();
		getConfigSettings();
		setupListeners();
    }

	public void setupListeners(){
		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			musicRecorder.PassTime();
			noteReplacer.PassTime();
			noteListenerHelper.tick();
			if(MinecraftClient.getInstance().options.getGuiScale().getValue() == 0){
				guiSize = 3;
			}
			else{
				guiSize = MinecraftClient.getInstance().options.getGuiScale().getValue();
			}

		});



		musicRecorder.main();
		noteReplacer.main();
		noteReplacer.RunSetup();

		ClientTickEvents.START_WORLD_TICK.register(client -> {
		});

		musicRecorder.recordBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("Record Midi", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "Province of Music"));
		NoteReplacer.replaceNoteBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("Toggle Replace Music", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "Province of Music"));
		openConfigScreenBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("Open POM Settings", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "Province of Music"));
		openSamplePackConfigScreenBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("Open Sample Pack Config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "Province of Music"));

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {

		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if(openConfigScreenBinding.wasPressed()){
				MinecraftClient.getInstance().setScreen(new CottonClientScreen(new ConfigScreen()));
			}
			if(openSamplePackConfigScreenBinding.wasPressed()){
				if(ProvinceOfMusicClient.configSettings.activeSamplePack != null) {
					if(SamplePack.getFile(ProvinceOfMusicClient.configSettings.activeSamplePack).exists()) {
						SamplePack pack = SamplePack.getSamplePack(SamplePack.getFile(ProvinceOfMusicClient.configSettings.activeSamplePack));
						if(pack != null) {
							MinecraftClient.getInstance().setScreen(new CottonClientScreen(new SamplePackEditor(pack)));
						}
					}
				}
			}
		});
	}

	public void setupFiles(){

		configsettingsdir = new File("provinceofmusic/");
		if (!configsettingsdir.exists()){
			configsettingsdir.mkdirs();
		}

		recordedmusicdir = new File("provinceofmusic/recorded-music/");
		if (!recordedmusicdir.exists()){
			recordedmusicdir.mkdirs();
		}

		exportedmusicdir = new File("provinceofmusic/exported-music/");
		if (!exportedmusicdir.exists()){
			exportedmusicdir.mkdirs();
		}

		playrulesheetsdir = new File("provinceofmusic/playrulesheets/");
		if (!playrulesheetsdir.exists()){
			playrulesheetsdir.mkdirs();
		}

		samplepacksdir = new File("provinceofmusic/samplepacks");
		if (!samplepacksdir.exists()){
			samplepacksdir.mkdirs();
		}
	}

	public void getConfigSettings() {

		File jsonTemp = new File(ProvinceOfMusicClient.configsettingsdir + "/configSettings"+".json");
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting().serializeNulls();
		Gson gson = builder.create();

		try {
			configSettings = gson.fromJson(Files.readString(jsonTemp.toPath(), Charset.defaultCharset()) + "", POMConfigObject.class);
		} catch (IOException e) {
			configSettings = new POMConfigObject();
			saveConfigSettings();
		}
	}

	public static void saveConfigSettings(){
		File jsonTemp = new File(ProvinceOfMusicClient.configsettingsdir + "/configSettings"+".json");
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting().serializeNulls();
		Gson gson = builder.create();

		try {
			FileWriter fileWriter = new FileWriter(jsonTemp);
			fileWriter.write(gson.toJson(configSettings));
			fileWriter.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}