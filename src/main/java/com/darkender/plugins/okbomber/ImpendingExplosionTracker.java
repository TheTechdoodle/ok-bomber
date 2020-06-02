package com.darkender.plugins.okbomber;

import org.bukkit.Location;
import org.bukkit.entity.TNTPrimed;

import java.util.HashSet;
import java.util.Set;

public class ImpendingExplosionTracker
{
    private final Set<TNTPrimed> primedTNT = new HashSet<>();
    
    public void addTNT(TNTPrimed tnt)
    {
        primedTNT.add(tnt);
    }
    
    private void removeInvalid()
    {
        primedTNT.removeIf(tntPrimed -> !tntPrimed.isValid());
    }
    
    public TNTPrimed getSoonest()
    {
        removeInvalid();
        TNTPrimed soonest = null;
        for(TNTPrimed primed : primedTNT)
        {
            if(soonest == null || primed.getFuseTicks() < soonest.getFuseTicks())
            {
                soonest = primed;
            }
        }
        return soonest;
    }
    
    public TNTPrimed getSoonestAndClosest(Location location)
    {
        removeInvalid();
        TNTPrimed soonest = null;
        for(TNTPrimed primed : primedTNT)
        {
            if(primed.getWorld() != location.getWorld())
            {
                continue;
            }
            if(soonest == null)
            {
                soonest = primed;
                continue;
            }
            
            if(primed.getFuseTicks() < soonest.getFuseTicks())
            {
                soonest = primed;
            }
            else if(primed.getFuseTicks() == soonest.getFuseTicks())
            {
                if(primed.getLocation().distance(location) < soonest.getLocation().distance(location))
                {
                    soonest = primed;
                }
            }
        }
        return soonest;
    }
    
    public TNTPrimed getRightNow(Location location)
    {
        removeInvalid();
        TNTPrimed soonest = null;
        for(TNTPrimed primed : primedTNT)
        {
            if(primed.getWorld() != location.getWorld() || primed.getFuseTicks() != 0)
            {
                continue;
            }
            
            if(soonest == null)
            {
                soonest = primed;
            }
            else if(primed.getLocation().distance(location) < soonest.getLocation().distance(location))
            {
                soonest = primed;
            }
        }
        return soonest;
    }
}
