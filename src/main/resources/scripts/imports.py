import com.biggestnerd.dynmods.DynMod as DynMod

import sys
import net.minecraftforge as forge
from java.lang import Class
from java.util.logging import Level
from java.util.logging import Logger
from java.io import File

class PyStdoutRedirect(object):
    def write(self, txt):
        if txt.endswith("\n"):
            sys.__stdout__.write(txt[:-1])
            sys.__stdout__.flush()
        else:
            sys.__stdout__.write(txt)
            
sys.stdout = PyStdoutRedirect()

class Log(object):
    prefix = ""
    logger = Logger.getLogger("DynMods")
    
    @staticmethod
    def info(*text):
        Log.logger.log(Level.INFO, Log.prefix+" ".join(map(unicode, text)))
    
    @staticmethod
    def severe(*text):
        Log.logger.log(Level.SEVERE, Log.prefix+" ".join(map(unicode, text)))
        
    @staticmethod
    def msg(player, *text):
        player.addChatMessage(minecraft.util.text.TextComponentString(Log.prefix+" ".join(map(unicode, text))))