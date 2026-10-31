# Casos de Uso — Descripción Detallada
## Sistema de Gestión de Gimnasio

Este documento describe los 30 casos de uso del sistema. Los diagramas correspondientes están en [`diagramas/`](diagramas/).

**Convenciones:** FN = Flujo Normal · FA = Flujo Alternativo · EX = Excepción · RN = Regla de Negocio · RF = Requisito Funcional (ver [01_requisitos.md](../01_requisitos.md)).

## Índice de casos de uso

| Código | Nombre | Actor principal | Módulo | Prioridad |
|---|---|---|---|---|
| CU-01 | Registrarse en la plataforma | Visitante | Cuenta | Alta |
| CU-02 | Iniciar sesión | Todos | Cuenta | Alta |
| CU-03 | Recuperar contraseña | Visitante / Usuarios | Cuenta | Media |
| CU-04 | Gestionar perfil | Cliente / Staff | Cuenta | Media |
| CU-05 | Consultar planes y precios | Visitante / Cliente | Membresías | Alta |
| CU-06 | Contratar suscripción | Cliente | Membresías | Alta |
| CU-07 | Cambiar de plan | Cliente | Membresías | Media |
| CU-08 | **Cancelar suscripción** | Cliente | Membresías | **Crítica** |
| CU-09 | **Gestionar renovación automática** | Cliente | Membresías | **Crítica** |
| CU-10 | Gestionar métodos de pago | Cliente | Membresías | Alta |
| CU-11 | Consultar historial de pagos y facturas | Cliente | Membresías | Media |
| CU-12 | **Procesar renovación automática** | Planificador | Membresías | **Crítica** |
| CU-13 | Realizar check-in con QR | Cliente | Acceso y Aforo | Alta |
| CU-14 | Registrar acceso manual y check-out | Recepcionista | Acceso y Aforo | Media |
| CU-15 | **Consultar ocupación en tiempo real** | Cliente / Recepción | Acceso y Aforo | **Crítica** |
| CU-16 | **Consultar horarios de mayor y menor afluencia** | Cliente | Acceso y Aforo | **Crítica** |
| CU-17 | Configurar capacidad y umbrales de aforo | Administrador | Acceso y Aforo | Alta |
| CU-18 | Reservar clase grupal (incl. lista de espera) | Cliente | Clases | Alta |
| CU-19 | Cancelar reserva de clase | Cliente | Clases | Media |
| CU-20 | Pasar lista de asistencia | Entrenador | Clases | Media |
| CU-21 | Gestionar clases y horarios | Administrador | Clases | Media |
| CU-22 | **Solicitar turno en cola virtual** | Cliente | Cola Virtual | **Crítica** |
| CU-23 | Consultar posición y tiempo de espera | Cliente | Cola Virtual | Alta |
| CU-24 | Cancelar turno en cola | Cliente | Cola Virtual | Media |
| CU-25 | Gestionar cola y llamar siguiente turno | Recepcionista | Cola Virtual | Alta |
| CU-26 | Gestionar usuarios y roles | Administrador | Administración | Alta |
| CU-27 | Gestionar planes y precios | Administrador | Administración | Alta |
| CU-28 | Gestionar zonas y salas | Administrador | Administración | Media |
| CU-29 | Consultar reportes y estadísticas | Administrador | Administración | Alta |
| CU-30 | Vender membresía presencial | Recepcionista | Administración | Media |

---

## Módulo 1 — Cuenta y Seguridad

### CU-01 — Registrarse en la plataforma

| Campo | Detalle |
|---|---|
| **Actor(es)** | Visitante · Administrador (aprueba solicitudes) · Servicio de Notificaciones (secundario) |
| **Objetivo** | Crear una cuenta propia: de cliente (activa al instante) o de personal (sujeta a aprobación). |
| **Precondiciones** | El visitante no tiene cuenta con el correo ingresado. |
| **Postcondiciones** | Cuenta de cliente creada con rol *Cliente*, estado *activo* y sesión iniciada. Cuenta de personal creada con estado *pendiente* y aviso enviado a los administradores; el personal nuevo solo queda *activo* al registrarse mientras no exista un administrador (arranque del sistema). |
| **RF** | RF-01, RF-31 · **RN**: RN-12 |

**Flujo Normal**
1. El visitante accede a *Crear cuenta*.
2. Ingresa nombre, apellidos, correo, teléfono, fecha de nacimiento, contraseña y el tipo de cuenta (cliente por defecto; recepcionista, entrenador o administrador).
3. Acepta los términos y condiciones y la política de datos.
4. El sistema valida formato de datos, unicidad del correo y que el rol solicitado exista.
5. El sistema crea la cuenta (contraseña almacenada con hash):
   - Rol *Cliente*: estado *activo*, envía correo de bienvenida y devuelve token de sesión.
   - Rol de personal con un administrador ya activo: estado *pendiente*, notifica a los administradores y **no** entrega token.
   - Rol de personal sin ningún administrador activo (arranque): estado *activo* y entrega token, porque no hay quien revise la solicitud.
6. El sistema confirma el registro: *Cliente* ofrece consultar planes (CU-05) para contratar (CU-06); el personal informa que queda a la espera de aprobación (CU-26).

