package net.novucs.ftop.delayedspawners;

import net.novucs.ftop.entity.BlockPos;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

import java.util.Objects;

public class DelayedSpawner {

	private final BlockPos pos;
	private final EntityType entityType;

	public static DelayedSpawner of(CreatureSpawner spawner) {
		return new DelayedSpawner(BlockPos.of(spawner.getBlock()), spawner.getSpawnedType());
	}

	public DelayedSpawner(BlockPos pos, EntityType entityType) {
		this.pos = pos;
		this.entityType = entityType;
	}

	public BlockPos getPos() {
		return pos;
	}

	public EntityType getEntityType() {
		return entityType;
	}

	@Override
	public boolean equals(Object obj) {
		if ((!(obj instanceof DelayedSpawner)))
			return false;

		DelayedSpawner other = (DelayedSpawner) obj;
		return other.getEntityType().equals(getEntityType()) && other.getPos().equals(getPos());
	}

	@Override
	public int hashCode() {
		return Objects.hash(pos, entityType);
	}

	@Override
	public String toString() {
		return "DelayedSpawner{" +
				"pos=" + pos +
				"entityType=" + entityType +
				'}';
	}
}
