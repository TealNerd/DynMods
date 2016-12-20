package com.biggestnerd.dynmods;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.Logger;

import org.python.core.Py;
import org.python.core.PyList;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

public class DynMod {

	private boolean isEnabled = false;
	private boolean initialized = false;
	private String name;
	private File file = null;
	private Logger logger = null;
	
	DynListener listener = new DynListener();
	
	PythonHooks hooks;
	
	PythonInterpreter interp;

	public String getName() {
		return name;
	}
	
	public final boolean isEnabled() {
		return isEnabled;
	}
	
	public File getFile() {
		return file;
	}
	
	public void setEnabled(boolean enabled) {
		if(!initialized) return;
		if(isEnabled != enabled) {
			isEnabled = enabled;
			if(isEnabled) {
				if(hooks.enable != null) 
					hooks.enable.__call__();
				hooks.doRegistrations(this);
				MinecraftForge.EVENT_BUS.register(listener);
			} else {
				if(hooks.disable != null)
					hooks.disable.__call__();
				MinecraftForge.EVENT_BUS.unregister(listener);
			}
		}
	}
	
	protected final void initialize(String name, File file) {
		if(!initialized) {
			this.initialized = true;
			this.name = name;
			this.file = file;
		}
	}
	
	public boolean isInitialized() {
		return initialized;
	}
	
	public Logger getLogger() {
		if(logger == null) {
			logger = Logger.getLogger(name);
		}
		return logger;
	}
	
	@SuppressWarnings("resource")
	public static DynMod loadMod(File file) {
		if(!file.exists() || file.isDirectory()) return null;
		System.out.println("Loading dynmod from file: " + file.getName());
		DynMod mod = null;
		String name = file.getName().split("\\.")[0];
		PyList pythonpath = Py.getSystemState().path;
		PyString filepath = new PyString(file.getAbsolutePath());
		try {
			InputStream instream = new FileInputStream(file);
			PythonHooks hooks = new PythonHooks();
			PythonInterpreter interp = new PythonInterpreter();
			interp.set("hook", hooks);
			interp.exec("import __builtin__");
			interp.exec("__builtin__.hook = hook");
			
			String[] pre_plugin_scripts = {"imports.py", "meta_decorators.py"};
			String[] post_plugin_scripts = {"meta_loader.py"};
			
			System.out.println("Running pre scripts...");
			for(String script : pre_plugin_scripts) {
				InputStream metastream = DynMod.class.getClassLoader().getResourceAsStream("scripts/" + script);
				interp.execfile(metastream);
				metastream.close();
			}
			
			System.out.println("Interpreting plugin file...");
			interp.execfile(instream);
			
			instream.close();
			
			mod = new DynMod();
			interp.set("pyplugin", mod);
			mod.hooks = hooks;
			mod.interp = interp;
			
			System.out.println("Running post scripts...");
			for(String script : post_plugin_scripts) {
				InputStream metastream = DynMod.class.getClassLoader().getResourceAsStream("scripts/" + script);
				interp.execfile(metastream);
				metastream.close();
			}
			
			mod.initialize(name, file);
		} catch (Throwable t) {
			System.out.println("Error loading dynmod: " + t.getMessage());
			t.printStackTrace();
		}
		return mod;
	}
}
