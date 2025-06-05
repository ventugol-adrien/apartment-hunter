plugins {
    id("java")
    id("application")
    id ("org.springframework.boot") version("3.2.5")
}
apply(plugin = "io.spring.dependency-management")
apply(plugin = "java")

group = "org.example"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(platform("org.mongodb:mongodb-driver-bom:5.4.0"))
    implementation("org.mongodb:mongodb-driver-sync")
    implementation(platform("software.amazon.awssdk:bom:2.27.21"))
    implementation("software.amazon.awssdk:secretsmanager")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.google.maps:google-maps-services:2.2.0")
    implementation("com.google.maps:google-maps-routing:1.44.0")
}

application {
    mainClass.set("org.server.Main")
}

tasks.test {
    useJUnitPlatform()
}