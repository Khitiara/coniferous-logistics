package coniferous.api

import coniferous.pipe.Pipe
import net.minecraft.util.math.Direction

trait PipeConnectable {
  def canConnect(pipe: Pipe, side: Direction): Boolean
}
