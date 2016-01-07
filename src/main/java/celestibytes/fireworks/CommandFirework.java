package celestibytes.fireworks;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import celestibytes.fireworks.twitch.TwitchChat;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeFireworks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class CommandFirework extends CommandBase {
	
	private CInvWrapper cinw = new CInvWrapper(new ItemStack[9]);
	private RecipeFireworks rf = new RecipeFireworks();

	@Override
	public String getCommandName() {
		return "firework";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "commands.firework.usage";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if(args.length >= 2 && args.length <= 9 && sender instanceof EntityPlayer) {
			EntityPlayer plr = (EntityPlayer) sender;
			int flight = 1;
			
			try {
				flight = Integer.parseInt(args[0]);
			} catch(NumberFormatException nfe) {
				return;
			}
			
			if(flight < 1 && flight > 3) {
				return;
			}
			
			int i = 0;
			ItemStack[] cinv = new ItemStack[9];
			cinv[i++] = new ItemStack(Items.paper);
			
			for(int k = 0; k < flight; k++) {
				cinv[i++] = new ItemStack(Items.gunpowder);
			}
			
			for(int j = 1; j < args.length; j++) {
				if(i >= cinv.length) {
					break;
				}
				
				cinv[i++] = craftFireworkStar(args[j], sender.getEntityWorld());
			}
			
			cinw.setInv(cinv);
			
			if(rf.matches(cinw, sender.getEntityWorld())) {
				ItemStack firework = rf.getCraftingResult(cinw);
				if(firework != null) {
					plr.inventory.addItemStackToInventory(firework);
				}
			}
			
//			ItemStack star = craftFireworkStar(args.length >= 1 ? args[0] : "D5e", sender.getEntityWorld());
//			if(star != null) {
//				plr.inventory.addItemStackToInventory(star);
//			}
		} else if(sender instanceof EntityPlayer) {
			World world = sender.getEntityWorld();
			TwitchChat tc = Fireworks.instance.tch;
			if(tc != null) {
				Random rand = new Random();
				if(tc.isActive()) {
					List<String> fws = tc.getFireworks();
					if(fws != null) {
						List<ItemStack> firews = new LinkedList<>();
						for(String s : fws) {
							ItemStack st = craftFirework(s.split(" "), world);
							if(st != null) {
								firews.add(st);
							}
						}
						
						for(ItemStack f : firews) {
							BlockPos pos = sender.getPosition();
							world.spawnEntityInWorld(new EntityFireworkRocket(world, pos.getX() + 12 - rand.nextInt(2), pos.getY(), pos.getZ() + rand.nextInt(10) - 5, f));
						}
					}
				}
			}
		}
	}
	
	private int getColorMeta(char c) {
		if(c >= 0x30 && c <= 0x39) {
			return c - 0x30;
		} else if(c >= 0x41 && c <= 0x46) {
			return c - 0x41 + 10;
		} else if(c >= 0x61 && c <= 0x66) {
			return c - 0x61 + 10;
		}
		
		return 0;
	}
	
	private ItemStack craftFirework(String[] args, World world) {
		int flight = 1;
		
		try {
			flight = Integer.parseInt(args[0]);
		} catch(NumberFormatException nfe) {
			return null;
		}
		
		if(flight < 1 || flight > 3) {
			return null;
		}
		
		int i = 0;
		ItemStack[] cinv = new ItemStack[18];
		cinv[i++] = new ItemStack(Items.paper);
		
		for(int k = 0; k < flight; k++) {
			cinv[i++] = new ItemStack(Items.gunpowder);
		}
		
		for(int j = 1; j < args.length; j++) {
			if(i >= cinv.length) {
				break;
			}
			
			cinv[i++] = craftFireworkStar(args[j], world);
		}
		
		cinw.setInv(cinv);
		
		if(rf.matches(cinw, world)) {
			return rf.getCraftingResult(cinw);
		}
		
		return null;
	}
	
	private ItemStack craftFireworkStar(String code, World world) {
		FWCIter iter = new FWCIter(code);
		ItemStack[] cinv = new ItemStack[9];
		int i = 0;
		
		cinv[i++] = new ItemStack(Items.gunpowder);
		
		while(iter.hasNext()) {
			if(i >= cinv.length) {
				System.out.println("CINV overflow");
				break;
			}
			
			char[] cc = iter.next();
			char id;
			char extra = 0;
			
			if(cc.length == 1) {
				id = cc[0];
			} else if(cc.length == 2) {
				id = cc[0];
				extra = cc[1];
			} else {
				return null;
			}
			
			ItemStack s = null;
			
			switch(id) {
			case 'd':
				System.out.println("diamond");
				s = new ItemStack(Items.diamond);
				break;
			case 'l':
				System.out.println("glowstone dust");
				s = new ItemStack(Items.glowstone_dust);
				break;
			case 's':
				System.out.println("skull");
				s = new ItemStack(Items.skull);
				break;
			case 'f':
				System.out.println("fire charge");
				s = new ItemStack(Items.fire_charge);
				break;
			case 'o':
				System.out.println("gold nugget");
				s = new ItemStack(Items.gold_nugget);
				break;
			case 'e':
				System.out.println("feather");
				s = new ItemStack(Items.feather);
				break;
			case 'D':
				System.out.println("dye: " + extra);
				s = new ItemStack(Items.dye, 1, getColorMeta(extra));
				break;
			}
			
			if(s == null) {
				return null;
			}
			
			cinv[i++] = s;
		}
		
		cinw.setInv(cinv);
		if(rf.matches(cinw, world)) {
			return rf.getCraftingResult(cinw);
		}
		
		return null;
	}
	
	private static class FWCIter {
		
		private boolean isDigit(char c) {
			return c >= 0x30 && c <= 0x39;
		}
		
		/** check if isDigit first */
		private int getNumber(char c) {
			return 0x30 - ((int) c);
		}
		
		private boolean isNormal(char c) {
			return "dlsfoe".indexOf(c) != -1;
		}
		
		private boolean isExtra(char c) {
			return 'D' == c;
		}
		
		private boolean isColor(char c) {
			return "0123456789abcdefABCDEF".indexOf(c) != -1;
		}
		
		private final Iterator<char[]> list;
		private boolean valid = true;
		
		public FWCIter(String code) {
			List<char[]> tuples = new LinkedList<>();
			code = code.replace("-", "");
			char[] cc = code.toCharArray();
			
			for(int i = 0; i < cc.length; i++) {
				char c = cc[i];
				System.out.println("-- c=" + c);
				
				if(isDigit(c)) {
					valid = false;
					break;
				} else {
					if(isExtra(c)) {
						System.out.println("-- extra");
						if(i + 1 < cc.length) {
							char c2 = cc[i + 1];
							System.out.println("-- c2=" + c2);
							if(isColor(c2)) {
								i++;
								tuples.add(new char[]{c, c2});
							} else {
								valid = false;
								break;
							}
						} else {
							valid = false;
							break;							
						}
					} else if(isNormal(c)) {
						System.out.println("-- normal");
						tuples.add(new char[]{c});
					} else {
						System.out.println("-- invalid");
						valid = false;
						break;
					}
				}
			}
			
			list = tuples.iterator();
		}
		
		public boolean isValid() {
			return valid;
		}
		
		public boolean hasNext() {
			return list.hasNext();
		}
		
		public char[] next() {
			return list.next();
		}
	}

}
