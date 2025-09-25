package org.samo_lego.taterzens.common.commands;

public class TeleportCommand {

    // TODO: Fix this implementation to match up with the 1.21 Teleport implementation
    
    /*
    public static void registerNode(LiteralCommandNode<CommandSourceStack> npcNode) {
        LiteralCommandNode<CommandSourceStack> tpNode = literal("tp")
                .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.tp", config.perms.npcCommandPermissionLevel))
                .then(argument("entity", EntityArgument.entity())
                        .executes(context -> teleportTaterzen(context, EntityArgument.getEntity(context, "entity").position()))
                )
                .then(argument("location", Vec3Argument.vec3())
                        .executes(context -> teleportTaterzen(context, Vec3Argument.getCoordinates(context, "location").getPosition(context.getSource())))
                )
                .build();

        npcNode.addChild(tpNode);
    }
    */

    /*
    private static int teleportTaterzen(CommandContext<CommandSourceStack> context, Vec3 destination) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        return selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> taterzen.teleportToWithTicket(destination.x(), destination.y(), destination.z()));
    }
    */
}
