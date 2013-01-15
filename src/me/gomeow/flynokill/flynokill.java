/*
 * Made by Gomeow
 * Please don't steak this idea!
 * It isn't proper!
 * Thanks for looking!
 */


package me.gomeow.flynokill;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

public class flynokill extends JavaPlugin implements Listener {

	boolean disableFlyOnHit = false;
	Integer cooldown = null;
	ArrayList<String> cooldownPlayers = new ArrayList<String>();
	
	@Override
	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this, this);
		disableFlyOnHit = this.getConfig().getBoolean("Disable-Fly-On-Hit", false);
		if(disableFlyOnHit) {
			cooldown = this.getConfig().getInt("Cooldown", 5)*20;
		}
		saveDefaultConfig();
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {
		Entity damager = event.getDamager();
		Entity damaged = event.getEntity();
		if (damager instanceof Player) {
			Player damager1 = (Player) event.getDamager();
			if (damager1.isOp() == false) {
				if (damager1.hasPermission("flynokill.bypass") == false) {
					if (damager1.isFlying()) {
						event.setCancelled(true);
						damager1.sendMessage(ChatColor.RED+"You cannot fight while flying!");
					}
				}

			}
			
		} else if (damager instanceof Arrow) {
			Arrow arrow = (Arrow) event.getDamager();
			if (arrow.getShooter() instanceof Player) {
				Player shooter = (Player) arrow.getShooter();
				if (shooter.isOp() == false) {
					if (shooter.hasPermission("flynokill.bypass") == false) {
						if (shooter.isFlying()) {
							event.setDamage(0);
							shooter.sendMessage(ChatColor.RED+"You cannot fight while flying!");
							
						}
					}
				}
			}
		}

		if(disableFlyOnHit) {
			if(damager instanceof EnderPearl) return;
			if(!(damaged instanceof Player)) return;
			final Player damaged1 = (Player) damaged;
			if(damaged1.hasPermission("flynokill.bypass")) {
				return;
			}
			if(!damaged1.isFlying()) {
				return;
			}
			if(!cooldownPlayers.contains(((Player) damaged).getName())) {
				cooldownPlayers.add(((Player) damaged).getName());
				damaged1.setFlying(false);
				Integer cooldownSecs = cooldown/20;
				damaged1.sendMessage(ChatColor.DARK_RED+"You can't fly for "+cooldownSecs.toString()+" seconds!");
				Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					
					@Override
					public void run() {
						cooldownPlayers.remove(damaged1.getName());
					}
					
				}, cooldown);
			}
		}
	}
	
	@EventHandler
	public void onThrow(ProjectileLaunchEvent event) {
		if(event.getEntity() instanceof ThrownPotion) {
			ThrownPotion potion = (ThrownPotion) event.getEntity();
			if(potion.getShooter() instanceof Player) {
				Player p = (Player) potion.getShooter();
				if(!p.hasPermission("flynokill.bypass")) {
					Collection<PotionEffect> effects = potion.getEffects();
					if(p.isFlying()) {
						boolean contains = false;
						for(PotionEffect pe : effects) {
							if(pe.getType().getId() == 6
							|| pe.getType().getId() == 7
							|| pe.getType().getId() == 17
							|| pe.getType().getId() == 18
							|| pe.getType().getId() == 19
							|| pe.getType().getId() == 2
							|| pe.getType().getId() == 9
							|| pe.getType().getId() == 15) {
								contains = true;
								break;
							}
						}
						if(contains) {
							event.setCancelled(true);
							p.sendMessage(ChatColor.RED+"You cannot fight while flying!");
						}
					}
				}
			}
		}
	}
	
	
	@EventHandler
	public void onFlyChange(PlayerToggleFlightEvent event) {
		Player flyChanger = event.getPlayer();
		if(cooldownPlayers.contains(flyChanger.getName())) {
			event.setCancelled(true);
		}
	}
}