**Flujos Alternativos**
- **FA-1** El visitante no acepta los términos: no puede continuar; se le informa que la aceptación es obligatoria.
- **FA-2** El visitante solicita un rol de personal: la cuenta nace *pendiente*; un administrador la aprueba (pasa a *activa* y puede iniciar sesión, CU-02) o la rechaza (pasa a *rechazada*) en la bandeja de CU-26.
- **FA-3** El primer administrador del sistema se crea por este caso de uso en modo arranque (no existe cuenta de administrador activa); no se siembra ninguna cuenta desde la base de datos.

**Excepciones**
- **EX-1** El correo ya está registrado: se informa y se ofrecen las opciones *Iniciar sesión* (CU-02) o *Recuperar contraseña* (CU-03).
- **EX-2** Datos con formato inválido: se resaltan los campos con error y no se crea la cuenta.
- **EX-3** Falla el envío del correo de bienvenida: la cuenta se crea igualmente; el envío se reintenta de forma asíncrona.
- **EX-4** El rol solicitado no existe: se rechaza la solicitud con *400 Datos inválidos* y no se crea la cuenta.
- **EX-5** Una cuenta *pendiente* intenta iniciar sesión: se le impide y se le indica que espere la aprobación (CU-02).

---

### CU-02 — Iniciar sesión

| Campo | Detalle |
|---|---|
| **Actor(es)** | Visitante (y todos los usuarios registrados) |
| **Objetivo** | Autenticarse para acceder a las funcionalidades según su rol. |
| **Precondiciones** | Cuenta registrada y en estado *activo*. |
| **Postcondiciones** | Sesión iniciada; el sistema redirige al panel correspondiente al rol. |
| **RF** | RF-02 · **RN**: — |

**Flujo Normal**
1. El usuario ingresa correo y contraseña.
2. El sistema valida las credenciales.
3. El sistema crea la sesión y redirige según rol: cliente → inicio; recepcionista → panel de recepción; entrenador → mis clases; administrador → panel administrativo.

**Flujos Alternativos**
- **FA-1** El usuario marca *Recordarme*: la sesión se mantiene por más tiempo en el dispositivo.

**Excepciones**
- **EX-1** Credenciales incorrectas: mensaje genérico de error. Tras 5 intentos fallidos, la cuenta se bloquea temporalmente por 15 minutos.
- **EX-2** Cuenta suspendida: se impide el acceso y se indica contactar a recepción.
- **EX-3** Cuenta de personal *pendiente* (CU-01): se impide el acceso y se indica que un administrador debe aprobar la solicitud (CU-26).
- **EX-4** Cuenta de personal *rechazada*: se impide el acceso y se indica contactar a la administración.

---

### CU-03 — Recuperar contraseña

| Campo | Detalle |
|---|---|
| **Actor(es)** | Visitante / Usuarios registrados · Servicio de Notificaciones |
| **Objetivo** | Restablecer la contraseña olvidada sin intervención del personal. |
| **Precondiciones** | — |
| **Postcondiciones** | Contraseña actualizada; enlace temporal invalidado. |
| **RF** | RF-03 · **RN**: — |

**Flujo Normal**
1. El usuario ingresa su correo en *Olvidé mi contraseña*.
2. El sistema envía un enlace temporal de restablecimiento (válido 15 minutos).
3. El usuario define una nueva contraseña desde el enlace.
4. El sistema valida la política de contraseña, actualiza el hash y confirma el cambio.

**Excepciones**
- **EX-1** El correo no existe: se muestra un mensaje genérico de confirmación (no se revela si la cuenta existe, por seguridad).
- **EX-2** Enlace expirado o ya usado: se informa y se ofrece solicitar uno nuevo.

---

### CU-04 — Gestionar perfil

| Campo | Detalle |
|---|---|
| **Actor(es)** | Cliente, Entrenador, Recepcionista, Administrador |
| **Objetivo** | Consultar y mantener actualizados los datos personales. |
| **Precondiciones** | Sesión iniciada. |
| **Postcondiciones** | Datos actualizados y auditados. |
| **RF** | RF-04 · **RN**: — |

**Flujo Normal**
1. El usuario accede a *Mi perfil*.
2. Consulta sus datos actuales.
3. Edita los campos permitidos (teléfono, dirección, foto; el correo requiere verificación).
4. El sistema valida y guarda los cambios.
5. El sistema confirma la actualización.

**Flujos Alternativos**
- **FA-1** Cambio de contraseña: se exige ingresar la contraseña actual; si es correcta, se aplica la nueva.

**Excepciones**
- **EX-1** El nuevo correo ya está en uso por otra cuenta: se rechaza el cambio.

---

## Módulo 2 — Membresías y Pagos

### CU-05 — Consultar planes y precios

| Campo | Detalle |
|---|---|
| **Actor(es)** | Visitante, Cliente |
| **Objetivo** | Conocer la oferta de planes con total transparencia de precios y condiciones. |
| **Precondiciones** | — |
| **Postcondiciones** | — |
| **RF** | RF-05 · **RN**: — |

**Flujo Normal**
1. El usuario accede al catálogo de planes (sin necesidad de cuenta).
2. El sistema lista los planes activos con nombre, descripción, precio, duración y beneficios.
3. El usuario puede comparar planes, y contratar (CU-06) o registrarse (CU-01).

