/**
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.davidmweber

import java.util.Properties

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.callback.Callback
import org.flywaydb.core.api.logging.{Log, LogCreator, LogFactory}
import org.flywaydb.core.internal.info.MigrationInfoDumper
import sbt.Keys._
import sbt._

import scala.collection.JavaConverters._
import org.flywaydb.core.api.configuration.FluentConfiguration

object FlywayPlugin extends AutoPlugin {

  override def trigger = noTrigger

  object autoImport {

    //*********************
    // common migration settings for all tasks
    //*********************
    val flywayDriver = settingKey[String]("The fully qualified classname of the jdbc driver to use to connect to the database. By default, the driver is autodetected based on the url.")
    val flywayUrl = settingKey[String]("The jdbc url to use to connect to the database.")
    val flywayUser = settingKey[String]("The user to use to connect to the database.")
    val flywayPassword = settingKey[String]("The password to use to connect to the database.")

    val flywaySchemas = settingKey[Seq[String]]("List of the schemas managed by Flyway. The first schema in the list will be automatically set as the default one during the migration. It will also be the one containing the metadata table. These schema names are case-sensitive. (default: The default schema for the datasource connection)")
    val flywayTable = settingKey[String]("The name of the metadata table that will be used by Flyway. (default: schema_version) By default (single-schema mode) the metadata table is placed in the default schema for the connection provided by the datasource. When the flyway.schemas property is set (multi-schema mode), the metadata table is placed in the first schema of the list.")
    val flywayBaselineVersion = settingKey[String]("The version to tag an existing schema with when executing baseline. (default: 1)")
    val flywayBaselineDescription = settingKey[String]("The description to tag an existing schema with when executing baseline. (default: << Flyway Baseline >>)")

    //*********************
    // common settings for migration loading tasks (used by migrate, validate, info)
    //*********************
    val flywayLocations = settingKey[Seq[String]]("Locations on the classpath to scan recursively for migrations. Locations may contain both sql and code-based migrations. (default: classpath:db/migration)")
    val flywayResolvers = settingKey[Seq[String]](" The fully qualified class names of the custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply.")
    val flywaySkipDefaultResolvers = settingKey[Boolean]("Whether default built-in resolvers should be skipped. (default: false)")
    val flywayEncoding = settingKey[String]("The encoding of Sql migrations. (default: UTF-8)")
    val flywaySqlMigrationPrefix = settingKey[String]("The file name prefix for Sql migrations (default: V)")
    val flywayRepeatableSqlMigrationPrefix = settingKey[String]("The file name prefix for repeatable sql migrations (default: R)")
    val flywaySqlMigrationSeparator = settingKey[String]("The file name separator for Sql migrations (default: __)")
    val flywaySqlMigrationSuffixes = settingKey[Seq[String]]("The file name suffixes for Sql migrations (default: .sql)")
    val flywayCleanOnValidationError = settingKey[Boolean]("Whether to automatically call clean or not when a validation error occurs. (default: {@code false})<br/> This is exclusively intended as a convenience for development. Even tough we strongly recommend not to change migration scripts once they have been checked into SCM and run, this provides a way of dealing with this case in a smooth manner. The database will be wiped clean automatically, ensuring that the next migration will bring you back to the state checked into SCM. Warning ! Do not enable in production !")
    val flywayCleanDisabled = settingKey[Boolean]("Whether to disable clean. This is especially useful for production environments where running clean can be quite a career limiting move. (default: false)")
    val flywayTarget = settingKey[String]("The target version up to which Flyway should run migrations. Migrations with a higher version number will not be  applied. (default: the latest version)")
    val flywayOutOfOrder = settingKey[Boolean]("Allows migrations to be run \"out of order\" (default: {@code false}). If you already have versions 1 and 3 applied, and now a version 2 is found, it will be applied too instead of being ignored.")
    val flywayCallbacks = settingKey[Seq[Callback]]("A list of callbacks that will be used for Flyway lifecycle notifications. (default: Empty)")
    val flywaySkipDefaultCallbacks = settingKey[Boolean]("Whether default built-in callbacks should be skipped. (default: false)")

    //*********************
    // settings for migrate
    //*********************
    val flywayIgnoreMissingMigrations = settingKey[Boolean]("Ignores missing migrations when reading the metadata table. (default: false)")
    val flywayIgnoreFutureMigrations = settingKey[Boolean]("Ignores future migrations when reading the metadata table. These are migrations that were performed by a newer deployment of the application that are not yet available in this version. For example: we have migrations available on the classpath up to version 3.0. The metadata table indicates that a migration to version 4.0 (unknown to us) has already been applied. Instead of bombing out (fail fast) with an exception, a warning is logged and Flyway continues normally. This is useful for situations where one must be able to redeploy an older version of the application after the database has been migrated by a newer one. (default: true)")
    val flywayIgnoreFailedFutureMigration = settingKey[Boolean]("Ignores failed future migrations when reading the metadata table. These are migrations that we performed by a newer deployment of the application that are not yet available in this version. For example: we have migrations available on the classpath up to version 3.0. The metadata table indicates that a migration to version 4.0 (unknown to us) has already been attempted and failed. Instead of bombing out (fail fast) with an exception, a warning is logged and Flyway terminates normally. This is useful for situations where a database rollback is not an option. An older version of the application can then be redeployed, even though a newer one failed due to a bad migration. (default: false)")
    val flywayPlaceholderReplacement = settingKey[Boolean]("Whether placeholders should be replaced. (default: true)")
    val flywayPlaceholders = settingKey[Map[String, String]]("A map of <placeholder, replacementValue> to apply to sql migration scripts.")
    val flywayPlaceholderPrefix = settingKey[String]("The prefix of every placeholder. (default: ${ )")
    val flywayPlaceholderSuffix = settingKey[String]("The suffix of every placeholder. (default: } )")
    val flywayBaselineOnMigrate = settingKey[Boolean]("Whether to automatically call baseline when migrate is executed against a non-empty schema with no metadata table. This schema will then be baselined with the {@code baselineVersion} before executing the migrations. Only migrations above {@code baselineVersion} will then be applied. This is useful for initial Flyway production deployments on projects with an existing DB. Be careful when enabling this as it removes the safety net that ensures Flyway does not migrate the wrong database in case of a configuration mistake! (default: {@code false})")
    val flywayValidateOnMigrate = settingKey[Boolean]("Whether to automatically call validate or not when running migrate. (default: true)")
    val flywayAllowMixedMigrations = settingKey[Boolean]("Whether to allow mixing transactional and non-transactional statements within the same migration. (default: false)")
    val flywayMixed = settingKey[Boolean]("Whether to allow mixing transactional and non-transactional statements within the same migration. (default: false)")
    val flywayGroup = settingKey[Boolean]("Whether to group all pending migrations together in the same transaction when applying them (only recommended for databases with support for DDL transactions). (default: false)")
    val flywayInstalledBy = settingKey[String]("The username that will be recorded in the metadata table as having applied the migration. (default: null)")

    //*********************
    // flyway tasks
    //*********************
    val flywayMigrate = taskKey[Unit]("Migrates of the configured database to the latest version.")
    val flywayValidate = taskKey[Unit]("Validate applied migrations against resolved ones (on the filesystem or classpath) to detect accidental changes that may prevent the schema(s) from being recreated exactly. Validation fails if differences in migration names, types or checksums are found, versions have been applied that aren't resolved locally anymore or versions have been resolved that haven't been applied yet")
    val flywayInfo = taskKey[Unit]("Retrieves the complete information about the migrations including applied, pending and current migrations with details and status.")
    val flywayClean = taskKey[Unit]("Drops all database objects.")
    val flywayBaseline = taskKey[Unit]("Baselines an existing database, excluding all migrations up to and including baselineVersion.")
    val flywayRepair = taskKey[Unit]("Repairs the metadata table.")
    val flywayDefaults = taskKey[FluentConfiguration]("Default configuration. This task is used to help resolve classpaths properly")
  }

  //*********************
  // convenience settings
  //*********************
  private case class ConfigDataSource(driver: String, url: String, user: String, password: String) {
    def asProps: Map[String, String] =  (if (driver.isEmpty) {
      Map()
    } else {
      Map("flyway.driver" -> driver)
    }) ++ Map("flyway.url" -> url, "flyway.user" -> user, "flyway.password" -> password)
  }
  private case class ConfigBase(schemas: Seq[String], table: String, baselineVersion: String, baselineDescription: String)
  private case class ConfigMigrationLoading(locations: Seq[String], resolvers: Seq[String], skipDefaultResolvers: Boolean, encoding: String,
                                            cleanOnValidationError: Boolean, cleanDisabled: Boolean, target: String, outOfOrder: Boolean,
                                            callbacks: Seq[Callback], skipDefaultCallbacks: Boolean)
  private case class ConfigSqlMigration(sqlMigrationPrefix: String, repeatableSqlMigrationPrefix: String, sqlMigrationSeparator: String, sqlMigrationSuffixes: String*)
  private case class ConfigMigrate(ignoreMissingMigrations: Boolean, ignoreFutureMigrations: Boolean, ignoreFailedMigrations: Boolean,
                                   baselineOnMigrate: Boolean, validateOnMigrate: Boolean, mixed: Boolean, group: Boolean, installedBy: String)
  private case class ConfigPlaceholder(placeholderReplacement: Boolean, placeholders: Map[String, String],
                                   placeholderPrefix: String, placeholderSuffix: String)
  private case class Config(dataSource: ConfigDataSource, base: ConfigBase, migrationLoading: ConfigMigrationLoading,
                            sqlMigration: ConfigSqlMigration, migrate: ConfigMigrate, placeholder: ConfigPlaceholder)


  private lazy val flywayConfigDataSource = taskKey[ConfigDataSource]("The Flyway data source configuration.")
  private lazy val flywayConfigBase = taskKey[ConfigBase]("The Flyway base configuration.")
  private lazy val flywayConfigMigrationLoading = taskKey[ConfigMigrationLoading]("The Flyway migration loading configuration.")
  private lazy val flywayConfigSqlMigration = taskKey[ConfigSqlMigration]("The Flyway sql migration configuration.")
  private lazy val flywayConfigMigrate = taskKey[ConfigMigrate]("The Flyway migrate configuration.")
  private lazy val flywayConfigPlaceholder = taskKey[ConfigPlaceholder]("The Flyway placeholder configuration.")
  private lazy val flywayConfig = taskKey[Config]("The Flyway configuration.")
  private lazy val flywayClasspath = taskKey[Types.Id[Classpath]]("The classpath used by Flyway.")

  //*********************
  // flyway defaults
  //*********************
  override def projectSettings :Seq[Setting[_]] = flywayBaseSettings(Runtime) ++ inConfig(Test)(flywayBaseSettings(Test))

  def flywayBaseSettings(conf: Configuration) :Seq[Setting[_]] = {
    import FlywayPlugin.autoImport._
    val defaults = getFlywayDefaults
    Seq[Setting[_]](
      flywayDriver := "",
      flywayUrl := "",
      flywayUser := "",
      flywayPassword := "",
      flywayLocations := List("db/migration"),
      flywayResolvers := Array.empty[String],
      flywaySkipDefaultResolvers := defaults.isSkipDefaultResolvers,
      flywaySchemas := defaults.getSchemas.toSeq,
      flywayTable := defaults.getTable,
      flywayBaselineVersion := defaults.getBaselineVersion.getVersion,
      flywayBaselineDescription := defaults.getBaselineDescription,
      flywayEncoding := defaults.getEncoding.toString,
      flywaySqlMigrationPrefix := defaults.getSqlMigrationPrefix,
      flywayRepeatableSqlMigrationPrefix := defaults.getRepeatableSqlMigrationPrefix,
      flywaySqlMigrationSeparator := defaults.getSqlMigrationSeparator,
      flywaySqlMigrationSuffixes := defaults.getSqlMigrationSuffixes,
      flywayTarget := "current",
      flywayOutOfOrder := defaults.isOutOfOrder,
      flywayCallbacks := new Array[Callback](0),
      flywaySkipDefaultCallbacks := defaults.isSkipDefaultCallbacks,
      flywayIgnoreMissingMigrations := defaults.isIgnoreMissingMigrations,
      flywayIgnoreFutureMigrations := defaults.isIgnoreFutureMigrations,
      flywayIgnoreFailedFutureMigration := defaults.isIgnoreFutureMigrations,
      flywayPlaceholderReplacement := defaults.isPlaceholderReplacement,
      flywayPlaceholders := defaults.getPlaceholders.asScala.toMap,
      flywayPlaceholderPrefix := defaults.getPlaceholderPrefix,
      flywayPlaceholderSuffix := defaults.getPlaceholderSuffix,
      flywayBaselineOnMigrate := defaults.isBaselineOnMigrate,
      flywayValidateOnMigrate := defaults.isValidateOnMigrate,
      flywayMixed := defaults.isMixed,
      flywayGroup := defaults.isGroup,
      flywayInstalledBy := "",
      flywayCleanOnValidationError := defaults.isCleanOnValidationError,
      flywayCleanDisabled := defaults.isCleanDisabled,
      flywayConfigDataSource := ConfigDataSource(flywayDriver.value, flywayUrl.value, flywayUser.value, flywayPassword.value),
      flywayConfigBase := ConfigBase(flywaySchemas.value, flywayTable.value, flywayBaselineVersion.value, flywayBaselineDescription.value),
      flywayConfigMigrationLoading := ConfigMigrationLoading(flywayLocations.value, flywayResolvers.value, flywaySkipDefaultResolvers.value, flywayEncoding.value, flywayCleanOnValidationError.value, flywayCleanDisabled.value, flywayTarget.value, flywayOutOfOrder.value, flywayCallbacks.value, flywaySkipDefaultCallbacks.value),
      flywayConfigSqlMigration := ConfigSqlMigration(flywaySqlMigrationPrefix.value, flywayRepeatableSqlMigrationPrefix.value, flywaySqlMigrationSeparator.value, flywaySqlMigrationSuffixes.value:_*),
      flywayConfigMigrate := ConfigMigrate(flywayIgnoreMissingMigrations.value, flywayIgnoreFutureMigrations.value, flywayIgnoreFailedFutureMigration.value,
      flywayBaselineOnMigrate.value, flywayValidateOnMigrate.value, flywayMixed.value, flywayGroup.value, flywayInstalledBy.value),
      flywayConfigPlaceholder := ConfigPlaceholder(flywayPlaceholderReplacement.value, flywayPlaceholders.value, flywayPlaceholderPrefix.value, flywayPlaceholderSuffix.value),
      flywayConfig := Config(flywayConfigDataSource.value, flywayConfigBase.value, flywayConfigMigrationLoading.value, flywayConfigSqlMigration.value, flywayConfigMigrate.value, flywayConfigPlaceholder.value),
      flywayClasspath := (Def.taskDyn {
        // fullClasspath triggers the compile task, so use a dynamic task to only run it if we need to.
        // https://github.com/flyway/flyway-sbt/issues/10
        if (flywayLocations.value.forall(_.startsWith("filesystem:"))) {
          externalDependencyClasspath in conf
        } else {
          fullClasspath in conf
        }
      }).value,
      // Tasks
      flywayDefaults := withPrepared(flywayClasspath.value, streams.value)(Flyway.configure()),
      flywayMigrate := flywayDefaults.value.configure(flywayConfig.value).migrate(),
      flywayValidate := flywayDefaults.value.configure(flywayConfig.value).validate(),
      flywayInfo := {
        val info = flywayDefaults.value.configure(flywayConfig.value).info()
        streams.value.log.info(MigrationInfoDumper.dumpToAsciiTable(info.all()))
      },
      flywayRepair := flywayDefaults.value.configure(flywayConfig.value).repair(),
      flywayClean := flywayDefaults.value.configure(flywayConfig.value).clean(),
      flywayBaseline := flywayDefaults.value.configure(flywayConfig.value).baseline()
    )
  }

  private def getFlywayDefaults: FluentConfiguration = {
    // This needs to be set so that Flyway could initialize properly
    // See https://github.com/flyway/flyway/issues/1922
    LogFactory.setLogCreator(SbtLogCreator)
    Flyway.configure()
  }

  private def withPrepared[T](cp: Types.Id[Keys.Classpath], streams: TaskStreams)(f: => T): T = {
    registerAsFlywayLogger(streams)
    withContextClassLoader(cp)(f)
  }

  /**
   * registers sbt log as a static logger for Flyway
   */
  private def registerAsFlywayLogger(streams: TaskStreams): Unit = {
    LogFactory.setLogCreator(SbtLogCreator)
    FlywaySbtLog.streams = Some(streams)
  }

  private def withContextClassLoader[T](cp: Types.Id[Keys.Classpath])(f: => T): T = {
    val classloader = sbt.internal.inc.classpath.ClasspathUtilities.toLoader(cp.map(_.data), getClass.getClassLoader)
    val thread = Thread.currentThread
    val oldLoader = thread.getContextClassLoader
    try {
      thread.setContextClassLoader(classloader)
      f
    } finally {
      thread.setContextClassLoader(oldLoader)
    }
  }

  private implicit class StringOps(val s: String) extends AnyVal {
    def emptyToNull(): String = s match {
      case ss if ss.isEmpty => null
      case _ => s
    }
  }

  private implicit class FluentConfigurationyOps(val flyway: FluentConfiguration) extends AnyVal {
    def configure(config: Config): Flyway = {
      flyway
      .configure(config.base)
      .configure(config.migrationLoading)
      .configure(config.sqlMigration)
      .configure(config.migrate)
      .configure(config.placeholder)
      .configureSysProps(config.dataSource)
      .load()
    }
    def configure(config: ConfigBase): FluentConfiguration = {
      flyway
      .schemas(config.schemas: _*)
      .table(config.table)
      .baselineVersion(config.baselineVersion)
      .baselineDescription(config.baselineDescription)

    }
    def configure(config: ConfigMigrationLoading): FluentConfiguration = {
      flyway.locations(config.locations: _*)
      .encoding(config.encoding)
      .cleanOnValidationError(config.cleanOnValidationError)
      .cleanDisabled(config.cleanDisabled)
      //.target(config.target) Setting this as-is will make the default be "current", which we don't want
      .outOfOrder(config.outOfOrder)
      .callbacks(config.callbacks: _*)
      .resolvers(config.resolvers: _*)
      .skipDefaultResolvers(config.skipDefaultResolvers)
      .skipDefaultCallbacks(config.skipDefaultCallbacks)
    }
    def configure(config: ConfigSqlMigration): FluentConfiguration = {
      flyway
      .sqlMigrationPrefix(config.sqlMigrationPrefix)
      .repeatableSqlMigrationPrefix(config.repeatableSqlMigrationPrefix)
      .sqlMigrationSeparator(config.sqlMigrationSeparator)
      .sqlMigrationSuffixes(config.sqlMigrationSuffixes: _*)
    }
    def configure(config: ConfigMigrate): FluentConfiguration = {
      val ignoreFutureMigrations = if(config.ignoreFailedMigrations) true else config.ignoreFutureMigrations

      flyway
      .ignoreMissingMigrations(config.ignoreMissingMigrations)
      .ignoreFutureMigrations(ignoreFutureMigrations)
      .baselineOnMigrate(config.baselineOnMigrate)
      .validateOnMigrate(config.validateOnMigrate)
      .mixed(config.mixed)
      .group(config.group)
      .installedBy(config.installedBy)
    }
    def configure(config: ConfigPlaceholder): FluentConfiguration = {
      flyway
      .placeholderReplacement(config.placeholderReplacement)
      .placeholders(config.placeholders.asJava)
      .placeholderPrefix(config.placeholderPrefix)
      .placeholderSuffix(config.placeholderSuffix)
    }
    def configureSysProps(config: ConfigDataSource): FluentConfiguration = {
      val props = new Properties()
      System.getProperties.asScala.filter(e => e._1.startsWith("flyway")).foreach(e => props.put(e._1, e._2))
      config.asProps.filter(e => !sys.props.contains(e._1)).foreach(e => props.put(e._1, e._2))
      flyway.configuration(props)
    }
  }

  private object SbtLogCreator extends LogCreator {
    def createLogger(clazz: Class[_]): FlywaySbtLog.type = FlywaySbtLog
  }

  private object FlywaySbtLog extends Log {
    var streams: Option[TaskStreams] = None

    def isDebugEnabled: Boolean = false
    def debug(message: String): Unit = { streams foreach (_.log.debug(message)) }
    def info(message: String): Unit = { streams foreach (_.log.info(message)) }
    def warn(message: String): Unit = { streams foreach (_.log.warn(message)) }
    def error(message: String): Unit = { streams foreach (_.log.error(message)) }
    def error(message: String, e: Exception): Unit = { streams foreach (_.log.error(message)); streams foreach (_.log.trace(e)) }
  }
}

