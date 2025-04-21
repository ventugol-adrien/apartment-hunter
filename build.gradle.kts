plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.maps:google-maps-services:2.2.0")
    implementation("com.google.maps:google-maps-routing:1.44.0")
}

tasks.test {
    useJUnitPlatform()
}