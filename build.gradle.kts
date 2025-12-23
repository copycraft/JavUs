plugins {
    id("java")
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("org.example.Main")
}

repositories {
    mavenCentral()
}

val lwjglVersion = "3.3.4"

dependencies {
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    implementation("org.lwjgl:lwjgl")
    implementation("org.lwjgl:lwjgl-glfw")
    implementation("org.lwjgl:lwjgl-opengl")

    runtimeOnly("org.lwjgl:lwjgl::natives-linux")
    runtimeOnly("org.lwjgl:lwjgl-glfw::natives-linux")
    runtimeOnly("org.lwjgl:lwjgl-opengl::natives-linux")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    runtimeOnly("org.lwjgl:lwjgl-stb::natives-linux")
    implementation("org.lwjgl:lwjgl-stb")
}

tasks.test {
    useJUnitPlatform()
}

/* 🔴 THIS IS THE IMPORTANT PART 🔴 */
tasks.withType<JavaExec>().configureEach {
    environment("GLFW_DISABLE_LIBDECOR", "1")
    // Optional fallback if Wayland is still annoying:
    // environment("GLFW_USE_WAYLAND", "0")
}
