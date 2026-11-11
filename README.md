<div align="center">

  <img src="app/src/main/res/drawable/app_logo.png" alt="Hato Logo" width="130" style="border-radius: 50%;" />

  # 🐮 HATO
  ### Plataforma Integral de Gestión Ganadera y Telemetría en Tiempo Real

  [![Android](https://img.shields.io/badge/Platform-Android_8.0+-3DDC84?style=flat&logo=android&logoColor=white)](https://developer.android.com)
  [![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-7F52FF?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org)
  [![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
  [![Spring Boot](https://img.shields.io/badge/Backend-Spring_Boot_3.3.4-6DB33F?style=flat&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
  [![Java](https://img.shields.io/badge/JDK-Java_21_LTS-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://openjdk.org)
  [![Database](https://img.shields.io/badge/Database-PostgreSQL_/_Supabase-336791?style=flat&logo=postgresql&logoColor=white)](https://supabase.com)
  [![Cloud](https://img.shields.io/badge/Deploy-Render_Cloud-46E3B7?style=flat&logo=render&logoColor=white)](https://render.com)
  [![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

  <p align="center">
    <b>Monitoreo GPS Satelital • Geocercas Inteligentes • Detección de Fugas e Inactividad • Pesaje y Proyección Comercial • Alertas en Tiempo Real</b>
  </p>

  [📲 Descargar APK](#-instalación-y-descarga-móvil) •
  [🌐 Backend en Vivo](#-despliegue-en-la-nube-render) •
  [✨ Características](#-características-principales) •
  [🏛️ Arquitectura](#-arquitectura-del-sistema) •
  [🚀 Guía de Inicio](#-guía-de-inicio-rápido)
</div>

---

## 📲 Instalación y Descarga Móvil

Puedes instalar la aplicación directamente en cualquier dispositivo Android (8.0 o superior):

<div align="center">
  <table>
    <tr>
      <td align="center" width="280">
        <img src="apk/hato-download-qr.png" alt="Código QR Descarga Hato" width="220" />
        <br />
        <sub><b>📱 Escanea para Descargar el APK</b></sub>
      </td>
      <td valign="middle">
        <h3>Descarga Directa</h3>
        <p>Apunta la cámara de tu celular al código QR o pulsa el botón oficial para descargar el APK compilado:</p>
        <p>
          <a href="https://github.com/DC268/Hato/raw/main/apk/hato-app.apk">
            <img src="https://img.shields.io/badge/Descargar_APK-hato--app.apk_(12.2_MB)-005C3B?style=for-the-badge&logo=android&logoColor=white" alt="Descargar APK" />
          </a>
        </p>
        <ul>
          <li><b>Archivo:</b> <code>apk/hato-app.apk</code></li>
          <li><b>Autenticación:</b> Correo/Contraseña cifrada y Google Sign-In oficial (Credential Manager)</li>
          <li><b>Hardware:</b> Compatible con collares GPS Bluetooth BLE y receptores satelitales</li>
        </ul>
      </td>
    </tr>
  </table>
</div>

---

## 🌐 Despliegue en la Nube (Render)

El microservicio de backend está desplegado y operativo en la nube de Render conectado a PostgreSQL en Supabase:

* 🌐 **URL Base del API:** `https://hato-backend-gkq3.onrender.com`
* 🩺 **Health Check:** [`https://hato-backend-gkq3.onrender.com/actuator/health`](https://hato-backend-gkq3.onrender.com/actuator/health) (Respuesta: `{"status":"UP"}`)
* 📡 **WebSocket en Vivo:** `wss://hato-backend-gkq3.onrender.com/ws/live?farmId={id}`
* 🗄️ **Base de Datos:** PostgreSQL en Supabase AWS (Oregon).

---

## ✨ Características Principales

### 🗺️ 1. Mapa Satelital y Geocercas Dinámicas
- Visualización cartográfica fluida mediante **OSMDroid** (OpenStreetMap, sin cuotas ni API keys de Google Maps).
- Creación interactiva de potreros mediante polígonos o círculos con algoritmo de ordenamiento convexo para prevenir intersecciones cruzadas.
- Monitoreo en vivo de animales con identificador de emoji de vaca (🐮) y telemetría de batería.
- Filtrado inteligente: el usuario solo visualiza los potreros, trayectorias y animales creados por su cuenta.

### 🐮 2. Gestión del Hato y Proyección Comercial
- Registro de bovinos con arete/RFID (únicamente numérico), raza, edad y peso en kilogramos (`kg`).
- **Calculadora Automática de Valor Comercial**: Proyección en vivo de la ganancia estimada de venta basada en el precio de ganado en pie en Colombia (~$9.000 COP/kg).
- Acciones rápidas al tocar un animal: ubicar directamente en el mapa o editar sus especificaciones.

### 🔔 3. Sistema de Alertas Jerárquico en 3 Niveles
- 🟢 **Informativas (Verde):** Dispositivos y collares conectados exitosamente, sincronización activa.
- 🟡 **Preventivas (Amarillo):** Ganado con inactividad prolongada o comportamiento anormal.
- 🔴 **Urgentes (Rojo):** Fuga de geocerca, cruce de perímetro o batería crítica.

### 📡 4. Conectividad Bluetooth & Collares GPS
- Escaneo y vinculación de dispositivos Bluetooth cercanos en carrusel horizontal.
- Persistencia permanente de dispositivos vinculados.
- Descarte automático del propio teléfono para evitar que se interprete a sí mismo como collar.

### 🔐 5. Seguridad y Autenticación Oficial con Google
- Inicio de sesión con correo y contraseña protegidos con hash BCrypt y tokens JWT.
- Integración oficial con **Google Sign-In / Credential Manager** mediante Google Cloud OAuth 2.0 y verificación criptográfica en backend (`GoogleIdTokenVerifier`).
- Creación automática de cuenta para nuevos usuarios y acceso directo para usuarios existentes.

---

## 🏛️ Arquitectura del Sistema

```
                      ┌────────────────────────────────────────┐
                      │        Dispositivos / Collares         │
                      │       Bluetooth BLE / GPS Tracker      │
                      └───────────────────┬────────────────────┘
                                          │
                                          ▼
                      ┌────────────────────────────────────────┐
                      │       📱 Hato Android Mobile App       │
                      │  Kotlin 2.0 • Jetpack Compose • MVVM   │
                      │   OSMDroid Maps • Credential Manager   │
                      └───────┬────────────────────────┬───────┘
                              │ REST / HTTPS           │ WSS (Live)
                              ▼                        ▼
                      ┌────────────────────────────────────────┐
                      │       🖥️ Hato Backend Service          │
                      │  Spring Boot 3.3.4 • Java 21 LTS       │
                      │  WebSocket Broker • Google Auth Verif  │
                      └───────────────────┬────────────────────┘
                                          │ JPA / Hibernate
                                          ▼
                      ┌────────────────────────────────────────┐
                      │       🗄️ Supabase PostgreSQL          │
                      │   Tablas: users, farms, animals,       │
                      │   geofences, positions, alerts         │
                      └────────────────────────────────────────┘
```

---

## 📂 Estructura del Proyecto

```text
Hato/
├── app/                                   # 📱 Frontend Android Studio (Kotlin 2.0)
│   ├── src/main/java/com/hato/
│   │   ├── MainActivity.kt               # Actividad principal, splash screen y navegación
│   │   ├── domain/                       # Lógica pura de negocio y modelos
│   │   │   ├── model/Models.kt           # Records (GeoPoint, Animal, Geofence, etc.)
│   │   │   └── engine/HatoEngine.kt      # Motor de confirmación de salida y tolerancia
│   │   ├── data/                         # Capa de datos y persistencia
│   │   │   ├── api/                      # Retrofit (HatoApiService, ApiClient)
│   │   │   ├── dto/Dtos.kt               # DTOs de autenticación y telemetría
│   │   │   ├── realtime/                 # Cliente WebSocket (LiveSocketManager)
│   │   │   └── repository/               # SessionManager (DataStore/SharedPreferences)
│   │   └── ui/                           # Interfaz de usuario con Jetpack Compose
│   │       ├── MainScreen.kt             # Scaffold con barra inferior dinámica
│   │       ├── theme/                    # Sistema de diseño "Verdant Precision"
│   │       └── screens/                  # Pantallas: Auth, Map, Zones, Animals, Alerts, Settings
│   └── build.gradle.kts
│
├── backend/                               # 🖥️ Backend Spring Boot (Java 21 LTS)
│   ├── src/main/java/com/hato/
│   │   ├── HatoApplication.java          # Entrada de la aplicación Spring Boot
│   │   ├── domain/                       # Modelos de dominio desacoplados
│   │   ├── application/service/          # Servicios (Auth, Position, Animal, Zone, Alert)
│   │   ├── infrastructure/               # JPA, WebSocket Handler, Seguridad
│   │   └── api/                          # Controladores REST (/api/v1/...)
│   ├── src/main/resources/application.yml# Configuración para Supabase y Render
│   └── pom.xml                           # Dependencias Maven
│
├── apk/                                   # 📦 APKs compilados y códigos QR
│   ├── hato-app.apk                      # APK más reciente de producción/debug
│   ├── hato-debug.apk                    # Copia de depuración
│   └── hato-download-qr.png              # Código QR para instalación en smartphone
│
├── render.yaml                            # 🚀 Configuración para despliegue en Render Cloud
├── build.gradle.kts                       # Script raíz de construcción
└── settings.gradle.kts                    # Definición del módulo :app
```

---

## 🚀 Guía de Inicio Rápido

### Prerrequisitos
- **Android:** Android Studio Jellyfish / Ladybug o IntelliJ IDEA 2024+, JDK 17 o 21.
- **Backend:** Java 21 LTS, Maven 3.9+.

### 1. Ejecutar la App Móvil en Android Studio / IntelliJ
```bash
# Compilar el APK directamente desde la terminal
./gradlew assembleDebug

# O abrir el proyecto en Android Studio y seleccionar la configuración 'app'
```

### 2. Ejecutar el Backend Localmente
```bash
cd backend
mvn spring-boot:run
```

---

## 📄 Licencia

Este proyecto se encuentra bajo la licencia **MIT**.
