/**
 * Copyright 2011-2013 eBusiness Information, Groupe Excilys (www.excilys.com)
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
package io.gatling.core.config

import scala.collection.JavaConversions.{ asScalaBuffer, mapAsJavaMap }
import scala.collection.mutable

import com.typesafe.config.{ Config, ConfigFactory }

import io.gatling.core.ConfigurationConstants._
import io.gatling.core.util.StringHelper.trimToOption

/**
 * Configuration loader of Gatling
 */
object GatlingConfiguration {

	var configuration: GatlingConfiguration = _

	def setUp(props: mutable.Map[String, Any] = mutable.Map.empty) {
		val classLoader = getClass.getClassLoader

		val defaultsConfig = ConfigFactory.parseResources(classLoader, "gatling-defaults.conf")
		val customConfig = ConfigFactory.parseResources(classLoader, "gatling.conf")
		val propertiesConfig = ConfigFactory.parseMap(props)

		val config = ConfigFactory.systemProperties.withFallback(propertiesConfig).withFallback(customConfig).withFallback(defaultsConfig)

		configuration = GatlingConfiguration(
			simulation = SimulationConfiguration(
				outputDirectoryBaseName = trimToOption(config.getString(CONF_SIMULATION_OUTPUT_DIRECTORY_BASE_NAME)),
				runDescription = config.getString(CONF_SIMULATION_RUN_DESCRIPTION),
				encoding = config.getString(CONF_SIMULATION_ENCODING),
				clazz = trimToOption(config.getString(CONF_SIMULATION_CLASS)),
				cacheRegex = config.getBoolean(CONF_SIMULATION_CACHE_REGEX),
				cacheXPath = config.getBoolean(CONF_SIMULATION_CACHE_XPATH),
				cacheJsonPath = config.getBoolean(CONF_SIMULATION_CACHE_JSONPATH)),
			timeOut = TimeOutConfiguration(
				simulation = config.getInt(CONF_TIME_OUT_SIMULATION),
				actor = config.getInt(CONF_TIME_OUT_ACTOR)),
			directory = DirectoryConfiguration(
				data = config.getString(CONF_DIRECTORY_DATA),
				requestBodies = config.getString(CONF_DIRECTORY_REQUEST_BODIES),
				sources = config.getString(CONF_DIRECTORY_SIMULATIONS),
				binaries = trimToOption(config.getString(CONF_DIRECTORY_BINARIES)),
				reportsOnly = trimToOption(config.getString(CONF_DIRECTORY_REPORTS_ONLY)),
				results = config.getString(CONF_DIRECTORY_RESULTS)),
			charting = ChartingConfiguration(
				noReports = config.getBoolean(CONF_CHARTING_NO_REPORTS),
				statsTsvSeparator = config.getString(CONF_CHARTING_STATS_TSV_SEPARATOR),
				maxPlotsPerSeries = config.getInt(CONF_CHARTING_MAX_PLOTS_PER_SERIES),
				accuracy = config.getInt(CONF_CHARTING_ACCURACY),
				indicators = IndicatorsConfiguration(
					lowerBound = config.getInt(CONF_CHARTING_INDICATORS_LOWER_BOUND),
					higherBound = config.getInt(CONF_CHARTING_INDICATORS_HIGHER_BOUND),
					percentile1 = config.getInt(CONF_CHARTING_INDICATORS_PERCENTILE1),
					percentile2 = config.getInt(CONF_CHARTING_INDICATORS_PERCENTILE2))),
			http = HttpConfiguration(
				provider = config.getString(CONF_HTTP_PROVIDER),
				allowPoolingConnection = config.getBoolean(CONF_HTTP_ALLOW_POOLING_CONNECTION),
				allowSslConnectionPool = config.getBoolean(CONF_HTTP_ALLOW_SSL_CONNECTION_POOL),
				compressionEnabled = config.getBoolean(CONF_HTTP_COMPRESSION_ENABLED),
				connectionTimeOut = config.getInt(CONF_HTTP_CONNECTION_TIMEOUT),
				idleConnectionInPoolTimeOutInMs = config.getInt(CONF_HTTP_IDLE_CONNECTION_IN_POOL_TIMEOUT_IN_MS),
				idleConnectionTimeOutInMs = config.getInt(CONF_HTTP_IDLE_CONNECTION_TIMEOUT_IN_MS),
				ioThreadMultiplier = config.getInt(CONF_HTTP_IO_THREAD_MULTIPLIER),
				maximumConnectionsPerHost = config.getInt(CONF_HTTP_MAXIMUM_CONNECTIONS_PER_HOST),
				maximumConnectionsTotal = config.getInt(CONF_HTTP_MAXIMUM_CONNECTIONS_TOTAL),
				maxRetry = config.getInt(CONF_HTTP_MAX_RETRY),
				requestCompressionLevel = config.getInt(CONF_HTTP_REQUEST_COMPRESSION_LEVEL),
				requestTimeOutInMs = config.getInt(CONF_HTTP_REQUEST_TIMEOUT_IN_MS),
				useProxyProperties = config.getBoolean(CONF_HTTP_USE_PROXY_PROPERTIES),
				userAgent = config.getString(CONF_HTTP_USER_AGENT),
				useRawUrl = config.getBoolean(CONF_HTTP_USE_RAW_URL),
				nonStandardJsonSupport = config.getStringList(CONF_HTTP_JSON_FEATURES),
				warmUpUrl = {
					val value = config.getString(CONF_HTTP_WARM_UP_URL).trim
					if (value.isEmpty) None else Some(value)
				},
				ssl = {
					def storeConfig(typeKey: String, fileKey: String, passwordKey: String, algorithmKey: String) = {

						def toOption(string: String) = {
							val trimmed = string.trim
							if (trimmed.isEmpty) None else Some(trimmed)
						}

						val storeType = toOption(config.getString(typeKey))
						val storeFile = toOption(config.getString(fileKey))
						val storePassword = config.getString(passwordKey)
						val storeAlgorithm = toOption(config.getString(algorithmKey))

						storeType.map { t =>
							StoreConfiguration(t, storeFile.getOrElse(throw new IllegalArgumentException(s"$typeKey defined as $t but store file isn't defined")), storePassword, storeAlgorithm)
						}
					}

					val trustStore = storeConfig(CONF_HTTP_SSS_TRUST_STORE_TYPE, CONF_HTTP_SSS_TRUST_STORE_FILE, CONF_HTTP_SSS_TRUST_STORE_PASSWORD, CONF_HTTP_SSS_TRUST_STORE_ALGORITHM)
					val keyStore = storeConfig(CONF_HTTP_SSS_KEY_STORE_TYPE, CONF_HTTP_SSS_KEY_STORE_FILE, CONF_HTTP_SSS_KEY_STORE_PASSWORD, CONF_HTTP_SSS_KEY_STORE_ALGORITHM)

					SslConfiguration(trustStore, keyStore)
				}),
			data = DataConfiguration(
				dataWriterClasses = config.getStringList(CONF_DATA_WRITER_CLASS_NAMES).map {
					case "console" => "io.gatling.core.result.writer.ConsoleDataWriter"
					case "file" => "io.gatling.core.result.writer.FileDataWriter"
					case "graphite" => "io.gatling.metrics.GraphiteDataWriter"
					case clazz => clazz
				},
				dataReaderClass = (config.getString(CONF_DATA_READER_CLASS_NAME)).trim match {
					case "file" => "io.gatling.charts.result.reader.FileDataReader"
					case clazz => clazz
				}),
			graphite = GraphiteConfiguration(
				host = config.getString(CONF_GRAPHITE_HOST),
				port = config.getInt(CONF_GRAPHITE_PORT),
				protocol = config.getString(CONF_GRAPHITE_PROTOCOL),
				rootPathPrefix = config.getString(CONF_ROOT_PATH_PREFIX),
				bucketWidth = config.getInt(CONF_GRAPHITE_BUCKET_WIDTH)),
			config)
	}
}

