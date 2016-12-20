class MetaRegister(type):
    handlers = []
    registered = []
    
    def __new__(cls, name, bases, dct):
        cls = type.__new__(cls, name, bases, dct)
        for method in MetaRegister.getClassMethods(cls):
            MetaRegister.register(method)
        return cls
    
    def __call__(cls, *args, **kwargs):
        instance = super(MetaRegister, cls).__call__(*args, **kwargs)
        for method in MetaRegister.getInstanceMethods(instance):
            MetaRegister.register(method)
        return instance
        
    @staticmethod
    def getClassMethods(cls):
        methods = []
        for key,value in cls.__dict__.items():
            if not hasattr(value, "__class__"):
                continue
            if str(type(value)) == "<type 'classmethod'>" or str(type(value)) == "<type 'staticmethod'>":
                methods.append(getattr(cls, key))
        return methods
    
    @staticmethod
    def getInstanceMethods(instance):
        methods = []
        for name in dir(instance):
            try:
                attr = getattr(instance,name)
            except:
                pass
            if not hasattr(attr,"__class__"):
                continue

            if str(type(attr)) == "<type 'instancemethod'>":
                if hasattr(type(instance),name) and attr == getattr(type(instance),name):
                    continue
                methods.append(attr)
        return methods

    @staticmethod
    def register(func):
        if hasattr(func,'_event_handler'):
            hook.registerEvent(func,*getattr(func,'_event_handler'))
            try:
                MetaRegister.registered.append(func.im_func)
            except:
                MetaRegister.registered.append(func)

    @staticmethod
    def registerPlugin(main):
        if main is not None:
            for method in MetaRegister.getClassMethods(main):
                MetaRegister.register(method)
            for method in MetaRegister.getInstanceMethods(pyplugin):
                MetaRegister.register(method)

    @staticmethod
    def registerStatic():
        for method in MetaRegister.handlers:
            if method not in MetaRegister.registered:
                MetaRegister.register(method)

class Listener(object):
    __metaclass__ = MetaRegister

import functools

def EventHandler(argument = None, priority = 'NORMAL'):
    PRIORITIES = ["HIGHEST", "HIGH", "NORMAL", "LOW", "LOWEST"]
    def wrapper(func, eventtype, priority):
        if eventtype is None:
            try:
                name = func.__name__
            except AttributeError:
                name = func.__get__(None, int).im_func.__name__
            if name.startwith("on"):
                name = name[2:]
            for category in ["brewing", "entity", "entity.item", "entity.living", "entity.minecart", "entity.player", "terraingen" "world"]:
                temp = "%s.%sEvent"%(category,name)
                try:
                    Class.forName("net.minecraftforge.event." + temp)
                    eventtype = temp
                    break
                except:
                    pass
            if eventtype is None:
                try:
                    temp = "%sEvent"%(name)
                    Class.forName("net.minecraftforge.client.event." + temp)
                    eventtype = temp
                except:
                    log.severe("Incorrect @EventHandler usage on %s"%func)
                    return func
            if eventtype is None:
                try:
                    temp = "%sEvent"%(name)
                    Class.forName("net.minecraftforge.fml.common.gameevent." + temp)
                    eventtype = temp
                except:
                    log.severe("Incorrect @EventHandler usage on %s"%func)
                    return func
            if eventtype is None:
                log.severe("Incorrect @EventHandler usage on %s"%func)
                return func
        try:
            func._event_handler = (eventtype, priority)
            MetaRegister.handlers.append(func)
        except AttributeError:
            func.__get__(None,int).im_func._event_handler = (eventtype, priority)
            MetaRegister.handlers.append(func.__get__(None,int).im_func)
        return func
    if callable(argument) or str(type(argument)) == "<type 'classmethod'>" or str(type(argument)) == "<type 'staticmethod'>":
        return wrapper(argument, None, priority)
    if argument.lower() in PRIORITIES:
        argument,priority = priority if priority.lower() not in PRIORITIES else None,argument
    return functools.partial(wrapper, eventtype=argument, priority=priority)
    
__builtin__.Listener = Listener
__builtin__.EventHandler = EventHandler