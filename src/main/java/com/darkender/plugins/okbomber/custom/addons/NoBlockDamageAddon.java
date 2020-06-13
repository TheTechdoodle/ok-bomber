package com.darkender.plugins.okbomber.custom.addons;

import com.darkender.plugins.okbomber.TNTData;
import com.darkender.plugins.okbomber.custom.TNTAddon;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;

public class NoBlockDamageAddon extends TNTAddon
{
    public NoBlockDamageAddon()
    {
        super("no-block-damage");
    }
    
    @Override
    public boolean conflictsWith(TNTAddon other)
    {
        return false;
    }
    
    @Override
    public String getName()
    {
        return "No Block Damage";
    }
    
    @Override
    public String getDescription()
    {
        return "Prevents TNT from damaging blocks";
    }
    
    @Override
    public void onExplode(EntityExplodeEvent event, TNTData data)
    {
        event.blockList().clear();
    }
    
    @Override
    public void onHangingBreak(HangingBreakEvent event, TNTData data)
    {
        event.setCancelled(true);
    }
}
