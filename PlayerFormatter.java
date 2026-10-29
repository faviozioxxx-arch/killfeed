package dev.killfeed;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.object.ObjectContents;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Собирает компонент вида: [голова] [РАНГ] Ник */
public final class PlayerFormatter {

    private final KillFeedPlugin plugin;

    public PlayerFormatter(KillFeedPlugin plugin) {
        this.plugin = plugin;
    }

    public Component format(Player player) {
        return build(player.getUniqueId(), player.getName(), badge(player));
    }

    /** По нику: если игрок онлайн - с рангом, иначе только голова и ник. */
    public Component formatName(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return format(online);
        }
        return build(null, name, null);
    }

    /** Для консоли / без головы. */
    public Component plain(String name) {
        return Component.text(name, color());
    }

    public String now() {
        String pattern = plugin.getConfig().getString("date-format", "yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
    }

    private Component build(UUID uuid, String name, Component badge) {
        FileConfiguration cfg = plugin.getConfig();
        Component out = Component.empty();

        if (cfg.getBoolean("show-heads", true)) {
            ObjectContents head = uuid != null
                    ? ObjectContents.playerHead(uuid)
                    : ObjectContents.playerHead(name);
            out = out.append(Component.object(head)).append(Component.space());
        }
        if (badge != null) {
            out = out.append(badge).append(Component.space());
        }
        return out.append(Component.text(name, color()));
    }

    private Component badge(Player player) {
        List<Map<?, ?>> ranks = plugin.getConfig().getMapList("ranks");
        for (Map<?, ?> rank : ranks) {
            Object perm = rank.get("permission");
            Object badge = rank.get("badge");
            if (perm != null && badge != null && player.hasPermission(perm.toString())) {
                return MiniMessage.miniMessage().deserialize(badge.toString());
            }
        }
        return null;
    }

    private TextColor color() {
        TextColor c = TextColor.fromHexString(plugin.getConfig().getString("name-color", "#ff5555"));
        return c != null ? c : TextColor.color(0xff5555);
    }
}
