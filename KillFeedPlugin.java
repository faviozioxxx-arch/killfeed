package dev.killfeed;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class KillFeedPlugin extends JavaPlugin {

    private PlayerFormatter formatter;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.formatter = new PlayerFormatter(this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new ActionListener(this), this);
    }

    public PlayerFormatter formatter() {
        return formatter;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            sender.sendRichMessage("<green>KillFeed: конфиг перезагружен.");
            return true;
        }
        sender.sendRichMessage("<gray>/killfeed reload");
        return true;
    }
}
