// Copyright (c) 2017-2019 Twitter, Inc.
// Licensed under the Apache License, Version 2.0 (see LICENSE.md).
package rsc.report

trait Reporter {
  def append(msg: Message): Message
  def problems: List[Message]
  def messages: List[Message]
}
