// Copyright (c) 2017-2019 Twitter, Inc.
// Licensed under the Apache License, Version 2.0 (see LICENSE.md).
package scala.meta.scalasig.lowlevel

import scala.meta.scalasig._
import scala.meta.internal.scalasig._

// NOTE: There is no specification for this aspect of ScalaSignatures.
// The best that we have is the Scala compiler source code:
// * https://github.com/scala/scala/blob/v2.12.6/src/reflect/scala/reflect/internal/pickling/PickleFormat.scala
// * https://github.com/scala/scala/blob/v2.12.6/src/reflect/scala/reflect/internal/pickling/UnPickler.scala

sealed trait Entity extends Pretty
sealed trait Entry extends Entity

sealed trait Name extends Entry { def value: String }
case class TermName(value: String) extends Name
case class TypeName(value: String) extends Name

sealed trait Symbol extends Entry
case object NoSymbol extends Symbol
sealed trait EmbeddedSymbol extends Symbol with Flagged
case class TypeSymbol(name: Ref, owner: Ref, flags: Long, within: Option[Ref], info: Ref) extends EmbeddedSymbol
case class AliasSymbol(name: Ref, owner: Ref, flags: Long, within: Option[Ref], info: Ref) extends EmbeddedSymbol
case class ClassSymbol(name: Ref, owner: Ref, flags: Long, within: Option[Ref], info: Ref, thisType: Option[Ref]) extends EmbeddedSymbol
case class ModuleSymbol(name: Ref, owner: Ref, flags: Long, within: Option[Ref], info: Ref) extends EmbeddedSymbol
case class ValSymbol(name: Ref, owner: Ref, flags: Long, within: Option[Ref], info: Ref, alias: Option[Ref]) extends EmbeddedSymbol
sealed trait ExternalSymbol extends Symbol
case class ExtRef(name: Ref, owner: Option[Ref]) extends ExternalSymbol
case class ExtModClassRef(name: Ref, owner: Option[Ref]) extends ExternalSymbol
case class Children(sym: Ref, children: List[Ref]) extends Entry

sealed trait Type extends Entry
case object NoType extends Type
case object NoPrefix extends Type
case class ThisType(sym: Ref) extends Type
case class SingleType(pre: Ref, sym: Ref) extends Type
case class ConstantType(lit: Ref) extends Type
case class TypeRef(pre: Ref, sym: Ref, targs: List[Ref]) extends Type
case class TypeBounds(lo: Ref, hi: Ref) extends Type
case class RefinedType(sym: Ref, parents: List[Ref]) extends Type
case class ClassInfoType(sym: Ref, parents: List[Ref]) extends Type
case class MethodType(ret: Ref, params: List[Ref]) extends Type
case class PolyType(tpe: Ref, params: List[Ref]) extends Type
case class SuperType(thisp: Ref, superp: Ref) extends Type
case class AnnotatedType(tpe: Ref, annots: List[Ref]) extends Type
case class ExistentialType(tpe: Ref, decls: List[Ref]) extends Type

sealed trait Lit extends Entry with ScalaAnnotValue with JavaAnnotValue
case object UnitLit extends Lit
case class BooleanLit(value: Boolean) extends Lit
case class ByteLit(value: Byte) extends Lit
case class ShortLit(value: Short) extends Lit
case class CharLit(value: Char) extends Lit
case class IntLit(value: Int) extends Lit
case class LongLit(value: Long) extends Lit
case class FloatLit(value: Float) extends Lit
case class DoubleLit(value: Double) extends Lit
case class StringLit(name: Ref) extends Lit
case object NullLit extends Lit
case class ClassLit(tpe: Ref) extends Lit
case class EnumLit(sym: Ref) extends Lit

case class SymAnnot(sym: Ref, tpe: Ref, args: List[AnnotArg]) extends Entry
case class AnnotInfo(tpe: Ref, args: List[AnnotArg]) extends Entry with JavaAnnotValue
sealed trait AnnotArg extends Entity
case class ScalaAnnotArg(value: Ref) extends AnnotArg
case class JavaAnnotArg(name: Ref, value: Ref) extends AnnotArg
sealed trait ScalaAnnotValue extends Entry
sealed trait JavaAnnotValue extends Entry
case class AnnotArgArray(values: List[Ref]) extends Entry with JavaAnnotValue

