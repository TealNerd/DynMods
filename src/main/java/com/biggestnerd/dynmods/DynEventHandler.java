package com.biggestnerd.dynmods;

import org.python.core.PyObject;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

public class DynEventHandler {

	/**
	 * Python function to call
	 */
	final PyObject handler;
	
	/**
	 * Event type this handler is listening for
	 */
	final Class<? extends Event> type;
	
	/**
	 * Priority for the handler
	 */
	final EventPriority priority;
	
	/**
	 * Whether we've registered yet
	 */
	boolean registered = false;
	
	DynEventHandler(PyObject handler, Class<? extends Event> type, EventPriority priority) {
		if(handler.isCallable()) {
			this.handler = handler;
		} else {
			throw new IllegalArgumentException("Tried to register event handler with an invalid type " + handler.getClass().getName());
		}
		this.type = type;
		this.priority = priority;
	}
	
	/**
	 * Register the handler with the event manager
	 * @param pm plugin manager to register with
	 * @param plugin plugin to register as
	 */
	void register(DynMod mod) {
		try {
			if(registered) {
				throw new IllegalStateException("Attempting to register an already registered handler");
			} else {
				mod.listener.addHandler(type, this);
			}
		} catch (IllegalStateException ex) {
			//TODO logging
		}
	}
}
