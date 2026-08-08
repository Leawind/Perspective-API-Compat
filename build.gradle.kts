import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.tasks.Sync

plugins {
    id("dev.kikugie.stonecutter")
    id("dev.isxander.modstitch.base")
    id("me.modmuss50.mod-publish-plugin")
}

// region Versions & Project Info
val mcVersion: String by project
val modGroupValue = requiredProp("mod.group")
val modIdValue = requiredProp("mod.id")
val modVersionString = requiredProp("mod.version")
val modNameValue = requiredProp("mod.name")
val modDescriptionValue = requiredProp("mod.description")
val modAuthorValue = requiredProp("mod.author")
val modLicenseValue = requiredProp("mod.license")
val modLogoFile = requiredProp("mod.logo_file")
val modHomeUrl = requiredProp("mod.home_url")
val modSourceUrl = requiredProp("mod.source_url")
val modIssuesUrl = requiredProp("mod.issues_url")
val modEmail = requiredProp("mod.email")

val isFabric = modstitch.isLoom
val isNeoforge = modstitch.isModDevGradleRegular
val isForge = modstitch.isModDevGradleLegacy
val loader = when {
    isFabric -> "fabric"
    isNeoforge -> "neoforge"
    isForge -> "forge"
    else -> error("Unknown loader")
}
// Extra version labels are declared by each version variant for publishing platforms.
val publishedMinecraftVersions = buildList {
    add(mcVersion)
    findProperty("publish.additionalMcVersions")
        ?.toString()
        ?.split(',')
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.forEach { add(it) }
}.distinct()

// Determine if unit testing is supported for this platform/version
// - Fabric: all versions
// - NeoForge: >= 1.20.5 (JUnit run type not available in older versions)
// - Forge: not supported
val supportsUnitTesting = isFabric || (isNeoforge && stonecutter.current.parsed >= "1.20.5")

val perspectiveApiDevelopmentDirectory =
    System.getenv("PERSPECTIVE_API_DIR") ?: System.getProperty("PERSPECTIVE_API_DIR")
val useLocalPerspectiveApi = !perspectiveApiDevelopmentDirectory.isNullOrBlank()
val perspectiveApiCoordinate =
    if (useLocalPerspectiveApi) {
        "io.github.leawind.perspectiveapi:perspective_api:0.0-SNAPSHOT+$loader-$mcVersion"
    } else {
        "maven.modrinth:LIqveQm1:${requiredProp("mod.perspective_api_version")}+$loader-$mcVersion"
    }
// endregion

// region ModStitch Setup
modstitch {
    minecraftVersion = mcVersion

    loom {
        if (isFabric) {
            fabricLoaderVersion = requiredProp("deps.fabricLoader")
            configureLoom {
                runs.named("client") {
                    runDir = project.relativePath(rootProject.file("run"))
                }
            }
        }
    }

    if (!isFabric) {
        runs {
            register("client") {
                client()
                gameDirectory.set(rootProject.layout.projectDirectory.dir("run"))
            }
        }
    }

    moddevgradle {
        if (isNeoforge) {
            neoForgeVersion = requiredProp("deps.neoforge")
        }
        if (isForge) {
            forgeVersion = requiredProp("deps.forge")
        }
    }

    metadata {
        modId = modIdValue
        modName = modNameValue
        modVersion = "$modVersionString+$loader-$mcVersion"
        modGroup = modGroupValue
        modDescription = modDescriptionValue
        modLicense = modLicenseValue
        modAuthor = modAuthorValue

        replacementProperties.put("logo_file", modLogoFile)
        replacementProperties.put("home_url", modHomeUrl)
        replacementProperties.put("source_url", modSourceUrl)
        replacementProperties.put("issues_url", modIssuesUrl)
        replacementProperties.put("email", modEmail)
        replacementProperties.put("github", "Leawind/Perspective-API-Compat")
        replacementProperties.put("mc", requiredProp("meta.mcDep"))
        replacementProperties.put(
            "perspectiveApiVersion",
            requiredProp("mod.perspective_api_version"),
        )
        if (isNeoforge) {
            replacementProperties.put("loaderVersion", requiredProp("meta.loaderDep"))
        } else if (isForge) {
            replacementProperties.put("loaderVersion", "*")
        }
    }

    // Optional target mods are mixed in via their own configs.
    mixin {
        addMixinsToModManifest = true
        configs.register("perspective_api_compat.shouldersurfing")
        if (isFabric) {
            configs.register("perspective_api_compat.shouldersurfing.fabric")
        }
    }

    // Enable unit testing for supported platforms
    if (supportsUnitTesting) {
        unitTesting()
    }
}
// endregion

// region Stonecutter
stonecutter {
    constants {
        put("fabric", isFabric)
        put("neoforge", isNeoforge)
        put("forge", isForge)
    }
}
// endregion

// The transitive `net.minecraftforge:unsafe` dependency uses the dynamic version `2.11.+`.
if (isForge || (isNeoforge && stonecutter.current.parsed < "1.21")) {
    configurations.configureEach {
        resolutionStrategy {
            force("org.apache.logging.log4j:log4j-api:2.24.3")
            force("org.apache.logging.log4j:log4j-core:2.24.3")
        }
    }
}

