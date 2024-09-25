package pl.m2p13cak7.Guilds.util;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class GuildsClock extends BukkitRunnable {
    HashMap<String, Guild> guilds;

    public GuildsClock(HashMap<String, Guild> guilds) {
        this.guilds = guilds;
    }

    @Override
    public void run() {
        for(Guild guild: guilds.values())
            guild.clock();
    }
}
