// Copyright (c) 2017-2019 Twitter, Inc.
// Licensed under the Apache License, Version 2.0 (see LICENSE.md).
package scala.meta.cli

import scala.meta.internal.cli._
import scala.meta.internal.scalap._
import scala.meta.scalap._

object Scalap {
  def main(args: Array[String]): Unit = {
    val expandedArgs = Args.expand(args)
    val reporter = Reporter().withOut(System.out).withErr(System.err)
    Settings.parse(expandedArgs, reporter) match {
      case Some(settings) =>
        if (process(settings, reporter)) sys.exit(0)
        else sys.exit(1)
      case None =>
        sys.exit(1)
    }
  }

  def process(settings: Settings, reporter: Reporter): Boolean = {
    val main = new Main(settings, reporter)
    main.process()
  }

}
