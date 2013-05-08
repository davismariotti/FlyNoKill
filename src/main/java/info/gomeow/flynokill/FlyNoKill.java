package info.gomeow.flynokill;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

public class FlyNoKill extends JavaPlugin implements Listener {

    private boolean disableFlyOnHit = false;
    private int cooldown = 5;
    private ArrayList<String> cooldownPlayers = new ArrayList<String>();

    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        if(disableFlyOnHit = getConfig().getBoolean("Disable-Fly-On-Hit", false)) {
            cooldown = getConfig().getInt("Cooldown", 5) * 20;
        }
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if(event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            if(!damager.hasPermission("flynokill.bypass") && damager.isFlying()) {
                event.setCancelled(true);
                damager.sendMessage(ChatColor.RED + "You cannot fight while flying!");
            }

        }
        else if(event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            if((arrow.getShooter() instanceof Player)) {
                Player shooter = (Player) arrow.getShooter();
                if(!shooter.hasPermission("flynokill.bypass") && shooter.isFlying()) {
                    event.setDamage(0);
                    shooter.sendMessage(ChatColor.RED + "You cannot fight while flying!");
                }

            }

        }

        if(disableFlyOnHit) {
            if(entity instanceof Player) {
                final Player damaged = (Player) entity;
                if(!damaged.hasPermission("flynokill.bypass")) {
                    if(damaged.isFlying()) {
                        if(!cooldownPlayers.contains(damaged.getName())) {
                            cooldownPlayers.add(damaged.getName());
                            damaged.setFlying(false);
                            damaged.sendMessage(ChatColor.DARK_RED + "You can't fly for " + (cooldown / 20) + " seconds!");
                            new BukkitRunnable() {

                                @Override
                                public void run() {
                                    cooldownPlayers.remove(damaged.getName());
                                }

                            }.runTaskLater(this, cooldown);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onThrow(ProjectileLaunchEvent event) {
        if((event.getEntity() instanceof ThrownPotion)) {
            ThrownPotion potion = (ThrownPotion) event.getEntity();
            if((potion.getShooter() instanceof Player)) {
                Player p = (Player) potion.getShooter();
                if(!p.hasPermission("flynokill.bypass")) {
                    Collection<PotionEffect> effects = potion.getEffects();
                    if(p.isFlying()) {
                        for(PotionEffect pe:effects) {
                            if((pe.getType().getId() == 6) || (pe.getType().getId() == 7) || (pe.getType().getId() == 17) || (pe.getType().getId() == 18) || (pe.getType().getId() == 19) || (pe.getType().getId() == 2) || (pe.getType().getId() == 9) || (pe.getType().getId() == 15)) {
                                event.setCancelled(true);
                                p.sendMessage(ChatColor.RED + "You cannot fight while flying!");
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if(p.getItemInHand().getType() == Material.FLINT_AND_STEEL) {
            if(!p.hasPermission("flynokill.bypass")) {
                if(p.isFlying()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onFlyChange(PlayerToggleFlightEvent event) {
        Player p = event.getPlayer();
        if(cooldownPlayers.contains(p.getName()))
            event.setCancelled(true);
    }
}