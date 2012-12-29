/**
 * Copyright 2011-2012 eBusiness Information, Groupe Excilys (www.excilys.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.excilys.ebi.gatling.app

import java.lang.System.currentTimeMillis
import java.util.{ Map => JMap }

import com.excilys.ebi.gatling.app.CommandLineConstants._
import com.excilys.ebi.gatling.charts.report.ReportsGenerator
import com.excilys.ebi.gatling.core.config.{ GatlingFiles, GatlingPropertiesBuilder }
import com.excilys.ebi.gatling.core.config.GatlingConfiguration
import com.excilys.ebi.gatling.core.config.GatlingConfiguration.configuration
import com.excilys.ebi.gatling.core.result.reader.DataReader
import com.excilys.ebi.gatling.core.runner.{ Runner, Selection }
import com.excilys.ebi.gatling.core.scenario.configuration.Simulation
import com.excilys.ebi.gatling.core.structure.Assertion
import com.excilys.ebi.gatling.core.util.FileHelper.formatToFilename

import grizzled.slf4j.Logging
import scopt.OptionParser

/**
 * Object containing entry point of application
 */
object Gatling extends Logging {

  val SUCCESS = 0
  val INCORRECT_ARGUMENTS = 1
  val SIMULATION_CHECK_FAILED = 2

  /**
   * Entry point of Application
   *
   * @param args Arguments of the main method
   */
  def main(args: Array[String]) {
    sys.exit(runGatling(args))
  }

  def fromMap(props: JMap[String, Any]) = {
    GatlingConfiguration.setUp(props)
    new Gatling().start
  }

  def runGatling(args: Array[String]) = {
    val props = new GatlingPropertiesBuilder

    val cliOptsParser = new OptionParser("gatling") {
      opt(CLI_NO_REPORTS, CLI_NO_REPORTS_ALIAS, "Runs simulation but does not generate reports", { props.noReports })
      opt(CLI_REPORTS_ONLY, CLI_REPORTS_ONLY_ALIAS, "<directoryName>", "Generates the reports for the simulation in <directoryName>", { v: String => props.reportsOnly(v) })
      opt(CLI_DATA_FOLDER, CLI_DATA_FOLDER_ALIAS, "<directoryPath>", "Uses <directoryPath> as the absolute path of the directory where feeders are stored", { v: String => props.dataDirectory(v) })
      opt(CLI_RESULTS_FOLDER, CLI_RESULTS_FOLDER_ALIAS, "<directoryPath>", "Uses <directoryPath> as the absolute path of the directory where results are stored", { v: String => props.resultsDirectory(v) })
      opt(CLI_REQUEST_BODIES_FOLDER, CLI_REQUEST_BODIES_FOLDER_ALIAS, "<directoryPath>", "Uses <directoryPath> as the absolute path of the directory where request bodies are stored", { v: String => props.requestBodiesDirectory(v) })
      opt(CLI_SIMULATIONS_FOLDER, CLI_SIMULATIONS_FOLDER_ALIAS, "<directoryPath>", "Uses <directoryPath> to discover simulations that could be run", { v: String => props.sourcesDirectory(v) })
      opt(CLI_SIMULATIONS_BINARIES_FOLDER, CLI_SIMULATIONS_BINARIES_FOLDER_ALIAS, "<directoryPath>", "Uses <directoryPath> to discover already compiled simulations", { v: String => props.binariesDirectory(v) })
      opt(CLI_SIMULATION, CLI_SIMULATION_ALIAS, "<className>", "Runs <className> simulation", { v: String => props.clazz(v) })
      opt(CLI_OUTPUT_DIRECTORY_BASE_NAME, CLI_OUTPUT_DIRECTORY_BASE_NAME_ALIAS, "<name>", "Use <name> for the base name of the output directory", { v: String => props.outputDirectoryBaseName(v) })
    }

    // if arguments are incorrect, usage message is displayed
    if (cliOptsParser.parse(args)) fromMap(props.build)
    else INCORRECT_ARGUMENTS
  }

}