**Excepciones**
- **EX-1** No hay planes activos: se muestra un mensaje informativo.

---

### CU-06 — Contratar suscripción

| Campo | Detalle |
|---|---|
| **Actor(es)** | Cliente · Pasarela de Pago · Servicio de Notificaciones |
| **Objetivo** | Adquirir un plan con consentimiento explícito del cobro y sus condiciones. |
| **Precondiciones** | Sesión iniciada; el cliente no tiene otra suscripción activa (RN-10). |
| **Postcondiciones** | Suscripción *activa* creada; pago registrado; factura emitida; renovación automática según la decisión (por defecto desactivada). |
| **RF** | RF-06 · **RN**: RN-01, RN-10 |

**Flujo Normal**
1. El cliente selecciona un plan del catálogo.
2. El sistema muestra el resumen: plan, monto a pagar, fecha de inicio, fecha de vencimiento y **fecha y monto del próximo cobro si activa renovación**.
3. El cliente selecciona o registra un método de pago (CU-10 si no tiene ninguno).
4. El sistema pregunta si desea **activar la renovación automática** (por defecto: **desactivada**, opt-in explícito) explicando consecuencias y cómo desactivarla después.
5. El cliente confirma la contratación.
6. El sistema procesa el cobro a través de la pasarela.
7. Pago aprobado: el sistema registra el pago, activa la suscripción (fecha de fin = inicio + duración del plan) y emite la factura.
8. El sistema notifica la confirmación con el detalle del próximo cobro (si aplica) y el enlace para cancelar.

**Flujos Alternativos**
- **FA-1** El cliente no activa la renovación automática: la suscripción vence al finalizar el período y se le recordará renovar manualmente.

**Excepciones**
- **EX-1** Pago rechazado: se muestra el motivo, no se crea la suscripción y se ofrece reintentar con otro método.
- **EX-2** Pasarela no disponible: se conserva la selección y se permite reintentar sin volver a empezar.

---

### CU-07 — Cambiar de plan

| Campo | Detalle |
|---|---|
| **Actor(es)** | Cliente · Pasarela de Pago |
| **Objetivo** | Pasar a otro plan (mejora o reducción) sin perder antigüedad ni generar cobros sorpresa. |
| **Precondiciones** | Suscripción activa. |
| **Postcondiciones** | Plan de la suscripción actualizado; diferencia de precio cobrada o acreditada según corresponda. |
| **RF** | RF-07 · **RN**: RN-01 |

**Flujo Normal**
1. El cliente selecciona *Cambiar de plan* y elige el nuevo plan.
2. El sistema calcula la diferencia de precio (prorrateo por días restantes del período).
3. El sistema muestra el resumen: cobro adicional o saldo a favor, nuevo precio y nueva fecha de renovación.
4. El cliente confirma.
5. Si corresponde pago adicional, se procesa por la pasarela.
6. El sistema actualiza la suscripción y notifica el cambio.

**Excepciones**
- **EX-1** Pago de la diferencia rechazado: el plan no se modifica y se informa al cliente.

---

### CU-08 — Cancelar suscripción  ⭐ (solución al problema de cobros sin control)

| Campo | Detalle |
|---|---|
| **Actor(es)** | Cliente · Servicio de Notificaciones |
| **Objetivo** | Terminar la membresía en línea, sin trámites presenciales ni telefónicos, sin cargos posteriores. |
| **Precondiciones** | Sesión iniciada; suscripción activa. |
| **Postcondiciones** | Renovación automática desactivada; suscripción en estado *cancelada* con acceso hasta la fecha de fin del período pagado; sin más cobros. |
| **RF** | RF-08 · **RN**: RN-01, RN-02, RN-07 |

**Flujo Normal**
1. El cliente entra a *Mi membresía* → *Cancelar suscripción*.
2. El sistema muestra el resumen completo: fecha de fin del período ya pagado, monto y fecha del próximo cobro (si la renovación estuviera activa) y qué servicios perderá.
3. El sistema explica la política: la cancelación **no tiene costo**, es efectiva al final del período pagado y el acceso se conserva hasta esa fecha (RN-02).
4. El cliente indica el motivo de cancelación (opcional).
5. El cliente confirma la cancelación (segunda confirmación explícita para evitar errores).
6. El sistema desactiva la renovación automática, marca la suscripción como *cancelada (vigente hasta fecha de fin)* y registra la acción en auditoría.
7. El sistema notifica la cancelación indicando la fecha de corte del acceso.

**Flujos Alternativos**
- **FA-1** El cliente desiste antes de la confirmación final: la operación se aborta sin ningún cambio.

**Excepciones**
- **EX-1** Cancelación inmediata con devolución por causa justificada: se gestiona presencialmente en recepción (fuera del flujo en línea).
- **EX-2** Error al confirmar: la operación es atómica; o se aplica por completo o no se aplica, y se informa para reintentar.

---

### CU-09 — Gestionar renovación automática  ⭐ (solución al problema de cobros sin control)

