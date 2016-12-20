package com.biggestnerd.dynmods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.core.PyString;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

public class PythonHooks {

	PyFunction enable;
	
	PyFunction disable;
	
	ArrayList<DynEventHandler> eventHandlers = new ArrayList<DynEventHandler>();
		
	Map<String, Class<? extends Event>> customEvents = new HashMap<String, Class<? extends Event>>();
	
	void doRegistrations(DynMod mod) {
		for(DynEventHandler handler : eventHandlers) {
			handler.register(mod);
		}
	}
	
	public void registerEvent(PyObject handler, Class<? extends Event> type, EventPriority priority) {
		DynEventHandler wrapper = new DynEventHandler(handler, type, priority);
		eventHandlers.add(wrapper);
	}
	
	public void registerEvent(PyObject handler, PyString type, PyString priority) {
		String clazz = type.asString();
		Class<?> event = null;
		try {
			event = Class.forName(clazz);
		} catch (ClassNotFoundException e) {
			try {
				event = Class.forName("net.minecraftforge.event." + clazz);
			} catch (ClassNotFoundException e1) {
				try {
					event = Class.forName("net.minecraftforge.client.event." + clazz);
				} catch (ClassNotFoundException e2) {
					try {
						event = Class.forName("net.minecraftforge.fml.common.gameevent." + clazz);
					} catch (ClassNotFoundException e3) {
						System.out.println("Failed to register event '" + clazz + "': " + e3.getMessage());
						return;
					}
				}
			}
		}
		if(!event.getClass().isInstance(event)) {
			System.out.println(type.asString() + " is not an Event");
			return;
		}
		Class<? extends Event> realType = (Class<? extends Event>) event;
		EventPriority realPriority = EventPriority.valueOf(priority.upper());
		registerEvent(handler, realType, realPriority);
	}
	
	public PyFunction enable(PyFunction func) {
		enable = func;
		return func;
	}
	
	public PyFunction disable(PyFunction func) {
		disable = func;
		return func;
	}
	
	public PyObject event(final PyString type, final PyString priority) {
		return new PyObject() {
			public PyObject __call__(PyObject func) {
				registerEvent(func, type, priority);
				return func;
			}
		};
	}
	
	public PyObject event(final PyString type) {
        return new PyObject() {
            public PyObject __call__(PyObject func) {
                registerEvent(func, type, new PyString("Normal"));
                return func;
            }
        };
    }
}
