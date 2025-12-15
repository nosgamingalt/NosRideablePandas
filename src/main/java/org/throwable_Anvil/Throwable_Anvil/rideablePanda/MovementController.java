package org.throwable_Anvil.Throwable_Anvil.rideablePanda;

import org.bukkit.Location;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MovementController {
    private final Map<UUID, Location> lastLocations = new HashMap<>();
    private final Map<UUID, Long> lastJumpTime = new HashMap<>();
    private final Map<UUID, Boolean> flyingMode = new HashMap<>();

    public void handleMovement(Player player, Panda panda, double speed, double jumpPower, 
                               boolean allowFlying, double flyingSpeed) {
        Location playerLoc = player.getLocation();
        UUID playerId = player.getUniqueId();

        // Get player's look direction
        Vector direction = playerLoc.getDirection();
        Vector horizontalDirection = direction.clone().setY(0).normalize();
        
        // Get right direction (for strafing)
        Vector rightDirection = new Vector(-horizontalDirection.getZ(), 0, horizontalDirection.getX()).normalize();

        Vector movement = new Vector(0, 0, 0);

        // Handle movement based on flight mode
        if (allowFlying) {
            // When flying is enabled, only apply horizontal movement
            // Vertical movement is handled by levitation effects in main plugin
            movement = horizontalDirection.clone().multiply(flyingSpeed);
            Vector currentVelocity = panda.getVelocity();
            movement.setY(currentVelocity.getY()); // Preserve current Y velocity from levitation
        } else {
            // Ground movement - forward in horizontal direction
            movement = horizontalDirection.clone().multiply(speed);
            
            // Preserve some of the existing vertical velocity for smooth falling
            Vector currentVelocity = panda.getVelocity();
            movement.setY(Math.max(currentVelocity.getY(), -2.0)); // Cap falling speed

            // Jump handling - detect if player tries to jump
            // Check vertical head movement as jump indicator
            if (panda.isOnGround() && Math.abs(panda.getVelocity().getY()) < 0.1) {
                Long lastJump = lastJumpTime.get(playerId);
                long now = System.currentTimeMillis();
                
                // Allow jump every 500ms
                if (lastJump == null || (now - lastJump) > 500) {
                    // Look for upward head movement
                    if (playerLoc.getPitch() < -20) {
                        movement.setY(jumpPower);
                        lastJumpTime.put(playerId, now);
                    }
                }
            }
        }

        // Apply movement
        panda.setVelocity(movement);
        
        // Store location for next tick
        lastLocations.put(playerId, playerLoc.clone());
    }

    public void cleanup(UUID playerId) {
        lastLocations.remove(playerId);
        lastJumpTime.remove(playerId);
        flyingMode.remove(playerId);
    }
}
