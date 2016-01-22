package celestibytes.fireworks;

import net.minecraft.entity.passive.EntityCow;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

public class WorldTickHandler {
	
	private FireworkCrafter fwc = new FireworkCrafter();
	private int delay = 200;

	@SubscribeEvent
	public void onWorldTickEvent(WorldTickEvent e) {
		if(!e.world.isRemote && e.phase == Phase.END) {
			boolean enable = true;
			if(enable && delay <= 0) {
				int x = -7, y = 77, z = 238;
				
//				delay = e.world.rand.nextInt(300) + 480;
				delay = 600;
				fwc.launchFireworks(e.world, x, y, z);
//				System.out.println("launch!");
//				EntityCow cow = new EntityCow(e.world);
//				cow.setPosition(x, y, z);
//				e.world.spawnEntityInWorld(cow);
//				System.out.println("summmon");
			}
			
			fwc.runUpdate(e.world);
			
			delay--;
		}
	}
}
