package com.darkender.plugins.okbomber.custom.addons;

import com.darkender.plugins.okbomber.TNTData;
import com.darkender.plugins.okbomber.custom.TNTAddon;
import org.bukkit.Particle;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityExplodeEvent;

public class IncendiaryAddon extends TNTAddon
{
    public IncendiaryAddon()
    {
        super("addon-incendiary");
    }
    
    @Override
    public boolean conflictsWith(TNTAddon other)
    {
        return false;
    }
    
    @Override
    public String getName()
    {
        return "Incendiary";
    }
    
    @Override
    public String getDescription()
    {
        return "Lights nearby blocks on fire";
    }
    
    @Override
    public void onIgnite(TNTPrimed tnt, TNTData data)
    {
        tnt.setIsIncendiary(true);
    }
    
    @Override
    public void entityTick(TNTPrimed tnt, TNTData data)
    {
        tnt.getWorld().spawnParticle(Particle.FLAME, tnt.getLocation().add(0.0, 1.1, 0.0), 0);
    }
    
    @Override
    public void onExplode(EntityExplodeEvent event, TNTData data)
    {
        if(!event.isCancelled())
        {
            event.getLocation().getWorld().spawnParticle(Particle.FLAME, event.getLocation(), 250);
        }
    }
}
