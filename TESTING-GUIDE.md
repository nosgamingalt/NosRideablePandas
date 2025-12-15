# Testing Guide for RideablePanda v2.0

## Quick Testing Checklist

### 1. Basic Installation Test
- [ ] Plugin loads without errors
- [ ] `config.yml` is created in `plugins/RideablePanda/`
- [ ] No console errors on startup

### 2. Permission Testing

#### Test as Regular Player (no special permissions)
- [ ] Can see/interact with adult pandas
- [ ] Receives "wrong food" message when clicking panda without bamboo
- [ ] Can feed bamboo to panda (item consumed)
- [ ] Receives taming progress messages
- [ ] Eventually tames panda after multiple attempts
- [ ] Can mount tamed panda
- [ ] Cannot mount untamed pandas
- [ ] Cannot fly (even when looking up)

#### Test with `rideablepanda.instant` permission
- [ ] Can instantly mount any adult panda without taming
- [ ] No bamboo required
- [ ] Skips taming messages entirely

#### Test with `rideablepanda.fly` permission (with `allow-flying: true` in config)
- [ ] Can enter flying mode by looking upward
- [ ] Flying works in all directions
- [ ] Landing disables flying mode
- [ ] Flying speed different from ground speed

### 3. Control Testing
- [ ] Panda moves forward when looking ahead
- [ ] Panda turns when player looks left/right
- [ ] Looking up makes panda jump (when on ground)
- [ ] Sneaking dismounts the player
- [ ] Movement feels smooth and responsive

### 4. Taming System Testing

#### With default config (30% base chance, max 10 attempts):
- [ ] First attempt has ~30% success chance
- [ ] Success chance increases with each attempt
- [ ] Guaranteed success by 10th attempt
- [ ] Attempts counter resets after successful taming
- [ ] Tamed status persists for the panda
- [ ] Can remount tamed panda without feeding again

#### Test config changes:
- [ ] Change `taming.base-chance` to 100 - instant taming
- [ ] Change `taming.base-chance` to 0 - relies on attempt increases
- [ ] Change `taming.food-item` to `CARROT` - bamboo doesn't work, carrot does
- [ ] Set `taming.enabled: false` - no taming required

### 5. Message Customization Testing
- [ ] Edit messages in config
- [ ] Reload plugin/server
- [ ] Verify custom messages appear
- [ ] Color codes work (`&a`, `&c`, etc.)
- [ ] Placeholder replacements work (`{attempts}`, `{max}`)

### 6. Edge Cases
- [ ] Cannot mount baby pandas (receives message)
- [ ] Dismounting works correctly
- [ ] Multiple players can have different tamed pandas
- [ ] Taming data persists while panda is loaded
- [ ] No errors when player logs out while riding
- [ ] No errors when panda dies while being ridden

### 7. Performance Testing
- [ ] No lag with multiple players riding pandas
- [ ] Movement tasks clean up properly on dismount
- [ ] No memory leaks after extended use
- [ ] Plugin reloads cleanly

## Common Commands for Testing

```bash
# Give yourself bamboo
/give @s bamboo 64

# Give yourself permissions
/lp user <name> permission set rideablepanda.instant true
/lp user <name> permission set rideablepanda.fly true

# Reload plugin after config changes
/reload confirm
# OR
/plugman reload RideablePanda

# Check for errors
# Watch server console for any stack traces
```

## Recommended Config for Testing

### Easy Mode (for quick testing):
```yaml
taming:
  enabled: true
  base-chance: 80  # High success rate
  max-attempts: 3  # Quick guaranteed success

controls:
  allow-flying: true  # Enable flying for testing
```

### Hard Mode (realistic gameplay):
```yaml
taming:
  enabled: true
  base-chance: 20   # Low initial chance
  max-attempts: 15  # Takes more attempts

controls:
  allow-flying: false  # Disable flying
```

### Admin Mode (instant access):
```yaml
taming:
  enabled: false  # No taming needed

controls:
  allow-flying: true  # Flying enabled
```

## Expected Behavior Summary

| Permission | Taming Required? | Can Fly? | Notes |
|------------|------------------|----------|-------|
| None | N/A | No | Cannot ride at all |
| `rideablepanda.ride` | Yes | No | Normal player |
| `rideablepanda.instant` | No | No | Admin quick mount |
| `rideablepanda.fly` | Depends* | Yes | Flying enabled |
| `rideablepanda.*` | No | Yes | All permissions |

*Depends on whether player also has ride or instant permission

## Troubleshooting

### Plugin not loading?
- Check main class path in `plugin.yml` matches actual package structure
- Ensure Spigot/Bukkit version is 1.20 or higher

### Taming not working?
- Verify `taming.enabled: true` in config
- Check player has correct food item
- Ensure panda is an adult

### Flying not working?
- Check `controls.allow-flying: true` in config
- Verify player has `rideablepanda.fly` permission
- Must be riding the panda first
- Look sharply upward (pitch < -50)

### Controls feel weird?
- Adjust `controls.speed` (default 0.35)
- Adjust `controls.jump-power` (default 0.6)
- Adjust `controls.flying-speed` (default 0.5)

---

**Happy Testing!** 🐼✨
