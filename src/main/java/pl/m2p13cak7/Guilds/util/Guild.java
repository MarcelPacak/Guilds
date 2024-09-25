package pl.m2p13cak7.Guilds.util;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.units.qual.N;

import java.util.*;

import static org.bukkit.util.NumberConversions.ceil;

public class Guild {
    private UUID owner;
    public final String name;
    public final String colorName;
    public final ChatColor color;
    private final Location location;
    private final Team team;
    private double hp;
    private final List<UUID> members;
    private boolean exists = true;
    private final World world;
    private UUID heartUUID = null;
    private long lastAlert = -1;
    private boolean someoneInside = true;
    public final String displayName;
    private long lastDamage = 0;
    private Healing healing;




    //constructor
    public Guild(@NonNull UUID player, @NonNull String name, @NonNull String colorName, @NonNull Location location, double hp, boolean wasLoaded) {
        this.owner = player;
        this.name = name;
        this.colorName = colorName;
        this.color = getColor(colorName);
        this.displayName = color + name;
        this.location = location;
        this.world = location.getWorld();
        this.members = new ArrayList<UUID>();
        this.hp = hp;
        this.team = scoreboard.registerNewTeam(name);
        join(owner);

        addGuild(this);
        if(!wasLoaded)
            newHeart();
        else
        {
            Chunk chunk = world.getChunkAt(location);
            chunk.load();
            if(heartUUID == null)
                for(Entity entity: chunk.getEntities())
                    if(isHeart(entity) && getGuildName((EnderCrystal) entity).equals(this.name))
                    {
                        if(setHeartUUID(entity.getUniqueId()))
                        {
                            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "The guild \"" + displayName + ChatColor.GREEN + "\" has been loaded successfully. (the chunk method)");
                            loaded++;
                        }
                        break;
                    }
        }

        location.getBlock().setType(Material.BEDROCK);

