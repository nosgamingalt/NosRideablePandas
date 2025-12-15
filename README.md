# RideablePanda Plugin v2.0

A Minecraft Spigot/Bukkit plugin that allows players to tame and ride pandas with customizable controls and permissions!

## Features

### 🐼 Taming System
- Players must feed pandas bamboo (configurable) to gain their trust
- Configurable success chance that increases with each attempt
- Maximum attempts setting for guaranteed success
- Admins with special permission can skip taming

### 🎮 Improved Controls
- **Natural movement** - Panda moves in the direction you're looking
- **No items required** - No need for carrot on a stick
- **Jump capability** - Look up slightly to make the panda jump
- **Sneak to dismount** - Press shift to get off the panda

### ✈️ Flying Mode (Optional)
- Enable flying in config
- Look sharply upward to enter flying mode
- Requires `rideablepanda.fly` permission
- Configurable flying speed

### 🔐 Permissions System
- `rideablepanda.ride` - Basic riding permission (default: true)
- `rideablepanda.instant` - Skip taming requirement (default: op)
- `rideablepanda.fly` - Allow flying on pandas (default: op)
- `rideablepanda.*` - All permissions

### 🌍 Language Support
- All messages fully customizable in config.yml
- Easy translation to any language
- Color code support with `&` formatting

## Configuration

### config.yml Example
```yaml
# Taming System
taming:
  enabled: true           # Require taming before riding
  base-chance: 30         # Starting success percentage
  food-item: BAMBOO       # Item used for taming
  max-attempts: 10        # Guaranteed success after X tries

# Movement Controls
controls:
  speed: 0.35            # Ground movement speed
  jump-power: 0.6        # Jump height
  allow-flying: false    # Enable flying mode
  flying-speed: 0.5      # Flying speed

# Messages (Customizable)
messages:
  tamed-success: "&aYou've successfully tamed the panda!"
  mount-success: "&aYou mounted the panda! Use WASD to move."
  taming-failed: "&cThe panda doesn't trust you yet. Try again!"
  # ... more messages available
```

## How to Use

### For Regular Players
1. Find an adult panda
2. Hold bamboo and right-click the panda repeatedly
3. Once tamed, right-click to mount
4. Look in the direction you want to go
5. Look up slightly to jump
6. Sneak (shift) to dismount

### For Admins
- With `rideablepanda.instant` permission, simply right-click any adult panda to mount immediately
- With `rideablepanda.fly` permission (and flying enabled in config), look sharply upward to fly

## Installation

1. Download the plugin JAR file
2. Place it in your server's `plugins` folder
3. Restart the server
4. Edit `plugins/RideablePanda/config.yml` to customize
5. Use `/reload` or restart to apply changes

## Permissions

```yaml
permissions:
  rideablepanda.ride:
    description: Allows riding pandas (requires taming)
    default: true
    
  rideablepanda.instant:
    description: Skip taming, instant mount (for admins)
    default: op
    
  rideablepanda.fly:
    description: Allow flying on pandas
    default: op
    
  rideablepanda.*:
    description: All permissions
    default: op
```

## Version History

### v2.0 (Latest)
- ✨ Added taming system with configurable success rates
- ✨ Implemented permission-based instant mounting for admins
- ✨ Completely redesigned control system (no items needed)
- ✨ Added optional flying mode with permission check
- ✨ Full language/message customization support
- ✨ Configurable movement speeds and jump power
- 🐛 Fixed various bugs from v1.0

### v1.0
- Initial release
- Basic saddle and carrot-on-a-stick controls

## Support

If you encounter any issues or have suggestions, please report them!

## Credits

**Author:** NOSgaming3125
**Version:** 1.2
**API:** Spigot/Bukkit 1.20+

---

*A fun and cute plugin that brings joy to your server!* 🐼✨
