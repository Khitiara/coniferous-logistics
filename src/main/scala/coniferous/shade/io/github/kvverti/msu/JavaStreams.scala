package coniferous.shade.io.github.kvverti.msu

import java.util
//import java.util.Spliterator
//import java.util.stream.{BaseStream, DoubleStream, IntStream, LongStream, Stream => ObjStream}
//
//import scala.collection.immutable
//import scala.collection.parallel.IterableSplitter
//import scala.collection.parallel.immutable.ParIterable
//
//import scala.collection.JavaConverters._

object JavaStreams {

  //
  //  class StreamBridge[A](val src: BaseStream[A, _]) extends ParIterable[A] {
  //
  //    private class Splitting(val self: Spliterator[A]) extends IterableSplitter[A] {
  //      var n: A = _
  //
  //      override def split: Seq[IterableSplitter[A]] = self.trySplit() match {
  //        case null => Seq(this)
  //        case other => Seq(this, new Splitting(other))
  //      }
  //
  //      override def dup: IterableSplitter[A] = splitter
  //
  //      override def remaining: Int = {
  //        val int = self.estimateSize().toInt
  //        if (int < 0) while (true) {}
  //        int
  //      }
  //
  //      override def hasNext: Boolean = self.tryAdvance(a => n = a)
  //
  //      override def next(): A = n
  //    }
  //
  //    override def seq: immutable.Iterable[A] = new immutable.Iterable[A] {
  //      override def iterator: Iterator[A] = src.iterator().asScala
  //    }
  //
  //    def splitter: IterableSplitter[A] = new Splitting(src.spliterator())
  //
  //    override def size: Int = src.spliterator().getExactSizeIfKnown.toInt
  //  }
  //
  //  implicit class ObjStreamBridge[A](val self: ObjStream[A]) extends AnyVal {
  //    def asScala: ParIterable[A] = new StreamBridge(self)
  //  }
  //
  //  implicit class IntStreamBridge(val self: IntStream) extends AnyVal {
  //    def asScala: ParIterable[Int] = new StreamBridge(self).map(Int.unbox)
  //  }
  //
  //  implicit class LongStreamBridge(val self: LongStream) extends AnyVal {
  //    def asScala: ParIterable[Long] = new StreamBridge(self).map(Long.unbox)
  //  }
  //
  //  implicit class DoubleStreamBridge(val self: DoubleStream) extends AnyVal {
  //    def asScala: ParIterable[Double] = new StreamBridge(self).map(Double.unbox)
  //  }

  implicit class OptionalBridge[A](val self: util.Optional[A]) extends AnyVal {
    def withFilter(p: A => Boolean): OptionalBridgeWithFilter[A] =
      new OptionalBridgeWithFilter(self, p)

    def foreach(f: A => Unit): Unit = self.ifPresent(f(_))

    def ofType[A1 >: A]: util.Optional[A1] = self.asInstanceOf[util.Optional[A1]]

    def toOption: Option[A] = if (self.isPresent) Some(self.get) else None
  }

  class OptionalBridgeWithFilter[A](self: util.Optional[A], p: A => Boolean) {
    def flatMap[B](f: A => util.Optional[B]): util.Optional[B] =
      self.filter(p(_)).flatMap(f(_))

    def map[B](f: A => B): util.Optional[B] = self.filter(p(_)).map(f(_))

    def withFilter(q: A => Boolean): OptionalBridgeWithFilter[A] =
      new OptionalBridgeWithFilter(self, a => p(a) && q(a))

    def foreach(f: A => Unit): Unit = self.filter(p(_)).ifPresent(f(_))
  }

}
