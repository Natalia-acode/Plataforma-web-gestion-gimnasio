# 01 — Documento de Requisitos
## Sistema de Gestión de Gimnasio

---

## 1. Introducción

El presente documento define los requisitos del **Sistema de Gestión de Gimnasio**, una plataforma web que centraliza la administración de un gimnasio: membresías, pagos, control de acceso, aforo, clases grupales y atención al cliente. El diseño parte de tres problemas concretos detectados en plataformas comerciales actuales y en la operación diaria de un gimnasio, descritos en la sección 2.

---

## 2. Problemática

### 2.1 Cobros automáticos sin control del usuario
Plataformas como la de SmartFit realizan el cobro de la membresía de forma automática y el usuario **no tiene forma de cancelar o modificar su plan desde la página web**; debe acudir físicamente o llamar. Esto genera:
- Cargos no deseados e indefinidos en el tiempo (la renovación sigue activa sin consentimiento claro).
- Desconocimiento de cuándo y cuánto se cobrará.
- Desconfianza del usuario hacia la plataforma.

### 2.2 Saturación en horarios pico
No existe información pública sobre la ocupación del gimnasio. Los usuarios llegan en horarios de alta demanda sin saberlo, lo que produce:
- Esperas para usar máquinas y equipos.
- Aglomeraciones que degradan la experiencia y la seguridad.
- Desaprovechamiento de las franjas horarias de baja afluencia.

### 2.3 Colas y esperas sin gestión
Cuando el gimnasio está lleno o una clase tiene cupo completo, no hay un mecanismo ordenado de espera:
- Personas esperando de pie sin saber cuánto falta ni si alcanzarán a ingresar.
- Pérdida de clases por falta de una lista de espera.
- Recepción sin herramientas para organizar el ingreso.

---

## 3. Objetivos

### 3.1 Objetivo general
Desarrollar un sistema web de gestión de gimnasio que otorgue al usuario **control y transparencia sobre su membresía y pagos**, y que **reduzca la saturación y las esperas** mediante información de aforo en tiempo real, reservas y cola virtual.

### 3.2 Objetivos específicos
- OE-1: Permitir al cliente contratar, cambiar, **cancelar** su plan y activar/desactivar la renovación automática **desde la plataforma**, sin trámites presenciales.
- OE-2: Notificar al cliente con antelación antes de cada cobro automático y entregar historial completo de pagos y facturas.
- OE-3: Medir la ocupación del gimnasio en tiempo real a partir del check-in/check-out y publicarla (aforo actual y mapa de calor por día y hora).
- OE-4: Gestionar reservas de clases grupales con cupos y lista de espera.
- OE-5: Implementar una cola virtual de ingreso con posición, estimación de espera y aviso automático.
- OE-6: Dotar al administrador de reportes de ocupación e ingresos para la toma de decisiones.

---

## 4. Alcance

### 4.1 Incluido en esta versión
- Registro y autenticación de usuarios con roles (cliente, recepcionista, entrenador, administrador).
- Catálogo de planes, contratación, cambio y cancelación de suscripciones.
- Pagos en línea mediante pasarela, cobro automático de renovación con consentimiento explícito (opt-in) y recordatorios previos.
- Check-in por código QR y registro manual en recepción.
- Aforo en tiempo real y estadísticas históricas de afluencia (mapa de calor).
- Reserva de clases grupales con cupos y lista de espera.
- Cola virtual de ingreso.
- Reportes administrativos básicos.

### 4.2 Fuera de alcance (versiones futuras)
- Multi-sucursal / franquicias.
- Rutinas de entrenamiento personalizadas y seguimiento nutricional.
- Control de inventario y venta de productos (tienda).
- Integración con hardware de torniquetes o sensores de conteo (en esta versión el aforo se calcula con check-in/check-out).
- Aplicación móvil nativa (la plataforma es web responsive).

---

## 5. Actores

### 5.1 Actores primarios

| Actor | Descripción |
|---|---|
| **Visitante** | Persona sin cuenta. Consulta planes, precios y ocupación; puede registrarse. |
| **Cliente** | Usuario registrado con membresía. Gestiona su suscripción y pagos, hace check-in, reserva clases, usa la cola virtual y consulta la afluencia. |
| **Recepcionista** | Atención presencial: registra accesos manuales, vende membresías presenciales, registra pagos en efectivo y gestiona la cola virtual. |
| **Entrenador** | Dicta clases grupales: consulta su horario y pasa lista de asistencia. |
| **Administrador** | Gestiona usuarios, planes, clases, zonas y configuración de aforo; consulta reportes y estadísticas. |

