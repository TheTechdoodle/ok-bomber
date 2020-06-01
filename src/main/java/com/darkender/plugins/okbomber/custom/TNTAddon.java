package com.darkender.plugins.okbomber.custom;

import com.darkender.plugins.okbomber.custom.addons.NoBlockDamageAddon;
import com.darkender.plugins.okbomber.custom.addons.TrapAddon;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class TNTAddon implements Serializable
{
    private final static Map<String, TNTAddon> addons = new HashMap<>();
    private static final long serialVersionUID = 2040291155742538032L;
    private final String key;
    
    public final static TNTAddon TRAP = new TrapAddon();
    public final static TNTAddon NO_BLOCK_DAMAGE = new NoBlockDamageAddon();
    
    static
    {
        addons.put(TRAP.getKey(), TRAP);
        addons.put(NO_BLOCK_DAMAGE.getKey(), NO_BLOCK_DAMAGE);
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
    
    public static TNTAddon get(NamespacedKey key)
    {
        return addons.get(key);
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
