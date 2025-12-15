package org.throwable_Anvil.Throwable_Anvil.rideablePanda;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RideablePanda extends JavaPlugin {
    private Messages messages;
    private MovementController movementController;
    private final Map<UUID, Integer> tamingAttempts = new HashMap<>();
    private final Map<UUID, BukkitRunnable> activeRiders = new HashMap<>();
    private final Map<UUID, Boolean> controlEnabled = new HashMap<>();
    private final Map<UUID, Boolean> flightEnabled = new HashMap<>();
    private final Map<UUID, int[]> controlSlots = new HashMap<>();
    private NamespacedKey tamedByKey;

    // Custom control items
    private ItemStack createControlItem(Material mat, String name, String lore) {
        ItemStack item = new ItemStack(mat, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + name);
        meta.setLore(java.util.Arrays.asList(ChatColor.GRAY + lore));
        meta.setUnbreakable(true);
        meta.setCustomModelData(987654321); // mark as plugin item
        item.setItemMeta(meta);
        return item;
    }
    private ItemStack getControlToggleItem() {
        return createControlItem(Material.DIAMOND, "Panda Control", "Toggle manual control");
    }
    private ItemStack getFlightToggleItem() {
        return createControlItem(Material.EMERALD, "Panda Flight", "Toggle flight while riding");
    }

    @Override
    public void onEnable() {
        // Save default config if not exists
        saveDefaultConfig();
        
        // Initialize messages and movement controller
        messages = new Messages(getConfig());
        movementController = new MovementController();
        tamedByKey = new NamespacedKey(this, "tamed_by");
        
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
        public void onPandaDamage(EntityDamageEvent event) {
            if (!(event.getEntity() instanceof Panda)) return;
            Panda panda = (Panda) event.getEntity();
            
            // Cancel fall damage if panda has a rider
            if (!panda.getPassengers().isEmpty() && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
            if (!(event.getWhoClicked() instanceof Player)) return;
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.hasItemMeta() && clicked.getItemMeta().hasCustomModelData() && clicked.getItemMeta().getCustomModelData() == 987654321) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onControlToggle(PlayerInteractEvent event) {
            Player player = event.getPlayer();
            ItemStack item = event.getItem();
            if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasCustomModelData() || item.getItemMeta().getCustomModelData() != 987654321) return;

            String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            if ("Panda Control".equalsIgnoreCase(name)) {
                boolean newState = !controlEnabled.getOrDefault(player.getUniqueId(), false);
                controlEnabled.put(player.getUniqueId(), newState);
                player.sendMessage(newState ? ChatColor.GREEN + "Manual control enabled" : ChatColor.YELLOW + "AI mode enabled");
                event.setCancelled(true);
            } else if ("Panda Flight".equalsIgnoreCase(name)) {
                boolean newState = !flightEnabled.getOrDefault(player.getUniqueId(), false);
                flightEnabled.put(player.getUniqueId(), newState);
                player.sendMessage(newState ? ChatColor.GREEN + "Flight enabled" : ChatColor.YELLOW + "Flight disabled");
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onRightClickPanda(PlayerInteractEntityEvent event) {
            // Process only main hand to prevent duplicate firing
            try {
                if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
            } catch (NoSuchMethodError ignored) {}
            
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

            // Taming system (all players must tame, including ops)
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
                if (handItem.getAmount() > 0) {
                    handItem.setAmount(handItem.getAmount() - 1);
                }

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
                    // Success! Use PersistentDataContainer for persistent taming
                    panda.getPersistentDataContainer().set(tamedByKey, PersistentDataType.STRING, player.getUniqueId().toString());
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
            // Use PersistentDataContainer for persistent storage
            if (panda.getPersistentDataContainer().has(tamedByKey, PersistentDataType.STRING)) {
                String tamedBy = panda.getPersistentDataContainer().get(tamedByKey, PersistentDataType.STRING);
                return tamedBy != null && tamedBy.equals(player.getUniqueId().toString());
            }
            return false;
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
            // Place control items with a gap: diamond at slot 3 (shown as 4), emerald at slot 5 (shown as 6)
            PlayerInventory inv = player.getInventory();
            int slotDiamond = 3; // visual 4
            int slotEmerald = 5; // visual 6

            // Check if slots are free (null or AIR only)
            ItemStack existingDiamond = inv.getItem(slotDiamond);
            ItemStack existingEmerald = inv.getItem(slotEmerald);
            boolean diamondFree = (existingDiamond == null || existingDiamond.getType() == Material.AIR);
            boolean emeraldFree = (existingEmerald == null || existingEmerald.getType() == Material.AIR);
            
            if (!diamondFree || !emeraldFree) {
                player.sendMessage(ChatColor.RED + "Please clear hotbar slots 4 and 6 to mount the panda.");
                return;
            }

            // Give control items and track slots
            ItemStack controlItem = getControlToggleItem();
            ItemStack flightItem = getFlightToggleItem();
            inv.setItem(slotDiamond, controlItem);
            inv.setItem(slotEmerald, flightItem);
            controlSlots.put(player.getUniqueId(), new int[]{slotDiamond, slotEmerald});
            controlEnabled.put(player.getUniqueId(), false); // Start with AI mode on
            flightEnabled.put(player.getUniqueId(), false); // Start with flight disabled

            panda.addPassenger(player);

            startControlTask(player, panda);
        }

        @EventHandler
        public void onDismount(EntityDismountEvent event) {
            if (event.getEntity() instanceof Player && event.getDismounted() instanceof Panda) {
                Player player = (Player) event.getEntity();
                Panda panda = (Panda) event.getDismounted();
                UUID playerId = player.getUniqueId();
                
                BukkitRunnable task = activeRiders.remove(playerId);
                if (task != null) {
                    task.cancel();
                }
                
                movementController.cleanup(playerId);
                // Remove control items and states
                int[] slots = controlSlots.remove(playerId);
                if (slots != null) {
                    for (int s : slots) {
                        ItemStack it = player.getInventory().getItem(s);
                        if (it != null && it.hasItemMeta() && it.getItemMeta().hasCustomModelData() && it.getItemMeta().getCustomModelData() == 987654321) {
                            player.getInventory().setItem(s, null);
                        }
                    }
                }
                controlEnabled.remove(playerId);
                flightEnabled.remove(playerId);
                
                // Remove potion effects from panda and player
                panda.removePotionEffect(PotionEffectType.LEVITATION);
                panda.removePotionEffect(PotionEffectType.SLOW_FALLING);
                player.removePotionEffect(PotionEffectType.SLOW_FALLING);
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

                    // Determine manual/flight state
                    Location pl = player.getLocation();
                    boolean manual = controlEnabled.getOrDefault(player.getUniqueId(), false);
                    // Flight enabled if: player toggled emerald AND has permission
                    boolean flightToggled = flightEnabled.getOrDefault(player.getUniqueId(), false);
                    boolean flight = flightToggled && player.hasPermission("rideablepanda.fly");

                    if (!manual) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 10, 0, true, false, false));
                        return;
                    }

                    // Sync panda rotation to player view (only when manual control is enabled)
                    panda.setRotation(pl.getYaw(), pl.getPitch());

                    // Step-up detection for 1-block obstacles ahead (only when not flying)
                    if (!flight) {
                        Vector dir = pl.getDirection().clone().setY(0).normalize();
                        Location ahead = panda.getLocation().add(dir.multiply(0.8));
                        Block aheadBlock = ahead.getBlock();
                        Block aboveAhead = aheadBlock.getRelative(BlockFace.UP);
                        boolean obstacle = aheadBlock.getType().isSolid();
                        boolean canStepUp = !aboveAhead.getType().isSolid();

                        if (obstacle && canStepUp && panda.isOnGround()) {
                            Vector v = panda.getVelocity();
                            v.setY(Math.max(v.getY(), jumpPower));
                            panda.setVelocity(v);
                        }
                    }

                    // Handle movement using the controller (flight depends on item toggle)
                    movementController.handleMovement(player, panda, speed, jumpPower, flight, flyingSpeed);

                    // Apply effects for flight or slow falling
                    if (flight) {
                        // Give panda levitation when flying - continuous application for sustained flight
                        float pitch = pl.getPitch();
                        if (pitch < -10) {
                            // Looking up: strong levitation for ascending
                            panda.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 5, false, false, false));
                        } else if (pitch > 30) {
                            // Looking down: remove levitation, add slow falling for safe descent
                            panda.removePotionEffect(PotionEffectType.LEVITATION);
                            panda.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20, 1, false, false, false));
                        } else {
                            // Level flight: moderate levitation to maintain altitude
                            panda.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 2, false, false, false));
                        }
                    } else {
                        // Not flying: remove levitation
                        panda.removePotionEffect(PotionEffectType.LEVITATION);
                    }
                    
                    // Apply slow falling to player while riding
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 10, 0, true, false, false));
                }
            };

            task.runTaskTimer(plugin, 0L, 1L);
            activeRiders.put(player.getUniqueId(), task);
        }
    }
}
