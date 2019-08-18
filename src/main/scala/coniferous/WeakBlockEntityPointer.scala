package coniferous

import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import coniferous.shade.io.github.kvverti.msu.WorldAccess._

import scala.ref.WeakReference
import scala.reflect.ClassTag

/**
 * Weak reference to a block entity position.
 * If the stored weak block entity is null, [[WeakBlockEntityPointer]]
 * re-attempts to fetch the block entity from the given [[BlockView]]
 *
 * @param pos    Position of the block entity
 * @param access Access to the world or a relevant subset thereof
 * @tparam A The block entity type
 */
case class WeakBlockEntityPointer[A >: Null <: BlockEntity : ClassTag](pos: BlockPos, access: BlockView) {
  private var ptr: WeakReference[A] = WeakReference[A](access.typedBlockEntity[A](pos).orNull)

  def get: Option[A] = ptr.get.orElse {
    val be = access.typedBlockEntity[A](pos)
    be.foreach(e => ptr = WeakReference(e))
    be
  }
}
