package com.biggestnerd.dynmods;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.python.core.Py;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DynListener {

	HashMap<Class<? extends Event>, Set<DynEventHandler>> handlers = new HashMap<Class<? extends Event>, Set<DynEventHandler>>();
	
	@SubscribeEvent
	public void onEvent(Event event) {
		Set<DynEventHandler> handlers = this.handlers.get(event.getClass());
		if(handlers != null) {
			for(DynEventHandler handler : handlers) {
				handler.handler.__call__(Py.java2py(event));
			}
		}
	}
	
	void addHandler(Class<? extends Event> type, DynEventHandler handler) {
		Set<DynEventHandler> set = handlers.get(type);
		if(set == null) {
			set= new HashSet<DynEventHandler>();
			handlers.put(type, set);
		}
		set.add(handler);
	}
}
