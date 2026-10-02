package ru.sirenblock.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import ru.sirenblock.SirenBlockMod;
import ru.sirenblock.block.entity.SirenBlockEntity;
import ru.sirenblock.network.OpenSirenScreenPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class SirenBlock extends BaseEntityBlock {
    public SirenBlock(Properties properties) { super(properties); }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(SirenBlock::new);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SirenBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof SirenBlockEntity be && player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, OpenSirenScreenPayload.from(be));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, SirenBlockMod.SIREN_BE, (lvl, p, s, be) -> be.serverTick());
    }
}
