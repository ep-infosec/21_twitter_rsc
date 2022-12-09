// Copyright (c) 2017-2019 Twitter, Inc.
// Licensed under the Apache License, Version 2.0 (see LICENSE.md).
package rsc.report

import rsc.input._
import rsc.lexis._
import rsc.outline._
import rsc.pretty._
import rsc.syntax._
import rsc.util._

sealed trait Message extends Pretty with Product {
  def sev: Severity
  def pos: Position
  def text: String
  def explanation: String = ""
  def printStr(p: Printer): Unit = PrettyMessage.str(p, this)
  def printRepl(p: Printer): Unit = PrettyMessage.repl(p, this)
}

// ============ FUNDAMENTAL ============

final case class CrashMessage(ex: Throwable) extends Message {
  private lazy val crash = translateCrash(NoPosition, ex)
  def pos = crash.pos
  def sev = FatalSeverity
  def text = crash.message
  override def explanation = if (crash.cause != null) crash.cause.str else crash.str
}

final case class ErrorSummary(errors: List[Message]) extends Message {
  def pos = NoPosition
  def sev = VerboseSeverity
  def text = {
    val numErrors = errors.length
    if (numErrors == 0) crash("ErrorSummary.errors cannot be empty")
    else if (numErrors == 1) "one error found"
    else if (numErrors == 2) "two errors found"
    else if (numErrors == 3) "three errors found"
    else if (numErrors == 4) "four errors found"
    else s"$numErrors errors found"
  }
}

final case class VerboseMessage(pos: Position, text: String) extends Message {
  def sev = VerboseSeverity
}

object VerboseMessage {
  def apply(text: String): VerboseMessage = VerboseMessage(NoPosition, text)
}

// ============ LEXER ============

final case class IllegalCharacter(pos: Position) extends Message {
  def sev = FatalSeverity
  def text = "illegal character"
}

final case class IllegalComment(pos: Position) extends Message {
  def sev = FatalSeverity
  def text = "illegal comment"
}

final case class IllegalEscape(pos: Position) extends Message {
  def sev = FatalSeverity
  def text = "illegal escape"
}

final case class IllegalNumber(pos: Position) extends Message {
  def sev = FatalSeverity
  def text = "illegal number"
}

final case class IllegalLanguage(pos: Position) extends Message {
  def sev = FatalSeverity
  def text = "illegal language"
}

final case class IllegalXml(pos: Position) extends Message {
  def sev = FatalSeverity
  def text = "illegal xml"
}

final case class LeadingZero(pos: Position) extends Message {
  def sev = FatalSeverity
  def text = "leading zeros not allowed"
}

final case class UnclosedBackquotedId(pos: Position) extends Message {
  def sev = FatalSeverity
  def text = "unclosed backquoted identifier"
}

final case class UnclosedCharacter(pos: Position) extends Message {
  def sev = FatalSeverity
  def text = "unclosed character literal"
}

final case class UnclosedInterpolation(pos: Position) extends Message {
  def sev = FatalSeverity
  def text = "unclosed interpolation"
}

final case class UnclosedString(pos: Position) extends Message {
  def sev = FatalSeverity
  def text = "unclosed string literal"
}

// ============ PARSER ============

final case class ExpectedToken(pos: Position, expected: Token, actual: Token) extends Message {
  def sev = FatalSeverity
  def text = s"${tokenStr(expected)} expected but ${tokenStr(actual)} found"
}

final case class ExpectedId(pos: Position, expected: String, actual: Token) extends Message {
  def sev = FatalSeverity
  def text = "$expected expected but ${tokenStr(actual)} found"
}

final case class ExpectedClassOrObjectDefinition(pos: Position) extends Message {
  def sev = FatalSeverity
  def text = "expected class or object definition"
}

final case class ExpectedStartOfDefinition(pos: Position) extends Message {
  def sev = FatalSeverity
  def text = "expected start of definition"
}

final case class ExpectedTypeRhs(pos: Position) extends Message {
  def sev = FatalSeverity
  def text = "expected =, >:, or <:"
}

final case class FileNotFound(input: Input) extends Message {
  def sev = ErrorSeverity
  def pos = Position(input, NoOffset, NoOffset)
  def text = s"file not found"
}

final case class IllegalEllipsis(pos: Position) extends Message {
  def sev = FatalSeverity
  def text = "illegal ellipsis"
}

final case class IllegalIdentifier(pos: Position) extends Message {
  def sev = FatalSeverity
  def text = "illegal identifier"
}

final case class IllegalLiteral(pos: Position) extends Message {
  def sev = FatalSeverity
  def text = "illegal literal"
}

final case class IllegalModifier(pos: Position) extends Message {
  def sev = ErrorSeverity
  def text = "illegal modifier"
}

final case class IllegalSelf(pos: Position) extends Message {
  def sev = FatalSeverity
  def text = "illegal self"
}

final case class IllegalSplice(pos: Position) extends Message {
  def sev = FatalSeverity
  def text = "illegal splice"
}

final case class IllegalStartOfDeclaration(pos: Position) extends Message {
  def sev = FatalSeverity
  def text = "illegal start of declaration"
}

final case class IllegalStartOfDefinition(pos: Position) extends Message {
  def sev = FatalSeverity
  def text = "illegal start of definition"
}

final case class IllegalStartOfSimplePat(pos: Position) extends Message {
  def sev = FatalSeverity
  def text = "illegal start of simple pattern"
}

