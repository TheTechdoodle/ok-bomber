package com.darkender.plugins.okbomber;

import com.darkender.plugins.okbomber.custom.TNTAddon;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TNTData implements Cloneable, Serializable
{
    private static final long serialVersionUID = -5200651817121602619L;
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
    
    public byte[] serialize() throws IOException
    {
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream())
        {
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(this);
            out.flush();
            return bos.toByteArray();
        }
    }
    
    public void apply(PersistentDataContainer persistentDataContainer)
    {
        try
        {
            persistentDataContainer.set(OkBomber.tntDataKey, PersistentDataType.BYTE_ARRAY, serialize());
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public void applyToItem(ItemStack base)
    {
        ItemMeta meta = base.getItemMeta();
        if(!hasData(base))
        {
            base.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 1);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.setDisplayName(ChatColor.AQUA + "Custom TNT");
        }
        List<String> lore = new ArrayList<>();
        for(TNTAddon addon : tntAddons)
        {
            lore.add(ChatColor.GOLD + " - " + addon.getName());
        }
        meta.setLore(lore);
        apply(meta.getPersistentDataContainer());
        base.setItemMeta(meta);
    }
    
    public static TNTData read(byte[] data) throws IOException, ClassNotFoundException
    {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        try(ObjectInput in = new ObjectInputStream(bis))
        {
            return (TNTData) in.readObject();
        }
    }
    
    public static TNTData read(PersistentDataContainer persistentDataContainer) throws IOException, ClassNotFoundException, IllegalArgumentException
    {
        if(!persistentDataContainer.has(OkBomber.tntDataKey, PersistentDataType.BYTE_ARRAY))
        {
            throw new IllegalArgumentException();
        }
        return read(persistentDataContainer.get(OkBomber.tntDataKey, PersistentDataType.BYTE_ARRAY));
    }
    
    public static boolean hasData(ItemStack item)
    {
        return item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer()
                .has(OkBomber.tntDataKey, PersistentDataType.BYTE_ARRAY);
    }
}
