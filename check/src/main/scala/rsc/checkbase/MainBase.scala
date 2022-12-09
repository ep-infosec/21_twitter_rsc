// Copyright (c) 2017-2019 Twitter, Inc.
// Licensed under the Apache License, Version 2.0 (see LICENSE.md).
package rsc.checkbase

import rsc.util._
import scala.collection.mutable
import scala.meta.cli._
import scala.meta.internal.cli._
import scala.util._

trait MainBase[S <: SettingsBase, I, N, R] extends DiffUtil with NscUtil with ToolUtil {
  def main(args: Array[String]): Unit = {
    val expandedArgs = Args.expand(args)
    settings(expandedArgs) match {
      case Right(settings) =>
        val reporter = Reporter()
        val problems = process(reporter, settings)
        reporter.err.flush()
        if (problems.nonEmpty) sys.exit(1) else sys.exit(0)
      case Left(failures) =>
        failures.foreach(println)
        sys.exit(1)
    }
  }

  def process(reporter: Reporter, settings: S): List[Problem] = {
    val allProblems = mutable.UnrolledBuffer[Problem]()
    def report(problem: Problem): Unit = {
      reporter.err.println(problem)
      allProblems += problem
    }

    var successes = 0
    val inputs = this.inputs(settings)
    val quiet = settings.quiet || inputs.length == 1
    val job = Job(inputs, if (quiet) devnull else Console.err)
    job.foreach { input =>
      try {
        (nscResult(settings, input), rscResult(settings, input)) match {
          case (Left(nscFailures), _) =>
            val nscProblems = nscFailures.map(FailedNscProblem)
            nscProblems.foreach(report)
          case (_, Left(rscFailures)) =>
            val rscProblems = rscFailures.map { rscFailure =>
              if (rscFailure.contains(".CrashException") &&
                  !rscFailure.contains(input.toString)) {
                FailedRscProblem(s"$input: $rscFailure")
              } else {
                FailedRscProblem(rscFailure)
              }
            }
            rscProblems.foreach(report)
          case (Right(nscResult), Right(rscResult)) =>
            val checker = this.checker(settings, nscResult, rscResult)
            checker.check()
            val checkerProblems = checker.problems
            if (checkerProblems.isEmpty) successes += 1
            checkerProblems.foreach(report)
        }
      } catch {
        case ex: Throwable =>
          report(FailedInputProblem(ex.str))
      }
    }

    val numProblems = allProblems.length
    if (numProblems == 0) ()
    else if (numProblems == 1) reporter.err.println("one problem found")
    else if (numProblems == 2) reporter.err.println("two problems found")
    else if (numProblems == 3) reporter.err.println("three problems found")
    else if (numProblems == 4) reporter.err.println("four problems found")
    else reporter.err.println(s"$numProblems problems found")

    if (!settings.quiet) {
      if (successes == 0) reporter.err.println("All checks failed")
      else if (successes == inputs.length) reporter.err.println("All checks succeeded")
      else {
        reporter.err.println(s"Only ${successes} out of ${inputs.length} checks succeeded")
      }
    }

    allProblems.toList
  }

  def settings(args: List[String]): Either[List[String], S]
  def inputs(settings: S): List[I]
  def nscResult(settings: S, input: I): Either[List[String], N]
  def rscResult(settings: S, input: I): Either[List[String], R]
  def checker(settings: S, nscResult: N, rscResult: R): CheckerBase
}
