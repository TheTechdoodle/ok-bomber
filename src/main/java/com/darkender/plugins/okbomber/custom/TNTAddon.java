package com.darkender.plugins.okbomber.custom;

import com.darkender.plugins.okbomber.custom.addons.*;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public abstract class TNTAddon
{
    private final static Map<String, TNTAddon> addons = new HashMap<>();
    private final String key;
    
    public final static TNTAddon TRAP = new TrapAddon();
    public final static TNTAddon NO_BLOCK_DAMAGE = new NoBlockDamageAddon();
    public final static TNTAddon INCENDIARY_ADDON = new IncendiaryAddon();
    public final static TNTAddon STICKY_ADDON = new StickyAddon();
    public final static TNTAddon FLOATING_ADDON = new FloatingAddon();
    public final static TNTAddon SMOKE_BOMB_ADDON = new SmokeBombAddon();
    
    static
    {
        addons.put(TRAP.getKey(), TRAP);
        addons.put(NO_BLOCK_DAMAGE.getKey(), NO_BLOCK_DAMAGE);
        addons.put(INCENDIARY_ADDON.getKey(), INCENDIARY_ADDON);
        addons.put(STICKY_ADDON.getKey(), STICKY_ADDON);
        addons.put(FLOATING_ADDON.getKey(), FLOATING_ADDON);
        addons.put(SMOKE_BOMB_ADDON.getKey(), SMOKE_BOMB_ADDON);
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
    
    public void onPlace(BlockPlaceEvent event) {}
    public void onBreak(BlockBreakEvent event) {}
    public void onIgnite(TNTPrimed tnt) {}
    public void onDispense(BlockDispenseEvent event) {}
    public void onExplode(EntityExplodeEvent event) {}
    public void onDamage(EntityDamageByEntityEvent event) {}
}
