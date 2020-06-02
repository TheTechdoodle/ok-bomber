package com.darkender.plugins.okbomber.custom.addons;

import com.darkender.plugins.okbomber.custom.TNTAddon;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class SmokeBombAddon extends TNTAddon
{
    public SmokeBombAddon()
    {
        super("addon-smoke-bomb");
    }
    
    @Override
    public boolean conflictsWith(TNTAddon other)
    {
        return other.equals(NO_BLOCK_DAMAGE) || other.equals(INCENDIARY_ADDON);
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
                    ThreadLocalRandom.current().nextDouble(-4.0, 4.0),
                    ThreadLocalRandom.current().nextDouble(0.0, 4.0),
                    ThreadLocalRandom.current().nextDouble(-4.0, 4.0)));
            smokePos.getWorld().spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, smokePos, 0);
        }
    }
}
