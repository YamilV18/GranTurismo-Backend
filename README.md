# 🚀 Guía de Integración - Módulo de Localización para Paquetes

## 📋 Pasos de Implementación

### 1. **Actualizar Base de Datos**

```sql
-- Ejecutar script SQL para preferences (del artefact anterior)
-- Ejecutar script SQL para paquetes:

ALTER TABLE paquetes 
ADD COLUMN moneda_original VARCHAR(3) DEFAULT 'USD' AFTER precio_total,
ADD COLUMN idioma_original VARCHAR(5) DEFAULT 'es' AFTER moneda_original;

UPDATE paquetes 
SET moneda_original = 'USD', idioma_original = 'es' 
WHERE moneda_original IS NULL OR idioma_original IS NULL;
```

### 2. **Actualizar Dependencias en POM.xml**

Tu `pom.xml` ya tiene todas las dependencias necesarias. Solo agregar en `application.properties`:

```properties
# APIs de localización
deepl.api.key=${DEEPL_API_KEY:}
exchangerate.api.key=${EXCHANGERATE_API_KEY:}
```

### 3. **Reemplazar Archivos Existentes**

#### 📁 **Modelo**
- Reemplazar `Paquete.java` con la versión actualizada (campos `monedaOriginal` e `idiomaOriginal`)

#### 📁 **DTOs** 
- Reemplazar `PaqueteDTO.java` con la versión actualizada (campos de localización + `PaqueteListDTO`)

#### 📁 **Mappers**
- Reemplazar `PaqueteMapper.java` con la versión actualizada (mapeos de localización)

#### 📁 **Servicios**
- Actualizar `PaqueteServiceImp.java` con los nuevos métodos de localización
- Actualizar `IPaqueteService.java` con las nuevas interfaces

#### 📁 **Controladores**
- Reemplazar `PaqueteController.java` con la versión actualizada (endpoints con localización)

### 4. **Agregar Nuevos Componentes**

#### 📁 **Crear en `org.example.granturismo.modelo`**
```java
// Preference.java (del artefact anterior)
```

#### 📁 **Crear en `org.example.granturismo.dtos`**
```java
// PreferenceResponseDto.java
// PreferenceUpdateDto.java
// TranslatedContentDto.java
// LocalizedResponseDto.java
```

#### 📁 **Crear en `org.example.granturismo.servicio`**
```java
// PreferenceService.java
// TranslationService.java
// CurrencyConversionService.java
// LocalizationService.java
```

#### 📁 **Crear en `org.example.granturismo.control`**
```java
// PreferenceController.java
```

#### 📁 **Crear en `org.example.granturismo.repositorio`**
```java
// PreferenceRepository.java
```

#### 📁 **Crear en `org.example.granturismo.mappers`**
```java
// PreferenceMapper.java
```

#### 📁 **Actualizar en `org.example.granturismo.excepciones`**
```java
// Agregar nuevas excepciones y ErrorResponse
// Actualizar GlobalExceptionHandler
```

### 5. **Configurar APIs Externas**

