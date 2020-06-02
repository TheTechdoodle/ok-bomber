package com.darkender.plugins.okbomber.custom.addons;

import com.darkender.plugins.okbomber.custom.TNTAddon;
import org.bukkit.entity.TNTPrimed;

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
    public void onIgnite(TNTPrimed tnt)
    {
        tnt.setGlowing(true);
        tnt.setGravity(false);
        tnt.setIsIncendiary(true);
    }
}
