package com.darkender.plugins.okbomber;

import com.darkender.plugins.okbomber.custom.TNTAddon;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class TNTData implements Cloneable
{
    private final Set<TNTAddon> tntAddons = new HashSet<>();
    
    public TNTData()
    {
    
    }
    
    public boolean hasAddon(String addonKey)
    {
        for(TNTAddon check : tntAddons)
        {
            if(check.getKey().equals(addonKey))
            {
                return true;
            }
        }
        return false;
    }
    
    public Set<TNTAddon> getTntAddons()
    {
        return tntAddons;
    }
    
    public String serialize()
    {
        StringJoiner joiner = new StringJoiner(";");
        for(TNTAddon addon : tntAddons)
        {
            joiner.add(addon.getKey());
        }
        return joiner.toString();
    }
    
    public void write(PersistentDataContainer container)
    {
        container.set(OkBomber.addonsListKey, PersistentDataType.STRING, serialize());
    }
    
    public void applyToItem(ItemStack base)
    {
        ItemMeta meta = base.getItemMeta();
        if(!hasData(base))
        {
            base.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 1);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            // Show the addon name in the item name if there's only one addon
            meta.setDisplayName(ChatColor.AQUA +
                    (tntAddons.size() != 1 ?
                            "Custom TNT " + ChatColor.BLUE + "[" + tntAddons.size() + " Addons]" :
                            tntAddons.iterator().next().getName() + " TNT"));
        }
        List<String> lore = new ArrayList<>();
        for(TNTAddon addon : tntAddons)
        {
            lore.add(ChatColor.GOLD + "\u2022 " + addon.getName());
            lore.add(ChatColor.DARK_AQUA + "    " + addon.getDescription());
        }
        meta.setLore(lore);
        write(meta.getPersistentDataContainer());
        base.setItemMeta(meta);
    }
    
    public static TNTData read(String data)
    {
        TNTData tntData = new TNTData();
        for(String key : data.split(";"))
        {
            if(TNTAddon.has(key))
            {
                tntData.getTntAddons().add(TNTAddon.get(key));
            }
        }
        return tntData;
    }
    
    public static TNTData read(PersistentDataContainer persistentDataContainer)
    {
        if(!persistentDataContainer.has(OkBomber.addonsListKey, PersistentDataType.STRING))
        {
            throw new IllegalArgumentException();
        }
        return read(persistentDataContainer.get(OkBomber.addonsListKey, PersistentDataType.STRING));
    }
    
    public static boolean hasData(PersistentDataContainer container)
    {
        return container.has(OkBomber.addonsListKey, PersistentDataType.STRING);
    }
    
    public static boolean hasData(Entity entity)
    {
        return hasData(entity.getPersistentDataContainer());
    }
    
    public static boolean hasData(ItemStack item)
    {
        return item.hasItemMeta() && hasData(item.getItemMeta().getPersistentDataContainer());
    }
}