#### **DeepL API (Gratuita)**
1. Ir a [DeepL API](https://www.deepl.com/pro-api)
2. Crear cuenta gratuita (500,000 caracteres/mes)
3. Obtener API Key
4. Configurar: `DEEPL_API_KEY=tu_clave_aqui`

#### **ExchangeRate-API (Gratuita)**
1. Ir a [ExchangeRate-API](https://exchangerate-api.com/)
2. Crear cuenta gratuita (1,500 requests/mes)
3. Obtener API Key
4. Configurar: `EXCHANGERATE_API_KEY=tu_clave_aqui`

### 6. **Actualizar Método JWT**

En `PaqueteController.java`, actualizar el método `getUserIdFromToken()` según tu implementación de JWT:

```java
private Long getUserIdFromToken() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    
    if (authentication == null || !authentication.isAuthenticated()) {
        throw new RuntimeException("Usuario no autenticado");
    }
    
    // OPCIÓN 1: Si guardas el ID como principal
    try {
        return Long.parseLong(authentication.getName());
    } catch (NumberFormatException e) {
        // OPCIÓN 2: Si usas un UserDetails personalizado
        if (authentication.getPrincipal() instanceof TuCustomUserDetails) {
            return ((TuCustomUserDetails) authentication.getPrincipal()).getUserId();
        }
        
        // OPCIÓN 3: Si guardas email, buscar usuario
        // return userService.findByEmail(authentication.getName()).getIdUsuario();
        
        throw new RuntimeException("No se pudo extraer el ID del usuario del token");
    }
}
```

## 🧪 Testing

### **1. Crear Preferencias de Usuario**
```http
POST /api/preferences/me
Authorization: Bearer {tu_jwt_token}
Content-Type: application/json

{
  "preferredCurrencyCode": "EUR",
  "preferredLanguageCode": "en"
}
```

### **2. Consultar Paquete Localizado**
```http
GET /api/paquetes/1
Authorization: Bearer {tu_jwt_token}
```

**Respuesta esperada:**
```json
{
  "idPaquete": 1,
  "titulo": "Beach Paradise Package",
  "descripcion": "Enjoy beautiful beaches and crystal clear waters...",
  "precioTotal": 425.50,
  "monedaOriginal": "USD",
  "idiomaOriginal": "es",
  "monedaAplicada": "EUR",
  "idiomaAplicado": "en",
  "fueTraducido": true,
  "fueConvertido": true,
  "tasaCambio": 0.85
}
```

### **3. Listar Paquetes Localizados**
```http
GET /api/paquetes?page=0&size=10
Authorization: Bearer {tu_jwt_token}
```

## 🔄 Flujo de Funcionamiento

### **Para Usuarios Normales:**
1. **Todas las consultas** se localizan automáticamente
2. **Endpoints principales** (`GET /paquetes`, `GET /paquetes/{id}`) retornan contenido en idioma y moneda preferidos
3. **Metadatos incluidos** muestran qué se tradujo/convirtió

### **Para Administradores:**
1. **Endpoints administrativos** (`/admin/*`) retornan contenido original
2. **Endpoint especial** (`/paquetes/original/{id}`) para ver versión sin localizar
3. **Estadísticas de localización** disponibles en `/paquetes/{id}/localizacion`

## 🎯 Compatibilidad

### **Endpoints Mantenidos:**
- `GET /paquetes/admin/all` - Lista sin localización (solo admin)
- `GET /paquetes/admin/{id}` - Paquete sin localización (solo admin)
- `POST /paquetes` - Crear paquete (sin cambios)
- `PUT /paquetes/{id}` - Actualizar paquete (sin cambios)
- `DELETE /paquetes/{id}` - Eliminar paquete (sin cambios)

### **Endpoints Mejorados:**
- `GET /paquetes` - Ahora con localización automática
- `GET /paquetes/{id}` - Ahora con localización automática
- `GET /paquetes/pageable` - Ahora con localización automática

### **Endpoints Nuevos:**
- `GET /paquetes/estado/{estado}` - Buscar por estado localizado
- `GET /paquetes/disponibles` - Solo disponibles localizado
- `GET /paquetes/{id}/localizacion` - Estadísticas de localización
- `GET /paquetes/original/{id}` - Versión original (admin)

## ⚠️ Consideraciones Importantes

1. **Fallback Automático**: Si las APIs externas fallan, retorna contenido original
2. **Performance**: El sistema cachea traducciones y tasas de cambio (configurable)
3. **Límites de API**: 
   - DeepL Free: 500,000 caracteres/mes
   - ExchangeRate Free: 1,500 requests/mes
4. **Códigos Soportados**:
   - Monedas: USD, EUR, GBP, JPY, PEN, MXN, ARS, CLP, etc.
   - Idiomas: es, en, fr, de, it, pt, ru, ja, etc.

## 🚨 Problemas Comunes y Soluciones

### **Error: "Usuario no autenticado"**
**Causa**: El método `getUserIdFromToken()` no está configurado correctamente
**Solución**: Actualizar el método según tu implementación de JWT específica

### **Error: "API Key no configurada"**
**Causa**: Variables de entorno no establecidas
**Solución**: 
```bash
export DEEPL_API_KEY=tu_clave_deepl
export EXCHANGERATE_API_KEY=tu_clave_exchangerate
```

### **Error: "Paquete no encontrado"**
**Causa**: ID de paquete no existe en la base de datos
**Solución**: Verificar que el paquete existe y que el usuario tiene permisos

### **Traducción no funciona**
**Causa**: API de DeepL no disponible o límite excedido
**Solución**: El sistema automáticamente retorna texto original. Verificar logs para detalles.

### **Conversión de moneda no funciona**
**Causa**: API de ExchangeRate no disponible o límite excedido
**Solución**: El sistema automáticamente retorna precio original. Verificar logs para detalles.

## 📈 Monitoreo

### **Logs Importantes**
```bash
# Seguir logs de localización
tail -f logs/application.log | grep "LocalizationService\|TranslationService\|CurrencyConversionService"

# Errores de APIs externas
tail -f logs/application.log | grep "ERROR.*External"

# Actividad de preferencias
tail -f logs/application.log | grep "PreferenceService"
```

### **Métricas Sugeridas**
- Tasa de éxito de traducciones
- Tasa de éxito de conversiones de moneda
- Tiempo de respuesta promedio
- Uso de cuotas de APIs externas

## 🔧 Configuración Avanzada

### **Habilitar Cache (Opcional)**
```properties
# En application.properties
spring.cache.cache-names=translations,currency-rates
spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=1h
```

### **Configurar Timeouts**
```properties
# Timeouts para APIs externas
deepl.api.timeout.connect=10000
deepl.api.timeout.read=30000
exchangerate.api.timeout.connect=5000
exchangerate.api.timeout.read=15000
```

### **Idiomas y Monedas Personalizados**
```properties
# Agregar más idiomas soportados
localization.supported.languages=es,en,fr,de,it,pt,ru,ja,ko,zh,ar
localization.supported.currencies=USD,EUR,GBP,JPY,PEN,MXN,ARS,CLP,COP,UYU
```

## 🎯 Próximos Pasos

1. **Probar la Integración**:
   - Crear usuario de prueba
   - Configurar preferencias
   - Consultar paquetes localizados

2. **Extender a Otras Entidades**:
   - Aplicar el mismo patrón a `Destino`, `Proveedor`, etc.
   - Agregar campos `moneda_original` e `idioma_original`
   - Usar `LocalizationService` en sus controladores

3. **Optimizaciones**:
   - Implementar cache para reducir llamadas a APIs
   - Agregar índices de base de datos para mejor performance
   - Configurar pool de conexiones para APIs externas

4. **Mejoras Futuras**:
   - Detección automática de idioma de contenido
   - Soporte para contenido multiidioma (JSON)
   - Traducción de imágenes con texto
   - Personalización de traducciones por usuario

## 📚 Documentación API

Una vez implementado, la documentación completa estará disponible en:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api-docs`

## 🤝 Soporte

Para cualquier problema durante la implementación:

1. **Verificar logs** para errores específicos
2. **Consultar esta guía** para soluciones comunes
3. **Revisar configuración** de APIs externas
4. **Validar estructura** de base de datos

---

**¡El módulo de localización está listo para transformar tu sistema de turismo🌍**