| Campo | Detalle |
|---|---|
| **Actor(es)** | Cliente · Servicio de Notificaciones |
| **Objetivo** | Que el cliente decida en todo momento si desea o no que se le cobre automáticamente. |
| **Precondiciones** | Sesión iniciada; suscripción activa. |
| **Postcondiciones** | Estado de renovación automática actualizado; próximos cobros coherentes con la decisión. |
| **RF** | RF-09 · **RN**: RN-01, RN-03 |

**Flujo Normal**
1. El cliente consulta el estado actual de la renovación (activada / desactivada) y la fecha del próximo cobro.
2. El cliente cambia el estado con un solo control (interruptor).
3. El sistema explica la consecuencia: al desactivar, *no se realizarán más cobros* y la membresía vencerá en la fecha indicada; al activar, se muestra monto y fecha del próximo cobro.
4. El cliente confirma.
5. El sistema aplica el cambio y envía notificación de confirmación con el nuevo estado.

**Flujos Alternativos**
- **FA-1** Activación con cobro programado en menos de 24 horas: la activación rige a partir del siguiente ciclo (el cobro en curso no se adelanta); se informa al cliente.

**Excepciones**
- **EX-1** Sin método de pago registrado: no se permite activar la renovación; se ofrece registrar uno (CU-10).

---

### CU-10 — Gestionar métodos de pago

| Campo | Detalle |
|---|---|
| **Actor(es)** | Cliente · Pasarela de Pago |
| **Objetivo** | Administrar las tarjetas/tokenes con los que se cobrará, sin almacenar datos sensibles. |
| **Precondiciones** | Sesión iniciada. |
| **Postcondiciones** | Lista de métodos de pago actualizada; token registrado en el sistema. |
| **RF** | RF-10 · **RN**: RN-01 |

**Flujo Normal**
1. El cliente consulta sus métodos de pago (los datos se muestran enmascarados: marca y últimos 4 dígitos).
2. El cliente agrega un método: los datos de la tarjeta se capturan directamente en un formulario seguro de la pasarela y esta devuelve un token.
3. El sistema guarda el token y lo marca como predeterminado si el cliente lo indica.
4. El cliente puede eliminar métodos no predeterminados.

**Excepciones**
- **EX-1** La tokenización falla: no se guarda ningún dato y se informa el error.
- **EX-2** Intento de eliminar el único método con renovación automática activa: se bloquea y se sugiere desactivar la renovación (CU-09) o agregar otro método.

---

### CU-11 — Consultar historial de pagos y facturas

| Campo | Detalle |
|---|---|
| **Actor(es)** | Cliente |
| **Objetivo** | Dar trazabilidad total de lo cobrado: qué, cuándo y cuánto. |
| **Precondiciones** | Sesión iniciada. |
| **Postcondiciones** | — |
| **RF** | RF-11 · **RN**: — |

**Flujo Normal**
1. El cliente abre *Historial de pagos*.
2. El sistema lista los pagos con fecha, concepto, monto, método y estado (aprobado, pendiente, rechazado, reembolsado).
3. El cliente puede filtrar por rango de fechas y descargar la factura en PDF de cualquier pago aprobado.

**Excepciones**
- **EX-1** Sin pagos registrados: se muestra un estado vacío informativo.

---

### CU-12 — Procesar renovación automática  ⭐ (motor de cobro transparente)

| Campo | Detalle |
|---|---|
| **Actor(es)** | Planificador (proceso programado) · Pasarela de Pago · Servicio de Notificaciones |
| **Objetivo** | Cobrar las renovaciones de forma confiable y transparente, solo con autorización vigente. |
| **Precondiciones** | Suscripciones activas con renovación automática autorizada y método de pago predeterminado. |
| **Postcondiciones** | Renovaciones cobradas y extendidas, o suscripciones suspendidas tras agotar reintentos; cliente notificado en todos los casos. |
| **RF** | RF-12, RF-13 · **RN**: RN-01, RN-03, RN-04 |

**Flujo Normal (proceso diario)**
1. **Recordatorios:** el sistema envía aviso 7 días y 1 día antes de cada cobro, indicando monto y fecha, con enlace directo a cancelar (CU-08) o desactivar renovación (CU-09).
2. **Cobro:** en la fecha de renovación, el sistema toma cada suscripción con renovación autorizada y ejecuta el cobro con el método predeterminado a través de la pasarela.
3. Pago aprobado: registra el pago (tipo *renovación*), extiende la fecha de fin, emite factura y notifica la confirmación.

**Flujos Alternativos**
- **FA-1** Suscripción sin renovación automática: al vencer, pasa a *vencida* y se notifica al cliente para renovar manualmente desde la plataforma (CU-06).

**Excepciones**
- **EX-1** Cobro rechazado: se notifica de inmediato con enlace para actualizar el método (CU-10) y se aplica la política de reintentos (RN-04: día +1, +3 y +7). Agotados los reintentos, la suscripción pasa a *suspendida* y se informa que se reactiva al regularizar el pago.
- **EX-2** Pasarela no disponible: el intento no consume un reintento; se reprocesa en el siguiente ciclo.

---

## Módulo 3 — Acceso y Aforo

### CU-13 — Realizar check-in con QR

