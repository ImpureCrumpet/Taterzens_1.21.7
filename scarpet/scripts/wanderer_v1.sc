// Wanderer v1 - simple background NPC
// Requirements:
// - Anchor (set via set_anchor_here / default fallback)
// - Wander within RADIUS blocks
// - Avoid water and common hazards
// - Look at nearby players and emote on a cooldown
// - Throttled movement (every 2–3 seconds)
// - Auto-respawn if missing (requires an online player to own the spawn action)

// -------------------- Config --------------------

RADIUS = 48;
LOOK_RANGE = 12;
MOVE_MIN_TICKS = 40;   // 2 seconds (20 tps)
MOVE_MAX_TICKS = 60;   // 3 seconds
EMOTE_COOLDOWN_TICKS = 200; // 10 seconds
NPC_NAME = 'Wanderer';

// Skin data from mineskin.json for "wanderer" job
SKIN_VALUE = 'ewogICJ0aW1lc3RhbXAiIDogMTc0NjEzMzg0ODg0NiwKICAicHJvZmlsZUlkIiA6ICJlZmI1ZWQ2YjVjOTU0ODBlYWFmMjAyZDIxOWVmNjBjNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNaWtlSHdhazAwMSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85ZTFjMjA0OTk1Mjg2ZjRmZTRmMTYzOTViZWQ3MDFiOGE3MjU1YWQzNjQwODFiZmE4ZDFjNTQ5MTdhMWIzNzFlIgogICAgfQogIH0KfQ==';
SKIN_SIGNATURE = 'QX822nc5h0wwoPPSOf9iVyawyEET2ztuNNQ593cPk5Mxiszfkfq6LznT1c6GM67eFPFB5ar7M1i4O3KnLoWHgNP6/EqirbpGEVxVgj1wN6n+27gj02fVOVegfYlbNhJ5hWgc6+7LfOKVMxk4y55icNWKgUB/baab7Zp+/ICgJoFkQIlA6Ltbf2Sc+KYfQlU6YHiPRY4NwpXptDwOLpUx0XsvoIgl8fBrJby9f+73Ux+fjxKEBAxQaNCdwfrF37o43wCiRkJg9oYbSmKK6bf3PY+FqNrkSP6JqY9d6osPXJELI7XBJoQcrGSK7T16P6mta9y75om/CZlDiplxnzGCC37lyV9U6aanhzZvyHXleKtSBQcAfuWHZC8crnyx3dTBwfRcFVC6Qu/INc1gMBev9O4jcaFQRKhuPDObLGJqtt29QZFX0lJIf6QOBMhDqRRBWog4c55fij0B1fOUR98ve609GxyyIE53JBdewXevcA45WB67Wbi6+q/waNCsowlYeQ70gT0ULjdB5yQGz5nK9cWkLb6EPTXFu5Z3R/61ygaSHyD9Kdy3X79H5UHTZqD3GrRHe73sPweFUxPoSd1ShKuEnC5ZaHcA5+cu365S75xZFxjdTvoQhi0ySpzMkr1Q4hA8omYGhgYv81rkuQSrcA20y1FfRwu65w2oOCGKfnM=';

// If anchor is never set, default fallback:
DEFAULT_ANCHOR = pos(0, 64, 0);

// -------------------- State --------------------

__memory = {};

get_anchor() -> (
    a = get(__memory, '__anchor');
    if (a == null, DEFAULT_ANCHOR, a)
);

set_anchor(p) -> (__memory:'__anchor' = p);

get_npc_uuid() -> get(__memory, 'npc_uuid');
set_npc_uuid(u) -> (__memory:'npc_uuid' = u);

now() -> tick_time();

// -------------------- Helpers --------------------

clamp(v, a, b) -> min(max(v, a), b);

rand_int(a, b) -> a + floor(random() * (b - a + 1));

in_radius(p, c, r) -> (distance(p, c) <= r);

safe_ground(p) -> (
    // Checks ground below p and p itself for hazards/water
    bp = floor(p);
    below = bp - pos(0, 1, 0);
    b  = block(bp);
    bb = block(below);
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
    name = string(b);
    name ~ '*lava*' or name ~ '*fire*' or name ~ '*cactus*' or name ~ '*sweet_berry_bush*'
);

