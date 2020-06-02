package com.darkender.plugins.okbomber.custom.addons;

import com.darkender.plugins.okbomber.TNTData;
import com.darkender.plugins.okbomber.custom.TNTAddon;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.data.type.TNT;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class TrapAddon extends TNTAddon
{
    public TrapAddon()
    {
        super("addon-trap");
    }
    
    @Override
    public boolean conflictsWith(TNTAddon other)
    {
        return false;
    }
    
    @Override
    public String getName()
    {
        return "Trapped";
    }
    
    @Override
    public String getDescription()
    {
        return "TNT ignites when it is mined";
    }
    
    @Override
    public void onBreak(BlockBreakEvent event, TNTData data)
    {
        if(event.isCancelled())
        {
            return;
        }
        
        // Clean up after previous method
        TNT blockData = (TNT) event.getBlock().getBlockData();
        if(blockData.isUnstable())
        {
            blockData.setUnstable(false);
        }
        
        if(event.getPlayer().getGameMode() == GameMode.CREATIVE)
        {
            return;
        }
        
        ItemStack hand = event.getPlayer().getInventory().getItemInMainHand();
        if(hand.getType() != Material.SHEARS && hand.getEnchantmentLevel(Enchantment.SILK_TOUCH) == 0)
        {
            event.setDropItems(false);
            event.getBlock().setType(Material.AIR);
            Location spawnPos = event.getBlock().getLocation().add(0.5, 0.0, 0.5);
            TNTPrimed tnt = spawnPos.getWorld().spawn(spawnPos, TNTPrimed.class);
            data.write(tnt.getPersistentDataContainer());
            spawnPos.getWorld().playSound(spawnPos, Sound.ENTITY_TNT_PRIMED, 1.0f, 1.0f);
        }
    }
}
