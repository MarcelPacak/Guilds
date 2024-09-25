package pl.m2p13cak7.Guilds;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.checkerframework.checker.nullness.qual.NonNull;
import pl.m2p13cak7.Guilds.util.Guild;
import pl.m2p13cak7.Guilds.util.Invitations;

import java.util.*;

public class Commands implements CommandExecutor, TabExecutor {
    private static final String[] colors = {
            "aqua",
            "black",
            "blue",
            "dark_aqua",
            "dark_blue",
            "dark_gray",
            "dark_green",
            "dark_purple",
            "dark_red",
            "gold",
            "gray",
            "green",
            "light_purple",
            "red",
            "white",
            "yellow"
    };

    private void newCmd(Player player, String name, String colorName) {
        Block block = player.getTargetBlockExact(5);

        if(Guild.getGuild(player.getUniqueId()) != null)
            player.sendMessage(ChatColor.RED + "You already are a member of a guild.");

        else if(name.length() > 20)
            player.sendMessage(ChatColor.RED + "The name of a guild needs to be no longer than 20 characters long.");

        else if(!Guild.nameAvailable(name))
            player.sendMessage(ChatColor.RED + "The name you chose is taken.");

        else if(Guild.getColor(colorName) == null)
            player.sendMessage(ChatColor.RED + "The color you provided is incorrect.");

        else if(block == null || block.getType() != Material.CHEST)
            player.sendMessage(ChatColor.RED + "You need to point at a chest to create a guild.");

        else
        {
            Inventory chest = ((Chest) block.getState()).getBlockInventory();

            if(!Guild.correctInventory(chest))
                player.sendMessage(ChatColor.RED + "The chest does not contain the required items.");

            else if(!(Guild.correctRoom(block) && block.getWorld().getEnvironment() == World.Environment.NORMAL))
                player.sendMessage(ChatColor.RED + "This location is not correct.");

            else if(Guild.withinGuild(block, Guild.guildDistance * 2 + 1) != null)
                player.sendMessage(ChatColor.RED + "Another guild is too close.");

            else if(Guild.enemyPlayer(player.getWorld().getNearbyEntities(block.getLocation(), Guild.guildDistance, Guild.guildDistance, Guild.guildDistance), null))
                player.sendMessage(ChatColor.RED + "There are players from other guilds nearby.");

            else
            {
                Guild guild = new Guild(player.getUniqueId(), name, colorName, block.getLocation(), Guild.maxHp, false);
                player.sendMessage(ChatColor.RED + "You became the owner of the guild: " + Guild.getColor(colorName) + name + ChatColor.RED + '!');
                Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "A new guild (" + guild.displayName + ChatColor.GREEN + ") has been created!");
            }
        }
    }

    private void inviteCmd(Player player, String name) {
        Player invitedPlayer = Bukkit.getPlayerExact(name);
        Guild guild = Guild.getGuild(player);

        if(guild == null || !guild.getOwner().equals(player.getUniqueId()))
            player.sendMessage(ChatColor.RED + "You need to be a guild's owner to use this command.");

        else if(Objects.equals(name, player.getName()))
            player.sendMessage(ChatColor.LIGHT_PURPLE + "You silly willy :33");

        else if(invitedPlayer == null)
            player.sendMessage(ChatColor.RED + "The player you invited is offline.");

        else if(Guild.getGuild(invitedPlayer) != null)
            player.sendMessage(ChatColor.RED + "The player you invited is already in a guild.");

        else if(!Invitations.invite(invitedPlayer, guild))
            player.sendMessage(ChatColor.RED + "The player you invited is already invited to your guild.");

        else
        {
            player.sendMessage(ChatColor.GREEN + "The invitation has been sent.");
            invitedPlayer.sendMessage(ChatColor.GREEN + "You have been invited to " + guild.color + guild.name + ChatColor.GREEN + '.');
            invitedPlayer.sendMessage(ChatColor.GREEN + "You have " + Guild.invitationExpiration + " seconds to accept the invitation. To do so, type: /guild accept " + guild.name + '.');
        }
    }

    private void acceptCmd(Player player, String guildName) {
        Guild guild = Guild.getGuild(guildName);

        if(Guild.getGuild(player) != null)
            player.sendMessage(ChatColor.RED + "You already are a member of a guild.");

        else if(!Invitations.accept(player, guild))
            player.sendMessage(ChatColor.RED + "You have no valid invitation to this guild.");

        else
        {
            guild.broadcastMessage(ChatColor.GREEN + player.getName() + " is a new member of your guild.");
            player.sendMessage(ChatColor.GREEN + "You became a member of " + guild.color + guild.name + ChatColor.GREEN + '.');
        }
    }

    private void leaveCmd(Player player) {
        Guild guild = Guild.getGuild(player);

        if(guild == null)
            player.sendMessage(ChatColor.RED + "You do not belong to any guild.");
        else
        {
            if(guild.getOwner().equals(player.getUniqueId()))
            {
                if(guild.getSize() > 1)
                    player.sendMessage(ChatColor.RED + "You have to pass your ownership to another player, before leaving the guild.");
                else
                {
                    Bukkit.broadcastMessage(ChatColor.RED + "The guild " + guild.displayName + ChatColor.RED +  " has been dissolved.");
                    guild.remove();
                }
            }
            else
            {
                guild.leave(player);
                player.sendMessage(ChatColor.GREEN + "You are no longer a member of " + guild.displayName + ChatColor.GREEN + '.');
                guild.broadcastMessage(ChatColor.RED + player.getName() + " is no longer a member of your guild.");
            }
        }
    }

    private void passCmd(Player player, String name) {
        Guild guild = Guild.getGuild(player);
        Player newOwner = Bukkit.getPlayer(name);

        if(guild == null || !guild.getOwner().equals(player.getUniqueId()))
            player.sendMessage(ChatColor.RED + "You need to be the owner of a guild to use this command.");

        else if(Objects.equals(name, player.getName()))
            player.sendMessage(ChatColor.LIGHT_PURPLE + "You silly willy :33");

        else if(newOwner == null)
            player.sendMessage(ChatColor.RED + "The player you specified is offline.");

        else if(guild != Guild.getGuild(newOwner.getUniqueId()))
            player.sendMessage(ChatColor.RED + "The player you specified is not in your guild.");

        else
        {
            guild.setOwner(newOwner);
            player.sendMessage(ChatColor.GREEN + "You are no longer the owner of " + guild.displayName + ChatColor.GREEN + '.');
            newOwner.sendMessage(ChatColor.GREEN + "You became the owner of " + guild.displayName + ChatColor.GREEN + '.');
        }
    }

    private void infoCmd(Player player) {
        Guild guild = Guild.getGuild(player);
        if(guild == null)
        {
            player.sendMessage(ChatColor.RED + "You do not belong to any guild.");
        }
        else
        {
            if(guild.getOwner().equals(player.getUniqueId()))
                player.sendMessage("You are the owner of " + guild.displayName);
            else
                player.sendMessage("You are a member of " + guild.displayName);
            player.sendMessage(String.valueOf(guild.getOnline()) + '/' + String.valueOf(guild.getSize()) + " players online.");
        }
    }

    private void costCmd(Player player) {
        player.sendMessage(ChatColor.GREEN + "To found a guild, you need:");
        for(Map.Entry<Material, Integer> entry: Guild.items.entrySet())
            player.sendMessage(entry.getKey().toString().toLowerCase().replace('_', ' ') + ", " + entry.getValue());
    }

    private void kickCmd(Player player, String name) {
        Guild guild = Guild.getGuild(player);

        if(guild == null || !guild.getOwner().equals(player.getUniqueId()))
            player.sendMessage(ChatColor.RED + "You need to be the owner of a guild to use this command.");

        else if(Objects.equals(name, player.getName()))
            player.sendMessage(ChatColor.LIGHT_PURPLE + "You silly willy :33");

        else if(guild.leave(name))
        {
            Player kickedPlayer = Bukkit.getPlayerExact(name);
            if(kickedPlayer != null)
                kickedPlayer.sendTitle(ChatColor.RED + "You were kicked from your guild", null, 0, 100, 40);
            guild.broadcastMessage(ChatColor.RED + player.getName() + " is no longer a member of your guild.");
        }
        else
            player.sendMessage(ChatColor.RED + "The player you specified is not in your guild.");
    }

    private void colorsCmd(Player player) {
        for(String i: colors)
            player.sendMessage(Guild.getColor(i) + i);
    }

    @Override
    public boolean onCommand(@NonNull CommandSender commandSender, @NonNull Command command, @NonNull String s, String[] strings) {
        if(!(commandSender instanceof Player))
        {
            commandSender.sendMessage(ChatColor.RED + "You have to be a player to use this command.");
            return true;
        }
        Player player = (Player) commandSender;

        if(strings.length == 0)
            return false;
        else
            switch(strings[0])
            {
                case "new":
                    if(strings.length != 3)
                        return false;
                    newCmd(player, strings[1], strings[2]);
                    break;

                case "invite":
                    if(strings.length != 2)
                        return false;
                    inviteCmd(player, strings[1]);
                    break;

                case "accept":
                    if(strings.length != 2)
                        return false;
                    acceptCmd(player, strings[1]);
                    break;

                case "leave":
                    if(strings.length != 1)
                        return false;
                    leaveCmd(player);
                    break;

                case "pass":
                    if(strings.length != 2)
                        return false;
                    passCmd(player, strings[1]);
                    break;

                case "info":
                    if(strings.length != 1)
                        return false;
                    infoCmd(player);
                    break;

                case "cost":
                    if(strings.length != 1)
                        return false;
                    costCmd(player);
                    break;

                case "kick":
                    if(strings.length != 2)
                        return false;
                    kickCmd(player, strings[1]);
                    break;

                case "colors":
                    if(strings.length != 1)
                        return false;
                    colorsCmd(player);
                    break;

                default:
                    return false;
            }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player))
            return null;

        Player player = (Player) commandSender;

        return switch (strings.length) {
            case 1 -> Arrays.asList("new", "invite", "accept", "leave", "pass", "info", "cost", "kick", "colors");

            case 2 ->
                switch (strings[0])
                {
                    case "accept" -> Invitations.getInvitedTo(player);
                    case "invite", "pass", "kick" -> null;
                    default -> new ArrayList<String>();
                };

            case 3 ->
            {
                if(Objects.equals(strings[0], "new"))
                    yield Arrays.asList(colors);
                yield new ArrayList<String>();
            }

            default -> new ArrayList<String>();
        };
    }
}
