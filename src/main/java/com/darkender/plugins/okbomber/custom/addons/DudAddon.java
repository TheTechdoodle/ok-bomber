package com.darkender.plugins.okbomber.custom.addons;

import com.darkender.plugins.okbomber.TNTData;
import com.darkender.plugins.okbomber.custom.TNTAddon;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.inventory.ItemStack;

public class DudAddon extends TNTAddon
{
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