### 5.2 Actores secundarios (sistemas externos)

| Actor | Descripción |
|---|---|
| **Pasarela de Pago** | Procesa cobros con tarjeta de forma segura (tokenización). El sistema nunca almacena datos completos de tarjeta. |
| **Servicio de Notificaciones** | Envía correos / notificaciones push: recordatorios de cobro, confirmaciones de reserva, avisos de turno, alertas. |
| **Planificador (Scheduler)** | Tarea programada del sistema que ejecuta los procesos automáticos (cobro de renovaciones, cierre de jornada, cálculo de estadísticas de aforo, expiración de turnos). |

---

## 6. Requisitos Funcionales

### Módulo 1 — Cuenta y seguridad

| ID | Requisito | Prioridad | CU |
|---|---|---|---|
| RF-01 | El sistema debe permitir el registro de visitantes como clientes con nombre, correo, teléfono y contraseña. | Alta | CU-01 |
| RF-02 | El sistema debe autenticar usuarios mediante correo y contraseña, gestionando sesiones seguras. | Alta | CU-02 |
| RF-03 | El sistema debe permitir recuperar la contraseña mediante enlace temporal enviado al correo. | Media | CU-03 |
| RF-04 | El cliente debe poder consultar y editar sus datos personales. | Media | CU-04 |

### Módulo 2 — Membresías y pagos

| ID | Requisito | Prioridad | CU |
|---|---|---|---|
| RF-05 | Cualquier visitante debe poder consultar el catálogo de planes (precio, duración, beneficios). | Alta | CU-05 |
| RF-06 | El cliente debe poder contratar un plan en línea, registrando un método de pago y autorizando **explícitamente** (opt-in) la renovación automática, mostrando monto y fecha del próximo cobro antes de confirmar. | Alta | CU-06 |
| RF-07 | El cliente debe poder cambiar de plan; el sistema calcula la diferencia de precio y ajusta la fecha de renovación. | Media | CU-07 |
| RF-08 | El cliente debe poder **cancelar su suscripción desde la plataforma**, sin trámites presenciales ni telefónicos. La cancelación es efectiva al final del período ya pagado y no genera cargos posteriores. | **Alta (crítica)** | CU-08 |
| RF-09 | El cliente debe poder activar o desactivar la renovación automática en cualquier momento. Al desactivarla no se realizarán más cobros. | **Alta (crítica)** | CU-09 |
| RF-10 | El cliente debe poder registrar, actualizar y eliminar métodos de pago tokenizados (tarjeta nunca almacenada en el sistema). | Alta | CU-10 |
| RF-11 | El cliente debe poder consultar su historial de pagos y descargar facturas. | Media | CU-11 |
| RF-12 | El sistema debe ejecutar el cobro automático de renovaciones únicamente para suscripciones con renovación autorizada, aplicando reintentos ante fallos y suspendiendo la membresía tras agotar reintentos. | **Alta (crítica)** | CU-12 |
| RF-13 | El sistema debe enviar un recordatorio al cliente **7 días y 1 día antes** de cada cobro automático, indicando monto y fecha, con enlace directo para cancelar o desactivar la renovación. | **Alta (crítica)** | CU-12 |

### Módulo 3 — Acceso y aforo

| ID | Requisito | Prioridad | CU |
|---|---|---|---|
| RF-14 | El sistema debe permitir el check-in del cliente mediante código QR, validando que su membresía esté vigente. | Alta | CU-13 |
| RF-15 | El recepcionista debe poder registrar el acceso manual de clientes sin QR (emergencia o QR no disponible). | Media | CU-14 |
| RF-16 | El sistema debe calcular y mostrar la ocupación en tiempo real del gimnasio (aforo actual / capacidad máxima) y por zona. | **Alta (crítica)** | CU-15 |
| RF-17 | El cliente debe poder consultar el mapa de calor de afluencia por día de la semana y hora (franjas de mayor y menor concurrencia). | **Alta (crítica)** | CU-16 |
| RF-18 | El administrador debe poder configurar la capacidad máxima, umbrales de alerta y parámetros de la cola virtual. | Alta | CU-17 |
| RF-19 | El sistema debe denegar el check-in y ofrecer la cola virtual cuando el aforo esté completo. | Alta | CU-13 |
| RF-20 | El sistema debe registrar check-out (manual o automático por cierre de jornada) para mantener la ocupación actualizada. | Alta | CU-14 |

