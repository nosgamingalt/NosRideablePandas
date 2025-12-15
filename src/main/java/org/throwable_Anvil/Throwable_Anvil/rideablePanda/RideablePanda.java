package org.throwable_Anvil.Throwable_Anvil.rideablePanda;

import org.bukkit.Material;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RideablePanda extends JavaPlugin {
    private Messages messages;
    private MovementController movementController;
    private final Map<UUID, Integer> tamingAttempts = new HashMap<>();
    private final Map<UUID, BukkitRunnable> activeRiders = new HashMap<>();

    @Override
    public void onEnable() {
        // Save default config if not exists
        saveDefaultConfig();
        
        // Initialize messages and movement controller
        messages = new Messages(getConfig());
        movementController = new MovementController();
        
        // Register events
        getServer().getPluginManager().registerEvents(new PandaListener(this), this);
        
        getLogger().info("RideablePanda v2.0 enabled!");
    }

    @Override
    public void onDisable() {
        // Cancel all active riding tasks
        for (BukkitRunnable task : activeRiders.values()) {
            task.cancel();
        }
        activeRiders.clear();
        tamingAttempts.clear();
        
        getLogger().info("RideablePanda v2.0 disabled!");
    }

    public Messages getMessages() {
        return messages;
    }

    public class PandaListener implements Listener {
        private final RideablePanda plugin;

        public PandaListener(RideablePanda plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onRightClickPanda(PlayerInteractEntityEvent event) {
            if (!(event.getRightClicked() instanceof Panda)) return;
            Panda panda = (Panda) event.getRightClicked();
            Player player = event.getPlayer();

            // Check if panda is adult
            if (!panda.isAdult()) {
                player.sendMessage(messages.get("not-adult"));
                return;
            }

            // Check basic permission
            if (!player.hasPermission("rideablepanda.ride") && !player.hasPermission("rideablepanda.instant")) {
                player.sendMessage(messages.get("no-permission"));
                return;
            }

            ItemStack handItem = player.getInventory().getItemInMainHand();
            Material foodItem = Material.valueOf(getConfig().getString("taming.food-item", "BAMBOO"));

            // Admin instant mount
            if (player.hasPermission("rideablepanda.instant")) {
                handleMount(player, panda, event);
                return;
            }

            // Taming system
            if (getConfig().getBoolean("taming.enabled", true)) {
                // Check if holding correct food
                if (handItem.getType() != foodItem) {
                    // Try to mount if already tamed
                    if (isPandaTamedBy(panda, player)) {
                        handleMount(player, panda, event);
                    } else {
                        player.sendMessage(messages.get("wrong-food"));
                    }
                    return;
                }

                // Already tamed by this player
                if (isPandaTamedBy(panda, player)) {
                    player.sendMessage(messages.get("already-tamed"));
                    handleMount(player, panda, event);
                    return;
                }

                // Taming attempt
                event.setCancelled(true);
                handItem.setAmount(handItem.getAmount() - 1);

                UUID pandaId = panda.getUniqueId();
                int attempts = tamingAttempts.getOrDefault(pandaId, 0) + 1;
                tamingAttempts.put(pandaId, attempts);

                int baseChance = getConfig().getInt("taming.base-chance", 30);
                int maxAttempts = getConfig().getInt("taming.max-attempts", 10);
                
                // Calculate success with increasing chance
                double successChance = baseChance + (attempts * 5.0);
                if (maxAttempts > 0 && attempts >= maxAttempts) {
                    successChance = 100; // Guaranteed success
                }

                if (Math.random() * 100 < successChance) {
                    // Success!
                    panda.setMetadata("tamed_by", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
                    tamingAttempts.remove(pandaId);
                    player.sendMessage(messages.get("tamed-success"));
                    handleMount(player, panda, event);
                } else {
                    // Failed
                    player.sendMessage(messages.get("taming-failed"));
                    if (maxAttempts > 0) {
                        player.sendMessage(messages.get("attempts-remaining", 
                            "attempts", String.valueOf(attempts),
                            "max", String.valueOf(maxAttempts)));
                    }
                }
            } else {
                // No taming required
                handleMount(player, panda, event);
            }
        }

        private boolean isPandaTamedBy(Panda panda, Player player) {
            if (!panda.hasMetadata("tamed_by")) return false;
            String tamedBy = panda.getMetadata("tamed_by").get(0).asString();
            return tamedBy.equals(player.getUniqueId().toString());
        }

        private void handleMount(Player player, Panda panda, PlayerInteractEntityEvent event) {
            // Check saddle requirement
            if (getConfig().getBoolean("general.require-saddle", false)) {
                ItemStack handItem = player.getInventory().getItemInMainHand();
                if (handItem.getType() != Material.SADDLE) {
                    return;
                }
                if (getConfig().getBoolean("general.consume-saddle", false)) {
                    handItem.setAmount(handItem.getAmount() - 1);
                }
            }

            event.setCancelled(true);
            panda.addPassenger(player);
            player.sendMessage(messages.get("mount-success"));

            startControlTask(player, panda);
        }

        @EventHandler
        public void onDismount(EntityDismountEvent event) {
            if (event.getEntity() instanceof Player && event.getDismounted() instanceof Panda) {
                Player player = (Player) event.getEntity();
                UUID playerId = player.getUniqueId();
                
                BukkitRunnable task = activeRiders.remove(playerId);
                if (task != null) {
                    task.cancel();
                }
                
                movementController.cleanup(playerId);
                player.sendMessage(messages.get("dismount"));
            }
        }

        private void startControlTask(Player player, Panda panda) {
            // Cancel existing task if any
            BukkitRunnable existingTask = activeRiders.get(player.getUniqueId());
            if (existingTask != null) {
                existingTask.cancel();
            }

            double speed = getConfig().getDouble("controls.speed", 0.35);
            double jumpPower = getConfig().getDouble("controls.jump-power", 0.6);
            boolean allowFlying = getConfig().getBoolean("controls.allow-flying", false);
            double flyingSpeed = getConfig().getDouble("controls.flying-speed", 0.5);

            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!panda.getPassengers().contains(player) || !player.isOnline()) {
                        cancel();
                        activeRiders.remove(player.getUniqueId());
                        movementController.cleanup(player.getUniqueId());
                        return;
                    }

                    // Dismount on sneak
                    if (player.isSneaking()) {
                        panda.removePassenger(player);
                        cancel();
                        activeRiders.remove(player.getUniqueId());
                        movementController.cleanup(player.getUniqueId());
                        return;
                    }

                    // Handle movement using the controller
                    movementController.handleMovement(player, panda, speed, jumpPower, 
                                                     allowFlying, flyingSpeed);
                }
            };

            task.runTaskTimer(plugin, 0L, 1L);
            activeRiders.put(player.getUniqueId(), task);
        }
    }
}
