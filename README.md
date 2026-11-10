# 🐮 Hato - Gestión y Monitoreo Ganadero Inteligente en Tiempo Real

Plataforma integral para el rastreo y telemetría de ganado en tiempo real mediante collares GPS, geocercas inteligentes (polígonos y círculos), detección automática de fugas e inactividad, comunicación WebSocket bidireccional y autenticación segura con JWT y Google Sign-In.

---

## 📱 Descarga Directa del APK Android

Puedes descargar el archivo APK compilado y listo para instalar en tu teléfono o tableta Android directamente desde este repositorio:

* 📥 **[Descargar Hato APK (hato-app.apk)](./apk/hato-app.apk)** *(12.95 MB)*
* 📥 **[Descargar Hato Debug APK (hato-debug.apk)](./apk/hato-debug.apk)**

> **Requisitos:** Android 8.0 (API 26) o superior. Habilitar permisos de Ubicación y Bluetooth para vincular collares GPS cercanos.

---

## 📂 Organización y Distribución del Proyecto

El repositorio ha sido reorganizado de manera modular:

```text
Hato/
├── app/                                   # 📱 FRONTEND ANDROID STUDIO (100% KOTLIN)
│   ├── src/main/java/com/hato/
│   │   ├── MainActivity.kt               # Punto de entrada de la app
│   │   ├── domain/
│   │   │   ├── model/Models.kt           # Modelos inmutables (GeoPoint, Animal, Farm, Geofence, etc.)
│   │   │   └── engine/HatoEngine.kt      # Algoritmo de confirmación de salida y tolerancia GPS
│   │   ├── data/
│   │   │   ├── api/                      # Clientes Retrofit (HatoApiService, ApiClient, AuthInterceptor)
│   │   │   ├── dto/Dtos.kt               # Contratos DTO de red (Auth, Position, Animal, Zone, Alert)
│   │   │   ├── realtime/                 # Cliente WebSocket en vivo (LiveSocketManager)
│   │   │   └── repository/               # Manejo de sesiones y tokens (SessionManager)
│   │   └── ui/
│   │       ├── MainScreen.kt             # Scaffold principal con navegación por pestañas
│   │       ├── theme/                    # Colores y temas claro/oscuro dinámicos
│   │       ├── screens/
│   │       │   ├── auth/LoginScreen.kt   # Login con Correo/Clave, JWT y Google Sign-In (Credential Manager)
│   │       │   ├── map/                  # Mapa satelital OSMDroid con dibujo de potreros y rastreo en vivo
│   │       │   ├── animals/              # Gestión de cabezas de ganado y aretes
│   │       │   ├── zones/                # Gestión de potreros y geocercas
│   │       │   ├── alerts/               # Historial de alertas de fuga e inactividad
│   │       │   └── settings/             # Escaneo y vinculación de collares Bluetooth GPS
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
│
├── backend/                               # 🖥️ BACKEND SPRING BOOT (JAVA 21)
│   ├── src/main/java/com/hato/
│   │   ├── HatoApplication.java          # Aplicación principal Spring Boot
│   │   ├── domain/                       # Núcleo de dominio desacoplado
│   │   │   ├── model/                    # Records inmutables (GeoPoint, Position, AlertEvent sellado, User)
│   │   │   ├── geofence/                 # Geocercas (PolygonGeofence, CircleGeofence, Ray-Casting, Haversine)
│   │   │   └── rule/                     # Motor de reglas (OutOfZoneRule, BackInZoneRule, LowBatteryRule, etc.)
│   │   ├── application/
│   │   │   ├── service/                  # Servicios de aplicación (AuthService, PositionService, ZoneService, etc.)
│   │   │   └── notification/             # Despachador de notificaciones con Hilos Virtuales
│   │   ├── infrastructure/
│   │   │   ├── persistence/              # Entidades JPA y Repositorios para Supabase (PostgreSQL)
│   │   │   ├── realtime/                 # Servidor WebSocket (/ws/live?farmId=) para push en tiempo real
│   │   │   ├── messaging/                # Notificadores FCM y canal de respaldo Telegram
│   │   │   ├── scheduling/               # Tareas programadas (SignalWatchdog, BatteryMonitor, IdleDetector)
│   │   │   └── gps/                      # Receptores TCP concurrentes con hilos virtuales y decodificadores GT06/H02
│   │   ├── api/
│   │   │   ├── controller/               # Controladores REST (Auth, Farm, Animal, Zone, Alert, Ingest, Report)
│   │   │   ├── dto/                      # Records DTO de transferencia
│   │   │   ├── exception/                # Manejador global de excepciones (RFC 7807)
│   │   │   └── security/                 # Filtro JWT, Verificador criptográfico de Google y BCrypt
│   │   └── config/                       # Configuración de WebSocket, CORS y propiedades
│   ├── src/main/resources/
│   │   ├── application.yml               # Configuración lista para Render y Supabase
│   │   └── db/migration/V1__init_schema.sql # Esquema SQL compatible con PostgreSQL / PostGIS
│   ├── Dockerfile                        # Multi-stage Dockerfile con Java 21 LTS
│   ├── pom.xml                           # Descriptor Maven
│   └── .env.example                      # Plantilla de variables de entorno
│
├── apk/                                   # 📦 Directorio con los APKs descargables
│   ├── hato-app.apk
│   └── hato-debug.apk
│
├── render.yaml                            # 🚀 Blueprint para despliegue automatizado en Render
├── build.gradle.kts                       # Configuración Gradle raíz para Android Studio
└── settings.gradle.kts                    # Configuración de proyectos incluidos (:app)
```

