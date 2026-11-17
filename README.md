# Liquid Mobility

Fluid, momentum-driven parkour movement, wall-running, flow acceleration, and procedural 1st/3rd-person animations for **Minecraft 1.16.5 (Forge)**.

## Features

- **Flow Momentum System**: Uninterrupted sprinting and chaining parkour moves build Flow (`0%–100%`), smoothly increasing top sprint speed. Collisions and hard landings interrupt Flow.
- **Wall Run, Wall Climb & Wall Cling**:
  - Sprint alongside a wall in mid-air to run horizontally along it.
  - Sprint directly into a wall in mid-air to climb vertically.
  - Hold **Sneak** against a wall to cling and slow your descent.
  - Press **Jump** while running or clinging to kick off toward your next target.
- **180° Quick Turn**: Rapid smooth 180-degree turnaround (`X` by default). Triggering a Quick Turn on a wall opens a brief grace window to kick off in the opposite direction.
- **Slide & Slide Kick**: Press **Sneak** while sprinting or moving fast on the ground to slide; colliding with mobs during a slide knocks them back and deals configurable damage. Jump mid-slide for a long slide-jump.
- **Double Jump, Coyote Time, Air Strafe & B-Hop**: Responsive aerial control, grace jump window after stepping off ledges, and timed bunny-hop speed boosts.
- **Directional Dash**: Instant burst movement in your chosen WASD direction (`Left Alt` by default).
- **Fast Drop & Ground Pound**: Tap **Sneak** early in mid-air to dive rapidly and unleash an area shockwave on landing.
- **Skill Roll & Soft Cushions**: Hold **Sneak** before hitting the ground from height to negate fall damage and roll forward while boosting Flow. Wool, Hay Bales, and Slime Blocks also cushion falls.
- **Step Vault & Ledge Vault**: Automatically vault over 1-block obstacles while sprinting (hold **Jump** for a higher springboard vault) and pull up onto chest-height ledges.
- **Route Vision**: Toggleable world-space route highlights (`V` by default) for vaultable obstacles, climbable ledges, runnable walls, and soft landing surfaces.
- **Dynamic Camera, Hand Kinematics & Procedural Player Poses**: Frame-rate-independent camera tilt/roll/FOV/landing dip, asymmetric 1st-person hand animations, 360° flip/roll animations, and synced 3D skin overlay poses in multiplayer.

## Default Controls

| Action | Default Key |
| --- | --- |
| Jump / Double Jump / Wall Jump / Springboard | `Space` |
| Slide / Wall Cling / Fast Drop / Skill Roll | `Left Shift` (Sneak) |
| Directional Dash | `Left Alt` |
| 180° Quick Turn | `X` |
| Toggle Route Vision | `V` |

All custom keybinds can be changed in **Controls → Liquid Mobility**.

## Requirements

- **Minecraft**: `1.16.5`
- **Forge**: `36.2.42` or newer (`1.16.5`)
- **Java**: `8`

## Configuration

- `config/liquidmobility-common.toml` — physics values, Flow momentum tuning, cooldowns, combat damage, and Creative-mode toggle (`allowInCreativeWhenNotFlying`).
- `config/liquidmobility-client.toml` — camera effects, hand animations, 3D player poses, Route Vision, and HUD overlay options.

## Building from Source

```bash
./gradlew build
```

The compiled JAR is generated in `build/libs/liquidmobility-1.0.0.jar`.

## License

Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)  
Licensed under the **GNU General Public License v3.0 only** (`GPL-3.0-only`). See `LICENSE` for details.