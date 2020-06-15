package com.darkender.plugins.okbomber.custom.addons;

import com.darkender.plugins.okbomber.TNTData;
import com.darkender.plugins.okbomber.custom.TNTAddon;
import org.bukkit.entity.TNTPrimed;

public class InstantAddon extends TNTAddon
{
    public InstantAddon()
    {
        super("addon-instant");
    }
    
    @Override
    public boolean conflictsWith(TNTAddon other)
    {
        return false;
    }
    
    @Override
    public String getName()
    {
        return "Instant";
    }
    
    @Override
    public String getDescription()
    {
        return "Removes the fuse from TNT";
    }
    
    @Override
    public void onIgnite(TNTPrimed tnt, TNTData data)
    {
        tnt.setFuseTicks(0);
    }
}