| Campo | Detalle |
|---|---|
| **Actor(es)** | Cliente |
| **Objetivo** | Ingresar al gimnasio de forma ágil y segura, validando membresía y aforo. |
| **Precondiciones** | Sesión iniciada o lector de QR disponible en la entrada. |
| **Postcondiciones** | Acceso registrado (entrada); ocupación actualizada en tiempo real. |
| **RF** | RF-14, RF-19 · **RN**: RN-05, RN-06, RN-11 |

**Flujo Normal**
1. El cliente muestra su código QR desde la web/app (o lo escanea en el lector de la entrada).
2. El sistema valida el QR y que la membresía esté **activa** (RN-05).
3. El sistema verifica que la ocupación no haya alcanzado la capacidad máxima (RN-06).
4. El sistema registra la entrada, actualiza la ocupación en tiempo real y confirma el acceso mostrando el aforo actual.

**Flujos Alternativos**
- **FA-1** Aforo completo: se deniega el ingreso y se ofrece entrar a la cola virtual (CU-22).

**Excepciones**
- **EX-1** QR inválido o expirado: el cliente puede generar uno nuevo; también puede solicitar acceso manual en recepción (CU-14).
- **EX-2** Membresía vencida o suspendida: se deniega el ingreso y se ofrecen opciones (renovar en línea o pasar a recepción).
- **EX-3** El cliente ya se encuentra dentro (sin salida registrada): se evita el doble registro.

---

### CU-14 — Registrar acceso manual y check-out

| Campo | Detalle |
|---|---|
| **Actor(es)** | Recepcionista |
| **Objetivo** | Permitir el acceso cuando el QR no está disponible y mantener la ocupación exacta con las salidas. |
| **Precondiciones** | Sesión de recepcionista iniciada. |
| **Postcondiciones** | Entrada/salida registrada con el recepcionista responsable (auditoría). |
| **RF** | RF-15, RF-20 · **RN**: RN-05, RN-06, RN-11 |

**Flujo Normal**
1. El recepcionista busca al cliente por nombre, documento o correo.
2. El sistema muestra el estado de la membresía.
3. Con membresía activa y aforo disponible, el recepcionista registra la entrada (método: manual).
4. Para el check-out: el recepcionista registra la salida cuando el cliente se retira, o el sistema la registra automáticamente al cierre de jornada para los rezagados (RN-11).

**Excepciones**
- **EX-1** Cliente no encontrado: se ofrece crearlo (registro asistido, CU-01).
- **EX-2** Membresía vencida: se ofrece vender/renovar la membresía en recepción (CU-30).
- **EX-3** Aforo completo: se deniega y se ofrece la cola virtual (CU-22) o se registra turno presencial (CU-25).

---

### CU-15 — Consultar ocupación en tiempo real  ⭐ (solución al problema de saturación)

| Campo | Detalle |
|---|---|
| **Actor(es)** | Cliente, Recepcionista, Administrador |
| **Objetivo** | Saber cuánta gente hay ahora en el gimnasio antes de ir, y operar el control de aforo. |
| **Precondiciones** | — (el visitante puede ver una versión pública resumida). |
| **Postcondiciones** | — |
| **RF** | RF-16 · **RN**: RN-06, RN-11 |

**Flujo Normal**
1. El usuario abre el panel *Ocupación*.
2. El sistema muestra: aforo actual (personas dentro / capacidad máxima), semáforo de estado (bajo, medio, alto, lleno) y ocupación por zona.
3. La información se actualiza automáticamente con latencia menor a 30 s (RNF-03).
4. El recepcionista dispone además de la lista de personas dentro para la operación diaria.

**Excepciones**
- **EX-1** Falla la actualización: se muestra el último dato disponible con su hora e indicador de *desactualizado*.

---

### CU-16 — Consultar horarios de mayor y menor afluencia  ⭐ (solución al problema de saturación)

| Campo | Detalle |
|---|---|
| **Actor(es)** | Cliente |
| **Objetivo** | Elegir las franjas menos concurridas para entrenar y evitar aglomeraciones. |
| **Precondiciones** | — |
| **Postcondiciones** | — |
| **RF** | RF-17 · **RN**: — |

**Flujo Normal**
1. El cliente abre la sección *Afluencia*.
2. El sistema muestra un mapa de calor por día de la semana y hora, construido con el promedio histórico de la ocupación (últimas 4–8 semanas) derivado de los check-ins.
3. El sistema destaca las franjas de **menor afluencia** y sugiere *la mejor hora para venir hoy*.
4. El cliente puede filtrar por zona (pesas, cardio, salas).

**Excepciones**
- **EX-1** Historial insuficiente (menos de 2 semanas de datos): se muestra aviso y los datos disponibles.

---

### CU-17 — Configurar capacidad y umbrales de aforo

| Campo | Detalle |
|---|---|
| **Actor(es)** | Administrador |
| **Objetivo** | Definir los límites operativos del gimnasio y los parámetros de la cola. |
| **Precondiciones** | Sesión de administrador. |
| **Postcondiciones** | Configuración aplicada de inmediato a check-in, ocupación y cola virtual. |
| **RF** | RF-18 · **RN**: RN-06, RN-08 |

**Flujo Normal**
1. El administrador abre *Configuración de aforo*.
2. Define: capacidad máxima global, capacidad por zona, umbrales del semáforo (%), franjas horarias de operación.
3. Define parámetros de la cola virtual: ventana de presentación del turno, tamaño máximo y tiempo estimado de rotación por persona.
4. El sistema valida y guarda; los cambios aplican de inmediato.

