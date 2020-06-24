package com.darkender.plugins.okbomber;

import com.darkender.plugins.okbomber.custom.TNTAddon;
import com.darkender.plugins.persistentblockmetadataapi.LoadUnloadTypeChecker;
import com.darkender.plugins.persistentblockmetadataapi.MetadataWorldTrackObserver;
import com.darkender.plugins.persistentblockmetadataapi.PersistentBlockMetadataAPI;
import com.darkender.plugins.persistentblockmetadataapi.WorldTrackingModule;
import com.destroystokyo.paper.event.block.TNTPrimeEvent;
import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class OkBomber extends JavaPlugin implements Listener
{
    public static NamespacedKey tntDataKey;
    public static NamespacedKey addonsListKey;
    public static NamespacedKey addonsDataKey;
    
    private static HashMap<Location, TNTData> preSpawn;
    public static OkBomber instance;
    private PersistentBlockMetadataAPI persistentBlockMetadataAPI;
    private HashMap<NamespacedKey, TNTAddon> addedRecipes;
    private final ImpendingExplosionTracker impendingExplosionTracker = new ImpendingExplosionTracker();
    private HashMap<TNTPrimed, TNTData> activeEntities;
    private HashMap<Block, TNTData> activeBlocks;
    
    @Override
    public void onEnable()
    {
        instance = this;
        tntDataKey = new NamespacedKey(this, "data");
        addonsListKey = new NamespacedKey(this, "addons");
        persistentBlockMetadataAPI = new PersistentBlockMetadataAPI(this);
        preSpawn = new HashMap<>();
        activeEntities = new HashMap<>();
        activeBlocks = new HashMap<>();
        getServer().getPluginManager().registerEvents(this, this);
    
        addedRecipes = new HashMap<>();
        addBasicRecipe(TNTAddon.TRAP, Material.TRIPWIRE_HOOK);
        addBasicRecipe(TNTAddon.NO_BLOCK_DAMAGE, Material.FEATHER);
        addBasicRecipe(TNTAddon.INCENDIARY, Material.FIRE_CHARGE);
        addBasicRecipe(TNTAddon.STICKY, Material.SLIME_BALL);
        addBasicRecipe(TNTAddon.FLOATING, Material.PHANTOM_MEMBRANE);
        addBasicRecipe(TNTAddon.SMOKE_BOMB, Material.CAMPFIRE);
        addBasicRecipe(TNTAddon.GLOWING, Material.GLOWSTONE_DUST);
        addBasicRecipe(TNTAddon.INSTANT, Material.REDSTONE_TORCH);
        addBasicRecipe(TNTAddon.PROXIMITY, Material.ENDER_EYE);
        addBasicRecipe(TNTAddon.DUD, Material.WATER_BUCKET);
    
        WorldTrackingModule worldTrackingModule = new WorldTrackingModule(this, persistentBlockMetadataAPI);
        worldTrackingModule.setMetadataWorldTrackObserver(new MetadataWorldTrackObserver()
        {
            @Override
            public void onBreak(Block block, Event event)
            {
                activeBlocks.remove(block);
            }
    
            @Override
            public void onMove(Block from, Block to, Event event)
            {
                activeBlocks.put(to, activeBlocks.get(from));
                activeBlocks.remove(from);
            }
        });
        
        persistentBlockMetadataAPI.setLoadUnloadTypeChecker(new LoadUnloadTypeChecker()
        {
            @Override
            public boolean shouldRemove(Block block, PersistentDataContainer persistentDataContainer)
            {
                return block.getType() != Material.TNT;
            }
        });
        
        for(World world : getServer().getWorlds())
        {
            for(Chunk chunk : world.getLoadedChunks())
            {
                checkChunk(chunk);
            }
        }
        
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () ->
        {
            Iterator<Map.Entry<TNTPrimed, TNTData>> iter = activeEntities.entrySet().iterator();
            while(iter.hasNext())
            {
                Map.Entry<TNTPrimed, TNTData> entry = iter.next();
                if(!entry.getKey().isValid())
                {
                    iter.remove();
                    continue;
                }
                
                for(TNTAddon addon : entry.getValue().getTntAddons())
                {
                    addon.entityTick(entry.getKey(), entry.getValue());
                }
            }
            
            for(Map.Entry<Block, TNTData> activeBlock : activeBlocks.entrySet())
            {
                for(TNTAddon addon : activeBlock.getValue().getTntAddons())
                {
                    addon.blockTick(activeBlock.getKey(), activeBlock.getValue());
                }
            }
        }, 1L, 1L);
    }
    
    private void addBasicRecipe(TNTAddon addon, Material center)
    {
        NamespacedKey tntKey = new NamespacedKey(this, addon.getKey());
        TNTData tntData = new TNTData();
        tntData.getTntAddons().add(addon);
        ItemStack tntItem = new ItemStack(Material.TNT, 8);
        tntData.applyToItem(tntItem);
        ShapedRecipe tntRecipe = new ShapedRecipe(tntKey, tntItem);
        tntRecipe.shape("TTT", "TFT", "TTT");
        tntRecipe.setIngredient('T', Material.TNT);
        tntRecipe.setIngredient('F', center);
        Bukkit.addRecipe(tntRecipe);
        addedRecipes.put(tntKey, addon);
    }
    
    public static void addToPrespawn(Location location, TNTData data)
    {
        preSpawn.put(location, data);
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
                if(addedRecipes.containsKey(((ShapedRecipe) check).getKey()))
                {
                    iter.remove();
                }
            }
        }
    }
    
    @EventHandler
    public void onTNTPrime(TNTPrimeEvent event)
    {
        if(persistentBlockMetadataAPI.has(event.getBlock()))
        {
            if(!event.isCancelled())
            {
                preSpawn.put(event.getBlock().getLocation().add(0.5, 0.5, 0.5),
                        TNTData.read(persistentBlockMetadataAPI.get(event.getBlock())));
                persistentBlockMetadataAPI.remove(event.getBlock());
                activeBlocks.remove(event.getBlock());
            }
        }
    }
    
    public void ignite(Block block)
    {
        if(persistentBlockMetadataAPI.has(block))
        {
            Location spawnLoc = block.getLocation().add(0.5, 0.5, 0.5);
            preSpawn.put(spawnLoc, TNTData.read(persistentBlockMetadataAPI.get(block)));
            persistentBlockMetadataAPI.remove(block);
            activeBlocks.remove(block);
            block.setType(Material.AIR);
            spawnLoc.getWorld().spawn(spawnLoc, TNTPrimed.class);
            spawnLoc.getWorld().playSound(spawnLoc, Sound.ENTITY_TNT_PRIMED, 1.0f, 1.0f);
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
                addon.onDispense(event, data);
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
            impendingExplosionTracker.addTNT((TNTPrimed) event.getEntity());
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
                    addon.onIgnite((TNTPrimed) event.getEntity(), locationTNTDataEntry.getValue());
                }
                activeEntities.put((TNTPrimed) event.getEntity(), locationTNTDataEntry.getValue());
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
                addon.onPlace(event, data);
            }
            
            if(!event.isCancelled())
            {
                PersistentDataContainer container = persistentBlockMetadataAPI.get(event.getBlock());
                data.write(container);
                persistentBlockMetadataAPI.set(event.getBlock(), container);
                activeBlocks.put(event.getBlock(), data);
            }
        }
    }
    
    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event)
    {
        if(event.getRecipe() instanceof ShapedRecipe)
        {
            ShapedRecipe recipe = (ShapedRecipe) event.getRecipe();
            if(addedRecipes.containsKey(recipe.getKey()))
            {
                TNTAddon recipeAddon = addedRecipes.get(recipe.getKey());
                
                // Make a set of addons all other tnt instances have
                Set<TNTAddon> common = new HashSet<>();
                boolean first = true;
                for(ItemStack item : event.getInventory().getMatrix())
                {
                    if(item != null)
                    {
                        if(TNTData.hasData(item))
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
                        else if(item.getType() == Material.TNT)
                        {
                            common.clear();
                            break;
                        }
                    }
                }
                
                if(common.contains(recipeAddon))
                {
                    event.getInventory().setResult(null);
                }
                else
                {
                    boolean conflicts = false;
                    for(TNTAddon check : common)
                    {
                        if(check.conflictsWith(recipeAddon) || recipeAddon.conflictsWith(check))
                        {
                            event.getInventory().setResult(null);
                            conflicts = true;
                            break; // Exit on first conflict
                        }
                    }
                    
                    if(!conflicts)
                    {
                        TNTData data = new TNTData();
                        data.getTntAddons().addAll(common);
                        data.getTntAddons().add(recipeAddon);
                        
                        for(TNTAddon addon : data.getTntAddons())
                        {
                            addon.onPrepareCraft(event, data);
                        }
                        
                        ItemStack item = new ItemStack(Material.TNT, 8);
                        data.applyToItem(item);
                        event.getInventory().setResult(item);
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        if(event.getBlock().getType() == Material.TNT && persistentBlockMetadataAPI.has(event.getBlock()))
        {
            TNTData data = TNTData.read(persistentBlockMetadataAPI.get(event.getBlock()));
            for(TNTAddon addon : data.getTntAddons())
            {
                addon.onBreak(event, data);
            }
            
            // If the block is removed either by not cancelling the event or by changing the type, remove associated metadata
            if(!event.isCancelled() || event.getBlock().getType() != Material.TNT)
            {
                persistentBlockMetadataAPI.remove(event.getBlock());
                activeBlocks.remove(event.getBlock());
            }
            
            if(!event.isCancelled() && event.isDropItems() && event.getPlayer().getGameMode() != GameMode.CREATIVE)
            {
                event.setDropItems(false);
                ItemStack item = new ItemStack(Material.TNT, 1);
                data.applyToItem(item);
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);
            }
        }
    }
    
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event)
    {
        if(event.getEntityType() == EntityType.PRIMED_TNT)
        {
            if(TNTData.hasData(event.getEntity()))
            {
                TNTData data = TNTData.read(event.getEntity().getPersistentDataContainer());
                for(TNTAddon addon : data.getTntAddons())
                {
                    addon.onExplode(event, data);
                }
            }
        }
        
        // Done outside of TNTPrimeEvent because this is called before and the WorldTrackingModule removes the data
        if(!event.isCancelled())
        {
            for(Block block : event.blockList())
            {
                if(block.getType() == Material.TNT && persistentBlockMetadataAPI.has(block))
                {
                    preSpawn.put(block.getLocation().add(0.5, 0.5, 0.5),
                            TNTData.read(persistentBlockMetadataAPI.get(block)));
                    persistentBlockMetadataAPI.remove(block);
                    activeBlocks.remove(block);
                }
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event)
    {
        if(persistentBlockMetadataAPI.has(event.getBlock()) && event.getEntityType() == EntityType.ARROW)
        {
            preSpawn.put(event.getBlock().getLocation().add(0.5, 0.5, 0.5),
                    TNTData.read(persistentBlockMetadataAPI.get(event.getBlock())));
            persistentBlockMetadataAPI.remove(event.getBlock());
            activeBlocks.remove(event.getBlock());
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event)
    {
        if(persistentBlockMetadataAPI.has(event.getBlock()))
        {
            preSpawn.put(event.getBlock().getLocation().add(0.5, 0.5, 0.5),
                    TNTData.read(persistentBlockMetadataAPI.get(event.getBlock())));
            persistentBlockMetadataAPI.remove(event.getBlock());
            activeBlocks.remove(event.getBlock());
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
                addon.onDamage(event, data);
            }
        }
    }

    @EventHandler
    public void onHangingBreak(HangingBreakEvent event)
    {
        if(event.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION)
        {
            TNTPrimed soonest = impendingExplosionTracker.getRightNow(event.getEntity().getLocation());
            if(soonest != null && TNTData.hasData(soonest))
            {
                TNTData data = TNTData.read(soonest.getPersistentDataContainer());
                for(TNTAddon addon : data.getTntAddons())
                {
                    addon.onHangingBreak(event, data);
                }
            }
        }
    }
    
    private void checkChunk(Chunk chunk)
    {
        Set<Block> locations = persistentBlockMetadataAPI.getMetadataLocations(chunk);
        if(locations != null)
        {
            for(Block block : locations)
            {
                activeBlocks.put(block, TNTData.read(persistentBlockMetadataAPI.get(block)));
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event)
    {
        checkChunk(event.getChunk());
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event)
    {
        activeBlocks.entrySet().removeIf(blockTNTDataEntry -> blockTNTDataEntry.getKey().getChunk() == event.getChunk());
    }
}
