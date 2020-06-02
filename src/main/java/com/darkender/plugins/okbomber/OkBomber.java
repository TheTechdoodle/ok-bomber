package com.darkender.plugins.okbomber;

import com.darkender.plugins.okbomber.custom.TNTAddon;
import com.darkender.plugins.persistentblockmetadataapi.PersistentBlockMetadataAPI;
import com.destroystokyo.paper.event.block.TNTPrimeEvent;
import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import org.bukkit.*;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.TNT;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class OkBomber extends JavaPlugin implements Listener
{
    public static NamespacedKey tntDataKey;
    public static NamespacedKey addonsListKey;
    public static NamespacedKey addonsDataKey;
    
    private HashMap<Location, TNTData> preSpawn;
    public static OkBomber instance;
    private PersistentBlockMetadataAPI persistentBlockMetadataAPI;
    private Set<NamespacedKey> addedRecipes;
    
    @Override
    public void onEnable()
    {
        instance = this;
        tntDataKey = new NamespacedKey(this, "data");
        addonsListKey = new NamespacedKey(this, "addons");
        persistentBlockMetadataAPI = new PersistentBlockMetadataAPI(this);
        preSpawn = new HashMap<>();
        getServer().getPluginManager().registerEvents(this, this);
    
        addedRecipes = new HashSet<>();
        
        NamespacedKey trapTNTKey = new NamespacedKey(this, "trap-tnt");
        TNTData trapTNTData = new TNTData();
        trapTNTData.getTntAddons().add(TNTAddon.TRAP);
        ItemStack trapTNTItem = new ItemStack(Material.TNT, 8);
        trapTNTData.applyToItem(trapTNTItem);
        ShapedRecipe trapTNT = new ShapedRecipe(trapTNTKey, trapTNTItem);
        trapTNT.shape("TTT", "THT", "TTT");
        trapTNT.setIngredient('T', Material.TNT);
        trapTNT.setIngredient('H', Material.TRIPWIRE_HOOK);
        Bukkit.addRecipe(trapTNT);
        addedRecipes.add(trapTNTKey);
    
        NamespacedKey noBlockDamageTNTKey = new NamespacedKey(this, "no-block-damage-tnt");
        TNTData noBlockDamageTNTData = new TNTData();
        noBlockDamageTNTData.getTntAddons().add(TNTAddon.NO_BLOCK_DAMAGE);
        ItemStack noBlockDamageTNTItem = new ItemStack(Material.TNT, 8);
        noBlockDamageTNTData.applyToItem(noBlockDamageTNTItem);
        ShapedRecipe noBlockDamageTNT = new ShapedRecipe(noBlockDamageTNTKey, noBlockDamageTNTItem);
        noBlockDamageTNT.shape("TTT", "TFT", "TTT");
        noBlockDamageTNT.setIngredient('T', Material.TNT);
        noBlockDamageTNT.setIngredient('F', Material.FEATHER);
        Bukkit.addRecipe(noBlockDamageTNT);
        addedRecipes.add(noBlockDamageTNTKey);
    }
    
    @Override
    public void onDisable()
    {
        Iterator<Recipe> iter = getServer().recipeIterator();
        while (iter.hasNext())
        {
            Recipe check = iter.next();
            if(check instanceof ShapedRecipe)
            {
                if(addedRecipes.contains(((ShapedRecipe) check).getKey()))
                {
                    iter.remove();
                }
            }
        }
    }
    
    @EventHandler
    public void onTNTPrime(TNTPrimeEvent event)
    {
        if(persistentBlockMetadataAPI.has(event.getBlock(), PersistentDataType.TAG_CONTAINER))
        {
            if(!event.isCancelled())
            {
                preSpawn.put(event.getBlock().getLocation().add(0.5, 0.5, 0.5),
                        TNTData.read(persistentBlockMetadataAPI.getContainer(event.getBlock())));
                persistentBlockMetadataAPI.removeContainer(event.getBlock());
            }
        }
        
    }
    
    @EventHandler
    public void onBlockDispense(BlockDispenseEvent event)
    {
        if(event.getItem().getType() == Material.TNT &&
                event.getBlock().getType() == Material.DISPENSER &&
                TNTData.hasData(event.getItem()))
        {
            TNTData data = TNTData.read(event.getItem().getItemMeta().getPersistentDataContainer());
            for(TNTAddon addon : data.getTntAddons())
            {
                addon.onDispense(event);
            }
            
            if(!event.isCancelled())
            {
                Directional direction = (Directional) event.getBlock().getBlockData();
                preSpawn.put(event.getBlock().getLocation().add(0.5, 0.5, 0.5)
                        .add(direction.getFacing().getDirection()), data);
            }
        }
    }
    
    @EventHandler
    public void onEntityAddToWorld(EntityAddToWorldEvent event)
    {
        if(event.getEntityType() == EntityType.PRIMED_TNT)
        {
            preSpawn.entrySet().removeIf(locationTNTDataEntry ->
            {
                if(locationTNTDataEntry.getKey().getWorld() != event.getEntity().getWorld())
                {
                    return false;
                }
                if(locationTNTDataEntry.getKey().distance(event.getEntity().getLocation()) > 0.5)
                {
                    return false;
                }
                
                locationTNTDataEntry.getValue().write(event.getEntity().getPersistentDataContainer());
                for(TNTAddon addon : locationTNTDataEntry.getValue().getTntAddons())
                {
                    addon.onIgnite((TNTPrimed) event.getEntity());
                }
                return true;
            });
        }
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event)
    {
        if(event.getBlock().getType() == Material.TNT && TNTData.hasData(event.getItemInHand()))
        {
            TNTData data = TNTData.read(event.getItemInHand().getItemMeta().getPersistentDataContainer());
            for(TNTAddon addon : data.getTntAddons())
            {
                addon.onPlace(event);
            }
            
            if(!event.isCancelled())
            {
                PersistentDataContainer container = persistentBlockMetadataAPI.getContainer(event.getBlock());
                data.write(container);
                persistentBlockMetadataAPI.setContainer(event.getBlock(), container);
            }
        }
    }
    
    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event)
    {
        if(event.getRecipe() instanceof ShapedRecipe)
        {
            ShapedRecipe recipe = (ShapedRecipe) event.getRecipe();
            if(recipe.getKey().equals(new NamespacedKey(this, "trap-tnt")))
            {
                Set<TNTAddon> common = new HashSet<>();
                boolean first = true;
                for(ItemStack item : event.getInventory().getMatrix())
                {
                    if(item != null && TNTData.hasData(item))
                    {
                        // TNT addons can only be common if they exist on the first element
                        // After that, remove each addon if it isn't on the other elements
                        TNTData data = TNTData.read(item.getItemMeta().getPersistentDataContainer());
                        if(first)
                        {
                            common.addAll(data.getTntAddons());
                            first = false;
                        }
                        else
                        {
                            common.removeIf(tntAddon -> !data.getTntAddons().contains(tntAddon));
                        }
                    }
                }
                
                if(common.contains(TNTAddon.TRAP))
                {
                    event.getInventory().setResult(null);
                }
                else
                {
                    TNTData data = new TNTData();
                    data.getTntAddons().addAll(common);
                    data.getTntAddons().add(TNTAddon.TRAP);
                    ItemStack item = new ItemStack(Material.TNT, 8);
                    data.applyToItem(item);
                    event.getInventory().setResult(item);
                }
            }
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        if(event.getBlock().getType() == Material.TNT && persistentBlockMetadataAPI.has(event.getBlock(), PersistentDataType.TAG_CONTAINER))
        {
            TNTData data = TNTData.read(persistentBlockMetadataAPI.getContainer(event.getBlock()));
            for(TNTAddon addon : data.getTntAddons())
            {
                addon.onBreak(event);
            }

            TNT blockData = (TNT) event.getBlock().getBlockData();
            if(!event.isCancelled())
            {
                persistentBlockMetadataAPI.removeContainer(event.getBlock());
                if(blockData.isUnstable())
                {
                    preSpawn.put(event.getBlock().getLocation().add(0.5, 0.5, 0.5), data);
                }
            }
            if(!event.isCancelled() && event.isDropItems() && !blockData.isUnstable() &&
                    event.getPlayer().getGameMode() != GameMode.CREATIVE)
            {
                event.setDropItems(false);
                ItemStack item = new ItemStack(Material.TNT, 1);
                data.applyToItem(item);
                event.getBlock().getWorld().dropItemNaturally(
                        event.getBlock().getLocation().add(0.5, 0.5, 0.5), item);
            }
        }
    }
    
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event)
    {
        if(event.getEntityType() == EntityType.PRIMED_TNT && TNTData.hasData(event.getEntity()))
        {
            TNTData data = TNTData.read(event.getEntity().getPersistentDataContainer());
            for(TNTAddon addon : data.getTntAddons())
            {
                addon.onExplode(event);
            }
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
    {
        if(event.getDamager() instanceof TNTPrimed && TNTData.hasData(event.getDamager()))
        {
            TNTData data = TNTData.read(event.getDamager().getPersistentDataContainer());
            for(TNTAddon addon : data.getTntAddons())
            {
                addon.onDamage(event);
            }
        }
    }
}
