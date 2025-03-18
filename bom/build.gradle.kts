plugins {
    id("com.vanniktech.maven.publish")
    id("java-platform")
}

dependencies {
    constraints {
        // all Android library modules
        rootProject.subprojects.forEach { subproject ->
            val isBomProject: Boolean = subproject.name.contains("bom")
            val isApplicationProject: Boolean =
                subproject.name.contains("example")

            if (listOf(isBomProject, isApplicationProject).none { it }) {
                api(subproject)
            }
        }

        // 3rd party wrappers
        api(libs.primer.threeds)
        api(libs.primer.ipay88)
        api(libs.primer.klarna)
        api(libs.primer.stripe)
        api(libs.primer.nol.pay)
    }
}

mavenPublishing {
    val version = project.findProperty("VERSION_NAME").toString()
    val groupId = project.findProperty("GROUP").toString()
    val artifactId = project.findProperty("POM_ARTIFACT_ID").toString()
    val artifactDescription = project.findProperty("POM_DESCRIPTION").toString()
    coordinates(groupId, artifactId, version)

    pom {
        description.set(artifactDescription)
    }
}
