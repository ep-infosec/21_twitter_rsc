// Copyright (c) 2017-2019 Twitter, Inc.
// Licensed under the Apache License, Version 2.0 (see LICENSE.md).
package rsc.settings

sealed trait Abi
case object Abi211 extends Abi
case object Abi212 extends Abi
