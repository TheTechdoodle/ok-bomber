package com.darkender.plugins.okbomber.custom.addons;

import com.darkender.plugins.okbomber.custom.TNTAddon;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.util.Vector;

import java.util.Random;

public class SmokeBombAddon extends TNTAddon
{
    private Random random = new Random();
    
    public SmokeBombAddon()
    {
        super("addon-smoke-bomb");
    }
    
    @Override
    public boolean conflictsWith(TNTAddon other)
    {
        return other.equals(NO_BLOCK_DAMAGE) || other.equals(INCENDIARY);
    }
    
    @Override
    public String getName()
    {
        return "Smoke Bomb";
    }
    
    @Override
    public String getDescription()
    {
        return "Covers the area in smoke";
    }
    
    @Override
    public void onExplode(EntityExplodeEvent event)
    {
        event.setCancelled(true);
        event.getLocation().getWorld().playSound(event.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0F, 1.0F);
        for(int i = 0; i < 1000; i++)
        {
            Location smokePos = event.getLocation().clone().add(new Vector(
                    (random.nextDouble() * 8.0) - 4.0,
                    (random.nextDouble() * 4.0) - 1.0,
                    (random.nextDouble() * 8.0) - 4.0));
            smokePos.getWorld().spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, smokePos, 0);
        }
    }
    
    @Override
    public void onDamage(EntityDamageByEntityEvent event)
    {
        event.setCancelled(true);
    }
    
    @Override
    public void onHangingBreak(HangingBreakEvent event)
    {
        event.setCancelled(true);
    }
}
