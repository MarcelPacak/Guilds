package pl.MarcelPacak.Guilds;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import pl.MarcelPacak.Guilds.util.Guild;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        PluginCommand command = getCommand("guild");
        if(command != null)
            command.setExecutor(new Commands());
        else
            getLogger().info("The \"guild\" command could not be loaded, restarting the server is recommended.");

        loadConfig();

        Bukkit.getPluginManager().registerEvents(new Events(), this);

        Guild.init(20L);
    }

    @Override
    public void onDisable() {
        Guild.save();
    }

    private void loadConfig() {
        FileConfiguration configuration = getConfig();

        if(configuration.get("maxHp") != null)
            Guild.maxHp = configuration.getInt("maxHp");
        else
            configuration.set("maxHp", 100);

        if(configuration.get("guildDistance") != null)
            Guild.guildDistance = configuration.getInt("guildDistance");
        else
            configuration.set("guildDistance", 20);

        if(configuration.get("alertCooldown") != null)
            Guild.alertCooldown = configuration.getInt("alertCooldown");
        else
            configuration.set("alertCooldown", 30);

        if(configuration.get("healingCooldown") != null)
            Guild.healingCooldown = configuration.getInt("healingCooldown");
        else
            configuration.set("healingCooldown", 10);

        if(configuration.get("ticksFor1Hp") != null)
            Guild.ticksFor1Hp = configuration.getInt("ticksFor1Hp");
        else
            configuration.set("ticksFor1Hp", 5);

        ConfigurationSection cost = configuration.getConfigurationSection("cost");
        if(cost == null)
        {
            configuration.set("cost.diamond", 1);
            cost = configuration.getConfigurationSection("cost");
        }

        for(String item: cost.getKeys(false))
        {
            Material material = Material.getMaterial(item.toUpperCase());
            if(material != null)
                Guild.items.put(material, cost.getInt(item));
            else
                cost.set(item, null);
        }

        if(configuration.get("invitationExpiration") != null)
            Guild.invitationExpiration = configuration.getInt("invitationExpiration");
        else
            configuration.set("invitationExpiration", 30);

        saveConfig();
    }
}