**Excepciones**
- **EX-1** Valores inválidos (capacidad ≤ 0, umbral > 100): se rechaza y se explica el rango válido.
- **EX-2** Nueva capacidad menor a la ocupación actual: se advierte y se permite, aplicando la restricción a los próximos ingresos.

---

## Módulo 4 — Clases y Reservas

### CU-18 — Reservar clase grupal (incl. lista de espera)

| Campo | Detalle |
|---|---|
| **Actor(es)** | Cliente · Servicio de Notificaciones |
| **Objetivo** | Asegurar un cupo en una clase grupal (o un lugar en la lista de espera), evitando colas presenciales. |
| **Precondiciones** | Sesión iniciada; membresía activa; la clase está activa y tiene sesión futura. |
| **Postcondiciones** | Reserva *confirmada* (o *en espera*); cupo descontado; notificaciones enviadas. |
| **RF** | RF-21, RF-22 · **RN**: RN-05, RN-09 |

**Flujo Normal**
1. El cliente abre el calendario de clases y filtra por disciplina, día u hora.
2. Selecciona una clase y la fecha de la sesión.
3. El sistema verifica membresía activa y cupo disponible.
4. Con cupo disponible, el sistema confirma la reserva, envía la confirmación y programa un recordatorio previo a la clase.

**Flujos Alternativos**
- **FA-1** Clase llena: el sistema ofrece **lista de espera**. Si el cliente acepta, la reserva queda *en espera* con su posición. Cuando alguien cancela, el sistema promueve automáticamente al primero y lo notifica de inmediato (RN-09).

**Excepciones**
- **EX-1** Membresía vencida: se bloquea la reserva y se ofrece renovar (CU-06).
- **EX-2** El cliente ya tiene una reserva para esa misma sesión: se impide duplicar.
- **EX-3** La sesión fue cancelada por el gimnasio: se notifica a los reservados y se liberan los cupos.

---

### CU-19 — Cancelar reserva de clase

| Campo | Detalle |
|---|---|
| **Actor(es)** | Cliente · Servicio de Notificaciones |
| **Objetivo** | Liberar el cupo con antelación para que otro cliente lo aproveche. |
| **Precondiciones** | Reserva confirmada o en espera para una sesión futura. |
| **Postcondiciones** | Reserva *cancelada*; cupo liberado; lista de espera promovida. |
| **RF** | RF-23 · **RN**: RN-07, RN-09 |

**Flujo Normal**
1. El cliente abre *Mis reservas*.
2. Selecciona la reserva a cancelar.
3. El sistema verifica que falten más de 2 horas para el inicio (RN-07).
4. El sistema cancela, libera el cupo, promueve al primero de la lista de espera (RN-09) y notifica.

**Excepciones**
- **EX-1** Falta menos de 2 horas: no se permite cancelar; se informa que la inasistencia quedará registrada.

---

### CU-20 — Pasar lista de asistencia

| Campo | Detalle |
|---|---|
| **Actor(es)** | Entrenador |
| **Objetivo** | Registrar la asistencia real a la clase dictada. |
| **Precondiciones** | Sesión de entrenador; clase próxima a iniciar o en curso. |
| **Postcondiciones** | Asistencia registrada por alumno (asistió / no asistió). |
| **RF** | RF-24 · **RN**: — |

**Flujo Normal**
1. El entrenador abre *Mis clases* y selecciona la sesión del día.
2. El sistema muestra la lista de reservas confirmadas.
3. El entrenador marca la asistencia de cada alumno.
4. El sistema guarda y actualiza el historial.

**Flujos Alternativos**
- **FA-1** Llega un cliente sin reserva y hay cupo: el entrenador puede agregarlo y marcarlo presente.

**Excepciones**
- **EX-1** Sin conexión momentánea: la lista se guarda localmente y se sincroniza al restablecerse.

---

### CU-21 — Gestionar clases y horarios

| Campo | Detalle |
|---|---|
| **Actor(es)** | Administrador |
| **Objetivo** | Mantener la programación de clases coherente con la oferta y disponibilidad. |
| **Precondiciones** | Sesión de administrador; zonas y entrenadores registrados. |
| **Postcondiciones** | Programación de clases actualizada; cambios notificados si afectan reservas. |
| **RF** | RF-25 · **RN**: — |

**Flujo Normal**
1. El administrador crea o edita una clase: nombre, disciplina, entrenador, zona, día(s), horario, duración y cupo máximo.
2. El sistema valida que no existan choques de horario del entrenador ni de la zona.
3. El sistema guarda y publica la clase en el calendario de reservas.

**Excepciones**
- **EX-1** Choque de horario: se rechaza con el detalle del conflicto.
- **EX-2** Desactivar una clase con reservas futuras: se advierte, se notifica a los reservados y se liberan las reservas.

---

## Módulo 5 — Cola Virtual

### CU-22 — Solicitar turno en cola virtual  ⭐ (solución al problema de colas)

