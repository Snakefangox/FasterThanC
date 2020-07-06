package net.snakefangox.fasterthanc.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.snakefangox.fasterthanc.FRegister;
import net.snakefangox.fasterthanc.blocks.blockentities.HighCapacityCableBE;
import net.snakefangox.fasterthanc.energy.CableNetworkStorage;
import net.snakefangox.fasterthanc.energy.Energy;

public class HighCapacityCable extends Block implements BlockEntityProvider {

	protected static final BooleanProperty CONNECTED_NORTH = BooleanProperty.of("north");
	protected static final BooleanProperty CONNECTED_SOUTH = BooleanProperty.of("south");
	protected static final BooleanProperty CONNECTED_EAST = BooleanProperty.of("east");
	protected static final BooleanProperty CONNECTED_WEST = BooleanProperty.of("west");
	protected static final BooleanProperty CONNECTED_UP = BooleanProperty.of("up");
	protected static final BooleanProperty CONNECTED_DOWN = BooleanProperty.of("down");

	private static final VoxelShape MAIN_BOX = VoxelShapes.cuboid(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
	public VoxelShape BOX_N = VoxelShapes.cuboid(0.25, 0.25, 0, 0.75, 0.75, 0.3);
	public VoxelShape BOX_E;
	public VoxelShape BOX_S;
	public VoxelShape BOX_W;
	public VoxelShape BOX_U;
	public VoxelShape BOX_D;

	public HighCapacityCable(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(CONNECTED_NORTH, false).with(CONNECTED_SOUTH, false)
				.with(CONNECTED_EAST, false).with(CONNECTED_WEST, false).with(CONNECTED_UP, false)
				.with(CONNECTED_DOWN, false));
		Box box = BOX_N.getBoundingBox();
		BOX_E = VoxelShapes.cuboid(1F - box.minZ, box.minY, box.minX, 1F - box.maxZ, box.maxY, box.maxX);
		BOX_S = VoxelShapes.cuboid(box.minX, box.minY, 1F - box.minZ, box.maxX, box.maxY, 1F - box.maxZ);
		BOX_W = VoxelShapes.cuboid(box.minZ, box.minY, box.minX, box.maxZ, box.maxY, box.maxX);
		BOX_U = VoxelShapes.cuboid(box.minX, 1F - box.minZ, box.minY, box.maxX, 1F - box.maxZ, box.maxY);
		BOX_D = VoxelShapes.cuboid(box.minX, box.minZ, box.minY, box.maxX, box.maxZ, box.maxY);
	}

	private void createNetwork(World world, BlockPos pos) {
		if (!(world instanceof ServerWorld))
			return;
		BlockEntity be = world.getBlockEntity(pos);
		boolean firstNetwork = true;
		if (be instanceof HighCapacityCableBE) {
			for (int i = 0; i < Direction.values().length; i++) {
				BlockEntity beo = world.getBlockEntity(pos.offset(Direction.values()[i]));
				if (beo instanceof HighCapacityCableBE) {
					if (firstNetwork) {
						firstNetwork = false;
						CableNetworkStorage.getInstance((ServerWorld) world)
								.addToCableNetwork(((HighCapacityCableBE) beo).getNetwork(), pos, world);
					} else {
						CableNetworkStorage.getInstance((ServerWorld) world).mergeCableNetworks(
								((HighCapacityCableBE) be).getNetwork(), ((HighCapacityCableBE) beo).getNetwork(), world);
					}
				}
			}
			if (firstNetwork) {
				CableNetworkStorage storage = CableNetworkStorage.getInstance((ServerWorld) world);
				storage.createNewCableNetwork(pos, world, storage.getNextID());
			}
		}
	}

	@Override
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		super.onBlockAdded(state, world, pos, oldState, notify);
		createNetwork(world, pos);
	}

	@Override
	public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
		super.onBroken(world, pos, state);
		if (!(world instanceof ServerWorld))
			return;
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof HighCapacityCableBE) {
			CableNetworkStorage.getInstance((ServerWorld) world)
					.removeCableFromNetwork(((HighCapacityCableBE) be).getNetwork(), pos, world);
		}
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
		VoxelShape shape = MAIN_BOX;
		if (state.get(CONNECTED_NORTH))
			shape = VoxelShapes.union(shape, BOX_N);
		if (state.get(CONNECTED_SOUTH))
			shape = VoxelShapes.union(shape, BOX_S);
		if (state.get(CONNECTED_EAST))
			shape = VoxelShapes.union(shape, BOX_E);
		if (state.get(CONNECTED_WEST))
			shape = VoxelShapes.union(shape, BOX_W);
		if (state.get(CONNECTED_UP))
			shape = VoxelShapes.union(shape, BOX_U);
		if (state.get(CONNECTED_DOWN))
			shape = VoxelShapes.union(shape, BOX_D);
		return shape;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(CONNECTED_NORTH, CONNECTED_SOUTH, CONNECTED_EAST, CONNECTED_WEST, CONNECTED_UP, CONNECTED_DOWN);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return getStateForCable(ctx.getWorld(), ctx.getBlockPos());
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
		return getStateForCable(world, pos);
	}

	private BlockState getStateForCable(WorldAccess world, BlockPos pos) {
		BlockEntity north = world.getBlockEntity(pos.offset(Direction.NORTH));
		BlockEntity south = world.getBlockEntity(pos.offset(Direction.SOUTH));
		BlockEntity east = world.getBlockEntity(pos.offset(Direction.EAST));
		BlockEntity west = world.getBlockEntity(pos.offset(Direction.WEST));
		BlockEntity up = world.getBlockEntity(pos.offset(Direction.UP));
		BlockEntity down = world.getBlockEntity(pos.offset(Direction.DOWN));

		return getDefaultState()
				.with(CONNECTED_NORTH, (north instanceof Energy && ((Energy) north).canCableConnect(Direction.SOUTH)))
				.with(CONNECTED_SOUTH, (south instanceof Energy && ((Energy) south).canCableConnect(Direction.NORTH)))
				.with(CONNECTED_EAST, (east instanceof Energy && ((Energy) east).canCableConnect(Direction.WEST)))
				.with(CONNECTED_WEST, (west instanceof Energy && ((Energy) west).canCableConnect(Direction.EAST)))
				.with(CONNECTED_UP, (up instanceof Energy && ((Energy) up).canCableConnect(Direction.DOWN)))
				.with(CONNECTED_DOWN, (down instanceof Energy && ((Energy) down).canCableConnect(Direction.UP)));
	}

	@Override
	public BlockEntity createBlockEntity(BlockView world) {
		return FRegister.high_capacity_cable_type.instantiate();
	}
}
