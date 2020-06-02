package com.darkender.plugins.okbomber.custom.addons;

import com.darkender.plugins.okbomber.custom.TNTAddon;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;

public class StickyAddon extends TNTAddon
{
    public StickyAddon()
    {
        super("addon-sticky");
    }
    
    @Override
    public boolean conflictsWith(TNTAddon other)
    {
        return false;
    }
    
    @Override
    public String getName()
    {
        return "Sticky";
    }
    
    @Override
    public String getDescription()
    {
        return "Won't move from where it was ignited";
    }
    
    @Override
    public void onIgnite(TNTPrimed tnt)
    {
        tnt.setGravity(false);
        tnt.setVelocity(new Vector(0, 0, 0));
    }
}
