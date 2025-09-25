# NPC Scarpet Scripts

This folder contains local Scarpet scripts and data for NPC behaviors.

Planned job types:
- Farmer
- Gardener
- Fisher
- Lumberjack
- Miner
- Courier/Mailman
- Guard/Sentry
- Musician/Bard
- Wanderers
- Animal handler

## Universal rules (proposed)
- Movement throttle: issue path/move updates every 2–3 seconds (avoid per-tick pathing).
- Emote throttle: per-player cooldown 10–20 seconds for emotes/greetings.
- Hazard avoidance: avoid lava, fire, cactus, berry bushes, drops >3 blocks.
- Water policy: avoid water/waterlogged blocks; hug shoreline if near coast.
- Radius leash: keep within configured radius of a home/anchor point; return if displaced.
- Auto-respawn: if missing or dead, respawn at anchor on server start/timer.
- Invulnerability: enable for background extras to prevent griefing; optionally lock edits.
- Chunk locality: do nothing if chunk/player not nearby; resume when loaded.

## Wanderer (test) settings
- Anchor: global spawn point
- Radius: 48 blocks
- Throttle: move every 2–3 seconds; emote cooldown 12–20 seconds
- Interactions: look at nearby players (≤12 blocks) and emote
- Safety: avoid hazards and water, prefer solid ground paths
