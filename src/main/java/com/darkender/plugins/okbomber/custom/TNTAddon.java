package com.darkender.plugins.okbomber.custom;

import com.darkender.plugins.okbomber.TNTData;
import com.darkender.plugins.okbomber.custom.addons.*;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public abstract class TNTAddon
{
    private final static Map<String, TNTAddon> addons = new HashMap<>();
    private final String key;
    
    public final static TNTAddon TRAP = new TrapAddon();
    public final static TNTAddon NO_BLOCK_DAMAGE = new NoBlockDamageAddon();
    public final static TNTAddon INCENDIARY = new IncendiaryAddon();
    public final static TNTAddon STICKY = new StickyAddon();
    public final static TNTAddon FLOATING = new FloatingAddon();
    public final static TNTAddon SMOKE_BOMB = new SmokeBombAddon();
    public final static TNTAddon GLOWING = new GlowingAddon();
    
    static
    {
        addons.put(TRAP.getKey(), TRAP);
        addons.put(NO_BLOCK_DAMAGE.getKey(), NO_BLOCK_DAMAGE);
        addons.put(INCENDIARY.getKey(), INCENDIARY);
        addons.put(STICKY.getKey(), STICKY);
        addons.put(FLOATING.getKey(), FLOATING);
        addons.put(SMOKE_BOMB.getKey(), SMOKE_BOMB);
        addons.put(GLOWING.getKey(), GLOWING);
    }
    
    public TNTAddon(@NotNull String key)
    {
        this.key = key;
    }
    
    public static void register(TNTAddon addon)
    {
        if(addons.containsKey(addon.getKey()))
        {
            //TODO Throw an error
        }
        addons.put(addon.getKey(), addon);
    }
    
    public static TNTAddon get(String key)
    {
        return addons.get(key);
    }
    
    public static boolean has(String key)
    {
        return addons.containsKey(key);
    }

    public String getKey()
    {
        return key;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof TNTAddon))
        {
            return false;
        }
        TNTAddon other = (TNTAddon) o;
        return key.equals(other.getKey());
    }
    
    @Override
    public int hashCode()
    {
        return key.hashCode();
    }
    
    public abstract boolean conflictsWith(TNTAddon other);
    public abstract String getName();
    public abstract String getDescription();
    
    public void onPlace(BlockPlaceEvent event, TNTData data) {}
    public void onBreak(BlockBreakEvent event, TNTData data) {}
    public void onIgnite(TNTPrimed tnt, TNTData data) {}
    public void onDispense(BlockDispenseEvent event, TNTData data) {}
    public void onExplode(EntityExplodeEvent event, TNTData data) {}
    public void onDamage(EntityDamageByEntityEvent event, TNTData data) {}
    public void onHangingBreak(HangingBreakEvent event, TNTData data) {}
    public void entityTick(TNTPrimed tnt, TNTData data) {}
}