pick_target(center, r) -> (
    // Try a few random points until we find safe ground near surface
    tries = 16;
    loop(tries, (
        a = random() * 6.28318530718; // 2*pi
        d = random() * r;
        tx = center~x + cos(a) * d;
        tz = center~z + sin(a) * d;
        // probe Y by scanning around center.y a bit
        y0 = center~y;
        ys = range(y0 + 6, y0 - 6, -1);
        for (y in ys, (
            cand = pos(tx, y, tz);
            if (safe_ground(cand), return cand)
        ));
    ));
    // fallback to center if nothing found
    center
);

step_towards(e, target, max_step) -> (
    p = pos(e);
    dir = target - p;
    dist = length(dir);
    if (dist <= max_step, (
        teleport(e, target);
    ), (
        teleport(e, p + normalize(dir) * max_step);
    ));
);

look_at_players(e, range) -> (
    ps = entities('player', pos(e), range, range, range);
    if (size(ps) > 0, (
        // face first player
        p = ps[0];
        look(e, pos(p));
        true
    ), false)
);

emote(e) -> (
    // lightweight emote: small particle and message to nearby players
    center = pos(e) + pos(0, 1.6, 0);
    particle('minecraft:happy_villager', center, 6, 0.25, 0.25, 0.25, 0.01);
);

// -------------------- Taterzen management --------------------

spawn_npc_at_anchor(owner_player) -> (
    a = get_anchor();
    // spawn via Taterzens helper (requires a player context)
    e = spawn_taterzen(owner_player, NPC_NAME);
    if (e != null, (
        teleport(e, a);
        modify(e, 'name', text(NPC_NAME));
        // set invulnerable via NBT
        modify(e, 'nbt', {'Invulnerable' -> true});
        // ensure persistence
        modify(e, 'nbt', {'PersistenceRequired' -> true});
        // apply skin from mineskin.json data
        skin_tag = {'value' -> SKIN_VALUE, 'signature' -> SKIN_SIGNATURE};
        call(e, 'setSkinFromTag', skin_tag);
        call(e, 'broadcastProfileUpdates');
        set_npc_uuid(str(uuid(e)));
    ));
    e
);

ensure_npc_alive() -> (
    u = get_npc_uuid();
    e = null;
    if (u != null, e = taterzen_by_uuid(u));
    if (e == null, (
        ops = players();
        if (size(ops) > 0, (
            // pick first online player to own spawn
            e = spawn_npc_at_anchor(ops[0]);
        ));
    ));
    e
);

// -------------------- Main loops --------------------

wander_loop() -> (
    e = ensure_npc_alive();
    if (e == null, (
        // retry in 5s if no players available to spawn
        schedule(100, wander_loop);
        return();
    ));

    center = get_anchor();
    if (not in_radius(pos(e), center, RADIUS + 6), (
        // out of bounds; pick target closer to center
        target = pick_target(center, RADIUS * 0.8);
        step_towards(e, target, 1.4);
    ), (
        // normal wander: pick a small offset step toward a safe random point
        target = pick_target(center, RADIUS);
        step_towards(e, target, 1.2);
    ));

    // Interaction: face and emote with cooldown
    t = now();
    last_emote = get(__memory, 'last_emote_tick');
    if (look_at_players(e, LOOK_RANGE) and (last_emote == null or t - last_emote >= EMOTE_COOLDOWN_TICKS), (
        emote(e);
        __memory:'last_emote_tick' = t;
    ));

    // schedule next movement tick
    schedule(rand_int(MOVE_MIN_TICKS, MOVE_MAX_TICKS), wander_loop);
);

// -------------------- Entry points --------------------

__on_start() -> (
    // kick off loop
    schedule(10, wander_loop);
);

// Command helpers to manage manually
spawn_wanderer(player_name) -> (
    p = player(player_name);
    if (p == null, return 'No such player online');
    e = spawn_npc_at_anchor(p);
    if (e == null, return 'Spawn failed');
    'Spawned wanderer at anchor'
);

set_anchor_here(player_name) -> (
    p = player(player_name);
    if (p == null, return 'No such player');
    set_anchor(pos(p));
    'Anchor updated'
);