| Campo | Detalle |
|---|---|
| **Actor(es)** | Cliente · Servicio de Notificaciones · Planificador |
| **Objetivo** | Esperar de forma ordenada y remota cuando el aforo está lleno o la clase no tiene cupo, sin hacer fila física. |
| **Precondiciones** | Sesión iniciada; membresía activa; aforo completo o clase llena; el cliente no tiene un turno activo. |
| **Postcondiciones** | Turno creado con posición y estimación; el cliente es notificado cuando sea su turno. |
| **RF** | RF-26, RF-29 · **RN**: RN-06, RN-08 |

**Flujo Normal**
1. Al denegarse un ingreso (CU-13/CU-14) o una reserva (CU-18), el sistema ofrece *Entrar a la cola virtual*.
2. El cliente acepta; el sistema valida membresía activa y ausencia de turno previo.
3. El sistema crea el turno (tipo: ingreso o clase), calcula la posición y estima la espera (personas delante × tiempo promedio de rotación configurado).
4. El sistema muestra la confirmación y la pantalla de estado (CU-23).
5. Cuando se libera un cupo, recepción o el sistema llama al siguiente turno y se **notifica al cliente: "es tu turno"**, con la ventana de presentación (RN-08).
6. El cliente se presenta; recepción marca el turno como *atendido*, registra el check-in y sale de la cola.

**Flujos Alternativos**
- **FA-1** El cliente no se presenta dentro de la ventana: el turno *expira* (Planificador), la cola avanza y se recalcula la estimación de los demás.

**Excepciones**
- **EX-1** La membresía vence mientras espera: el turno se cancela al detectarse en el siguiente ciclo.
- **EX-2** Corte del servicio: los turnos persisten almacenados y se restauran con el servicio.

---

### CU-23 — Consultar posición y tiempo de espera del turno

| Campo | Detalle |
|---|---|
| **Actor(es)** | Cliente |
| **Objetivo** | Saber cuántas personas hay delante y cuánto falta para el ingreso. |
| **Precondiciones** | Turno activo en la cola. |
| **Postcondiciones** | — |
| **RF** | RF-27 · **RN**: RN-08 |

**Flujo Normal**
1. El cliente abre *Mi turno* (o el sistema lo muestra al solicitar el turno — include).
2. El sistema muestra: posición actual, personas delante, tiempo estimado de espera y estado (esperando / llamado).
3. La información se actualiza automáticamente ante cada movimiento de la cola.

**Excepciones**
- **EX-1** El turno expiró o fue cancelado: se informa el resultado y la opción de volver a la cola si sigue llena.

---

### CU-24 — Cancelar turno en cola

| Campo | Detalle |
|---|---|
| **Actor(es)** | Cliente |
| **Objetivo** | Abandonar la cola de forma limpia cuando el cliente ya no podrá asistir. |
| **Precondiciones** | Turno activo. |
| **Postcondiciones** | Turno *cancelado*; cola recalculada. |
| **RF** | RF-28 · **RN**: — |

**Flujo Normal**
1. El cliente elige *Salir de la cola*.
2. El sistema pide confirmación (para evitar salidas accidentales).
3. El sistema marca el turno como *cancelado* y recalcula posiciones/estimaciones restantes.
4. El sistema notifica la salida de la cola.

**Excepciones**
- **EX-1** El turno ya fue llamado: no puede cancelarse; solo atenderse o expirar.

---

### CU-25 — Gestionar cola y llamar siguiente turno

| Campo | Detalle |
|---|---|
| **Actor(es)** | Recepcionista · Planificador · Servicio de Notificaciones |
| **Objetivo** | Operar la cola presencialmente con orden y datos para la toma de decisiones. |
| **Precondiciones** | Sesión de recepcionista; hay turnos activos. |
| **Postcondiciones** | Turno atendido/expirado; cola actualizada; accesos registrados. |
| **RF** | RF-30 · **RN**: RN-08 |

**Flujo Normal**
1. El recepcionista abre el panel *Cola*: ve posición, nombre, tipo de turno y tiempo de espera de cada cliente.
2. Al liberarse un cupo, pulsa *Llamar siguiente*; el sistema notifica al cliente y arranca el temporizador de la ventana de presentación.
3. El cliente llega: el recepcionista marca *Atendido*, lo que registra el check-in y lo retira de la cola.
4. Si el cliente no llega en la ventana: *Expirar* (manual o automático por el Planificador) y la cola avanza.

**Flujos Alternativos**
- **FA-1** El cliente llega sin haber recibido la notificación: el recepcionista puede marcar *Atendido* manualmente dejando justificación en auditoría.

**Excepciones**
- **EX-1** Cola vacía: la acción *Llamar siguiente* queda deshabilitada.

---

## Módulo 6 — Administración y Reportes

### CU-26 — Gestionar usuarios y roles

| Campo | Detalle |
|---|---|
| **Actor(es)** | Administrador |
| **Objetivo** | Administrar las cuentas del sistema, aprobar las solicitudes de personal y definir sus permisos. |
| **Precondiciones** | Sesión de administrador. |
| **Postcondiciones** | Usuarios, roles y solicitudes actualizados; acciones auditadas; solicitantes notificados. |
| **RF** | RF-31 · **RN**: RN-12 |

