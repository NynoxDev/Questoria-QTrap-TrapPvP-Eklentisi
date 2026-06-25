package me.questoria.qtrap.hologram;

import me.questoria.qtrap.QTrapPlugin;
import me.questoria.qtrap.config.MessageManager;
import me.questoria.qtrap.model.TrapChunk;
import me.questoria.qtrap.model.TrapModel;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class HologramManager {
    private final QTrapPlugin plugin;
    private Class<?> dhApiClass;
    private boolean available;

    public HologramManager(QTrapPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        available = false;
        dhApiClass = null;
        if (!plugin.getConfig().getBoolean("holograms.enabled", true)) {
            return;
        }
        if (!plugin.getServer().getPluginManager().isPluginEnabled("DecentHolograms")) {
            plugin.getLogger().info("DecentHolograms bulunamadi, trap hologramlari devre disi.");
            return;
        }
        try {
            dhApiClass = Class.forName("eu.decentsoftware.holograms.api.DHAPI");
            available = true;
            plugin.getLogger().info("DecentHolograms baglantisi aktif.");
        } catch (ClassNotFoundException exception) {
            plugin.getLogger().warning("DecentHolograms API bulunamadi, hologramlar devre disi.");
        }
    }

    public void updateAll() {
        if (!available) {
            return;
        }
        for (TrapModel trap : plugin.traps().traps()) {
            update(trap);
        }
    }

    public void deleteAll() {
        if (!available) {
            return;
        }
        for (TrapModel trap : plugin.traps().traps()) {
            delete(trap);
        }
    }

    public void update(TrapModel trap) {
        if (!available || trap == null || trap.chunks().isEmpty()) {
            return;
        }
        Location location = hologramLocation(trap);
        if (location == null) {
            return;
        }
        List<String> lines = lines(trap);
        String name = hologramName(trap);
        try {
            Object hologram = invokeStatic("getHologram", name);
            if (hologram == null) {
                invokeStatic("createHologram", name, location, lines);
            } else {
                Method setLines = findMethod("setHologramLines", hologram, lines);
                if (setLines != null) {
                    setLines.invoke(null, hologram, lines);
                } else {
                    setLines = findMethod("setHologramLines", name, lines);
                    if (setLines != null) {
                        setLines.invoke(null, name, lines);
                    }
                }
                Method move = findMethod("moveHologram", hologram, location);
                if (move != null) {
                    move.invoke(null, hologram, location);
                } else {
                    move = findMethod("moveHologram", name, location);
                    if (move != null) {
                        move.invoke(null, name, location);
                    }
                }
            }
        } catch (ReflectiveOperationException | RuntimeException exception) {
            plugin.getLogger().warning("Trap hologrami guncellenemedi (" + trap.id() + "): " + exception.getMessage());
        }
    }

    public void delete(TrapModel trap) {
        if (!available || trap == null) {
            return;
        }
        String name = hologramName(trap);
        try {
            Method removeByName = findMethod("removeHologram", name);
            if (removeByName != null) {
                removeByName.invoke(null, name);
                return;
            }
            Method deleteByName = findMethod("deleteHologram", name);
            if (deleteByName != null) {
                deleteByName.invoke(null, name);
                return;
            }
            Object hologram = invokeStatic("getHologram", name);
            if (hologram != null) {
                Method delete = hologram.getClass().getMethod("delete");
                delete.invoke(hologram);
            }
        } catch (ReflectiveOperationException | RuntimeException exception) {
            plugin.getLogger().warning("Trap hologrami silinemedi (" + trap.id() + "): " + exception.getMessage());
        }
    }

    private Object invokeStatic(String methodName, Object... args) throws ReflectiveOperationException {
        Method method = findMethod(methodName, args);
        if (method == null) {
            throw new NoSuchMethodException(methodName);
        }
        return method.invoke(null, args);
    }

    private Method findMethod(String name, Object... args) {
        if (dhApiClass == null) {
            return null;
        }
        for (Method method : dhApiClass.getMethods()) {
            if (method.getName().equals(name) && accepts(method.getParameterTypes(), args)) {
                return method;
            }
        }
        return null;
    }

    private boolean accepts(Class<?>[] parameterTypes, Object[] args) {
        if (parameterTypes.length != args.length) {
            return false;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            if (args[i] == null) {
                continue;
            }
            if (!wrap(parameterTypes[i]).isAssignableFrom(args[i].getClass())) {
                return false;
            }
        }
        return true;
    }

    private Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        if (type == char.class) return Character.class;
        return Void.class;
    }

    private Location hologramLocation(TrapModel trap) {
        TrapChunk first = trap.chunks().iterator().next();
        World world = Bukkit.getWorld(first.world());
        if (world == null) {
            return null;
        }

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (TrapChunk chunk : trap.chunks()) {
            if (!chunk.world().equals(first.world())) {
                continue;
            }
            minX = Math.min(minX, chunk.x() * 16);
            maxX = Math.max(maxX, chunk.x() * 16 + 15);
            minZ = Math.min(minZ, chunk.z() * 16);
            maxZ = Math.max(maxZ, chunk.z() * 16 + 15);
        }

        double centerX = (minX + maxX) / 2.0D + 0.5D;
        double centerZ = (minZ + maxZ) / 2.0D + 0.5D;
        double frontOffset = plugin.getConfig().getDouble("holograms.front-offset", 1.5D);
        float yaw = trap.spawn() == null ? 0F : trap.spawn().getYaw();
        int face = Math.floorMod(Math.round(yaw / 90F), 4);

        double x = centerX;
        double z = centerZ;
        switch (face) {
            case 0 -> z = maxZ + 0.5D + frontOffset; // south
            case 1 -> x = minX - 0.5D - frontOffset; // west
            case 2 -> z = minZ - 0.5D - frontOffset; // north
            case 3 -> x = maxX + 0.5D + frontOffset; // east
            default -> {
            }
        }

        double y = world.getHighestBlockYAt((int) x, (int) z) + plugin.getConfig().getDouble("holograms.y-offset", 3.2);
        return new Location(world, x, y, z);
    }

    private List<String> lines(TrapModel trap) {
        Map<String, String> placeholders = MessageManager.placeholders(
                "%id%", trap.id(),
                "%owner%", ownerName(trap),
                "%health%", trap.health(),
                "%max_health%", trap.maxHealth(),
                "%bank%", plugin.vault().format(trap.bankBalance()),
                "%level%", trap.level(),
                "%name%", trap.name()
        );
        List<String> output = new ArrayList<>();
        for (String line : plugin.getConfig().getStringList("holograms.lines")) {
            String applied = MessageManager.apply(line, placeholders);
            if (plugin.getConfig().getBoolean("holograms.small-font", true)) {
                applied = smallFont(applied);
            }
            output.add(MessageManager.color(applied));
        }
        return output;
    }

    private String smallFont(String input) {
        StringBuilder output = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);
            if ((current == '&' || current == '§') && i + 1 < input.length()) {
                output.append(current).append(input.charAt(++i));
                continue;
            }
            output.append(smallChar(current));
        }
        return output.toString();
    }

    private char smallChar(char character) {
        return switch (Character.toLowerCase(character)) {
            case 'a' -> 'ᴀ';
            case 'b' -> 'ʙ';
            case 'c' -> 'ᴄ';
            case 'd' -> 'ᴅ';
            case 'e' -> 'ᴇ';
            case 'f' -> 'ꜰ';
            case 'g' -> 'ɢ';
            case 'h' -> 'ʜ';
            case 'i' -> 'ɪ';
            case 'j' -> 'ᴊ';
            case 'k' -> 'ᴋ';
            case 'l' -> 'ʟ';
            case 'm' -> 'ᴍ';
            case 'n' -> 'ɴ';
            case 'o' -> 'ᴏ';
            case 'p' -> 'ᴘ';
            case 'q' -> 'ǫ';
            case 'r' -> 'ʀ';
            case 's' -> 'ꜱ';
            case 't' -> 'ᴛ';
            case 'u' -> 'ᴜ';
            case 'v' -> 'ᴠ';
            case 'w' -> 'ᴡ';
            case 'x' -> 'x';
            case 'y' -> 'ʏ';
            case 'z' -> 'ᴢ';
            default -> character;
        };
    }

    private String ownerName(TrapModel trap) {
        if (trap.owner() == null) {
            return "Yok";
        }
        OfflinePlayer owner = Bukkit.getOfflinePlayer(trap.owner());
        return owner.getName() == null ? trap.owner().toString().substring(0, 8) : owner.getName();
    }

    private String hologramName(TrapModel trap) {
        return plugin.getConfig().getString("holograms.name-prefix", "qtrap_") + trap.id();
    }
}
