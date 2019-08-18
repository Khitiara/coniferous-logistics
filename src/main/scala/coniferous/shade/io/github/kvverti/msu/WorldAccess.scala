package coniferous.shade.io.github.kvverti.msu

import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.{Block, BlockState}
import net.minecraft.util.math.{BlockPos, Direction}
import net.minecraft.world.BlockView

import scala.reflect.ClassTag

object WorldAccess {

  implicit class RichBlockState(val self: BlockState) extends AnyVal {

    def typedBlock[B <: Block : ClassTag]: Option[B] = {
      self.getBlock match {
        case b: B => Some(b)
        case _ => None
      }
    }

    def collectBlock[A](pf: PartialFunction[Block, A]): Option[A] =
      Option(self.getBlock) collect pf
  }

  implicit class RichWorld(val self: BlockView) extends AnyVal {

    def typedBlockEntity[B <: BlockEntity : ClassTag](pos: BlockPos): Option[B] = {
      self.getBlockEntity(pos) match {
        case b: B => Some(b)
        case _ => None
      }
    }

    def collectBlockEntity[A](pos: BlockPos)(pf: PartialFunction[BlockEntity, A]): Option[A] =
      Option(self.getBlockEntity(pos)) collect pf
  }

  implicit class RichBlockPos(val self: BlockPos) extends AnyVal {
    def neighbors: Seq[BlockPos] = Direction.values.map(self.offset)
  }

}
