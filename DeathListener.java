package dev.killfeed;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public final class DeathListener implements Listener {

    private final KillFeedPlugin plugin;

    public DeathListener(KillFeedPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        if (!plugin.getConfig().getBoolean("kill.enabled", true)) {
            return;
        }

        PlayerFormatter fmt = plugin.formatter();
        Player victim = event.getEntity();
        Player killerPlayer = victim.getKiller();
        EntityDamageEvent last = victim.getLastDamageCause();

        Component killer;
        Component weapon = Component.text("—");
        Component reason;

        if (killerPlayer != null) {
            killer = fmt.format(killerPlayer);
            ItemStack hand = killerPlayer.getInventory().getItemInMainHand();
            weapon = hand.getType().isAir() ? Component.text("Кулак") : hand.effectiveName();
            reason = Component.text("Убийство игрока");
        } else if (last instanceof EntityDamageByEntityEvent byEntity) {
            Entity damager = byEntity.getDamager();
            if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter) {
                damager = shooter;
            }
            killer = damager.name();
            reason = Component.text("Убит существом");
        } else {
            killer = Component.text("—");
            reason = Component.text(causeName(last));
        }

        TagResolver resolver = TagResolver.resolver(
                Placeholder.unparsed("date", fmt.now()),
                Placeholder.component("killer", killer),
                Placeholder.component("victim", fmt.format(victim)),
                Placeholder.component("weapon", weapon),
                Placeholder.component("reason", reason)
        );

        if (plugin.getConfig().getBoolean("kill.hide-vanilla-message", true)) {
            event.deathMessage(null);
        }
        Panel.broadcast(plugin.getConfig().getStringList("kill.format"), resolver);
    }

    private static String causeName(EntityDamageEvent last) {
        if (last == null) {
            return "Неизвестно";
        }
        return switch (last.getCause()) {
            case FALL -> "Падение";
            case FIRE, FIRE_TICK -> "Огонь";
            case LAVA -> "Лава";
            case DROWNING -> "Утопление";
            case SUFFOCATION -> "Удушье";
            case VOID -> "Бездна";
            case STARVATION -> "Голод";
            case POISON -> "Яд";
            case WITHER -> "Иссушение";
            case MAGIC -> "Магия";
            case LIGHTNING -> "Молния";
            case BLOCK_EXPLOSION, ENTITY_EXPLOSION -> "Взрыв";
            case CONTACT -> "Кактус / куст";
            case FREEZE -> "Замерзание";
            case FLY_INTO_WALL -> "Удар об стену";
            case HOT_FLOOR -> "Горячий пол";
            case FALLING_BLOCK -> "Упавший блок";
            case THORNS -> "Шипы";
            case DRAGON_BREATH -> "Дыхание дракона";
            case KILL, SUICIDE -> "Самоубийство";
            default -> "Неизвестно";
        };
    }
}
