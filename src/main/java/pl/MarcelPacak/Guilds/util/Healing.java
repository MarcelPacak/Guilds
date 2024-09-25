package pl.MarcelPacak.Guilds.util;

import org.bukkit.scheduler.BukkitRunnable;

public class Healing extends BukkitRunnable {
    private final Guild guild;

    public Healing(Guild guild) {
        this.guild = guild;
    }

    @Override
    public void run() {
        if(guild.exists())
        {
            guild.heal(1);
            if(guild.getHp() >= Guild.maxHp)
                cancel();
        }
        else
            cancel();
    }
}
