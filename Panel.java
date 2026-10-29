package dev.killfeed;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;

import java.util.List;

public final class Panel {

    private Panel() {}

    public static Component build(List<String> lines, TagResolver resolver) {
        MiniMessage mm = MiniMessage.miniMessage();
        Component out = Component.empty();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                out = out.append(Component.newline());
            }
            out = out.append(mm.deserialize(lines.get(i), resolver));
        }
        return out;
    }

    public static void broadcast(List<String> lines, TagResolver resolver) {
        Bukkit.broadcast(build(lines, resolver));
    }
}
