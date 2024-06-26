plugins {
    base
    id("via.build-logic")
}

allprojects {
    group = "com.viaversion"
    version = property("projectVersion") as String // from gradle.properties
    description = "Allow newer clients to join older server versions."
}

val main = setOf(
    projects.viaversion,
    projects.viaversionCommon,
    projects.viaversionApi,
    projects.viaversionBukkit,
    projects.viaversionVelocity,
    projects.viaversionFabric
).map { it.dependencyProject }

subprojects {
    when (this) {
        in main -> plugins.apply("via.shadow-conventions")
        else -> plugins.apply("via.base-conventions")
    }
}
