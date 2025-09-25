// Wanderer v1 - simple background NPC
// Requirements:
// - Anchor at global spawn (fallback to configurable ANCHOR if not available)
// - Wander within RADIUS blocks
// - Avoid water and common hazards
// - Look at nearby players and emote on a cooldown
// - Throttled movement (every 2–3 seconds)
// - Auto-respawn if missing (requires an online player to own the spawn action)

// -------------------- Config --------------------

RADIUS := 48;
LOOK_RANGE := 12;
MOVE_MIN_TICKS := 40;   // 2 seconds (20 tps)
MOVE_MAX_TICKS := 60;   // 3 seconds
EMOTE_COOLDOWN_TICKS := 200; // 10 seconds
NPC_NAME := 'Wanderer';

// If world spawn lookup fails in your environment, set a static anchor here:
// ANCHOR := pos(0, 64, 0);

// -------------------- State --------------------

__memory := {};

get_anchor() -> (
    // Try world spawn; fallback to stored or manual ANCHOR
    anchor := world_spawn();
    if(anchor == null, (
        if('__anchor' in __memory, __memory.'__anchor',
            // Fallback default; edit if needed
            pos(0, 64, 0)
        )
    ), anchor)
);

set_anchor(p) -> (__memory.'__anchor' := p);

get_npc_uuid() -> __memory.'npc_uuid';
set_npc_uuid(u) -> (__memory.'npc_uuid' := u);

now() -> tick_time();

// -------------------- Helpers --------------------

clamp(v, a, b) -> min(max(v, a), b);

rand_int(a, b) -> a + floor(random() * (b - a + 1));

in_radius(p, c, r) -> (distance(p, c) <= r);

safe_ground(p) -> (
    // Checks ground below p and p itself for hazards/water
    bp := floor(p);
    below := bp - pos(0, 1, 0);
    b  := block(bp);
    bb := block(below);
    // no liquids/waterlogged and not immediate hazards
    not(
        is_water(b) or is_water(bb) or
        is_hazard(b) or is_hazard(bb)
    )
);

is_water(b) -> (
    // both pure water and waterlogged
    (string(b) ~ '*water*') or (property(b, 'waterlogged') == true)
);

is_hazard(b) -> (
    name := string(b);
    name ~ '*lava*' or name ~ '*fire*' or name ~ '*cactus*' or name ~ '*sweet_berry_bush*'
);

pick_target(center, r) -> (
    // Try a few random points until we find safe ground near surface
    #tries := 16;
    loop(#tries, (
        a := random() * 6.28318530718; // 2*pi
        d := random() * r;
        tx := center~x + cos(a) * d;
        tz := center~z + sin(a) * d;
        // probe Y by scanning down from sky_limit to ground; simplify by sampling around center.y
        y0 := center~y;
        // small vertical search
        ys := range(y0 + 6, y0 - 6, -1);
        for(y in ys, (
            cand := pos(tx, y, tz);
            if(safe_ground(cand), return cand)
        ));
    ));
    // fallback to center if nothing found
    center
);

step_towards(e, target, max_step) -> (
    p := pos(e);
    dir := target - p;
    dist := length(dir);
    if(dist <= max_step, (
        set_pos(e, target);
    ), (
        set_pos(e, p + normalize(dir) * max_step);
    ));
);

look_at_players(e, range) -> (
    ps := query(e, 'players', range);
    if(size(ps) > 0, (
        // face first player
        p := ps[0];
        face(e, pos(p));
        true
    ), false)
);

emote(e) -> (
    // lightweight emote: small particle and message to nearby players
    center := pos(e) + pos(0, 1.6, 0);
    particle('minecraft:happy_villager', center, 6, 0.25, 0.25, 0.25, 0.01);
);

// -------------------- Taterzen management --------------------

find_npc_by_name(name) -> (
    for (ent in entities(), if (name(ent) == name, return ent));
    null
);

spawn_npc_at_anchor(owner_player) -> (
    a := get_anchor();
    // spawn via Taterzens helper (requires a player context)
    e := spawn_taterzen(owner_player, NPC_NAME);
    if(e != null, (
        set_pos(e, a);
        set_nametag(e, NPC_NAME);
        // set invulnerable via NBT
        modify(e, 'nbt', {'Invulnerable' -> true});
        // optional: ensure persistent
        modify(e, 'nbt', {'PersistenceRequired' -> true});
        // Load skin from mineskin/mineskin.json by name if you also keep mapping in script; otherwise set via command manually
        // Example command (if run manually): /npc edit skin <mineskin-id>
        set_npc_uuid(str(uuid(e)));
    ));
    e
);

ensure_npc_alive() -> (
    u := get_npc_uuid();
    e := null;
    if(u != null, e := taterzen_by_uuid(u));
    if(e == null, (
        ops := players();
        if(size(ops) > 0, (
            // pick first online player to own spawn
            e := spawn_npc_at_anchor(ops[0]);
        ));
    ));
    e
);

// -------------------- Main loops --------------------

wander_loop() -> (
    e := ensure_npc_alive();
    if(e == null, (
        // retry in 5s if no players available to spawn
        schedule(100, wander_loop);
        return null;
    ));

    center := get_anchor();
    if(not in_radius(pos(e), center, RADIUS + 6), (
        // out of bounds; pick target closer to center
        target := pick_target(center, RADIUS * 0.8);
        step_towards(e, target, 1.4);
    ), (
        // normal wander: pick a small offset step toward a safe random point
        target := pick_target(center, RADIUS);
        step_towards(e, target, 1.2);
    ));

    // Interaction: face and emote with cooldown
    t := now();
    last_emote := __memory.'last_emote_tick';
    if(look_at_players(e, LOOK_RANGE) and (last_emote == null or t - last_emote >= EMOTE_COOLDOWN_TICKS), (
        emote(e);
        __memory.'last_emote_tick' := t;
    ));

    // schedule next movement tick
    schedule(rand_int(MOVE_MIN_TICKS, MOVE_MAX_TICKS), wander_loop);
);

// -------------------- Entry points --------------------

__on_start() -> (
    // capture anchor from world spawn
    a := world_spawn();
    if(a != null, set_anchor(a));
    // kick off loop
    schedule(10, wander_loop);
);

// Command helpers to manage manually
spawn_wanderer(player_name) -> (
    p := player(player_name);
    if(p == null, return 'No such player online');
    e := spawn_npc_at_anchor(p);
    if(e == null, return 'Spawn failed');
    'Spawned wanderer at anchor'
);

set_anchor_here(player_name) -> (
    p := player(player_name);
    if(p == null, return 'No such player');
    set_anchor(pos(p));
    'Anchor updated'
);