### Módulo 4 — Clases y reservas

| ID | Requisito | Prioridad | CU |
|---|---|---|---|
| RF-21 | El cliente debe poder reservar cupos en clases grupales verificando cupo disponible y membresía vigente. | Alta | CU-18 |
| RF-22 | Cuando una clase esté llena, el sistema debe ofrecer lista de espera y promover automáticamente al primero de la lista cuando se libere un cupo. | Media | CU-18 |
| RF-23 | El cliente debe poder cancelar una reserva dentro del plazo permitido (ej. hasta 2 horas antes). | Media | CU-19 |
| RF-24 | El entrenador debe poder pasar lista de asistencia de sus clases. | Media | CU-20 |
| RF-25 | El administrador debe poder crear, modificar y desactivar clases y horarios, asignando entrenador, zona y cupo. | Media | CU-21 |

### Módulo 5 — Cola virtual

| ID | Requisito | Prioridad | CU |
|---|---|---|---|
| RF-26 | El cliente debe poder solicitar un turno en la cola virtual (ingreso al gimnasio o clase) cuando no haya disponibilidad. | **Alta (crítica)** | CU-22 |
| RF-27 | El sistema debe calcular y mostrar la posición en la cola y una estimación del tiempo de espera. | Alta | CU-23 |
| RF-28 | El cliente debe poder abandonar la cola (cancelar su turno) en cualquier momento. | Media | CU-24 |
| RF-29 | El sistema debe notificar al cliente cuando esté próximo su turno, otorgando una ventana de tiempo para presentarse; si no se presenta, el turno expira y avanza la cola. | Alta | CU-22 |
| RF-30 | El recepcionista debe poder visualizar y gestionar la cola (llamar siguiente turno, marcar atendido, expirar). | Alta | CU-25 |

### Módulo 6 — Administración y reportes

| ID | Requisito | Prioridad | CU |
|---|---|---|---|
| RF-31 | El administrador debe poder crear, editar, suspender y reactivar usuarios, y asignar roles. | Alta | CU-26 |
| RF-32 | El administrador debe poder gestionar el catálogo de planes (crear, modificar precio/duración, activar o desactivar). | Alta | CU-27 |
| RF-33 | El administrador debe poder gestionar zonas y salas con sus capacidades máximas. | Media | CU-28 |
| RF-34 | El administrador debe poder consultar reportes de ocupación (por hora, día y zona), ingresos (por plan, período) y membresías activas. | Alta | CU-29 |
| RF-35 | El recepcionista debe poder vender membresías presencialmente, registrando el pago (efectivo o pasarela). | Media | CU-30 |

---

## 7. Requisitos No Funcionales

| ID | Categoría | Requisito |
|---|---|---|
| RNF-01 | Seguridad | Contraseñas almacenadas con hash (bcrypt/argon2); comunicaciones por HTTPS; control de acceso por roles. |
| RNF-02 | Seguridad / Pagos | Los datos de tarjeta nunca se almacenan; se usan tokens de la pasarela (cumplimiento PCI-DSS delegado). |
| RNF-03 | Rendimiento | El aforo en tiempo real debe actualizarse con latencia menor a 30 segundos; las páginas deben responder en menos de 2 segundos en condiciones normales. |
| RNF-04 | Disponibilidad | El servicio debe estar disponible ≥ 99% del tiempo del horario del gimnasio; el cobro automático debe ser tolerante a fallos con reintentos. |
| RNF-05 | Usabilidad | Interfaz web responsive, operativa desde móvil y escritorio; los flujos de cancelación y baja de renovación deben ser accesibles en máximo 3 clics desde el perfil. |
| RNF-06 | Transparencia | Toda comunicación de cobro (confirmación, recordatorio, factura) debe indicar monto, concepto y fecha; nunca se cobrará sin suscripción con renovación activa. |
| RNF-07 | Auditabilidad | Los pagos, cancelaciones y cambios de plan deben quedar registrados con fecha/hora y usuario para trazabilidad. |
| RNF-08 | Escalabilidad | La arquitectura debe soportar al menos 5.000 usuarios y 500 check-ins concurrentes. |
| RNF-09 | Compatibilidad | Navegadores modernos (Chrome, Firefox, Edge, Safari últimas 2 versiones). |
| RNF-10 | Mantenibilidad | Código con arquitectura por capas (presentación, lógica de negocio, datos) y cobertura de pruebas en módulos de pagos y accesos. |

