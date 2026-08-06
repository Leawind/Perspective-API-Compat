plugins {
  id("dev.kikugie.stonecutter")

  id("dev.isxander.modstitch.base") version "0.8.5" apply false
  id("me.modmuss50.mod-publish-plugin") version "2.2.0" apply false
  id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT" apply false
}

stonecutter active "26.2-fabric"

val checkArchitecture by tasks.registering {
  group = "verification"
  description = "Checks compatibility and platform dependency boundaries."

  val sourceRoot = layout.projectDirectory.dir("src/main/java")
  val javaSources = fileTree(sourceRoot) { include("**/*.java") }
  inputs.files(javaSources)

  doLast {
    val basePackage = "io.github.leawind.perspectiveapicompat"
    val compatibilityPackage = "$basePackage.internal.compat"
    val logicPackage = "$basePackage.internal.logic"
    val platformApiPackage = "$basePackage.platform.api"
    val concretePlatformPackages =
        listOf(
            "$basePackage.platform.fabric",
            "$basePackage.platform.forge",
            "$basePackage.platform.neoforge",
        )
    val violations = mutableListOf<String>()

    fun importedType(line: String): String? {
      val declaration = line.trim().removePrefix("/*").trim()
      if (!declaration.startsWith("import ")) return null
      return declaration
          .removePrefix("import ")
          .removePrefix("static ")
          .substringBefore(';')
          .trim()
    }

    javaSources.files.sortedBy { it.path }.forEach { sourceFile ->
      val relativePath = sourceFile.relativeTo(sourceRoot.asFile).invariantSeparatorsPath
      val isCompatibility =
          relativePath.startsWith("io/github/leawind/perspectiveapicompat/internal/compat/")
      val isLogic =
          relativePath.startsWith("io/github/leawind/perspectiveapicompat/internal/logic/")
      val isPlatformApi =
          relativePath.startsWith("io/github/leawind/perspectiveapicompat/platform/api/")

      sourceFile.readLines().forEachIndexed { index, line ->
        val location = "$relativePath:${index + 1}"
        if (line.contains("@Redirect") || line.contains(".injection.Redirect")) {
          violations += "$location: Mixin @Redirect is forbidden; use a composable injector"
        }
        if (
            line.contains("@ModifyArgs") ||
                line.contains(".injection.ModifyArgs") ||
                line.contains(".injection.invoke.arg.Args")
        ) {
          violations +=
              "$location: Mixin @ModifyArgs is incompatible with Forge 1.20.1; avoid synthetic Args classes"
        }

        val importedName = importedType(line) ?: return@forEachIndexed
        val importsConcretePlatform =
            concretePlatformPackages.any {
              importedName == it || importedName.startsWith("$it.")
            }
        if ((isCompatibility || isLogic) && importsConcretePlatform) {
          violations += "$location: shared compatibility code must depend on platform.api only"
        }
        if (
            isPlatformApi &&
                (importedName.startsWith("$compatibilityPackage.") ||
                    importedName.startsWith("$logicPackage."))
        ) {
          violations += "$location: platform.api cannot depend on compatibility logic"
        }
        if (
            isCompatibility &&
                importedName.startsWith("$basePackage.platform.") &&
                !importedName.startsWith("$platformApiPackage.")
        ) {
          violations += "$location: compatibility code must not bypass platform.api"
        }
      }
    }

    if (violations.isNotEmpty()) {
      throw GradleException(
          "Architecture violations:\n" + violations.joinToString("\n") { "- $it" },
      )
    }
  }
}

val buildAndCollect by tasks.registering(Sync::class) {
  group = "build"
  description = "Builds and collects all distributable jars."
  dependsOn(checkArchitecture)
  into(layout.buildDirectory.dir("libs"))
}

allprojects {
  repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.neoforged.net/releases/") {
      content {
        includeGroupByRegex("net\\.neoforged(\\..*)?")
      }
    }
    maven("https://maven.minecraftforge.net/") {
      content {
        includeGroupByRegex("net\\.minecraftforge(\\..*)?")
      }
    }
  }
}
