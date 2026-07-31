package dev.simulated_team.simulated;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.client.AllBlockEntityBehaviours;
import com.zurrtum.create.client.content.kinetics.transmission.SplitShaftVisual;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.audio.KineticAudioBehaviour;
import com.zurrtum.create.client.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import com.zurrtum.create.client.foundation.item.ItemDescription;
import dev.simulated_team.simulated.client.BlockPropertiesTooltip;
import dev.simulated_team.simulated.compat.create.LegacyCustomKineticTooltipBehaviour;
import dev.simulated_team.simulated.compat.create.LegacyFilteringClientBehaviour;
import dev.simulated_team.simulated.compat.create.LegacyItemTooltips;
import dev.simulated_team.simulated.compat.create.LegacyKineticTooltipBehaviour;
import dev.simulated_team.simulated.compat.create.LegacyScrollValueClientBehaviour;
import dev.simulated_team.simulated.compat.create.LegacyTooltipBehaviour;
import dev.simulated_team.simulated.compat.create.SableCreateBlockEntityRenderer;
import dev.simulated_team.simulated.compat.create.SableCreateKineticRenderer;
import dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer;
import dev.simulated_team.simulated.content.blocks.analog_transmission.AnalogTransmissionVisual;
import dev.simulated_team.simulated.content.blocks.directional_gearshift.DirectionalGearshiftRenderer;
import dev.simulated_team.simulated.content.blocks.gimbal_sensor.GimbalSensorRenderer;
import dev.simulated_team.simulated.content.blocks.gimbal_sensor.GimbalSensorVisual;
import dev.simulated_team.simulated.content.blocks.nav_table.NavTableVisual;
import dev.simulated_team.simulated.content.blocks.physics_assembler.PhysicsAssemblerRenderer;
import dev.simulated_team.simulated.content.blocks.portable_engine.PortableEngineRotationDirectionClientBehaviour;
import dev.simulated_team.simulated.content.blocks.spring.SpringRenderer;
import dev.simulated_team.simulated.content.blocks.redstone.modulating_receiver.ModulatingLinkVisual;
import dev.simulated_team.simulated.content.blocks.redstone.redstone_inductor.RedstoneInductorRenderer;
import dev.simulated_team.simulated.content.blocks.redstone.redstone_inductor.RedstoneInductorVisual;
import dev.simulated_team.simulated.content.blocks.steering_wheel.SteeringWheelRenderer;
import dev.simulated_team.simulated.content.blocks.steering_wheel.SteeringWheelVisual;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelLockingClientBehaviour;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingVisual;
import dev.simulated_team.simulated.content.blocks.throttle_lever.ThrottleLeverVisual;
import dev.simulated_team.simulated.content.blocks.torsion_spring.TorsionSpringVisual;
import dev.simulated_team.simulated.content.items.merging_glue.MergingGlueItemHandler;
import dev.simulated_team.simulated.content.items.plunger_launcher.PlungerLauncherItemRenderer;
import dev.simulated_team.simulated.content.physics_staff.PhysicsStaffClientHandler;
import dev.simulated_team.simulated.client.model.SimulatedItemModels;
import dev.simulated_team.simulated.index.SimPartialModels;
import dev.simulated_team.simulated.index.SimBlocks;
import dev.simulated_team.simulated.index.SimBlockEntityTypes;
import dev.simulated_team.simulated.index.SimResourceManagers;
import dev.simulated_team.simulated.index.ponder.SimPonderPlugin;
import com.zurrtum.create.client.catnip.render.SuperByteBufferCache;
import com.zurrtum.create.client.ponder.foundation.PonderIndex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class SimulatedClient {

    public static final PhysicsStaffClientHandler PHYSICS_STAFF_CLIENT_HANDLER = new PhysicsStaffClientHandler();
    public static PlungerLauncherItemRenderer.RenderHandler PLUNGER_LAUNCHER_RENDER_HANDLER = new PlungerLauncherItemRenderer.RenderHandler();
	public static final MergingGlueItemHandler MERGING_GLUE_ITEM_HANDLER = new MergingGlueItemHandler();

    public static void init() {
        SimulatedItemModels.register();
        LegacyKineticTooltipBehaviour.installBridge();
        registerBlockEntityBehaviours();
        registerVisuals();
        registerSableImmediateRenderers();

        ItemDescription.useKey(SimBlocks.IRON_HANDLE.get(), "block.simulated.handle");
        ItemDescription.useKey(SimBlocks.COPPER_HANDLE.get(), "block.simulated.handle");
        SimBlocks.DYED_HANDLES.forEach(block -> ItemDescription.useKey(block, "block.simulated.handle"));
        SimBlocks.NAMEPLATES.forEach(block -> ItemDescription.useKey(block, "block.simulated.nameplate"));
        SimBlocks.PORTABLE_ENGINES.forEach(block ->
                ItemDescription.useKey(block, "block.simulated.red_portable_engine"));

        LegacyItemTooltips.register(Simulated.getRegistrate());

        SimPartialModels.init();
        BlockPropertiesTooltip.init();
        SimResourceManagers.init();

        PonderIndex.addPlugin(new SimPonderPlugin());

        SuperByteBufferCache.getInstance().registerCompartment(SteeringWheelRenderer.STEERING_WHEEL);
    }

    /**
     * The 1.21.10 port builds Simulated against Sable's published API artifact,
     * while the Sodium fallback hook is supplied by the sibling Sable project.
     * Register reflectively so the two jars remain independently buildable and
     * older Sable versions simply retain their previous rendering behavior.
     */
    private static void registerSableImmediateRenderers() {
        try {
            final Class<?> registry = Class.forName("dev.ryanhcode.sable.api.client.SubLevelBlockEntityRenderRegistry");
            final Class<?> rendererType = Class.forName("dev.ryanhcode.sable.api.client.SubLevelBlockEntityRenderRegistry$Renderer");
            final Method register = registry.getMethod("register", BlockEntityType.class, rendererType);

            registerSableImmediateRenderer(
                    register,
                    rendererType,
                    AllBlockEntityTypes.BRACKETED_KINETIC,
                    SableCreateKineticRenderer::renderBracketedKinetic
            );
            registerSableImmediateRenderer(
                    register,
                    rendererType,
                    AllBlockEntityTypes.BACKTANK,
                    SableCreateKineticRenderer::renderBacktank
            );
            registerCreateImmediateRenderers(
                    register,
                    rendererType,
                    AllBlockEntityTypes.GEARBOX,
                    AllBlockEntityTypes.CLUTCH,
                    AllBlockEntityTypes.GEARSHIFT,
                    AllBlockEntityTypes.ENCASED_SHAFT,
                    AllBlockEntityTypes.ADJUSTABLE_CHAIN_GEARSHIFT,
                    AllBlockEntityTypes.CHAIN_CONVEYOR,
                    AllBlockEntityTypes.BELT,
                    AllBlockEntityTypes.MOTOR,
                    AllBlockEntityTypes.WATER_WHEEL,
                    AllBlockEntityTypes.LARGE_WATER_WHEEL,
                    AllBlockEntityTypes.ENCASED_FAN,
                    AllBlockEntityTypes.TURNTABLE,
                    AllBlockEntityTypes.HAND_CRANK,
                    AllBlockEntityTypes.CUCKOO_CLOCK,
                    AllBlockEntityTypes.MILLSTONE,
                    AllBlockEntityTypes.CRUSHING_WHEEL,
                    AllBlockEntityTypes.MECHANICAL_PRESS,
                    AllBlockEntityTypes.MECHANICAL_MIXER,
                    AllBlockEntityTypes.HEATER,
                    AllBlockEntityTypes.DEPOT,
                    AllBlockEntityTypes.WEIGHTED_EJECTOR,
                    AllBlockEntityTypes.SPEEDOMETER,
                    AllBlockEntityTypes.STRESSOMETER,
                    AllBlockEntityTypes.FLUID_PIPE,
                    AllBlockEntityTypes.ENCASED_FLUID_PIPE,
                    AllBlockEntityTypes.MECHANICAL_PUMP,
                    AllBlockEntityTypes.SMART_FLUID_PIPE,
                    AllBlockEntityTypes.FLUID_VALVE,
                    AllBlockEntityTypes.VALVE_HANDLE,
                    AllBlockEntityTypes.HOSE_PULLEY,
                    AllBlockEntityTypes.ITEM_DRAIN,
                    AllBlockEntityTypes.SPOUT,
                    AllBlockEntityTypes.PORTABLE_FLUID_INTERFACE,
                    AllBlockEntityTypes.STEAM_ENGINE,
                    AllBlockEntityTypes.MECHANICAL_PISTON,
                    AllBlockEntityTypes.GANTRY_PINION,
                    AllBlockEntityTypes.GANTRY_SHAFT,
                    AllBlockEntityTypes.WINDMILL_BEARING,
                    AllBlockEntityTypes.MECHANICAL_BEARING,
                    AllBlockEntityTypes.CLOCKWORK_BEARING,
                    AllBlockEntityTypes.ROPE_PULLEY,
                    AllBlockEntityTypes.ELEVATOR_PULLEY,
                    AllBlockEntityTypes.STICKER,
                    AllBlockEntityTypes.DRILL,
                    AllBlockEntityTypes.SAW,
                    AllBlockEntityTypes.DEPLOYER,
                    AllBlockEntityTypes.PORTABLE_STORAGE_INTERFACE,
                    AllBlockEntityTypes.HARVESTER,
                    AllBlockEntityTypes.MECHANICAL_ROLLER,
                    AllBlockEntityTypes.MECHANICAL_CRAFTER,
                    AllBlockEntityTypes.SEQUENCED_GEARSHIFT,
                    AllBlockEntityTypes.FLYWHEEL,
                    AllBlockEntityTypes.MECHANICAL_ARM,
                    AllBlockEntityTypes.TRACK_STATION,
                    AllBlockEntityTypes.TRACK_SIGNAL,
                    AllBlockEntityTypes.TRACK_OBSERVER,
                    AllBlockEntityTypes.CONTRAPTION_CONTROLS,
                    AllBlockEntityTypes.PACKAGER,
                    AllBlockEntityTypes.REPACKAGER,
                    AllBlockEntityTypes.PACKAGE_FROGPORT,
                    AllBlockEntityTypes.PACKAGE_POSTBOX,
                    AllBlockEntityTypes.PACKAGER_LINK,
                    AllBlockEntityTypes.DISPLAY_LINK,
                    AllBlockEntityTypes.FLAP_DISPLAY,
                    AllBlockEntityTypes.NIXIE_TUBE,
                    AllBlockEntityTypes.ANALOG_LEVER,
                    AllBlockEntityTypes.PECULIAR_BELL,
                    AllBlockEntityTypes.HAUNTED_BELL,
                    AllBlockEntityTypes.TOOLBOX,
                    AllBlockEntityTypes.TRACK
            );
            // Brewin' And Chewin' Fly renders both the coaster and its contents
            // through its block-entity renderer, so neither is part of Sable's
            // normal sublevel block-model pass.
            registerOptionalRenderStateRenderer(
                    register,
                    rendererType,
                    ResourceLocation.fromNamespaceAndPath("brewinandchewin", "coaster")
            );
            registerSableImmediateRenderer(register, rendererType, SimBlockEntityTypes.PHYSICS_ASSEMBLER.get(),
                    (be, partialTick, poseStack, bufferSource, light, overlay) ->
                            PhysicsAssemblerRenderer.renderInSubLevel(
                                    (dev.simulated_team.simulated.content.blocks.physics_assembler.PhysicsAssemblerBlockEntity) be,
                                    partialTick, poseStack, bufferSource, light, overlay));
            registerSableImmediateRenderer(register, rendererType, SimBlockEntityTypes.SPRING.get(),
                    (be, partialTick, poseStack, bufferSource, light, overlay) ->
                            SpringRenderer.renderInSubLevel(
                                    (dev.simulated_team.simulated.content.blocks.spring.SpringBlockEntity) be,
                                    partialTick, poseStack, bufferSource, light, overlay));
            registerSableImmediateRenderer(register, rendererType, SimBlockEntityTypes.GIMBAL_SENSOR.get(),
                    (be, partialTick, poseStack, bufferSource, light, overlay) ->
                            GimbalSensorRenderer.renderInSubLevel(
                                    (dev.simulated_team.simulated.content.blocks.gimbal_sensor.GimbalSensorBlockEntity) be,
                                    partialTick, poseStack, bufferSource, light, overlay));

            // Explicit compatibility list: do not opt unrelated block entities
            // into the immediate renderer automatically.
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.NAVIGATION_TABLE.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.LASER_POINTER.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.OPTICAL_SENSOR.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.ALTITUDE_SENSOR.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.VELOCITY_SENSOR.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.NAMEPLATE.get());
            registerSableImmediateRenderer(register, rendererType, SimBlockEntityTypes.PORTABLE_ENGINE.get(),
                    (be, partialTick, poseStack, bufferSource, light, overlay) -> {
                        if (be.getBlockState().is(SimBlocks.RED_PORTABLE_ENGINE.get())) {
                            renderRegisteredLegacyRenderer(be, partialTick, poseStack, bufferSource, light, overlay);
                        }
                    });
            registerSableImmediateRenderer(register, rendererType, SimBlockEntityTypes.AUGER_SHAFT.get(),
                    (be, partialTick, poseStack, bufferSource, light, overlay) -> {
                        if (be.getBlockState().is(SimBlocks.AUGER_COG.get())) {
                            renderRegisteredLegacyRenderer(be, partialTick, poseStack, bufferSource, light, overlay);
                        }
                    });
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.TORSION_SPRING.get());
            registerSableImmediateRenderer(register, rendererType, SimBlockEntityTypes.DIRECTIONAL_GEARSHIFT.get(),
                    (be, partialTick, poseStack, bufferSource, light, overlay) ->
                            DirectionalGearshiftRenderer.renderInSubLevel(
                                    (dev.simulated_team.simulated.content.blocks.directional_gearshift.DirectionalGearshiftBlockEntity) be,
                                    partialTick, poseStack, bufferSource, light, overlay));
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.ROPE_WINCH.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.ROPE_CONNECTOR.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.SWIVEL_BEARING.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.DOCKING_CONNECTOR.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.MERGING_GLUE.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.SIMPLE_BE.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.STEERING_WHEEL.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.THROTTLE_LEVER.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.LINKED_TYPEWRITER.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.MODULATING_LINKED_RECEIVER.get());
            registerLegacySubLevelRenderer(register, rendererType, SimBlockEntityTypes.REDSTONE_ACCUMULATOR.get());
            registerSableImmediateRenderer(register, rendererType, SimBlockEntityTypes.REDSTONE_INDUCTOR.get(),
                    (be, partialTick, poseStack, bufferSource, light, overlay) ->
                            RedstoneInductorRenderer.renderInSubLevel(
                                    (dev.simulated_team.simulated.content.blocks.redstone.redstone_inductor.RedstoneInductorBlockEntity) be,
                                    partialTick, poseStack, bufferSource, light, overlay));
        } catch (final ReflectiveOperationException e) {
            Simulated.LOGGER.debug("Sable immediate block-entity renderer hook is unavailable", e);
        }
    }

    private static void registerOptionalRenderStateRenderer(
            final Method register,
            final Class<?> rendererType,
            final ResourceLocation blockEntityTypeId
    ) throws ReflectiveOperationException {
        final BlockEntityType<?> blockEntityType = BuiltInRegistries.BLOCK_ENTITY_TYPE
                .getOptional(blockEntityTypeId)
                .orElse(null);
        if (blockEntityType != null) {
            registerSableImmediateRenderer(
                    register,
                    rendererType,
                    blockEntityType,
                    SableCreateBlockEntityRenderer::render
            );
        }
    }

    private static void registerLegacySubLevelRenderer(final Method register, final Class<?> rendererType,
                                                       final BlockEntityType<?> blockEntityType)
            throws ReflectiveOperationException {
        registerSableImmediateRenderer(register, rendererType, blockEntityType,
                SimulatedClient::renderRegisteredLegacyRenderer);
    }

    private static void registerCreateImmediateRenderers(
            final Method register,
            final Class<?> rendererType,
            final BlockEntityType<?>... blockEntityTypes
    ) throws ReflectiveOperationException {
        for (final BlockEntityType<?> blockEntityType : blockEntityTypes) {
            registerSableImmediateRenderer(
                    register,
                    rendererType,
                    blockEntityType,
                    SableCreateBlockEntityRenderer::render
            );
        }
    }

    private static void registerSableImmediateRenderer(final Method register, final Class<?> rendererType,
                                                       final BlockEntityType<?> blockEntityType,
                                                       final SableImmediateRenderer renderer)
            throws ReflectiveOperationException {
        final Object proxy = Proxy.newProxyInstance(
                SimulatedClient.class.getClassLoader(),
                new Class<?>[]{rendererType},
                (ignoredProxy, method, args) -> {
                    if ("render".equals(method.getName()) && args != null && args.length == 6) {
                        renderer.render(
                                (BlockEntity) args[0],
                                (float) args[1],
                                (PoseStack) args[2],
                                (MultiBufferSource) args[3],
                                (int) args[4],
                                (int) args[5]
                        );
                    }
                    return null;
                }
        );
        register.invoke(null, blockEntityType, proxy);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void renderRegisteredLegacyRenderer(final BlockEntity blockEntity, final float partialTick,
                                                       final PoseStack poseStack, final MultiBufferSource bufferSource,
                                                       final int light, final int overlay) {
        final Object registeredRenderer = Minecraft.getInstance()
                .getBlockEntityRenderDispatcher()
                .getRenderer(blockEntity);
        if (registeredRenderer instanceof final SmartBlockEntityRenderer<?> renderer) {
            ((SmartBlockEntityRenderer) renderer).renderExplicitlyInSubLevel(
                    blockEntity, partialTick, poseStack, bufferSource, light, overlay);
        }
    }

    @FunctionalInterface
    private interface SableImmediateRenderer {
        void render(BlockEntity blockEntity, float partialTick, PoseStack poseStack,
                    MultiBufferSource bufferSource, int light, int overlay);
    }

    private static void registerBlockEntityBehaviours() {
        AllBlockEntityBehaviours.add(SimBlockEntityTypes.SIMPLE_BE.get(), LegacyKineticTooltipBehaviour::new, KineticAudioBehaviour::new);
        AllBlockEntityBehaviours.add(SimBlockEntityTypes.TORSION_SPRING.get(), LegacyKineticTooltipBehaviour::new, KineticAudioBehaviour::new, LegacyScrollValueClientBehaviour::new);
        AllBlockEntityBehaviours.add(SimBlockEntityTypes.ROPE_WINCH.get(), LegacyKineticTooltipBehaviour::new, KineticAudioBehaviour::new);
        AllBlockEntityBehaviours.add(SimBlockEntityTypes.PORTABLE_ENGINE.get(), LegacyKineticTooltipBehaviour::new, KineticAudioBehaviour::new, PortableEngineRotationDirectionClientBehaviour::new);
        AllBlockEntityBehaviours.add(SimBlockEntityTypes.SWIVEL_BEARING.get(), LegacyKineticTooltipBehaviour::new, KineticAudioBehaviour::new, SwivelLockingClientBehaviour::new);
        AllBlockEntityBehaviours.add(SimBlockEntityTypes.STEERING_WHEEL.get(), LegacyKineticTooltipBehaviour::new, KineticAudioBehaviour::new, LegacyScrollValueClientBehaviour::new);
        AllBlockEntityBehaviours.add(SimBlockEntityTypes.SWIVEL_BEARING_LINK_BLOCK.get(), LegacyKineticTooltipBehaviour::new, KineticAudioBehaviour::new);
        AllBlockEntityBehaviours.add(SimBlockEntityTypes.AUGER_SHAFT.get(), LegacyKineticTooltipBehaviour::new, KineticAudioBehaviour::new);
        AllBlockEntityBehaviours.add(SimBlockEntityTypes.DIRECTIONAL_GEARSHIFT.get(), LegacyCustomKineticTooltipBehaviour::new, KineticAudioBehaviour::new);

        AllBlockEntityBehaviours.add(SimBlockEntityTypes.PHYSICS_ASSEMBLER.get(), LegacyTooltipBehaviour::new);
        AllBlockEntityBehaviours.add(SimBlockEntityTypes.ALTITUDE_SENSOR.get(), LegacyTooltipBehaviour::new);
        AllBlockEntityBehaviours.add(SimBlockEntityTypes.GIMBAL_SENSOR.get(), LegacyTooltipBehaviour::new, LegacyScrollValueClientBehaviour::new);
        AllBlockEntityBehaviours.add(SimBlockEntityTypes.VELOCITY_SENSOR.get(), LegacyTooltipBehaviour::new, LegacyScrollValueClientBehaviour::new);
        AllBlockEntityBehaviours.add(SimBlockEntityTypes.THROTTLE_LEVER.get(), LegacyTooltipBehaviour::new);
        AllBlockEntityBehaviours.add(SimBlockEntityTypes.REDSTONE_ACCUMULATOR.get(), LegacyTooltipBehaviour::new, LegacyScrollValueClientBehaviour::new);
        AllBlockEntityBehaviours.add(SimBlockEntityTypes.REDSTONE_INDUCTOR.get(), LegacyTooltipBehaviour::new, LegacyScrollValueClientBehaviour::new);

        AllBlockEntityBehaviours.add(SimBlockEntityTypes.OPTICAL_SENSOR.get(), LegacyScrollValueClientBehaviour::new, LegacyFilteringClientBehaviour::new);
        AllBlockEntityBehaviours.add(SimBlockEntityTypes.LASER_POINTER.get(), LegacyScrollValueClientBehaviour::new);
        AllBlockEntityBehaviours.add(SimBlockEntityTypes.LASER_SENSOR.get(), LegacyFilteringClientBehaviour::new);
    }

    private static void registerVisuals() {
        SimpleBlockEntityVisualizer.builder(SimBlockEntityTypes.SIMPLE_BE.get()).factory(AnalogTransmissionVisual::new).apply();
        SimpleBlockEntityVisualizer.builder(SimBlockEntityTypes.TORSION_SPRING.get()).factory(TorsionSpringVisual::new).apply();
        SimpleBlockEntityVisualizer.builder(SimBlockEntityTypes.GIMBAL_SENSOR.get()).factory(GimbalSensorVisual::new).apply();
        SimpleBlockEntityVisualizer.builder(SimBlockEntityTypes.SWIVEL_BEARING.get()).factory(SwivelBearingVisual::new).apply();
        SimpleBlockEntityVisualizer.builder(SimBlockEntityTypes.STEERING_WHEEL.get()).factory(SteeringWheelVisual::new).apply();
        SimpleBlockEntityVisualizer.builder(SimBlockEntityTypes.THROTTLE_LEVER.get()).factory(ThrottleLeverVisual::new).apply();
        SimpleBlockEntityVisualizer.builder(SimBlockEntityTypes.NAVIGATION_TABLE.get()).factory(NavTableVisual::new).apply();
        SimpleBlockEntityVisualizer.builder(SimBlockEntityTypes.MODULATING_LINKED_RECEIVER.get()).factory(ModulatingLinkVisual::new).apply();
        SimpleBlockEntityVisualizer.builder(SimBlockEntityTypes.REDSTONE_INDUCTOR.get()).factory(RedstoneInductorVisual::new).apply();
        SimpleBlockEntityVisualizer.builder(SimBlockEntityTypes.DIRECTIONAL_GEARSHIFT.get()).factory(SplitShaftVisual::new).apply();
    }
}