**Flujo Normal**
1. El administrador busca o lista usuarios con filtros por rol y estado.
2. Revisa la bandeja de *Solicitudes de personal*: cuentas nacidas del registro público (CU-01) en estado *pendiente*, ordenadas por fecha de registro.
3. Aprueba una solicitud (pasa a *activa* y puede iniciar sesión) o la rechaza (pasa a *rechazada*); en ambos casos el solicitante recibe notificación.
4. Puede: crear usuarios de staff directamente (recepcionista, entrenador, administrador — nacen *activos*), editar datos, suspender/reactivar cuentas y asignar roles.
5. Suspender una cuenta bloquea el acceso al sistema y el check-in.
6. Cada acción queda auditada (fecha, hora, responsable).

**Excepciones**
- **EX-1** El administrador intenta quitarse su propio rol o suspender su cuenta: se bloquea.
- **EX-2** Quitar el rol al último administrador existente: se bloquea para no perder la administración.
- **EX-3** Aprobar o rechazar una cuenta que no está *pendiente*: se rechaza la operación.
- **EX-4** Suspender una cuenta *pendiente* o *rechazada*: se rechaza la operación; use *Aprobar* o *Reactivar*.
- **EX-5** No hay solicitudes pendientes: la bandeja se muestra vacía.

---

### CU-27 — Gestionar planes y precios

| Campo | Detalle |
|---|---|
| **Actor(es)** | Administrador |
| **Objetivo** | Mantener el catálogo de planes vigente y competitivo. |
| **Precondiciones** | Sesión de administrador. |
| **Postcondiciones** | Catálogo actualizado; suscriptores notificados ante cambios de precio. |
| **RF** | RF-32 · **RN**: RN-01 |

**Flujo Normal**
1. El administrador lista el catálogo de planes.
2. Crea o edita un plan: nombre, descripción, precio, duración y beneficios.
3. El sistema valida coherencia (precio > 0, duración > 0) y guarda.
4. Ante un cambio de precio, el sistema notifica a los suscriptores afectados antes de su próxima renovación (transparencia, RNF-06), permitiéndoles cancelar (CU-08).

**Excepciones**
- **EX-1** Desactivar un plan con suscriptores vigentes: se permite; los contratos actuales se mantienen hasta su vencimiento y se notifica a los suscriptores con plan desactivado para elegir uno nuevo en su próxima renovación.

---

### CU-28 — Gestionar zonas y salas

| Campo | Detalle |
|---|---|
| **Actor(es)** | Administrador |
| **Objetivo** | Mantener la estructura física del gimnasio (zonas, salas y sus capacidades). |
| **Precondiciones** | Sesión de administrador. |
| **Postcondiciones** | Zonas actualizadas; capacidades aplicadas al aforo. |
| **RF** | RF-33 · **RN**: RN-06 |

**Flujo Normal**
1. El administrador lista las zonas (pesas, cardio, sala grupal, funcional).
2. Crea o edita: nombre, tipo y capacidad máxima.
3. El sistema valida y publica los cambios en el panel de ocupación.

**Excepciones**
- **EX-1** Eliminar/desactivar una zona con clases activas asignadas: se bloquea indicando las clases dependientes.
- **EX-2** Capacidad menor a la ocupación actual de la zona: se advierte.

---

### CU-29 — Consultar reportes y estadísticas

| Campo | Detalle |
|---|---|
| **Actor(es)** | Administrador |
| **Objetivo** | Tomar decisiones con datos: ocupación, ingresos, uso de clases y colas. |
| **Precondiciones** | Sesión de administrador. |
| **Postcondiciones** | — |
| **RF** | RF-34 · **RN**: — |

**Flujo Normal**
1. El administrador abre el panel de reportes y elige el período.
2. El sistema muestra: membresías activas por plan, ingresos (cobros aprobados, por plan y método), promedios de ocupación por hora y día, horas pico, tasa de clases llenas, uso de lista de espera, tiempos de espera de la cola virtual y motivos de cancelación.
3. El administrador exporta los reportes (CSV / PDF).

**Excepciones**
- **EX-1** Período sin datos: se muestra estado vacío informativo.

---

### CU-30 — Vender membresía presencial

| Campo | Detalle |
|---|---|
| **Actor(es)** | Recepcionista · Pasarela de Pago |
| **Objetivo** | Atender a clientes que contratan directamente en recepción. |
| **Precondiciones** | Cliente presente en recepción (con o sin cuenta). |
| **Postcondiciones** | Suscripción activa; pago registrado; comprobante emitido; renovación automática según decisión explícita del cliente. |
| **RF** | RF-35 · **RN**: RN-01, RN-10 |

**Flujo Normal**
1. El recepcionista busca al cliente; si no tiene cuenta, la crea (registro asistido, CU-01).
2. Selecciona el plan y registra el pago: efectivo o tarjeta (CU-10 si requiere registrar método de pago).
3. Se registra la decisión del cliente sobre la renovación automática (**opt-in explícito**, por defecto desactivada).
4. El sistema activa la suscripción y emite comprobante/factura.

**Excepciones**
- **EX-1** Pago con tarjeta rechazado: no se activa la suscripción; puede intentarse otro método o efectivo.
- **EX-2** El cliente ya tiene una suscripción activa: se bloquea (RN-10) y se ofrece cambiar de plan (CU-07).
