package me.petterim1.bedfixvanilla;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockBed;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.player.PlayerLoginEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import cn.nukkit.plugin.PluginBase;

import java.lang.reflect.Field;

public class Main extends PluginBase implements Listener {

    public static Main plugin;

    private Field spawnPosition;

    public void onEnable() {
        plugin = this;
        try {
            spawnPosition = Player.class.getDeclaredField("spawnPosition");
            spawnPosition.setAccessible(true);
        } catch (NoSuchFieldException ex) {
            getLogger().error("Failed to get spawnPoint field in Player", ex);
            return;
        }
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(ignoreCancelled = true)
    public void bedBreakListener(BlockBreakEvent e) {
        if (e.getBlock() instanceof BlockBed) {
            Player p = e.getPlayer();
            if (p.getSpawn().distance(e.getBlock()) < 2) {
                p.setSpawn(getServer().getDefaultLevel().getSpawnLocation());
            }
        }
    }

    @EventHandler
    public void playerLoginEvent(PlayerLoginEvent e) {
        Player p = e.getPlayer();
        Location bed = locationFromSavedData(p);
        if (bed != null) {
            if (bed.getLevel().getBlockIdAt(bed.getFloorX(), bed.getFloorY(), bed.getFloorZ()) == Block.BED_BLOCK && bed.level.getDimension() == Level.DIMENSION_OVERWORLD) {
                setSpawnDirectly(p, bed);
            } else {
                setSpawnDirectly(p, getServer().getDefaultLevel().getSpawnLocation());
            }
        }
    }

    private Location locationFromSavedData(Player p) {
        Level level = getServer().getLevelByName(p.namedTag.getString("SpawnLevel"));
        if (level != null) {
            return new Location(p.namedTag.getInt("SpawnX"), p.namedTag.getInt("SpawnY"), p.namedTag.getInt("SpawnZ"), level);
        }
        return null;
    }

    private void setSpawnDirectly(Player p, Vector3 spawn) {
        try {
            spawnPosition.set(p, spawn);
        } catch (Exception ex) {
            getLogger().error("Setting spawnPosition failed for " + p.getName(), ex);
        }
    }
}