---

## 8. Reglas de Negocio

| ID | Regla |
|---|---|
| RN-01 | La renovación automática solo se ejecuta si el cliente la autorizó expresamente al contratar o la activó después. Por defecto viene **desactivada**. |
| RN-02 | La cancelación de la suscripción nunca tiene costo y es efectiva al finalizar el período ya pagado; el cliente conserva acceso hasta esa fecha. |
| RN-03 | Se envía recordatorio de cobro 7 días y 1 día antes de cada renovación, siempre con opción de cancelar. |
| RN-04 | Tras un cobro rechazado se reintenta: 1º al día siguiente, 2º a los 3 días y 3º a los 7 días; agotados los reintentos, la suscripción pasa a estado *suspendida* hasta regularizar el pago. |
| RN-05 | El check-in requiere membresía en estado *activa*. Membresías vencidas, suspendidas o canceladas (fuera de su período) no permiten el ingreso. |
| RN-06 | No se permite el ingreso si la ocupación alcanzó la capacidad máxima; en ese caso se ofrece la cola virtual. |
| RN-07 | Las reservas de clase pueden cancelarse hasta 2 horas antes del inicio; cancelaciones posteriores se registran como inasistencia. |
| RN-08 | El turno de la cola virtual expira si el cliente no se presenta dentro de la ventana otorgada (por defecto 10 minutos): pierde la posición. |
| RN-09 | La lista de espera de una clase promueve automáticamente al primer inscrito cuando alguien cancela, notificándolo de inmediato. |
| RN-10 | Un cliente no puede tener más de una suscripción activa simultánea. |
| RN-11 | La ocupación se calcula como check-ins activos (entrada sin salida registrada); el cierre de jornada registra la salida automática de los rezagados. |

---

## 9. Supuestos y Restricciones

- El gimnasio opera en una sola sede en esta versión.
- Los clientes disponen de un dispositivo con cámara o de un lector QR/torniquete conectado en la entrada para el check-in (el lector en esta versión es un dispositivo con navegador).
- La pasarela de pago ofrece API de tokenización y cobro recurrente.
- Los servicios de correo/notificaciones se consumen como servicio externo.
- El proyecto se documenta y desarrolla con enfoque de proceso incremental; esta documentación corresponde a la línea base de análisis.

---

## 10. Trazabilidad Problema → Solución

| Problema | Solución en el sistema | Casos de uso |
|---|---|---|
| Cobros automáticos sin control; imposibilidad de cancelar en línea | Cancelación en línea, renovación opt-in desactivable, recordatorios previos, historial y facturas | CU-06, CU-08, CU-09, CU-11, CU-12 |
| Saturación en horarios pico | Aforo en tiempo real, mapa de calor de afluencia, reservas con cupo | CU-15, CU-16, CU-18 |
| Colas y esperas sin gestión | Cola virtual con turno, estimación de espera y aviso automático | CU-22, CU-23, CU-24, CU-25 |

---

## 11. Tecnologías y arquitectura

| Capa | Tecnología | Notas |
|---|---|---|
| Frontend | **React** (SPA responsive) | Consumo de la API REST (JSON), rutas por rol, sesión con JWT |
| Backend | **Spring Boot** (Java) | Spring MVC (REST), Spring Security (JWT + BCrypt), Spring Data JPA |
| Base de datos | **Microsoft SQL Server** (2019+) | Script DDL: `BD/ddl_gimnasio.sql` (13 tablas, índices y vistas) |
| Procesos automáticos | Spring `@Scheduled` | Recordatorios y cobros de renovación (CU-12), expiración de turnos (RN-08), snapshots de aforo (CU-16) |
| Integraciones externas | Pasarela de pago y servicio de notificaciones | Vía API HTTPS (tokenización) y SMTP/push |

La arquitectura sigue un estilo **en capas** (presentación, servicios, datos), coherente con RNF-10. El diagrama de arquitectura está en [`arquitectura/arquitectura_sistema.puml`](arquitectura/arquitectura_sistema.puml) y el diseño detallado del backend (controladores, servicios, repositorios, entidades JPA) en [`clases/diagrama_clases_diseno.puml`](clases/diagrama_clases_diseno.puml).
