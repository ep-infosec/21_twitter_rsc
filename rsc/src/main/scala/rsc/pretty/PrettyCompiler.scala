// Copyright (c) 2017-2019 Twitter, Inc.
// Licensed under the Apache License, Version 2.0 (see LICENSE.md).
package rsc.pretty

import java.nio.file._
import rsc.Compiler
import rsc.input._
import rsc.lexis._
import rsc.report._
import rsc.scan._

object PrettyCompiler {
  def str(p: Printer, x: Compiler): Unit = {
    p.rep(x.trees, EOL) { tree =>
      p.header(tree.pos.input.path.toString)
      p.str(tree)
      p.newline()
      p.repl(tree)
      p.newline()
    }
    // TODO: Implement me.
    // p.str(x.symtab)
    // p.str(x.todo)
  }

  def repl(p: Printer, x: Compiler): Unit = {
    ???
  }

  def xprintScan(p: Printer, x: Compiler): Unit = {
    val allInputs = x.settings.ins.map(in => Input(in))
    val existingInputs = allInputs.filter(input => Files.exists(input.path))
    p.rep(existingInputs, EOL) { input =>
      p.header(input.path.toString)
      val reporter = StoreReporter(x.settings)
      val scanner = Scanner(x.settings, reporter, input)
      try {
        while (scanner.token != EOF) {
          p.str(s"[${scanner.start}..${scanner.end}) ")
          p.str(tokenRepl(scanner.token))
          if (scanner.value != null) {
            p.str(" ")
            p.str(Escape(scanner.value))
          }
          p.newline()
          scanner.next()
        }
      } catch {
        case ex: Throwable =>
          p.str(s"<${ex.getMessage}>")
      }
    }
  }
}
