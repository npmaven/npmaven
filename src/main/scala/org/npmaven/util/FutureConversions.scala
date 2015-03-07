package org.npmaven.util

import net.liftweb.actor.LAFuture
import net.liftweb.common.{Empty, Failure, Full, Box}

import scala.concurrent.{ExecutionContext, Future}

// Borrowed from lift-ng
// https://github.com/joescii/lift-ng/blob/master/src/main/scala/net/liftmodules/ng/Futures_2.10.scala
object FutureConversions {
  implicit def FutureToLAFuture[T](f:Future[T])(implicit ctx:ExecutionContext):LAFuture[Box[T]] = f.la

  implicit class ConvertToLA[T](f: Future[T])(implicit ctx:ExecutionContext) {
    lazy val la:LAFuture[Box[T]] = {
      val laf = new LAFuture[Box[T]]()
      f.foreach(t => laf.satisfy(Full(t)))
      f.onFailure({ case t:Throwable => laf.satisfy(Failure(t.getMessage, Full(t), Empty)) })
      laf
    }
  }
}
