package celestibytes.fireworks;

import celestibytes.fireworks.twitch.TwitchChat;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Fireworks.MODID, version = Fireworks.VERSION, name = Fireworks.MOD_NAME)
public class Fireworks {
    public static final String MODID = "cbts_fireworks";
    public static final String MOD_NAME = "Fireworks";
    public static final String VERSION = "1.0";
    
    public TwitchChat tch = null;
    
    @Instance(Fireworks.MODID)
    public static Fireworks instance;
    
    @EventHandler
    public void serverStarting(FMLServerStartingEvent e) {
    	if(tch == null) {
    		tch = new TwitchChat();
    		tch.start();
    	}
    	
    	ICommandManager cmgr = e.getServer().getCommandManager();
    	if(cmgr instanceof ServerCommandManager) {
    		((ServerCommandManager)cmgr).registerCommand(new CommandFirework());
    		System.out.println("Firework command registered");
    	} else {
    		System.out.println("Firework command register FAILED!!! - " + (cmgr != null ? cmgr.getClass().getCanonicalName() : "NULL!!!"));
    	}
    }
}
