package pl.MarcelPacak.Guilds.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Invitations {
    private static final HashMap<Player, ArrayList<Guild>> invitations = new HashMap<Player, ArrayList<Guild>>();

    public static boolean invite(Player player, Guild guild) {
        if(!invitations.containsKey(player))
            invitations.put(player, new ArrayList<Guild>());
        else if(invitations.get(player).contains(guild))
            return false;
        invitations.get(player).add(guild);

        Bukkit.getScheduler().scheduleSyncDelayedTask(Guild.plugin, new Runnable() {
            @Override
            public void run() {
                ArrayList<Guild> guilds = invitations.get(player);
                if(guilds != null)
                    guilds.remove(guild);
            }
        }, Guild.invitationExpiration * 20L);

        return true;
    }

    public static boolean accept(Player player, Guild guild) {
        ArrayList<Guild> list = invitations.get(player);
        if(list != null && list.contains(guild))
        {
            guild.join(player.getUniqueId());
            list.remove(guild);
        }
        else
            return false;
        return true;
    }

    public static ArrayList<String> getInvitedTo(Player player) {
        ArrayList<String> guilds = new ArrayList<String>();
        if(invitations.get(player) != null)
            for(Guild guild: invitations.get(player))
                guilds.add(guild.name);

        return guilds;
    }
}
