# Usar imagen base de OpenJDK 17
FROM openjdk:17-jdk-slim

# Información del mantenedor
LABEL maintainer="tu-email@ejemplo.com"

# Crear directorio de trabajo
WORKDIR /app

# Copiar el archivo JAR generado por Maven
COPY target/analytics_service-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto 8083 (según tu configuración)
EXPOSE 8083

# Configurar variables de entorno opcionales
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Variables de entorno para base de datos (opcional)
ENV SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/analytics_db
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=1234

# Comando para ejecutar la aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]