case class SimulationConfiguration(
	outputDirectoryBaseName: Option[String],
	runDescription: String,
	encoding: String,
	clazz: Option[String],
	cacheRegex: Boolean,
	cacheXPath: Boolean,
	cacheJsonPath: Boolean)

case class TimeOutConfiguration(
	simulation: Int,
	actor: Int)

case class DirectoryConfiguration(
	data: String,
	requestBodies: String,
	sources: String,
	binaries: Option[String],
	reportsOnly: Option[String],
	results: String)

case class ChartingConfiguration(
	noReports: Boolean,
	statsTsvSeparator: String,
	maxPlotsPerSeries: Int,
	accuracy: Int,
	indicators: IndicatorsConfiguration)

case class IndicatorsConfiguration(
	lowerBound: Int,
	higherBound: Int,
	percentile1: Int,
	percentile2: Int)

case class HttpConfiguration(
	provider: String,
	allowPoolingConnection: Boolean,
	allowSslConnectionPool: Boolean,
	compressionEnabled: Boolean,
	connectionTimeOut: Int,
	idleConnectionInPoolTimeOutInMs: Int,
	idleConnectionTimeOutInMs: Int,
	ioThreadMultiplier: Int,
	maximumConnectionsPerHost: Int,
	maximumConnectionsTotal: Int,
	maxRetry: Int,
	requestCompressionLevel: Int,
	requestTimeOutInMs: Int,
	useProxyProperties: Boolean,
	userAgent: String,
	useRawUrl: Boolean,
	nonStandardJsonSupport: Seq[String],
	ssl: SslConfiguration,
	warmUpUrl: Option[String])

case class SslConfiguration(
	trustStore: Option[StoreConfiguration],
	keyStore: Option[StoreConfiguration])

case class StoreConfiguration(
	storeType: String,
	file: String,
	password: String,
	algorithm: Option[String])

case class DataConfiguration(
	dataWriterClasses: Seq[String],
	dataReaderClass: String)

case class GraphiteConfiguration(
	host: String,
	port: Int,
	protocol: String,
	rootPathPrefix: String,
	bucketWidth: Int)

case class GatlingConfiguration(
	simulation: SimulationConfiguration,
	timeOut: TimeOutConfiguration,
	directory: DirectoryConfiguration,
	charting: ChartingConfiguration,
	http: HttpConfiguration,
	data: DataConfiguration,
	graphite: GraphiteConfiguration,
	config: Config)
