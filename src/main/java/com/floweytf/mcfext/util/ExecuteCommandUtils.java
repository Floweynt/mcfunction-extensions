package com.floweytf.mcfext.util;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.*;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.SwizzleArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.commands.ExecuteCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ExecuteCommandUtils {
    private static void addPredicates(LiteralCommandNode<CommandSourceStack> root,
                                      ArgumentBuilder<CommandSourceStack, ?> builder,
                                      CommandBuildContext access) {
        builder.then(ExecuteCommand.addConditionals(root, literal("unless"), false, access))
            .then(ExecuteCommand.addConditionals(root, literal("if"), true, access));
    }

    private static void addModifiers(LiteralCommandNode<CommandSourceStack> root,
                                     ArgumentBuilder<CommandSourceStack, ?> builder,
                                     CommandBuildContext access) {
        builder
            .then(literal("as").then(argument("targets", EntityArgument.entities()).fork(root, context -> {
                List<CommandSourceStack> list = Lists.newArrayList();

                for (Entity entity : EntityArgument.getOptionalEntities(context, "targets")) {
                    list.add(context.getSource().withEntity(entity));
                }

                return list;
            })))
            .then(literal("at").then(argument("targets", EntityArgument.entities()).fork(root, context -> {
                List<CommandSourceStack> list = Lists.newArrayList();

                for (Entity entity : EntityArgument.getOptionalEntities(context, "targets")) {
                    list.add(
                        context.getSource().withLevel((ServerLevel) entity.level()).withPosition(entity.position()).withRotation(entity.getRotationVector())
                    );
                }

                return list;
            })))
            .then(
                literal("positioned")
                    .then(
                        argument("pos", Vec3Argument.vec3())
                            .redirect(
                                root,
                                context -> context.getSource()
                                    .withPosition(Vec3Argument.getVec3(context, "pos"))
                                    .withAnchor(EntityAnchorArgument.Anchor.FEET)
                            )
                    )
                    .then(literal("as").then(argument("targets", EntityArgument.entities()).fork(root, context -> {
                        List<CommandSourceStack> list = Lists.newArrayList();

                        for (Entity entity : EntityArgument.getOptionalEntities(context, "targets")) {
                            list.add(context.getSource().withPosition(entity.position()));
                        }

                        return list;
                    })))
                    .then(
                        literal("over")
                            .then(argument("heightmap", HeightmapTypeArgument.heightmap()).redirect(root, context -> {
                                Vec3 vec3 = context.getSource().getPosition();
                                ServerLevel serverLevel = context.getSource().getLevel();
                                double d = vec3.x();
                                double e = vec3.z();
                                if (!serverLevel.hasChunk(SectionPos.blockToSectionCoord(d),
                                    SectionPos.blockToSectionCoord(e))) {
                                    throw BlockPosArgument.ERROR_NOT_LOADED.create();
                                } else {
                                    int i = serverLevel.getHeight(HeightmapTypeArgument.getHeightmap(context,
                                        "heightmap"), Mth.floor(d), Mth.floor(e));
                                    return context.getSource().withPosition(new Vec3(d, i, e));
                                }
                            }))
                    )
            )
            .then(
                literal("rotated")
                    .then(
                        argument("rot", RotationArgument.rotation())
                            .redirect(
                                root,
                                context -> context.getSource().withRotation(RotationArgument.getRotation(context,
                                    "rot").getRotation(context.getSource()))
                            )
                    )
                    .then(literal("as").then(argument("targets", EntityArgument.entities()).fork(root, context -> {
                        List<CommandSourceStack> list = Lists.newArrayList();

                        for (Entity entity : EntityArgument.getOptionalEntities(context, "targets")) {
                            list.add(context.getSource().withRotation(entity.getRotationVector()));
                        }

                        return list;
                    })))
            )
            .then(
                literal("facing")
                    .then(
                        literal("entity")
                            .then(
                                argument("targets", EntityArgument.entities())
                                    .then(argument("anchor", EntityAnchorArgument.anchor()).fork(root, context -> {
                                        List<CommandSourceStack> list = Lists.newArrayList();
                                        EntityAnchorArgument.Anchor anchor = EntityAnchorArgument.getAnchor(context,
                                            "anchor");

                                        for (Entity entity : EntityArgument.getOptionalEntities(context, "targets")) {
                                            list.add(context.getSource().facing(entity, anchor));
                                        }

                                        return list;
                                    }))
                            )
                    )
                    .then(
                        argument("pos", Vec3Argument.vec3())
                            .redirect(root, context -> context.getSource().facing(Vec3Argument.getVec3(context, "pos")))
                    )
            )
            .then(
                literal("align")
                    .then(
                        argument("axes", SwizzleArgument.swizzle())
                            .redirect(
                                root,
                                context -> context.getSource()
                                    .withPosition(context.getSource().getPosition().align(SwizzleArgument.getSwizzle(context, "axes")))
                            )
                    )
            )
            .then(
                literal("anchored")
                    .then(
                        argument("anchor", EntityAnchorArgument.anchor())
                            .redirect(root,
                                context -> context.getSource().withAnchor(EntityAnchorArgument.getAnchor(context,
                                    "anchor")))
                    )
            )
            .then(
                literal("in")
                    .then(
                        argument("dimension", DimensionArgument.dimension())
                            .redirect(root,
                                context -> context.getSource().withLevel(DimensionArgument.getDimension(context,
                                    "dimension")))
                    )
            )
            .then(
                literal("summon")
                    .then(
                        argument("entity", ResourceArgument.resource(access, Registries.ENTITY_TYPE))
                            .suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                            .redirect(
                                root,
                                context -> ExecuteCommand.spawnEntityAndRedirect(context.getSource(),
                                    ResourceArgument.getSummonableEntityType(context, "entity"))
                            )
                    )
            )
            .then(ExecuteCommand.createRelationOperations(root, literal("on")));
    }

    public static void registerV1ControlFlow(
        CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext access, String name,
        Command<CommandSourceStack> command
    ) {
        LiteralCommandNode<CommandSourceStack> root = dispatcher.register(literal(name));
        final var builder = literal(name).then(literal("{").executes(command));
        addPredicates(root, builder, access);
        addModifiers(root, builder, access);

        dispatcher.register(builder);
    }
}