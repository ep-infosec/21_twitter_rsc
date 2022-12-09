// Copyright (c) 2017-2019 Twitter, Inc.
// Licensed under the Apache License, Version 2.0 (see LICENSE.md).
package rsc.checkoutline

import java.nio.file._
import rsc.checkbase._

object Main extends SimpleBase[Settings, Path, Path] {
  def settings(args: List[String]) = {
    Settings.parse(args)
  }

  def nscResult(settings: Settings) = {
    val nscJar = nsc(settings.cp, settings.ins)
    val metaJars = nscJar.right.flatMap(path => metacp(settings.cp, List(path)))
    metaJars.right.map(_.head)
  }

  def rscResult(settings: Settings) = {
    rsc(settings.cp, settings.ins)
  }

  def checker(settings: Settings, nscResult: Path, rscResult: Path) = {
    new Checker(nscResult, rscResult)
  }
}
