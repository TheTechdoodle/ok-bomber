package com.darkender.plugins.okbomber.custom.addons;

import com.darkender.plugins.okbomber.TNTData;
import com.darkender.plugins.okbomber.custom.TNTAddon;
import org.bukkit.entity.TNTPrimed;

public class GlowingAddon extends TNTAddon
{
    public GlowingAddon()
    {
        super("addon-glowing");
    }
    
    @Override
    public boolean conflictsWith(TNTAddon other)
    {
        return false;
    }
    
    @Override
    public String getName()
    {
        return "Glowing";
    }
    
    @Override
    public String getDescription()
    {
        return "Causes the TNT to glow when ignited";
    }
    
    @Override
    public void onIgnite(TNTPrimed tnt, TNTData data)
    {
        tnt.setGlowing(true);
    }
}