class Gatling extends Logging {

  import GatlingConfiguration.configuration

  private def defaultOutputDirectoryBaseName(clazz: Class[Simulation]) = configuration.simulation.outputDirectoryBaseName.getOrElse(formatToFilename(clazz.getSimpleName))

  def start = {
    val (outputDirectoryName, simulation) = GatlingFiles.reportsOnlyDirectory.map((_, None))
      .getOrElse {
        val simulations = GatlingFiles.binariesDirectory
          .map(SimulationClassLoader.fromClasspathBinariesDirectory) // expect simulations to have been pre-compiled (ex: IDE)
          .getOrElse(SimulationClassLoader.fromSourcesDirectory(GatlingFiles.sourcesDirectory))
          .simulationClasses(configuration.simulation.clazz)
          .sortWith(_.getName < _.getName)

        val selection = configuration.simulation.clazz.map { _ =>
          val simulation = simulations.head
          val outputDirectoryBaseName = defaultOutputDirectoryBaseName(simulation)
          new Selection(simulation, outputDirectoryBaseName, outputDirectoryBaseName)
        }.getOrElse(interactiveSelect(simulations))

        val (runId, simulation) = new Runner(selection).run
        (runId, Some(simulation))
      }

    val dataReader = DataReader.newInstance(outputDirectoryName)

    val result = simulation match {
      case Some(simulation) => if (checkSimulation(simulation, dataReader)) Gatling.SUCCESS else Gatling.SIMULATION_CHECK_FAILED
      case None => Gatling.SUCCESS
    }

    if (!configuration.charting.noReports) generateReports(outputDirectoryName, dataReader)

    result
  }

  private def interactiveSelect(simulations: List[Class[Simulation]]): Selection = {

    val simulation = selectSimulationClass(simulations)

    val myDefaultOutputDirectoryBaseName = defaultOutputDirectoryBaseName(simulation)

    println("Select simulation id (default is '" + myDefaultOutputDirectoryBaseName + "'). Accepted characters are a-z, A-Z, 0-9, - and _")
    val simulationId = {
      val userInput = Console.readLine.trim

      require(userInput.matches("[\\w-_]*"), userInput + " contains illegal characters")

      if (!userInput.isEmpty) userInput else myDefaultOutputDirectoryBaseName
    }

    println("Select run description (optional)")
    val runDescription = Console.readLine.trim

    new Selection(simulation, simulationId, runDescription)
  }

  private def selectSimulationClass(simulations: List[Class[Simulation]]): Class[Simulation] = {

    val selection = simulations.size match {
      case 0 =>
        // If there is no simulation file
        println("There is no simulation script. Please check that your scripts are in user-files/simulations")
        sys.exit
      case 1 =>
        info(simulations.head.getName + " is the only simulation, executing it.")
        0
      case size =>
        println("Choose a simulation number:")
        for ((simulation, index) <- simulations.zipWithIndex) {
          println("     [" + index + "] " + simulation.getName)
        }
        Console.readInt
    }

    val validRange = 0 until simulations.size
    if (validRange contains selection)
      simulations(selection)
    else {
      println("Invalid selection, must be in " + validRange)
      selectSimulationClass(simulations)
    }
  }

  /**
   * This method call the statistics module to generate the charts and statistics
   *
   * @param outputDirectoryName The directory from which the simulation.log will be parsed
   */
  private def generateReports(outputDirectoryName: String, dataReader: DataReader) {
    println("Generating reports...")
    val start = currentTimeMillis
    val indexFile = ReportsGenerator.generateFor(outputDirectoryName, dataReader)
    println("Reports generated in " + (currentTimeMillis - start) / 1000 + "s.")
    println("Please open the following file : " + indexFile)
  }

  private def checkSimulation(simulation: Simulation, dataReader: DataReader) = {
    val successful = Assertion.assertThat(simulation.assertions, dataReader)

    if (successful) println("Simulation successful.")
    else println("Simulation failed.")

    successful
  }
}
