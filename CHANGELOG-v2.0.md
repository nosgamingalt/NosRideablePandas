# RideablePanda v2.0 - Changelog

## Summary of Improvements Based on User Feedback

### ✅ Language Settings
- **IMPLEMENTED**: Full message customization in `config.yml`
- All player-facing messages are now configurable
- Supports color codes with `&` formatting
- Easy translation to any language

### ✅ Taming System with Percentage Chances
- **IMPLEMENTED**: Configurable taming mechanic
- Players feed pandas bamboo (configurable item)
- Base success chance: 30% (configurable)
- Chance increases with each attempt (+5% per try)
- Maximum attempts setting for guaranteed success
- Similar to horse taming - must gain the panda's trust first

### ✅ Permission-Based Access
- **IMPLEMENTED**: Three-tier permission system
  1. `rideablepanda.ride` - Regular players (must tame pandas)
  2. `rideablepanda.instant` - Admins (skip taming, instant mount)
  3. `rideablepanda.fly` - Special permission for flying

### ✅ Normal Control System
- **IMPLEMENTED**: Natural directional controls
- **Removed**: Item-based control (carrot on a stick)
- **New**: Move in the direction you're looking
- Jump by looking slightly upward
- Sneak to dismount
- Smooth and intuitive movement

### ✅ Flying Control with Permissions
- **IMPLEMENTED**: Optional flying mode
- Configurable in config.yml (`allow-flying: true/false`)
- Requires `rideablepanda.fly` permission
- Activate by looking sharply upward while riding
- Separate flying speed configuration
- Can be completely disabled in config

## New Files Created

1. **config.yml** - Main configuration file with all settings
2. **Messages.java** - Handles all customizable messages
3. **MovementController.java** - Advanced movement handling system
4. **README.md** - Complete documentation

## Modified Files

1. **RideablePanda.java** - Complete rewrite with all new features
2. **plugin.yml** - Added permissions and updated version

## Configuration Options Added

### Taming Settings
- `taming.enabled` - Toggle taming requirement
- `taming.base-chance` - Starting success percentage
- `taming.food-item` - Item used for taming
- `taming.max-attempts` - Maximum tries before guaranteed success

### Control Settings
- `controls.speed` - Ground movement speed
- `controls.jump-power` - Jump height
- `controls.allow-flying` - Enable/disable flying
- `controls.flying-speed` - Flying movement speed

### General Settings
- `general.require-saddle` - Optional saddle requirement
- `general.consume-saddle` - Whether saddle is consumed
- `general.show-action-bar` - Action bar messages toggle

### All Messages Customizable
- Success messages (tamed, mounted, etc.)
- Failure messages (wrong food, no permission, etc.)
- Information messages (attempts remaining, dismount, etc.)

## How It Addresses Each Point in the Review

1. **"setting options. Like language"**
   ✅ Full message customization in config.yml for any language

2. **"percentage of chances that we will be able to get on a panda"**
   ✅ Configurable base-chance (0-100%) with increasing success rate

3. **"something like with a horse, you have to feed the horse first"**
   ✅ Must feed pandas bamboo repeatedly to gain trust

4. **"permissions, who can get on a panda right away (e.g. admins)"**
   ✅ rideablepanda.instant permission for instant mounting

5. **"who has to try a few times to succeed (e.g. players)"**
   ✅ Regular players need rideablepanda.ride and must complete taming

6. **"normal control, not with items"**
   ✅ Completely removed item-based controls, now uses look direction

7. **"completely disable flying (adding additional permissions)"**
   ✅ Flying requires rideablepanda.fly permission + config toggle

## Technical Improvements

- Proper metadata storage for tamed pandas
- Task cleanup on dismount to prevent memory leaks
- Separate MovementController class for clean code organization
- Configuration validation and defaults
- Better event handling
- Persistent taming data (stored in entity metadata)

---

**All requested features have been successfully implemented!** 🎉
