// Copyright (c) 2017-2019 Twitter, Inc.
// Licensed under the Apache License, Version 2.0 (see LICENSE.md).
package rsc.parse.scala

import rsc.lexis.scala._
import rsc.report._
import rsc.syntax._
import rsc.util._

trait Paths {
  self: Parser =>

  private def value(): String = {
    if (in.token == ID) {
      val value = in.idValue
      if (value == "<init>") {
        reportOffset(in.offset, IllegalIdentifier)
        gensym.error()
      } else {
        in.nextToken()
        value
      }
    } else {
      reportOffset(in.offset, ExpectedToken(_, ID, in.token))
      gensym.error()
    }
  }

  def ambigId(): AmbigId = {
    atPos(in.offset)(AmbigId(value()))
  }

  def anonId(): AnonId = {
    atPos(in.offset)(AnonId())
  }

  def termId(): TermId = {
    atPos(in.offset)(TermId(value()))
  }

  def tptId(): TptId = {
    atPos(in.offset)(TptId(value()))
  }

  def patPath(): Pat = {
    termPath() match {
      case _: TermThis =>
        val errOffset = in.offset
        reportOffset(errOffset, ExpectedToken(_, DOT, in.token))
        atPos(errOffset)(errorPat())
      case termPath =>
        patPath(termPath)
    }
  }

  def patPath(termPath: TermPath): Pat = {
    termPath match {
      case TermId(value) =>
        atPos(termPath.pos)(PatId(value))
      case TermSelect(qual: TermPath, termId) =>
        atPos(termPath.pos)(PatSelect(qual, termId))
      case _ =>
        crash(termPath)
    }
  }

  def termPath(): TermPath = {
    val start = in.offset
    rawPath() match {
      case UnstartedPath =>
        errorTermId()
      case UnfinishedPath(path) =>
        val idErr = errorTermId()
        atPos(start)(TermSelect(path, idErr))
      case FinishedPath(path) =>
        path
    }
  }

  def tptPath(): TptPath = {
    val start = in.offset
    rawPath() match {
      case UnstartedPath =>
        errorTptId()
      case UnfinishedPath(path) =>
        if (in.token == TYPE) {
          in.nextToken()
          atPos(start)(TptSingleton(path))
        } else {
          val idErr = errorTptId()
          atPos(start)(TptSelect(path, idErr))
        }
      case FinishedPath(path) =>
        path match {
          case TermId(value) =>
            atPos(path.pos)(TptId(value))
          case TermSelect(qual: TermPath, termId @ TermId(value)) =>
            val tptId = atPos(termId.pos)(TptId(value))
            atPos(path.pos)(TptSelect(qual, tptId))
          case _ =>
            crash(path)
        }
    }
  }

  private sealed trait RawPath
  private case object UnstartedPath extends RawPath
  private case class UnfinishedPath(path: TermPath) extends RawPath
  private case class FinishedPath(path: TermPath) extends RawPath

  private def rawPath(): RawPath = {
    val start = in.offset
    def loop(qual: TermPath): RawPath = {
      if (in.token == DOT) {
        in.nextToken()
        if (in.token == ID) {
          val nextId = termId()
          loop(atPos(start)(TermSelect(qual, nextId)))
        } else {
          UnfinishedPath(qual)
        }
      } else {
        FinishedPath(qual)
      }
    }
    if (in.token == ID) {
      val firstId = termId()
      if (in.token == DOT) {
        in.nextToken()
        if (in.token == ID) {
          val nextId = termId()
          loop(atPos(start)(TermSelect(firstId, nextId)))
        } else if (in.token == THIS) {
          in.nextToken()
          val qual = atPos(firstId.pos)(AmbigId(firstId.value))
          loop(atPos(start)(TermThis(qual)))
        } else if (in.token == SUPER) {
          in.nextToken()
          val qual = atPos(firstId.pos)(AmbigId(firstId.value))
          val mix = {
            if (in.token == LBRACKET) {
              inBrackets(atPos(in.offset)(ambigId()))
            } else {
              anonId()
            }
          }
          loop(atPos(start)(TermSuper(qual, mix)))
        } else {
          UnfinishedPath(firstId)
        }
      } else {
        FinishedPath(firstId)
      }
    } else if (in.token == THIS) {
      in.nextToken()
      val qual = anonId()
      loop(atPos(start)(TermThis(qual)))
    } else if (in.token == SUPER) {
      in.nextToken()
      val qual = anonId()
      val mix = {
        if (in.token == LBRACKET) {
          inBrackets(atPos(in.offset)(ambigId()))
        } else {
          anonId()
        }
      }
      loop(atPos(start)(TermSuper(qual, mix)))
    } else {
      UnstartedPath
    }
  }

  def errorAmbigId(): AmbigId = {
    ambigId()
  }

  def errorTermId(): TermId = {
    termId()
  }

  def errorTptId(): TptId = {
    tptId()
  }
}
