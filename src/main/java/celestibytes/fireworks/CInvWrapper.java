package celestibytes.fireworks;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

public class CInvWrapper extends InventoryCrafting {

	private ItemStack[] cinv;
	
	public CInvWrapper(ItemStack[] cinv) {
		super(null, 3, 3);
		this.cinv = cinv;
	}
	
	public void setInv(ItemStack[] cinv) {
		this.cinv = cinv;
	}
	
	@Override
	public ItemStack getStackInSlot(int index) {
		return index >= 0 && index <= cinv.length ? cinv[index] : null;
	}
	
	@Override
	public int getSizeInventory() {
		return cinv.length;
	}

}