        team.setColor(color);
        team.setAllowFriendlyFire(false);
        nameUpdate();
    }




    //heart
    public void newHeart() {
        if(heartUUID == null)
        {
            EnderCrystal heart = (EnderCrystal) world.spawnEntity(location.clone().add(0.5, 1.27, 0.5), EntityType.ENDER_CRYSTAL);
            this.heartUUID = heart.getUniqueId();
            nameUpdate();
        }
    }

    private @Nullable EnderCrystal getHeart() {
        return (EnderCrystal) Bukkit.getEntity(heartUUID);
    }

    public boolean setHeartUUID(UUID uuid) {
        if(this.heartUUID == null)
        {
            this.heartUUID = uuid;
            return true;
        }
        return false;
    }




    //name
    public void nameUpdate(@Nullable EnderCrystal heart) {
        if(heart == null)
            return;

        StringBuilder health = new StringBuilder(Integer.toString(ceil(hp)));
        while(health.length() < maxHpLength)
            health.insert(0, ' ');
        heart.setCustomName(color + name + "§c [" + health + '/' + maxHp + "§4❤§c]");
    }

    public void nameUpdate() {
        nameUpdate(getHeart());
    }




    //removal
    public void remove(@Nullable EnderCrystal heart) {
        if(heart != null)
            heart.remove();
        for(UUID uuid: members)
        {
            players.remove(uuid);
            Player player = Bukkit.getPlayer(uuid);
            if(player != null)
                player.setPlayerListName(player.getName());
        }
        removeGuild(this);

        team.unregister();
        location.getBlock().setType(Material.AIR);
        exists = false;
    }

    public void remove() {
        remove(getHeart());
    }

    public void destroy(@Nullable EnderCrystal heart) {
        world.spawnParticle(Particle.EXPLOSION_HUGE, location, 1);
        world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 0.5f);
        Bukkit.getServer().broadcastMessage(ChatColor.RED + "The guild " + color + name + ChatColor.RED + " has been destroyed!");
        for(Player player: Bukkit.getServer().getOnlinePlayers())
            player.playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.1f);

        for(UUID uuid: members)
        {
            Player player = Bukkit.getPlayer(uuid);
            if(player != null)
                player.sendTitle(ChatColor.DARK_RED + "Your guild has been destroyed!", "", 0, 100, 40);
        }
        remove(heart);
    }

    public boolean exists() {
        return exists;
    }




    //members
    public boolean notMember(@NonNull Player player) {
        return !members.contains(player.getUniqueId());
    }

    public int getSize() {
        return members.size();
    }

    public void join(@NonNull UUID uuid) {
        if(!players.containsKey(uuid))
        {
            members.add(uuid);
            players.put(uuid, this);
            addEntry(uuid);
        }
    }

    public void leave(@NonNull Player player) {
        leave(player.getUniqueId());
    }

    public void leave(@NonNull UUID uuid) {
        members.remove(uuid);
        players.remove(uuid);
        removeEntry(uuid);
        Player player = Bukkit.getPlayer(uuid);
        if(player != null)
            player.setPlayerListName(player.getName());
    }

    public boolean leave(@NonNull String name) {
        for(UUID uuid: members)
        {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            if(player.getName().equals(name))
            {
                leave(uuid);
                return true;
            }
        }
        return false;
    }

    public @NonNull UUID[] getMembers() {
        return members.toArray(new UUID[0]);
    }

    public @NonNull UUID getOwner() {
        return owner;
    }

    public void broadcastMessage(@NonNull String s) {
        for(UUID id: members)
        {
            Player player = Bukkit.getPlayer(id);
            if(player != null)
                player.sendMessage(s);
        }
    }

    public void setOwner(@NonNull Player newOwner) {
        this.owner = newOwner.getUniqueId();
    }

    public int getOnline() {
        int c = 0;
        for(UUID uuid: members)
            if(Bukkit.getPlayer(uuid) != null)
                c++;
        return c;
    }

    private void addEntry(@NonNull UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if(player != null)
            addEntry(player);
    }

    private void addEntry(@NonNull Player player) {
        player.setScoreboard(scoreboard);
        team.addEntry(player.getName());
        setPlayerName(player);
    }

    private void removeEntry(@NonNull UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if(player != null)
            team.removeEntry(player.getName());
    }

    public void setPlayerName(@NonNull Player player) {
        player.setPlayerListName('<' + displayName + ChatColor.RESET + "> " + player.getName());
    }




    //hp
    public double getHp() {
        return hp;
    }

    public void dealDamage(double damage, @Nullable EnderCrystal heart) {
        if(healing != null)
        {
            healing.cancel();
            healing = null;
        }

        lastDamage = System.currentTimeMillis();
        hp -= damage;
        if(hp <= 0)
            destroy(heart);
        else
            nameUpdate(heart);
    }

    public void heal(double hp) {
        this.hp = Math.min(this.hp + hp, maxHp);
        nameUpdate();
    }




    //clock
    public void clock() {
        if(hp < maxHp && healing == null && System.currentTimeMillis() - lastDamage > healingCooldown * 1000L)
        {
            healing = new Healing(this);
            healing.runTaskTimer(plugin, 0, ticksFor1Hp);
        }


        boolean foundSomeone = enemyPlayer(world.getNearbyEntities(location, guildDistance, guildDistance, guildDistance), this);
        if(foundSomeone && !someoneInside && (lastAlert == -1 || System.currentTimeMillis() - lastAlert > alertCooldown * 1000L))
        {
            lastAlert = System.currentTimeMillis();
            for(UUID uuid : members)
            {
                Player player = Bukkit.getPlayer(uuid);
                if(player != null)
                    player.sendTitle(ChatColor.RED + "Someone has entered your guild", null, 0, 100, 40);
            }
        }
        someoneInside = foundSomeone;
    }




    //location
    public @NonNull Location getLocation() {
        return location.clone();
    }
    public @NonNull Location getSpawnLocation() {
        Location location = getLocation().add(3.5, 0, 0.5);
        location.setPitch(-5);
        location.setYaw(90);
        return location;
    }











    //static methods:
    public static int guildDistance = 20, maxHp = 200, maxHpLength = 3, alertCooldown = 60, healingCooldown = 10;
    public static long ticksFor1Hp = 5;
    public static final int guildRoomDistance = 3;
    private static final HashMap<String, Guild> guilds = new HashMap<String, Guild>();
    private static final HashMap<UUID, Guild> players = new HashMap<UUID, Guild>();
    private static final GuildsClock runnable = new GuildsClock(guilds);
    public static int toLoad;
    public static int loaded = 0;
    public static final Plugin plugin = Bukkit.getPluginManager().getPlugin("Guilds");
    private static final DataFile file = new DataFile(plugin, "guilds.yml");
    private static final Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
    public static final HashMap<Material, Integer> items = new HashMap<Material, Integer>();
    public static int invitationExpiration = 20;

    public static void init(long clockTicks) {
        maxHpLength = Integer.toString(maxHp).length();

        YamlConfiguration configuration = file.getConfig();
        Set<String> list = configuration.getKeys(false);

        toLoad = list.size();
        for(String name: list)
        {
            String colorName = configuration.getString(name + ".colorName");
            UUID owner = UUID.fromString(configuration.getString(name + ".owner"));
            Location location = configuration.getLocation(name + ".location");

            double hp = configuration.getDouble(name + ".hp");
            double oldMaxHp = configuration.getDouble(name + ".maxHp");
            if(oldMaxHp != maxHp)
                hp = (hp / oldMaxHp) * maxHp;

            Guild guild = new Guild(owner, name, colorName, location, hp, true);

            List<String> members = configuration.getStringList(name + ".members");
            for(String member: members)
            {
                UUID uuid = UUID.fromString(member);
                guild.join(uuid);
            }

        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                if(loaded < toLoad)
                {
                    for(Guild guild: guilds.values())
                        guild.newHeart();
                    loaded = toLoad;
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Some hearts needed to be restored.");
                }
            }
        }, 100L);

        runnable.runTaskTimer(plugin, 0L, clockTicks);
    }

    public static void save() {
        YamlConfiguration configuration = new YamlConfiguration();

        for(Map.Entry<String, Guild> guildMap: guilds.entrySet())
        {
            configuration.set(guildMap.getKey() + ".colorName", guildMap.getValue().colorName);
            configuration.set(guildMap.getKey() + ".owner", guildMap.getValue().getOwner().toString());
            configuration.set(guildMap.getKey() + ".location", guildMap.getValue().location);
            configuration.set(guildMap.getKey() + ".hp", guildMap.getValue().getHp());
            configuration.set(guildMap.getKey() + ".maxHp", maxHp);

            List<String> ids = new ArrayList<String>();
            for(UUID id: guildMap.getValue().getMembers())
                ids.add(id.toString());
            configuration.set(guildMap.getKey() + ".members", ids);

        }
        file.save(configuration);
    }

    public static @Nullable Guild getGuild(@NonNull UUID player) {
        return players.get(player);
    }

    public static @Nullable Guild getGuild(@NonNull Player player) {
        return getGuild(player.getUniqueId());
    }

    public static @Nullable Guild getGuild(@NonNull String name) {
        return guilds.get(name);
    }

    private static void addGuild(@NonNull Guild guild) {
        guilds.put(guild.name, guild);
        save();
    }

    private static void removeGuild(@NonNull Guild guild) {
        guilds.remove(guild.name);
        file.getConfig().set(guild.name, null);
        save();
    }

    public static @Nullable Guild withinGuild(@NonNull Entity entity, int distance) {
        for(Entity e: entity.getNearbyEntities(distance + 1.5, distance + 1.5, distance + 1.5))
            if(isHeart(e))
            {
                Location location = e.getLocation().subtract(0.5, 1.27, 0.5);
                location.subtract(round(entity.getLocation()));
                if(Math.abs(location.getX()) <= distance && Math.abs(location.getY()) <= distance && Math.abs(location.getZ()) <= distance)
                    return getGuild((EnderCrystal) e);
            }
        return null;
    }

    public static @Nullable Guild withinGuild(@NonNull Entity entity) {
        return withinGuild(entity, guildDistance);
    }

    public static @Nullable Guild withinGuild(@NonNull Block block, int distance) {
        for(Entity e: block.getWorld().getNearbyEntities(block.getLocation(), distance+1.5, distance+1.5, distance+1.5))
            if(isHeart(e))
            {
                Location location = e.getLocation().subtract(0.5, 1.27, 0.5);
                location.subtract(round(block.getLocation()));
                if(Math.abs(location.getX()) <= distance && Math.abs(location.getY()) <= distance && Math.abs(location.getZ()) <= distance)
                    return getGuild((EnderCrystal) e);
            }
        return null;
    }

    public static @Nullable Guild withinGuild(@NonNull Block block) {
        return withinGuild(block, guildDistance);
    }

    public static boolean nameAvailable(@NonNull String s) {
        return !guilds.containsKey(s);
    }

    public static @NonNull ChatColor getColor(@NonNull String colorName) {
        return switch(colorName.toLowerCase())
        {
            case "aqua" -> ChatColor.AQUA;
            case "black" -> ChatColor.BLACK;
            case "blue" -> ChatColor.BLUE;
            case "dark_aqua" -> ChatColor.DARK_AQUA;
            case "dark_blue" -> ChatColor.DARK_BLUE;
            case "dark_gray" -> ChatColor.DARK_GRAY;
            case "dark_green" -> ChatColor.DARK_GREEN;
            case "dark_purple" -> ChatColor.DARK_PURPLE;
            case "dark_red" -> ChatColor.DARK_RED;
            case "gold" -> ChatColor.GOLD;
            case "gray" -> ChatColor.GRAY;
            case "green" -> ChatColor.GREEN;
            case "light_purple" -> ChatColor.LIGHT_PURPLE;
            case "red" -> ChatColor.RED;
            case "white" -> ChatColor.WHITE;
            case "yellow" -> ChatColor.YELLOW;
            default -> throw new IllegalArgumentException();
        };
    }

    private static boolean incorrectBlock(@NonNull Block block, @NonNull Collection<BoundingBox> boundingBoxes) {
        Material material = block.getType();
        if(!block.getCollisionShape().getBoundingBoxes().equals(boundingBoxes))
            return true;
        if(!material.isSolid())
            return true;
        if(material.isInteractable())
            return true;
        if(material.hasGravity())
            return true;
        return material.toString().contains("LEAVES");
    }

    public static boolean correctRoom(@NonNull Block block) {
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        World world = block.getWorld();
        Collection<BoundingBox> boundingBoxes = world.getBlockAt(x, -64, y).getCollisionShape().getBoundingBoxes();

        for (int i = x - guildRoomDistance; i <= x + guildRoomDistance; i++)
            for (int j = z - guildRoomDistance; j <= z + guildRoomDistance; j++)
                if (incorrectBlock(world.getBlockAt(i, y - 1, j), boundingBoxes))
                    return false;

        for (int i = x - guildRoomDistance; i <= x + guildRoomDistance; i++)
            for (int j = z - guildRoomDistance; j <= z + guildRoomDistance; j++) {
                if (!(world.getBlockAt(i, y, j).getType() == Material.AIR || (x == i && z == j)))
                    return false;
            }

        for (int k = 1; k <= 3; k++)
            for (int i = x - guildRoomDistance; i <= x + guildRoomDistance; i++)
                for (int j = z - guildRoomDistance; j <= z + guildRoomDistance; j++)
                    if (!(world.getBlockAt(i, y + k, j).getType() == Material.AIR))
                        return false;
        return true;
    }

    public static boolean cannotBeChanged(@NonNull Block block, @NonNull Guild guild) {
        Location location = block.getLocation().subtract(guild.location);
        return Math.abs(location.getX()) <= guildRoomDistance && location.getY() >= -1 && location.getY() <= guildRoomDistance && Math.abs(location.getZ()) <= guildRoomDistance;
    }

    public static boolean cannotBeChanged(@NonNull Block block) {
        Guild guild = withinGuild(block);
        if(guild == null)
            return false;
        return cannotBeChanged(block, guild);
    }

    public static @Nullable Guild getGuild(@NonNull EnderCrystal heart) {
        return guilds.get(getGuildName(heart));
    }

    public static @NonNull String getGuildName(@NonNull EnderCrystal heart) {
        String name = heart.getName();
        return name.substring(2, name.length()-11-2*maxHpLength);
    }

    public static boolean isHeart(@NonNull Entity entity) {
        return entity.getType() == EntityType.ENDER_CRYSTAL && entity.getCustomName() != null && entity.getCustomName().startsWith("§");
    }

    public static @NonNull Vector round(@NonNull Location location) {
        Vector vector = new Vector();
        vector.setX(Math.floor(location.getX()));
        vector.setY(Math.floor(location.getY()));
        vector.setZ(Math.floor(location.getZ()));
        return vector;
    }

    public static boolean correctInventory(@NonNull Inventory inventory) {
        for(Map.Entry<Material, Integer> entry: items.entrySet())
            if(!inventory.contains(entry.getKey(), entry.getValue()))
                return false;

        return true;
    }

    public static boolean enemyPlayer(@NonNull Collection<Entity> entities, @Nullable Guild guild) {
        for(Entity entity: entities)
            if(entity instanceof Player && getGuild((Player) entity) != guild)
                return true;
        return false;
    }

    public static void teamCheck(@NonNull Player player, @Nullable Guild guild) {
        player.setScoreboard(scoreboard);
        if(guild != null)
            guild.addEntry(player);
        else
        {
            Team team = scoreboard.getEntryTeam(player.getName());
            if(team != null)
                team.removeEntry(player.getName());
        }
    }
}