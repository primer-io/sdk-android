plugins {
    id("com.vanniktech.maven.publish.base")
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

publishing {
    val version = project.findProperty("VERSION_NAME") as String
    val groupId = project.findProperty("GROUP") as String
    setVersion(version)
    group = groupId
    publications.create<MavenPublication>("maven") {
        from(project.components["javaPlatform"])
    }
}