final case class IllegalStartOfSimpleTerm(pos: Position) extends Message {
  def sev = FatalSeverity
  def text = "illegal start of simple expression"
}

final case class IllegalStartOfStatement(pos: Position) extends Message {
  def sev = FatalSeverity
  def text = "illegal start of statement"
}

final case class MixedLeftAndRightAssociativeOps(pos: Position, op1: String, op2: String)
    extends Message {
  def sev = ErrorSeverity
  def text = {
    import rsc.lexis.scala._
    def status(op: String) = {
      if (op1.isLeftAssoc) "which is left-associative"
      else "which is right-associative"
    }
    val status1 = status(op1)
    val status2 = status(op2)
    val error = "have same precedence and may not be mixed"
    s"`$op1` ($status1) and `$op2` ($status2) $error"
  }
}

final case class RepeatedModifier(pos: Position) extends Message {
  def sev = ErrorSeverity
  def text = s"repeated modifier"
}

final case class UnboundWildcard(pos: Position) extends Message {
  def sev = ErrorSeverity
  def text = s"unbound wildcard"
}

// ============ TYPECHECKER ============

final case class AmbiguousMember(env: Env, id: Id, resolution: AmbiguousResolution)
    extends Message {
  def sev = ErrorSeverity
  def pos = id.point
  def text = {
    val qual = env.scopes.head.sym
    id match {
      case AmbigId(value) => s"ambiguous: $qual$value (${resolution.syms.mkString(", ")})"
      case AnonId() => crash(id)
      case CtorId() => crash(id)
      case PatId(value) => s"ambiguous: value $qual$value (${resolution.syms.mkString(", ")})"
      case TermId(value) => s"ambiguous: value $qual$value (${resolution.syms.mkString(", ")})"
      case TptId(value) => s"ambiguous: type $qual$value (${resolution.syms.mkString(", ")})"
    }
  }
}

final case class AmbiguousId(id: Id, resolution: AmbiguousResolution) extends Message {
  def sev = ErrorSeverity
  def pos = id.point
  def text = {
    id match {
      case AmbigId(value) => s"ambiguous: $value (${resolution.syms.mkString(", ")})"
      case AnonId() => crash(id)
      case CtorId() => crash(id)
      case PatId(value) => s"ambiguous: value $value (${resolution.syms.mkString(", ")})"
      case TermId(value) => s"ambiguous: value $value (${resolution.syms.mkString(", ")})"
      case TptId(value) => s"ambiguous: type $value (${resolution.syms.mkString(", ")})"
    }
  }
}

final case class IllegalCyclicReference(work: Work) extends Message {
  def sev = ErrorSeverity
  def pos = {
    work match {
      case scope: ImporterScope => scope.tree.point
      case scope: TemplateScope => scope.tree.point
      case sketch: Sketch => sketch.tree.point
      case _ => crash(work)
    }
  }
  def text = {
    val CyclicStatus(cycle) = work.status
    val description = {
      def loop(works: List[Work]): String = {
        works match {
          case List() => crash(cycle)
          case List(only) => name(only)
          case List(prelast, last) => name(prelast) + " and " + name(last)
          case scope :: rest => name(scope) + ", " + loop(rest)
        }
      }
      loop(cycle)
    }
    s"illegal cyclic reference involving $description"
  }
  private def name(work: Work): String = {
    work match {
      case scope: TemplateScope =>
        PrettyOutline.desc(scope.tree)
      case scope: ImporterScope =>
        val p = new Printer
        p.str("import ")
        p.str(scope.tree)
        p.toString
      case sketch: Sketch =>
        sketch.str
      case _ =>
        crash(work)
    }
  }
}

final case class IllegalOutline(tree: Tree) extends Message {
  def sev = ErrorSeverity
  def pos = tree.point
  def text = "illegal outline"
}

final case class IllegalParent(tpt: Tpt) extends Message {
  def sev = ErrorSeverity
  def pos = tpt.point
  def text = "illegal parent"
}

final case class UnboundMember(env: Env, id: Id) extends Message {
  def sev = ErrorSeverity
  def pos = id.point
  def text = {
    val qual = env.scopes.head.sym
    id match {
      case AmbigId(value) => s"unbound: $qual$value"
      case AnonId() => crash(id)
      case CtorId() => crash(id)
      case PatId(value) => s"unbound: value $qual$value"
      case TermId(value) => s"unbound: value $qual$value"
      case TptId(value) => s"unbound: type $qual$value"
    }
  }
}

final case class UnboundId(id: Id) extends Message {
  def sev = ErrorSeverity
  def pos = id.point
  def text = {
    id match {
      case AmbigId(value) => s"unbound: $value"
      case AnonId() => crash(id)
      case CtorId() => crash(id)
      case PatId(value) => s"unbound: value $value"
      case TermId(value) => s"unbound: value $value"
      case TptId(value) => s"unbound: type $value"
    }
  }
}

final case class DefnMethodNotype(outline: DefnMethod, warn: Boolean = true) extends Message {
  def sev = if (warn) WarningSeverity else ErrorSeverity
  def pos = outline.point
  def text = s"No type found at $pos for definition: $outline"
}

final case class DefnClassInitTypeParamsMissing(
    outline: DefnClass,
    init: Init,
    warn: Boolean = true
) extends Message {
  def sev = if (warn) WarningSeverity else ErrorSeverity
  def pos = init.point
  def text = s"Type parameters required but missing at $pos for parent init: $init"
}
