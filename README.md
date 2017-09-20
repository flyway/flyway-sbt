# flyway-sbt


Work in progress for the sbt plugin factored out as a project separate from the core
flyway repo.


Todo/discussion points:

- Merge the test (flyway-sbt-largetest) into flyway-sbt repo
- Use sbt for build instead of Maven
- Decide on version. The plugin does not need to be the same version as Flyway itself.
- Use sbt to test its own plugin instead of using sbt-loader.jar
- CI integration (travis)
- Migrate current code to sbt 1.0 plugin (mostly done)
- Stop the plugin from *insisting* on a compilation prior to migration


