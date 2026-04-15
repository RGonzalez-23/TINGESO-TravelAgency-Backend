FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
# Damos permisos de ejecución al wrapper de Gradle y compilamos
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

# Etapa final: Imagen liviana solo con JRE 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copiamos el .jar generado por Gradle a la nueva imagen
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
