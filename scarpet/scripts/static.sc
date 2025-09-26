# static.sc - spawn a single static NPC at spawn (fallback: first player)
# - Spawns once on load (or when a player is online)
# - Stays put (no movement)
# - Invulnerable and persistent
# - Name: "Wanderer" (adjust as desired)
# - Skin: auto-apply via Mineskin link

NPC_NAME = 'Wanderer';
SKIN_URL = 'https://minesk.in/dfcc2f01f6ce403781051b34fcf0d6e8';

__memory = {};

get_npc_uuid() -> __memory['npc_uuid'];
set_npc_uuid(u) -> (__memory['npc_uuid'] = u);

# Attempt to get world spawn. If not available, return null.
get_world_spawn() -> (
    s = null;
    s = world_spawn();
    if (s == null, null, s)
);

spawn_static_at(p) -> (
    pl = players();
    if (size(pl) == 0, return null);
    owner = pl[0];
    e = spawn_taterzen(owner, NPC_NAME);
    if (e != null, (
        teleport(e, p);
        modify(e, 'name', text(NPC_NAME));
        modify(e, 'nbt', {'Invulnerable' -> true});
        modify(e, 'nbt', {'PersistenceRequired' -> true});
        set_npc_uuid(str(uuid(e)));
        # Try to select and set skin using Taterzens commands as the owner
        run('/npc select uuid ' + str(uuid(e)), owner);
        run('/npc edit skin ' + SKIN_URL, owner);
    ));
    e
);

ensure_spawned() -> (
    u = get_npc_uuid();
    e = null;
    if (u != null, e = taterzen_by_uuid(u));
    if (e != null, return e);

    # Determine target position: world spawn if available, else first player's position
    s = get_world_spawn();
    target = null;
    if (s != null, target = s, (
        pl = players();
        if (size(pl) > 0, target = pos(pl[0]));
    ));

    if (target == null, (
        # No players yet; retry shortly
        schedule(100, ensure_spawned);
        return null;
    ));

    e = spawn_static_at(target);
    if (e == null, schedule(100, ensure_spawned));
    e
);

__on_start() -> (
    schedule(10, ensure_spawned);
);
