package pl.MarcelPacak.Guilds.util;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class DataFile {
    private final File file;

    public YamlConfiguration getConfig() {
        return YamlConfiguration.loadConfiguration(file);
    }

    public void save(YamlConfiguration configuration) {
        try
        {
            configuration.save(file);
        }
        catch(IOException e)
        {
            Bukkit.getLogger().info(e.getMessage());
        }
    }

    public DataFile(Plugin plugin, String path) {
        File dir = plugin.getDataFolder();
        if(!dir.exists())
            dir.mkdir();

        this.file = new File(dir, path);
        if(!this.file.exists())
            try
            {
                this.file.createNewFile();
            }
            catch(IOException e)
            {
                Bukkit.getLogger().info(e.getMessage());
                plugin.getServer().shutdown();
            }
    }
}
