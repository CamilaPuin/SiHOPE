# ============================================================
# Etapa 1 — Compilación con Maven (JDK 17)
# ============================================================
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Descarga de dependencias en capa cacheable (solo se repite si cambia el pom).
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Compilación del artefacto (los tests se ejecutan en CI, no en la imagen).
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ============================================================
# Etapa 2 — Imagen de ejecución (solo JRE, más liviana)
# ============================================================
FROM eclipse-temurin:17-jre
WORKDIR /app

# Usuario no root por seguridad.
RUN useradd -r -u 1001 spring
USER spring

# Copia el jar ejecutable generado por Spring Boot.
COPY --from=build /app/target/libraryapp-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
