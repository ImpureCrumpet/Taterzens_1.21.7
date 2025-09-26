# static.sc - spawn a single static NPC at spawn (fallback: first player)
# - Spawns once on load (or when a player is online)
# - Stays put (no movement)
# - Invulnerable and persistent
# - Name: "Static" (adjust as desired)
# - Skin: auto-apply via setSkinFromTag with mineskin.json data

NPC_NAME = 'Static';

# Skin data from mineskin.json for "static" job (Waldo)
SKIN_VALUE = 'ewogICJ0aW1lc3RhbXAiIDogMTc1MDczNTQ5Mzk4NywKICAicHJvZmlsZUlkIiA6ICI2ZDcwZjM0OGFjODA0MWM5YjY4ZDA4MWUwMTUyNzVjNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJ0ZXN0YWx0MSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80MGY4ODE2NWI5OTg5NTM0NmUyM2I5OTI0MGRmYzBjNTNkNzYwMTAwY2I2MGM2YzA1ZjBjYjEyMjZlYWE2OWM5IgogICAgfQogIH0KfQ==';
SKIN_SIGNATURE = 'KGiKtQfmHWJslnQznkQ0WAQeP0MbcNNnP6JfxnGVZsPRVYD4bdayBTsw/dz3IgvfzyzhZ7cuHlQyfCv4/8N6seHF5SZUd2UnH+bTnRcvhSo0NI64Igloukwcp/xBO3CHzO7HXHjvnd+l8B8gqvI1UEwjFJ5ZjiLxDa/L8iVy+U7/ZVSTplYbrvCIjgU/+DKX7H52bkgtKBKOjRPE3AkOkbEJ2OSUoHY9zgxCCRvMG8Ps2QOcYuu1N2TPeqIlNrxWL4OVlu8rya6yJwLOMdD/PR4GyWzxQ6QpEh6nwcPAYKyXO11EGYTC831ruuKfh3yieSuGxTQvDhX4Eu6a2wjiYa1CV86VHRuxmCFhN6Yp7YtqXr0zU9sHCMCMkMhCA1jMKjFh3fxfTFklD2Prx6cANNfoYfAxT9gAsyF9M3lGkwanUkZaqpcQDOVCAIngAybGPCOhwrvD0XXtPwxj5zequGg//NtxxPfAcxd2nznGSwL901tyGNqyW/sfaH//TxhWe9AfdYLQSsy6SYtESOMMK6jPkwzq0adk9um6kp+UIGjfCLSfAYPVP04/G1Ph3IRXQ5vjGirRiR4KrtxF1dKwWMTCkyBOriMOlAef7C2av6t5YkIXvQIWszv5tJFBZF67a+u9pGNDGNI7SB+CCvYBdg5AC50AE9L2yNTYgwaCKZY=';

__memory = {};

get_npc_uuid() -> __memory['npc_uuid'];
set_npc_uuid(u) -> (__memory['npc_uuid'] = u);

# Attempt to get world spawn. If not available, return null.
get_world_spawn() -> (
    s = null;
    try(
        s = world_spawn_point(),
        s = null
    );
    s
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
        # apply skin from mineskin.json data
        skin_tag = {'value' -> SKIN_VALUE, 'signature' -> SKIN_SIGNATURE};
        call(e, 'setSkinFromTag', skin_tag);
        call(e, 'broadcastProfileUpdates');
        set_npc_uuid(str(uuid(e)));
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
    print('static.sc: Starting up...');
    schedule(10, ensure_spawned);
);

# Debug command to check script status
check_static() -> (
    u = get_npc_uuid();
    if (u == null, 
        print('No static NPC UUID stored'),
        (
            e = taterzen_by_uuid(u);
            if (e == null,
                print('Static NPC UUID stored but entity not found: ' + u),
                print('Static NPC found: ' + str(e) + ' at ' + str(pos(e)))
            )
        )
    );
    spawn_pos = get_world_spawn();
    if (spawn_pos == null,
        print('World spawn not available, will use first player position'),
        print('World spawn point: ' + str(spawn_pos))
    );
);
