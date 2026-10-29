package dev.killfeed;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Показывает панель при /ban, /kick, /pardon, /mute и т.д. (список в config.yml -> actions). */
public final class ActionListener implements Listener {

    private static final Pattern DURATION = Pattern.compile("(\\d+)([smhdw])");

    private final KillFeedPlugin plugin;

    public ActionListener(KillFeedPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        handle(event.getPlayer(), event.getMessage());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConsoleCommand(ServerCommandEvent event) {
        handle(event.getSender(), event.getCommand());
    }

    private void handle(CommandSender sender, String raw) {
        String line = raw.startsWith("/") ? raw.substring(1) : raw;
        String[] parts = line.trim().split("\\s+");
        if (parts.length < 2) {
            return;
        }

        String label = parts[0].toLowerCase(Locale.ROOT);
        int colon = label.indexOf(':');
        if (colon >= 0) {
            label = label.substring(colon + 1);
        }

        ConfigurationSection action = plugin.getConfig().getConfigurationSection("actions." + label);
        if (action == null) {
            return;
        }
        String permission = action.getString("permission");
        if (permission != null && !sender.hasPermission(permission)) {
            return;
        }

        String targetName = parts[1];
        int reasonStart = 2;
        Long millis = null;
        if (action.getBoolean("duration") && parts.length > 2) {
            millis = parseDuration(parts[2]);
            if (millis != null) {
                reasonStart = 3;
            }
        }

        String reason = parts.length > reasonStart
                ? String.join(" ", Arrays.copyOfRange(parts, reasonStart, parts.length))
                : action.getString("default-reason", "Не указана");

        String time;
        String remaining;
        if (millis != null) {
            time = formatDuration(millis);
            remaining = time;
        } else if (action.getBoolean("permanent")) {
            time = "НАВСЕГДА";
            remaining = "НАВСЕГДА";
        } else {
            time = "—";
            remaining = "—";
        }

        PlayerFormatter fmt = plugin.formatter();
        Component moderator = sender instanceof Player p ? fmt.format(p) : fmt.plain("Консоль");

        TagResolver resolver = TagResolver.resolver(
                Placeholder.unparsed("title", action.getString("title", label)),
                Placeholder.unparsed("date", fmt.now()),
                Placeholder.unparsed("time", time),
                Placeholder.unparsed("remaining", remaining),
                Placeholder.unparsed("reason", reason),
                Placeholder.component("moderator", moderator),
                Placeholder.component("target", fmt.formatName(targetName))
        );

        List<String> format = action.isList("format")
                ? action.getStringList("format")
                : plugin.getConfig().getStringList("default-action-format");

        Panel.broadcast(format, resolver);
    }

    private static Long parseDuration(String input) {
        Matcher m = DURATION.matcher(input.toLowerCase(Locale.ROOT));
        long total = 0;
        int end = 0;
        while (m.find()) {
            if (m.start() != end) {
                return null;
            }
            long n = Long.parseLong(m.group(1));
            total += n * switch (m.group(2)) {
                case "s" -> 1_000L;
                case "m" -> 60_000L;
                case "h" -> 3_600_000L;
                case "d" -> 86_400_000L;
                default -> 604_800_000L; // w
            };
            end = m.end();
        }
        return (end == input.length() && total > 0) ? total : null;
    }

    private static String formatDuration(long millis) {
        long s = millis / 1000;
        long d = s / 86400;
        long h = (s % 86400) / 3600;
        long m = (s % 3600) / 60;
        long sec = s % 60;
        StringBuilder sb = new StringBuilder();
        if (d > 0) sb.append(d).append("д ");
        if (h > 0) sb.append(h).append("ч ");
        if (m > 0) sb.append(m).append("м ");
        if (sec > 0 || sb.isEmpty()) sb.append(sec).append("с");
        return sb.toString().trim();
    }
}
