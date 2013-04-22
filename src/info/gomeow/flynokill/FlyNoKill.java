package info.gomeow.flynokill;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Bukkit;
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

public class FlyNoKill extends JavaPlugin implements Listener {
    boolean disableFlyOnHit = false;
    Integer cooldown = null;
    ArrayList<String> cooldownPlayers = new ArrayList<String>();

    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        this.disableFlyOnHit = getConfig().getBoolean("Disable-Fly-On-Hit", false);
        if(this.disableFlyOnHit) {
            this.cooldown = Integer.valueOf(getConfig().getInt("Cooldown", 5) * 20);
        }
        saveDefaultConfig();
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity damagedEnt = event.getEntity();
        if((damager instanceof Player)) {
            Player damager1 = (Player) event.getDamager();
            if((!damager1.isOp()) && (!damager1.hasPermission("flynokill.bypass")) && (damager1.isFlying())) {
                event.setCancelled(true);
                damager1.sendMessage(ChatColor.RED + "You cannot fight while flying!");
            }

        }
        else if((damager instanceof Arrow)) {
            Arrow arrow = (Arrow) event.getDamager();
            if((arrow.getShooter() instanceof Player)) {
                Player shooter = (Player) arrow.getShooter();
                if((!shooter.isOp()) && (!shooter.hasPermission("flynokill.bypass")) && (shooter.isFlying())) {
                    event.setDamage(0);
                    shooter.sendMessage(ChatColor.RED + "You cannot fight while flying!");
                }

            }

        }

        if(this.disableFlyOnHit) {
            if(damagedEnt instanceof Player) {
                final Player damaged = (Player) damagedEnt;
                if(!damaged.hasPermission("flynokill.bypass")) {
                    if(damaged.isFlying()) {
                        if(!this.cooldownPlayers.contains(damaged.getName())) {
                            this.cooldownPlayers.add(damaged.getName());
                            damaged.setFlying(false);
                            Integer cooldownSecs = Integer.valueOf(this.cooldown.intValue() / 20);
                            damaged.sendMessage(ChatColor.DARK_RED + "You can't fly for " + cooldownSecs.toString() + " seconds!");
                            Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

                                public void run() {
                                    cooldownPlayers.remove(damaged.getName());
                                }

                            }, this.cooldown.intValue());
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
                        boolean contains = false;
                        for(PotionEffect pe:effects) {
                            if((pe.getType().getId() != 6) &&
                                    (pe.getType().getId() != 7) &&
                                    (pe.getType().getId() != 17) &&
                                    (pe.getType().getId() != 18) &&
                                    (pe.getType().getId() != 19) &&
                                    (pe.getType().getId() != 2) &&
                                    (pe.getType().getId() != 9) &&
                                    (pe.getType().getId() != 15)) continue;
                            contains = true;
                            break;
                        }
                        if(contains) {
                            event.setCancelled(true);
                            p.sendMessage(ChatColor.RED + "You cannot fight while flying!");
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
        Player flyChanger = event.getPlayer();
        if(this.cooldownPlayers.contains(flyChanger.getName()))
            event.setCancelled(true);
    }
}