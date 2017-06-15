// Here we turn on the sbt-assembly plugin. It is used to build a fat jar with all external dependencies.
logLevel := Level.Warn

resolvers += "JBoss" at "https://repository.jboss.org"  // Workaround for sbt-assembly issue.

addSbtPlugin("com.artima.supersafe" % "sbtplugin" % "1.1.2")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.4")