sealed trait Tree extends Entry with ScalaAnnotValue
case object EmptyTree extends Tree
case class PackageDefTree(tpe: Ref, sym: Ref, pid: Ref, stats: List[Ref]) extends Tree
case class ClassDefTree(tpe: Ref, sym: Ref, mods: Ref, name: Ref, tparams: List[Ref], impl: Ref) extends Tree
case class ModuleDefTree(tpe: Ref, sym: Ref, mods: Ref, name: Ref, impl: Ref) extends Tree
case class ValDefTree(tpe: Ref, sym: Ref, mods: Ref, name: Ref, tpt: Ref, rhs: Ref) extends Tree
case class DefDefTree(tpe: Ref, sym: Ref, mods: Ref, name: Ref, tparams: List[Ref], paramss: List[List[Ref]], ret: Ref, rhs: Ref) extends Tree
case class TypeDefTree(tpe: Ref, sym: Ref, mods: Ref, name: Ref, tparams: List[Ref], tpt: Ref) extends Tree
case class LabelDefTree(tpe: Ref, sym: Ref, name: Ref, params: List[Ref], rhs: Ref) extends Tree
case class ImportTree(tpe: Ref, sym: Ref, qual: Ref, selectors: List[ImportSelector]) extends Tree
case class TemplateTree(tpe: Ref, sym: Ref, parents: List[Ref], self: Ref, stats: List[Ref]) extends Tree
case class BlockTree(tpe: Ref, stats: List[Ref]) extends Tree
case class CaseTree(tpe: Ref, pat: Ref, guard: Ref, body: Ref) extends Tree
case class AlternativeTree(tpe: Ref, trees: List[Ref]) extends Tree
case class StarTree(tpe: Ref, elem: Ref) extends Tree
case class BindTree(tpe: Ref, sym: Ref, name: Ref, body: Ref) extends Tree
case class UnapplyTree(tpe: Ref, fun: Ref, args: List[Ref]) extends Tree
case class ArrayValueTree(tpe: Ref, elemtpt: Ref, elems: List[Ref]) extends Tree
case class FunctionTree(tpe: Ref, sym: Ref, params: List[Ref], body: Ref) extends Tree
case class AssignTree(tpe: Ref, lhs: Ref, rhs: Ref) extends Tree
case class IfTree(tpe: Ref, cond: Ref, thenp: Ref, elsep: Ref) extends Tree
case class MatchTree(tpe: Ref, scrut: Ref, cases: List[Ref]) extends Tree
case class ReturnTree(tpe: Ref, sym: Ref, expr: Ref) extends Tree
case class TryTree(tpe: Ref, expr: Ref, cases: List[Ref], fin: Ref) extends Tree
case class ThrowTree(tpe: Ref, expr: Ref) extends Tree
case class NewTree(tpe: Ref, tpt: Ref) extends Tree
case class TypedTree(tpe: Ref, expr: Ref, tpt: Ref) extends Tree
case class TypeApplyTree(tpe: Ref, fun: Ref, targs: List[Ref]) extends Tree
case class ApplyTree(tpe: Ref, fun: Ref, args: List[Ref]) extends Tree
case class ApplyDynamicTree(tpe: Ref, sym: Ref, fun: Ref, args: List[Ref]) extends Tree
case class SuperTree(tpe: Ref, sym: Ref, qual: Ref, mix: Ref) extends Tree
case class ThisTree(tpe: Ref, sym: Ref, qual: Ref) extends Tree
case class SelectTree(tpe: Ref, sym: Ref, qual: Ref, name: Ref) extends Tree
case class IdentTree(tpe: Ref, sym: Ref, name: Ref) extends Tree
case class LiteralTree(tpe: Ref, lit: Ref) extends Tree
case class TypeTree(tpe: Ref) extends Tree
case class AnnotatedTree(tpe: Ref, annot: Ref, arg: Ref) extends Tree
case class SingletonTypeTree(tpe: Ref, ref: Ref) extends Tree
case class SelectFromTypeTree(tpe: Ref, qual: Ref, name: Ref) extends Tree
case class CompoundTypeTree(tpe: Ref, impl: Ref) extends Tree
case class AppliedTypeTree(tpe: Ref, fun: Ref, targs: List[Ref]) extends Tree
case class TypeBoundsTree(tpe: Ref, lo: Ref, hi: Ref) extends Tree
case class ExistentialTypeTree(tpe: Ref, tpt: Ref, decls: List[Ref]) extends Tree
case class ImportSelector(name: Ref, rename: Ref) extends Entity
case class Modifiers(flags: Long, within: Ref) extends Entry with Flagged
