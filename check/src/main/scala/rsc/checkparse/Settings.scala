// Copyright (c) 2017-2019 Twitter, Inc.
// Licensed under the Apache License, Version 2.0 (see LICENSE.md).
package rsc.checkparse

import java.nio.file._
import rsc.checkbase._

final case class Settings(ins: List[Path] = Nil, quiet: Boolean = false) extends SettingsBase

// FIXME: https://github.com/twitter/rsc/issues/166
object Settings {
  def parse(args: List[String]): Either[List[String], Settings] = {
    def loop(
        settings: Settings,
        allowOptions: Boolean,
        args: List[String]): Either[List[String], Settings] = {
      args match {
        case "--" +: rest =>
          loop(settings, false, rest)
        case "--quiet" +: rest if allowOptions =>
          loop(settings.copy(quiet = true), true, rest)
        case flag +: rest if allowOptions && flag.startsWith("-") =>
          Left(List(s"unknown flag $flag"))
        case in +: rest =>
          val ins = List(Paths.get(in))
          loop(settings.copy(ins = settings.ins ++ ins), allowOptions, rest)
        case Nil =>
          Right(settings)
      }
    }
    loop(Settings(), true, args)
  }
}
