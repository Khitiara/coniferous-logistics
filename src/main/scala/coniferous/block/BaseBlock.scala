package coniferous.block

import coniferous.block.BlockRegistry.BlockEntry
import net.minecraft.block.Block
import net.minecraft.item.BlockItem

class BaseBlock(entry: Block => BlockEntry, settings: Block.Settings) extends Block(settings) {
  BlockRegistry.registrants.append(entry(this))

  def this(name: String, settings: Block.Settings) {
    this(BlockEntry(name), settings)
  }

  def this(name: String, item: Option[BlockItem], settings: Block.Settings) {
    this(BlockEntry(name, item), settings)
  }
}