---

## 🛠️ Cómo abrir y compilar en Android Studio

1. Abre **Android Studio** (Hedgehog, Iguana, Koala o superior).
2. Selecciona **Open** y navega hasta la carpeta del proyecto `g:\DC\Programas\Hato`.
3. Android Studio reconocerá automáticamente el proyecto a través de `settings.gradle.kts` (cargando el módulo `:app`).
4. Espera a que termine la sincronización de Gradle.
5. Selecciona un dispositivo físico o emulador con Google Play Services y presiona **Run ('app')** (Shift + F10).

---

## 🗄️ Configuración de Base de Datos en Supabase (PostgreSQL)

1. Ingresa a tu cuenta en [Supabase](https://supabase.com) y crea un nuevo proyecto (ej. `hato-prod`).
2. Dirígete a **SQL Editor** en el panel lateral de Supabase.
3. Copia y pega el contenido del archivo [`backend/src/main/resources/db/migration/V1__init_schema.sql`](./backend/src/main/resources/db/migration/V1__init_schema.sql) y ejecuta el script (**Run**).
4. El script creará automáticamente las tablas optimizadas con índices:
   * `users`, `farms`, `memberships`, `trackers`, `zones`, `animals`, `positions`, `alerts`, `device_tokens`.
5. Ve a **Project Settings > Database > Connection string** y copia la URL JDBC (modo Connection Pooling o Directo).

---

## 🚀 Despliegue del Backend en Render

El backend está listo para desplegarse mediante Docker y `render.yaml`.

### Opción A: Despliegue con Render Blueprint (Recomendado)
1. Conecta tu repositorio de GitHub `https://github.com/DC268/Hato` a Render.
2. Selecciona **New + > Blueprint** y selecciona el repositorio.
3. Render detectará automáticamente el archivo `render.yaml` y configurará el servicio web Docker.

### Opción B: Despliegue Manual como Web Service en Render
1. En Render, crea un **New Web Service**.
2. Conecta el repositorio `https://github.com/DC268/Hato`.
3. Configura:
   * **Runtime:** Docker
   * **Dockerfile Path:** `backend/Dockerfile`
   * **Docker Context:** `backend`
   * **Instance Type:** Free o Starter
4. Configura las siguientes **Environment Variables**:
   * `PORT`: `8080`
   * `HATO_DB_URL`: `jdbc:postgresql://db.<tu-ref>.supabase.co:5432/postgres?sslmode=require`
   * `HATO_DB_USER`: `postgres`
   * `HATO_DB_PASSWORD`: `<tu-contrasena-supabase>`
   * `HATO_JWT_SECRET`: `4c9b3d2f8e1a7c5b6a9e8d7c6b5a4f3e2d1c0b9a8f7e6d5c4b3a2f1e0d9c8b7a` (o genera una aleatoria)
   * `HATO_INGEST_KEY`: `hato-secret-ingest-key-2026`
   * `GOOGLE_CLIENT_ID`: Tu Web Client ID de Google Cloud Console.
5. Guarda y presiona **Deploy**.

---

## 🔒 Autenticación: JWT y Google Sign-In

### 1. Inicio de Sesión Tradicional (Email / Password)
* Contraseñas cifradas con algoritmo **BCrypt**.
* Emisión de tokens **JWT (JJWT 0.12.x)** firmados con HMAC-SHA256 y expiración configurable.
* Endpoints:
  * `POST /api/auth/register`
  * `POST /api/auth/login`

### 2. Inicio de Sesión con Google (Google Sign-In)
* **Android:** Implementado con la moderna API **Credential Manager** (`androidx.credentials` y `GoogleIdTokenCredential`).
* **Backend:** Implementado en `GoogleTokenVerifierService` con `com.google.api-client:google-api-client`, validando criptográficamente la firma del ID Token contra los certificados JWKS de Google (`/v3/certs`) sin peticiones HTTP redundantes.
* Endpoint:
  * `POST /api/auth/google` (recibe `{ "idToken": "..." }` y retorna `{ "token": "...", "user": {...} }`).

---

## 📍 Corrección: Visualización de Dispositivos Vinculados en Tiempo Real

Se resolvió la limitación anterior en la que los collares Bluetooth y teléfonos vinculados no aparecían de inmediato en el mapa:
1. **Asignación Inmediata de Coordenadas:** Al vincular un collar GPS por Bluetooth en `MapViewModel.connectToDevice()`, se le asigna de inmediato la ubicación actual en el mapa sin requerir que existan geocercas previas.
2. **Identificador Unificado:** Se unificó el rastreo para que `processPhoneLocation()` actualice tanto el collar del teléfono (`a-phone`) como cualquier MAC vinculado (`a-<mac>`).
3. **Marcador Visual Diferenciado:** En `MapScreen.kt`, los dispositivos vinculados se dibujan con un marcador con halo fluorescente (`#00E676`), fondo azul de telemetría y distintivo `📡 GPS VINCULADO`.
4. **WebSocket en Vivo:** Se habilitó la conexión automática al endpoint WebSocket `/ws/live?farmId=...` para recibir coordenadas y alertas en tiempo real desde el servidor.

---

## 📡 Endpoints de la API REST

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/auth/register` | Registro de usuario ganadero |
| `POST` | `/api/auth/login` | Inicio de sesión con correo y contraseña |
| `POST` | `/api/auth/google` | Inicio de sesión y verificación de Google ID Token |
| `GET` | `/api/farms` | Listar fincas del usuario |
| `GET` | `/api/farms/{id}/animals` | Listar animales de una finca con su estado |
| `POST` | `/api/animals` | Crear nuevo animal |
| `PUT` | `/api/animals/{id}` | Actualizar datos del animal |
| `GET` | `/api/animals/{id}/track` | Obtener historial de recorrido GPS por fechas |
| `GET` | `/api/zones` | Listar potreros y geocercas activas |
| `POST` | `/api/zones` | Crear potrero o geocerca poligonal/circular |
| `DELETE` | `/api/zones/{id}` | Eliminar geocerca |
| `GET` | `/api/alerts` | Listar alertas (fugas, inactividad, batería) |
| `POST` | `/api/alerts/{id}/ack` | Marcar alerta como atendida |
| `POST` | `/api/ingest/positions` | Ingesta de posiciones GPS (Traccar / LoRa / Móvil) con cabecera `X-Ingest-Key` |
| `POST` | `/api/devices/token` | Registrar token FCM para notificaciones push |
| `GET` | `/api/reports/exits` | Reporte consolidado de salidas de zona |
| `WS` | `/ws/live?farmId={id}` | Conexión WebSocket para streaming en tiempo real |
