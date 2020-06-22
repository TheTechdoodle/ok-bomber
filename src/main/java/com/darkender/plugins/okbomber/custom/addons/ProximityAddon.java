package com.darkender.plugins.okbomber.custom.addons;

import com.darkender.plugins.okbomber.OkBomber;
import com.darkender.plugins.okbomber.TNTData;
import com.darkender.plugins.okbomber.custom.TNTAddon;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.util.BoundingBox;

public class ProximityAddon extends TNTAddon
{
    public ProximityAddon()
    {
        super("addon-proximity");
    }
    
    @Override
    public boolean conflictsWith(TNTAddon other)
    {
        return false;
    }
    
    @Override
    public String getName()
    {
        return "Proximity";
    }
    
    @Override
    public String getDescription()
    {
        return "Ignites when approached by a player";
    }
    
    @Override
    public void onPrepareCraft(PrepareItemCraftEvent event, TNTData data)
    {
        data.getTntData().put("proximity-crafter", event.getViewers().get(0).getUniqueId());
    }
    
    @Override
    public void blockTick(Block tnt, TNTData data)
    {
        BoundingBox box = BoundingBox.of(tnt.getLocation(), 5.0, 5.0, 5.0);
        for(Entity entity : tnt.getWorld().getNearbyEntities(box))
        {
            if(entity.getType() != EntityType.PLAYER)
            {
                continue;
            }
            
            if(!data.getTntData().containsKey("proximity-crafter") ||
                    !entity.getUniqueId().equals(data.getTntData().get("proximity-crafter")))
            {
                OkBomber.instance.ignite(tnt);
                break;
            }
        }
    }
}
