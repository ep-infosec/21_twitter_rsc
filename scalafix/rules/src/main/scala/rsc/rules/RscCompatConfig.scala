// Copyright (c) 2017-2019 Twitter, Inc.
// Licensed under the Apache License, Version 2.0 (see LICENSE.md).
package rsc.rules

import metaconfig._
import metaconfig.generic._

case class RscCompatConfig(
    autoimport: Boolean,
    better: Boolean,
    hardcoded: Map[String, String],
)

object RscCompatConfig {
  lazy val default = RscCompatConfig(
    autoimport = false,
    better = true,
    hardcoded = Map()
  )
  implicit val surface: Surface[RscCompatConfig] = deriveSurface[RscCompatConfig]
  implicit val decoder: ConfDecoder[RscCompatConfig] = deriveDecoder[RscCompatConfig](default)
}
