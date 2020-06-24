package com.darkender.plugins.okbomber.custom.addons;

import com.darkender.plugins.okbomber.OkBomber;
import com.darkender.plugins.okbomber.TNTData;
import com.darkender.plugins.okbomber.custom.TNTAddon;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.Directional;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class DudAddon extends TNTAddon
{
    private static final List<Material> replaceableMaterials = Arrays.asList(Material.AIR, Material.WATER, Material.LAVA);
    
    public DudAddon()
    {
        super("addon-dud");
    }
    
    @Override
    public boolean conflictsWith(TNTAddon other)
    {
        return other != null && (other.equals(NO_BLOCK_DAMAGE) ||
                other.equals(SMOKE_BOMB) ||
                other.equals(INCENDIARY));
    }
    
    @Override
    public String getName()
    {
        return "Dud";
    }
    
    @Override
    public String getDescription()
    {
        return "Drops as an item when it would explode";
    }
    
    @Override
    public void onExplode(EntityExplodeEvent event, TNTData data)
    {
        event.setCancelled(true);
        Location l = event.getLocation().add(0.0, 0.5, 0.0);
        l.getWorld().playSound(l, Sound.BLOCK_FIRE_EXTINGUISH, 1.0F, 1.0F);
        l.getWorld().spawnParticle(Particle.SMOKE_NORMAL, l, 0);
    
        ItemStack item = new ItemStack(Material.TNT, 1);
        data.applyToItem(item);
        l.getWorld().dropItem(l, item);
    }
    
    @Override
    public void onDispense(BlockDispenseEvent event, TNTData data)
    {
        event.setCancelled(true);
        Directional direction = (Directional) event.getBlock().getBlockData();
        Block block = event.getBlock().getRelative(direction.getFacing());
        // Don't place the block if it would replace another block
        if(!replaceableMaterials.contains(block.getType()))
        {
            return;
        }
        
        OkBomber.instance.addBlock(block, data);
        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_GRASS_PLACE, 1.0f, 1.0f);
        block.setType(Material.TNT);
    
        // You can probably dupe dud tnt
        Bukkit.getScheduler().runTaskLater(OkBomber.instance, () ->
        {
            Container container = (Container) event.getBlock().getState();
            container.getInventory().removeItem(event.getItem());
        }, 1L);
    }
    
    @Override
    public void onDamage(EntityDamageByEntityEvent event, TNTData data)
    {
        event.setCancelled(true);
    }
    
    @Override
    public void onHangingBreak(HangingBreakEvent event, TNTData data)
    {
        event.setCancelled(true);
    }
}
