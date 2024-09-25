package pl.MarcelPacak.Guilds;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.world.*;
import pl.MarcelPacak.Guilds.util.Guild;

import java.util.List;

public class Events implements Listener {
    @EventHandler
    public void onEnderCrystalDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if(Guild.isHeart(entity))
        {
            event.setCancelled(true);
            Entity damager = event.getDamager();
            if(damager instanceof Player)
            {
                Guild guild = Guild.getGuild((EnderCrystal) entity);
                if(guild == null)
                {
                    entity.remove();
                    entity.getLocation().subtract(0.5, 1.27, 0.5).getBlock().setType(Material.AIR);
                    return;
                }
                Player player = (Player) damager;
                Guild damagerGuild = Guild.getGuild(player);
                if((damagerGuild != null && damagerGuild != guild) || player.getGameMode() == GameMode.CREATIVE)
                    guild.dealDamage(event.getDamage(), (EnderCrystal) entity);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if(Guild.cannotBeChanged(event.getBlock()) && event.getPlayer().getGameMode() != GameMode.CREATIVE)
        {
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot place blocks in the Guild's Room.");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(Guild.cannotBeChanged(event.getBlock()) && event.getPlayer().getGameMode() != GameMode.CREATIVE)
        {
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot break the floor in the Guild's Room.");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        if(Guild.cannotBeChanged(event.getBlock()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        if(Guild.cannotBeChanged(event.getBlock()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onStructureGrow(StructureGrowEvent event) {
        Guild guild = Guild.withinGuild(event.getBlocks().get(0).getBlock());
        if(guild == null)
            return;

        for(BlockState block: event.getBlocks()) {
            if(Guild.cannotBeChanged(block.getBlock(), guild))
            {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if(Guild.cannotBeChanged(event.getBlock()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if(Guild.cannotBeChanged(event.getBlock()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && block != null && block.getType() == Material.CHEST)
        {
            Guild guild = Guild.withinGuild(block);
            if(guild != null)
            {
                Chest chest = (Chest) block.getState();
                chest.setCustomName("Chest of " + guild.displayName);
                chest.update();
                if(guild.notMember(event.getPlayer()))
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if(event.getEntity() instanceof Player)
        {
            Guild guild = Guild.withinGuild(event.getItem());
            if(guild != null && guild.notMember((Player) event.getEntity()))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMessage(AsyncPlayerChatEvent event) {
        Guild guild = Guild.getGuild(event.getPlayer().getUniqueId());
        if(guild != null)
            event.setFormat("<" + guild.color + event.getPlayer().getName() + ChatColor.RESET + "> " + event.getMessage());
    }

    @EventHandler
    public void onExplosion(ExplosionPrimeEvent event) {
        Entity entity = event.getEntity();
        if(Guild.withinGuild(entity) != null)
        {
            event.setCancelled(true);
            if(entity instanceof EnderCrystal)
                entity.remove();
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        List<Block> blocks = event.getBlocks();
        if(blocks.isEmpty())
            return;
        Block blockBehind = blocks.get(blocks.size()-1).getLocation().add(event.getDirection().getDirection()).getBlock();
        for(Block block: blocks)
        {
            if(Guild.cannotBeChanged(block)) {
                event.setCancelled(true);
                return;
            }
        }
        if(Guild.cannotBeChanged(blockBehind))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for(Block block: event.getBlocks())
            if(Guild.cannotBeChanged(block))
            {
                event.setCancelled(true);
                return;
            }
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        if(Guild.loaded < Guild.toLoad)
        {
            for (Entity entity : event.getEntities())
                if (Guild.isHeart(entity)) {
                    Guild guild = Guild.getGuild((EnderCrystal) entity);
                    if (guild == null) {
                        entity.remove();
                        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "A heart had to be removed.");
                        return;
                    }
                    if (Guild.loaded < Guild.toLoad) {
                        if (guild.setHeartUUID(entity.getUniqueId())) {
                            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "The guild \"" + guild.displayName + ChatColor.GREEN + "\" has been loaded successfully. (the event method)");
                            Guild.loaded++;
                        }
                    }
                    guild.nameUpdate((EnderCrystal) entity);
                }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(Guild.loaded < Guild.toLoad)
            player.kickPlayer(ChatColor.RED + "The guilds are still being loaded");
        else
        {
            Guild guild = Guild.getGuild(player);
            Guild.teamCheck(player, guild);
            if(guild != null)
                guild.setPlayerName(player);
        }
    }

    @EventHandler
    public void onPlayerSpawnChange(PlayerSpawnChangeEvent event) {
        if(event.getCause() != PlayerSpawnChangeEvent.Cause.RESPAWN_ANCHOR)
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Guild guild = Guild.getGuild(event.getPlayer());
        if(guild != null && !event.isAnchorSpawn())
            event.setRespawnLocation(guild.getSpawnLocation());
    }
}
