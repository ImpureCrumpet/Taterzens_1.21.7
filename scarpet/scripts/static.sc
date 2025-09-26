__config() -> {
  'requires' -> {
    'carpet' -> '>=1.4.33',
    'taterzens' -> '>=1.11.6'
  },
  'stay_loaded' -> true,
  'commands' -> {
    'check' -> 'check_static'
  }
};

global_npc_name = 'Static';

global_skin_value = 'ewogICJ0aW1lc3RhbXAiIDogMTc1MDczNTQ5Mzk4NywKICAicHJvZmlsZUlkIiA6ICI2ZDcwZjM0OGFjODA0MWM5YjY4ZDA4MWUwMTUyNzVjNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJ0ZXN0YWx0MSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80MGY4ODE2NWI5OTg5NTM0NmUyM2I5OTI0MGRmYzBjNTNkNzYwMTAwY2I2MGM2YzA1ZjBjYjEyMjZlYWE2OWM5IgogICAgfQogIH0KfQ==';
global_skin_signature = 'KGiKtQfmHWJslnQznkQ0WAQeP0MbcNNnP6JfxnGVZsPRVYD4bdayBTsw/dz3IgvfzyzhZ7cuHlQyfCv4/8N6seHF5SZUd2UnH+bTnRcvhSo0NI64Igloukwcp/xBO3CHzO7HXHjvnd+l8B8gqvI1UEwjFJ5ZjiLxDa/L8iVy+U7/ZVSTplYbrvCIjgU/+DKX7H52bkgtKBKOjRPE3AkOkbEJ2OSUoHY9zgxCCRvMG8Ps2QOcYuu1N2TPeqIlNrxWL4OVlu8rya6yJwLOMdD/PR4GyWzxQ6QpEh6nwcPAYKyXO11EGYTC831ruuKfh3yieSuGxTQvDhX4Eu6a2wjiYa1CV86VHRuxmCFhN6Yp7YtqXr0zU9sHCMCMkMhCA1jMKjFh3fxfTFklD2Prx6cANNfoYfAxT9gAsyF9M3lGkwanUkZaqpcQDOVCAIngAybGPCOhwrvD0XXtPwxj5zequGg//NtxxPfAcxd2nznGSwL901tyGNqyW/sfaH//TxhWe9AfdYLQSsy6SYtESOMMK6jPkwzq0adk9um6kp+UIGjfCLSfAYPVP04/G1Ph3IRXQ5vjGirRiR4KrtxF1dKwWMTCkyBOriMOlAef7C2av6t5YkIXvQIWszv5tJFBZF67a+u9pGNDGNI7SB+CCvYBdg5AC50AE9L2yNTYgwaCKZY=';

global_npc_uuid = null;

get_world_spawn() -> system_info('world_spawn_point');

spawn_static_at(p) -> (
    online_players = player('all');
    if (length(online_players) == 0, return());
    owner = online_players:0;

    npc = taterzen:create(owner, global_npc_name);

    if (npc != null,
        modify(npc, 'pos', p);
        taterzen:set_name(npc, format('w ' + global_npc_name));
        modify(npc, 'nbt_merge', nbt('{Invulnerable:true,PersistenceRequired:true}'));
        
        taterzen:set_skin(npc, global_skin_value, global_skin_signature);
        
        global_npc_uuid = query(npc, 'uuid');
    );
    npc
);

ensure_spawned() -> (
    npc = null;
    if (global_npc_uuid != null,
        npc = taterzen:get_by_uuid(global_npc_uuid)
    );
    if (npc != null, return(npc));

    target_pos = get_world_spawn();
    if (target_pos == null,
        online_players = player('all');
        if (length(online_players) > 0, target_pos = pos(online_players:0));
    );

    if (target_pos == null,
        schedule(100, ensure_spawned);
        return();
    );

    npc = spawn_static_at(target_pos);
    if (npc == null, schedule(100, ensure_spawned));
    npc
);

__on_start() -> (
    print(format('gi static.sc: Starting up...'));
    ensure_spawned();
);

check_static() -> (
    if (global_npc_uuid == null,
        print('No static NPC UUID stored.'),
        (
            npc = taterzen:get_by_uuid(global_npc_uuid);
            if (npc == null,
                print(format('r Static NPC UUID stored but entity not found: ' + global_npc_uuid)),
                print(format('g Static NPC found: ' + query(npc, 'id') + ' at ' + pos(npc)))
            )
        )
    );
    spawn_pos = get_world_spawn();
    if (spawn_pos == null,
        print('World spawn not available, will use first player position.'),
        print(format('w World spawn point: ' + spawn_pos))
    );
);