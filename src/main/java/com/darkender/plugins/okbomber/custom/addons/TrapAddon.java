package com.darkender.plugins.okbomber.custom.addons;

import com.darkender.plugins.okbomber.custom.TNTAddon;
import org.bukkit.block.data.type.TNT;
import org.bukkit.event.block.BlockPlaceEvent;

public class TrapAddon extends TNTAddon
{
    public TrapAddon()
    {
        super("addon-trap");
    }
    
    @Override
    public boolean conflictsWith(TNTAddon other)
    {
        return false;
    }
    
    @Override
    public String getName()
    {
        return "Trapped";
    }
    
    @Override
    public String getDescription()
    {
        return "TNT ignites when it is mined";
    }
    
    @Override
    public void onPlace(BlockPlaceEvent event)
    {
        TNT data = (TNT) event.getBlock().getBlockData();
        data.setUnstable(true);
        event.getBlock().setBlockData(data);
    }
}