// region Dependencies
dependencies {
    // Perspective API is required at compile time and at runtime. Local development can use a
    // freshly published Perspective API checkout by setting PERSPECTIVE_API_DIR. Its optional
    // development dependencies are deliberately not inherited: Perspective API's own metadata
    // remains the runtime authority for those dependencies.
    (modstitchModImplementation(perspectiveApiCoordinate) as ExternalModuleDependency).isTransitive =
        false

    // AutoService generates the loader-specific PlatformHelper service descriptor.
    compileOnly("com.google.auto.service:auto-service-annotations:1.1.1")
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")

    // Perspective API owns the runtime MixinExtras artifact. Compile against its common API, and
    // let Legacy Forge's annotation processor emit production mappings for the optional mixin.
    val mixinExtrasCommon = "io.github.llamalad7:mixinextras-common:0.5.4"
    compileOnly(mixinExtrasCommon)
    if (isForge) {
        annotationProcessor(mixinExtrasCommon)
    }

    if (isFabric) {
        modstitchModImplementation(
            "net.fabricmc.fabric-api:fabric-api:${requiredProp("deps.fabricApi")}",
        )
    }

    // Shoulder Surfing Reloaded is an optional target mod; its API is only accessed reflectively.
    modstitchModImplementation("maven.modrinth:kepjj2sy:${requiredProp("mod.shouldersurfing_version")}")

    // SSR hard-depends on Forge Config API Port on Fabric (and its legacy NeoForge builds).
    findProperty("mod.forgeconfigapiport_version")
        ?.toString()
        ?.takeIf { it.isNotBlank() }
        ?.let { modstitchModImplementation("maven.modrinth:forge-config-api-port:$it") }

    // Loom's remap removes the nested jars of dependency mods on obfuscated versions, so the
    // nightconfig bundled by Forge Config API Port is missing from the dev runtime classpath
    // there; add the same version explicitly for those variants.
    findProperty("deps.nightconfigVersion")
        ?.toString()
        ?.takeIf { it.isNotBlank() }
        ?.let { nightconfig ->
            runtimeOnly("com.electronwill.night-config:core:$nightconfig")
            runtimeOnly("com.electronwill.night-config:toml:$nightconfig")
        }

    // Test
    testCompileOnly("org.jspecify:jspecify:1.0.0")
    // Note: fabric-loader-junit is added by modstitch.unitTesting() for Fabric
    if (!isFabric) {
        testImplementation("org.junit.jupiter:junit-jupiter:6.0.3")
    }
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.0.3")
    testImplementation("com.google.jimfs:jimfs:1.3.0") {
        exclude(group = "com.google.guava", module = "guava")
    }
}
// endregion

// region Tasks
tasks.test {
    useJUnitPlatform()
    // Disable tests for unsupported platforms
    if (!supportsUnitTesting) {
        enabled = false
    }
}

tasks.withType<JavaExec>().configureEach {
    if (name == "runClient") {
        workingDir(rootProject.layout.projectDirectory.dir("run"))
    }
}

// Skip test compilation for unsupported platforms
if (!supportsUnitTesting) {
    tasks.compileTestJava {
        enabled = false
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

tasks.named("check") {
    dependsOn(rootProject.tasks.named("checkArchitecture"))
}

// Resources include both template-generated and explicitly excluded per-platform metadata.
tasks.withType<Jar>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
tasks.withType<ProcessResources>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

// Exclude loader-specific metadata. Forge's AP generates its refmap, while Fabric and NeoForge
// retain the empty resource refmap used by their runtime remappers.
tasks.named<ProcessResources>("processResources") {
    if (isForge) {
        exclude("perspective_api_compat.refmap.json")
    }
    if (!isFabric) {
        exclude("perspective_api_compat.shouldersurfing.fabric.mixins.json")
    }
}
// endregion

// region Publishing
rootProject.tasks.named<Sync>("buildAndCollect") {
    dependsOn(modstitch.finalJarTask)
    from(modstitch.finalJarTask.flatMap { it.archiveFile })
}

// read changelog
val changelogFile = rootProject.file("CHANGELOG.md")
val changelogText = if (changelogFile.exists()) changelogFile.readText() else ""

afterEvaluate {
    publishMods {
        // Always dry run until environment variable `DRY_RUN` is set to `false`
        dryRun.set(System.getenv("DRY_RUN") != "false")
        displayName.set("$modVersionString for $mcVersion $loader")
        file = modstitch.finalJarTask.flatMap { it.archiveFile }
        changelog.set(changelogText)

        type = if (modVersionString.contains("beta", true)) {
            BETA
        } else if (modVersionString.contains("alpha", true)) {
            ALPHA
        } else {
            STABLE
        }

        modLoaders.add(loader)
        modrinth {
            accessToken = System.getenv("MODRINTH_TOKEN")
            projectId = System.getenv("MODRINTH_ID")
            minecraftVersions.addAll(publishedMinecraftVersions)
        }
        curseforge {
            accessToken = System.getenv("CURSEFORGE_TOKEN")
            projectId = System.getenv("CURSEFORGE_ID")
            minecraftVersions.addAll(publishedMinecraftVersions)
            clientRequired = true
            serverRequired = false
        }
    }
}
// endregion

// region Helpers
fun requiredProp(property: String): String =
    findProperty(property)?.toString()?.takeIf { it.isNotBlank() }
        ?: error("Required Gradle property '$property' is missing or blank")
// endregion
