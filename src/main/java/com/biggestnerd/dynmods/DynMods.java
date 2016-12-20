package com.biggestnerd.dynmods;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import com.biggestnerd.dynmods.gui.GuiModList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

@Mod(modid="dynmod", name="Dynamin Mod Loader", version="0.0.1")
public class DynMods {
	
	private static DynMods instance;
	
	private Minecraft mc;
	private File modsFolder;
	private Map<String, DynMod> mods;
	private boolean jython = true;
	private KeyBinding guiBind = new KeyBinding("DynMods List", Keyboard.KEY_M, "DynMods");

	@EventHandler
	public void init(FMLInitializationEvent event) {
		instance = this;
		mc = Minecraft.getMinecraft();
		if(!new File("lib/jython.jar").exists()) {
			downloadJython();
		}
		if(!jython) return;
		ClientRegistry.registerKeyBinding(guiBind);
		MinecraftForge.EVENT_BUS.register(this);
		mods = new HashMap<String, DynMod>();
		modsFolder = new File(Minecraft.getMinecraft().mcDataDir, "DynMods");
		if(!modsFolder.exists()) {
			modsFolder.mkdirs();
		}
		for(File file : modsFolder.listFiles()) {
			if(file.getName().endsWith(".dynmod")) {
				System.out.println("Attempting to load mod from " + file.getName());
				DynMod mod = DynMod.loadMod(file);
				if(mod != null) {
					System.out.println("Loaded mod: " + mod.getName());
					mods.put(mod.getName(), mod);
					mod.setEnabled(true);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
			if(guiBind.isPressed()) {
				mc.displayGuiScreen(new GuiModList(mc.currentScreen));
			}
	}
	
	public void refreshMods() {
		System.out.println("Reloading mods...");
		Map<String, Boolean> enabled = new HashMap<String, Boolean>();
		for(DynMod mod : mods.values()) {
			enabled.put(mod.getName(), mod.isEnabled());
			mod.setEnabled(false);
		}
		mods.clear();
		for(File file : modsFolder.listFiles()) {
			if(file.getName().endsWith(".dynmod")) {
				System.out.println("Attempting to load mod from " + file.getName());
				DynMod mod = DynMod.loadMod(file);
				if(mod != null) {
					System.out.println("Loaded mod: " + mod.getName());
					mods.put(mod.getName(), mod);
					if(enabled.containsKey(mod.getName())) {
						mod.setEnabled(enabled.get(mod.getName()));
					} else {
						mod.setEnabled(true);
					}
				}
			}
		}
	}
	
	public static void refreshModList() {
		instance.refreshMods();
	}
	
	private void downloadJython() {
		System.out.println("Could not find jython jar, automagically downloading!");
		try {
			URL dl = new URL("http://search.maven.org/remotecontent?filepath=org/python/jython-standalone/2.7.0/jython-standalone-2.7.0.jar");
			URLConnection conn = dl.openConnection();
			conn.connect();
			
			File lib = new File("lib");
			if(!lib.exists()) {
				lib.mkdirs();
			}
			
			File dl_file = new File("lib/jython.jar_dl");
			if(dl_file.exists()) {
				dl_file.delete();
			}
			
			InputStream in = conn.getInputStream();
			FileOutputStream out = new FileOutputStream(dl_file);
			
			long total = conn.getContentLengthLong();
			long progress = 0;
			byte[] buffer = new byte[1024];
			int read;
			long start = System.currentTimeMillis();
			
			while((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
				progress += read;
				if(System.currentTimeMillis() - start > 2000) {
					System.out.println("Downloading jython: " + (progress * 100 / total) + "%");
					start = System.currentTimeMillis();
				}
			}
			
			out.close();
			in.close();
			
			dl_file.renameTo(new File("lib/jython.jar"));
			System.out.println("Jython download successful!");
		} catch (IOException e) {
			System.out.println("Error downloading jython, disabling DynMod cus it won't work.");
			e.printStackTrace();
			jython = false;
		}
	}
	
	public static ArrayList<DynMod> getModList() {
		return new ArrayList<DynMod>(instance.mods.values());
	}
}
