package coniferous.block

import coniferous.Coniferous
import net.minecraft.block.Block
import net.minecraft.item.{BlockItem, Item}
import net.minecraft.util.registry.Registry

import scala.collection.mutable

object BlockRegistry {

  val registrants: mutable.ListBuffer[BlockEntry] = mutable.ListBuffer.empty[BlockEntry]
  val entries: mutable.Map[String, Block] = mutable.HashMap.empty[String, Block]

  def init(): Unit = registrants.foreach { case BlockEntry(name, block, item) =>
    entries(name) = Registry.register(Registry.BLOCK, Coniferous :/ name, block)
    item.foreach(i => Registry.register(Registry.ITEM, Coniferous :/ name, i))
  }

  case class BlockEntry private[block](name: String, block: Block, item: Option[BlockItem])

  object BlockEntry {
    def apply(name: String, item: Option[BlockItem]): Block => BlockEntry = apply(name, _, item)

    def apply(name: String): Block => BlockEntry = apply(name, _)

    private[block] def apply(name: String, block: Block): BlockEntry = {
      val item = Some(new BlockItem(block, new Item.Settings().group(Coniferous.group)))
      apply(name, block, item)
    }
  }

}
