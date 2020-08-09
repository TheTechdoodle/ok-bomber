package com.darkender.plugins.okbomber.custom.addons;

import com.darkender.plugins.okbomber.TNTData;
import com.darkender.plugins.okbomber.custom.TNTAddon;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MiningAddon extends TNTAddon
{
    private static final Set<Material> shouldDestroy = new HashSet<>(Arrays.asList(
            Material.STONE, Material.COBBLESTONE, Material.DIRT, Material.DIORITE, Material.GRANITE,
            Material.ANDESITE, Material.GRAVEL, Material.SAND, Material.NETHERRACK, Material.MAGMA_BLOCK
    ));
    
    static
    {
        // Add 1.16 blocks
        try
        {
            shouldDestroy.add(Material.valueOf("BASALT"));
            shouldDestroy.add(Material.valueOf("BLACKSTONE"));
            shouldDestroy.add(Material.valueOf("BASALT"));
            shouldDestroy.add(Material.valueOf("CRIMSON_NYLIUM"));
            shouldDestroy.add(Material.valueOf("WARPED_NYLIUM"));
        }
        catch(Exception ignored) {}
    }
    
    public MiningAddon()
    {
        super("addon-mining");
    }
    
    @Override
    public boolean conflictsWith(TNTAddon other)
    {
        return other != null && (other.equals(TNTAddon.DUD) || other.equals(NO_BLOCK_DAMAGE) || other.equals(SMOKE_BOMB));
    }
    
    @Override
    public String getName()
    {
        return "Mining";
    }
    
    @Override
    public String getDescription()
    {
        return "Only Destroys common underground blocks and doesn't damage entities";
    }
    
    @Override
    public void onExplode(EntityExplodeEvent event, TNTData data)
    {
        if(event.isCancelled())
        {
            return;
        }
        event.blockList().removeIf(block -> !shouldDestroy.contains(block.getType()));
    }
    
    @Override
    public void onDamage(EntityDamageByEntityEvent event, TNTData data)
    {
        event.setCancelled(true);
    }
    
    @Override
    public void onHangingBreak(HangingBreakEvent event, TNTData data)
    {
        event.setCancelled(true);
    }
}