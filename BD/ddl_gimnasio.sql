/***********************************************************************************************
 *  DDL — Base de datos: GimnasioDB (Microsoft SQL Server)
 *  Sistema de Gestión de Gimnasio
 *
 *  Generado a partir de las entidades JPA del backend (com.gimnasio.domain).
 *  13 tablas, foreign keys, índices, vistas y datos iniciales.
 *
 *  Cómo ejecutar:
 *    1. Abre SQL Server Management Studio o la extensión de SQL Server en VS Code.
 *    2. Conéctate a tu instancia (localhost:1433).
 *    3. Ejecuta este script completo.
 ***********************************************************************************************/

/* ========================================================================
   0. Base de datos (comenta estas dos líneas si ya creaste GimnasioDB)
   ======================================================================== */
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'GimnasioDB')
    CREATE DATABASE GimnasioDB;
GO

USE GimnasioDB;
GO

/* ========================================================================
   1. TABLAS
   ======================================================================== */

-- ------------------------------------------------------------------------
-- Tabla: usuario
-- ------------------------------------------------------------------------
IF OBJECT_ID('[usuario]', 'U') IS NOT NULL DROP TABLE [usuario];
CREATE TABLE [usuario] (
    [id_usuario]         BIGINT IDENTITY(1,1) PRIMARY KEY,
    [nombre]             NVARCHAR(80)  NOT NULL,
    [apellidos]          NVARCHAR(80)  NOT NULL,
    [email]              NVARCHAR(120) NOT NULL UNIQUE,
    [password_hash]      NVARCHAR(255) NOT NULL,
    [password_reset_token] NVARCHAR(255),
    [password_reset_expira] DATETIME2(0),
    [telefono]           NVARCHAR(20),
    [fecha_nacimiento]   DATE,
    [rol]                NVARCHAR(20)  NOT NULL,
    [estado]             NVARCHAR(20)  NOT NULL DEFAULT 'activo',
    [fecha_registro]     DATETIME2(0)  NOT NULL
);
GO

-- ------------------------------------------------------------------------
-- Tabla: plan
-- ------------------------------------------------------------------------
IF OBJECT_ID('[plan]', 'U') IS NOT NULL DROP TABLE [plan];
CREATE TABLE [plan] (
    [id_plan]        BIGINT IDENTITY(1,1) PRIMARY KEY,
    [nombre]         NVARCHAR(60)  NOT NULL,
    [descripcion]    NVARCHAR(255),
    [precio]         DECIMAL(10,2) NOT NULL,
    [duracion_dias]  INT           NOT NULL,
    [activo]         BIT           NOT NULL DEFAULT 1
);
GO

-- ------------------------------------------------------------------------
-- Tabla: zona
-- ------------------------------------------------------------------------
IF OBJECT_ID('[zona]', 'U') IS NOT NULL DROP TABLE [zona];
CREATE TABLE [zona] (
    [id_zona]           BIGINT IDENTITY(1,1) PRIMARY KEY,
    [nombre]            NVARCHAR(60)  NOT NULL,
    [tipo]              NVARCHAR(20)  NOT NULL,
    [capacidad_maxima]  INT           NOT NULL,
    [activa]            BIT           NOT NULL DEFAULT 1
);
GO

-- ------------------------------------------------------------------------
-- Tabla: configuracion (fila única, parámetros del sistema)
-- ------------------------------------------------------------------------
IF OBJECT_ID('[configuracion]', 'U') IS NOT NULL DROP TABLE [configuracion];
CREATE TABLE [configuracion] (
    [id_config]         BIGINT IDENTITY(1,1) PRIMARY KEY,
    [capacidad_maxima]  INT            NOT NULL,
    [umbral_medio]      TINYINT        NOT NULL,
    [umbral_alto]       TINYINT        NOT NULL,
    [ventana_turno_min] INT            NOT NULL,
    [tiempo_rotacion_min] INT          NOT NULL,
    [reintentos_cobro]  INT            NOT NULL,
    [horario_apertura]  TIME(0)        NOT NULL,
    [horario_cierre]    TIME(0)        NOT NULL
);
GO

-- ------------------------------------------------------------------------
-- Tabla: metodo_pago (tokenizada, nunca datos completos de tarjeta)
-- ------------------------------------------------------------------------
IF OBJECT_ID('[metodo_pago]', 'U') IS NOT NULL DROP TABLE [metodo_pago];
CREATE TABLE [metodo_pago] (
    [id_metodo]       BIGINT IDENTITY(1,1) PRIMARY KEY,
    [id_usuario]      BIGINT NOT NULL,
    [tipo]            NVARCHAR(20) NOT NULL,
    [ultimos_cuatro]  NCHAR(4)     NOT NULL,
    [titular]         NVARCHAR(120),
    [token_pasarela]  NVARCHAR(255) NOT NULL,
    [predeterminado]  BIT          NOT NULL DEFAULT 0,
    [activo]          BIT          NOT NULL DEFAULT 1,
    CONSTRAINT [FK_metodo_pago_usuario] FOREIGN KEY ([id_usuario])
        REFERENCES [usuario]([id_usuario])
);
GO

-- ------------------------------------------------------------------------
-- Tabla: suscripcion
-- ------------------------------------------------------------------------
IF OBJECT_ID('[suscripcion]', 'U') IS NOT NULL DROP TABLE [suscripcion];
CREATE TABLE [suscripcion] (
    [id_suscripcion]      BIGINT IDENTITY(1,1) PRIMARY KEY,
    [id_usuario]          BIGINT NOT NULL,
    [id_plan]             BIGINT NOT NULL,
    [fecha_inicio]        DATE      NOT NULL,
    [fecha_fin]           DATE      NOT NULL,
    [estado]              NVARCHAR(20) NOT NULL DEFAULT 'activa',
    [renovacion_automatica] BIT    NOT NULL DEFAULT 0,
    [intentos_cobro]      INT       NOT NULL DEFAULT 0,
    [fecha_cancelacion]   DATETIME2(0),
    [motivo_cancelacion]  NVARCHAR(255),
    CONSTRAINT [FK_suscripcion_usuario] FOREIGN KEY ([id_usuario])
        REFERENCES [usuario]([id_usuario]),
    CONSTRAINT [FK_suscripcion_plan] FOREIGN KEY ([id_plan])
        REFERENCES [plan]([id_plan])
);
GO

-- ------------------------------------------------------------------------
-- Tabla: pago
-- ------------------------------------------------------------------------
IF OBJECT_ID('[pago]', 'U') IS NOT NULL DROP TABLE [pago];
CREATE TABLE [pago] (
    [id_pago]                   BIGINT IDENTITY(1,1) PRIMARY KEY,
    [id_suscripcion]            BIGINT NOT NULL,
    [id_metodo]                 BIGINT,
    [monto]                     DECIMAL(10,2) NOT NULL,
    [fecha_pago]                DATETIME2(0) NOT NULL,
    [tipo]                      NVARCHAR(20) NOT NULL,
    [estado]                    NVARCHAR(20) NOT NULL,
    [numero_factura]            NVARCHAR(30),
    [id_transaccion_pasarela]   NVARCHAR(100),
    CONSTRAINT [FK_pago_suscripcion] FOREIGN KEY ([id_suscripcion])
        REFERENCES [suscripcion]([id_suscripcion]),
    CONSTRAINT [FK_pago_metodo_pago] FOREIGN KEY ([id_metodo])
        REFERENCES [metodo_pago]([id_metodo])
);
GO

-- ------------------------------------------------------------------------
-- Tabla: check_in (entradas y salidas; base del cálculo de aforo)
-- ------------------------------------------------------------------------
IF OBJECT_ID('[check_in]', 'U') IS NOT NULL DROP TABLE [check_in];
CREATE TABLE [check_in] (
    [id_checkin]         BIGINT IDENTITY(1,1) PRIMARY KEY,
    [id_usuario]         BIGINT NOT NULL,
    [id_recepcionista]   BIGINT,
    [fecha_hora_entrada] DATETIME2(0) NOT NULL,
    [fecha_hora_salida]  DATETIME2(0),   -- NULL = sigue dentro
    [metodo]             NVARCHAR(10) NOT NULL,
    CONSTRAINT [FK_check_in_usuario] FOREIGN KEY ([id_usuario])
        REFERENCES [usuario]([id_usuario]),
    CONSTRAINT [FK_check_in_recepcionista] FOREIGN KEY ([id_recepcionista])
        REFERENCES [usuario]([id_usuario])
);
GO

-- ------------------------------------------------------------------------
-- Tabla: clase (clases grupales programadas)
-- ------------------------------------------------------------------------
IF OBJECT_ID('[clase]', 'U') IS NOT NULL DROP TABLE [clase];
CREATE TABLE [clase] (
    [id_clase]      BIGINT IDENTITY(1,1) PRIMARY KEY,
    [id_entrenador] BIGINT NOT NULL,
    [id_zona]       BIGINT NOT NULL,
    [nombre]        NVARCHAR(80)  NOT NULL,
    [dia_semana]    NVARCHAR(10)  NOT NULL,
    [hora_inicio]   TIME(0)       NOT NULL,
    [hora_fin]      TIME(0)       NOT NULL,
    [cupo_maximo]   INT           NOT NULL,
    [activa]        BIT           NOT NULL DEFAULT 1,
    CONSTRAINT [FK_clase_entrenador] FOREIGN KEY ([id_entrenador])
        REFERENCES [usuario]([id_usuario]),
    CONSTRAINT [FK_clase_zona] FOREIGN KEY ([id_zona])
        REFERENCES [zona]([id_zona])
);
GO

-- ------------------------------------------------------------------------
-- Tabla: reserva_clase (reservas y lista de espera)
-- ------------------------------------------------------------------------
IF OBJECT_ID('[reserva_clase]', 'U') IS NOT NULL DROP TABLE [reserva_clase];
CREATE TABLE [reserva_clase] (
    [id_reserva]     BIGINT IDENTITY(1,1) PRIMARY KEY,
    [id_clase]       BIGINT NOT NULL,
    [id_usuario]     BIGINT NOT NULL,
    [fecha]          DATE           NOT NULL,
    [estado]         NVARCHAR(20)   NOT NULL,
    [posicion_espera] SMALLINT,
    [fecha_reserva]  DATETIME2(0)   NOT NULL,
    CONSTRAINT [FK_reserva_clase_clase] FOREIGN KEY ([id_clase])
        REFERENCES [clase]([id_clase]),
    CONSTRAINT [FK_reserva_clase_usuario] FOREIGN KEY ([id_usuario])
        REFERENCES [usuario]([id_usuario])
);
GO

-- ------------------------------------------------------------------------
-- Tabla: turno_cola (cola virtual de ingreso)
-- ------------------------------------------------------------------------
IF OBJECT_ID('[turno_cola]', 'U') IS NOT NULL DROP TABLE [turno_cola];
CREATE TABLE [turno_cola] (
    [id_turno]        BIGINT IDENTITY(1,1) PRIMARY KEY,
    [id_usuario]      BIGINT NOT NULL,
    [tipo]            NVARCHAR(10) NOT NULL,
    [fecha_solicitud] DATETIME2(0) NOT NULL,
    [posicion]        INT          NOT NULL,
    [estado]          NVARCHAR(20) NOT NULL,
    [hora_llamado]    DATETIME2(0),
    [hora_atencion]   DATETIME2(0),
    CONSTRAINT [FK_turno_cola_usuario] FOREIGN KEY ([id_usuario])
        REFERENCES [usuario]([id_usuario])
);
GO

-- ------------------------------------------------------------------------
-- Tabla: notificacion (avisos al usuario)
-- ------------------------------------------------------------------------
IF OBJECT_ID('[notificacion]', 'U') IS NOT NULL DROP TABLE [notificacion];
CREATE TABLE [notificacion] (
    [id_notificacion] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [id_usuario]      BIGINT NOT NULL,
    [tipo]            NVARCHAR(30) NOT NULL,
    [mensaje]         NVARCHAR(255) NOT NULL,
    [fecha_envio]     DATETIME2(0) NOT NULL,
    [leida]           BIT NOT NULL DEFAULT 0,
    CONSTRAINT [FK_notificacion_usuario] FOREIGN KEY ([id_usuario])
        REFERENCES [usuario]([id_usuario])
);
GO

-- ------------------------------------------------------------------------
-- Tabla: registro_aforo (analítica horaria para el mapa de calor)
-- ------------------------------------------------------------------------
IF OBJECT_ID('[registro_aforo]', 'U') IS NOT NULL DROP TABLE [registro_aforo];
CREATE TABLE [registro_aforo] (
    [id_registro]         BIGINT IDENTITY(1,1) PRIMARY KEY,
    [fecha]               DATE     NOT NULL,
    [hora]                TINYINT  NOT NULL,
    [ocupacion_promedio]  INT      NOT NULL,
    [ocupacion_maxima]    INT      NOT NULL,
    [total_ingresos]      INT      NOT NULL
);
GO

/* ========================================================================
   2. ÍNDICES (para consultas frecuentes del backend)
   ======================================================================== */

CREATE INDEX [IX_usuario_email] ON [usuario]([email]);
CREATE INDEX [IX_usuario_rol] ON [usuario]([rol]);
CREATE INDEX [IX_usuario_estado] ON [usuario]([estado]);

CREATE INDEX [IX_plan_activo] ON [plan]([activo]);

CREATE INDEX [IX_suscripcion_usuario_estado] ON [suscripcion]([id_usuario], [estado]);
CREATE INDEX [IX_suscripcion_fecha_fin] ON [suscripcion]([fecha_fin]);
CREATE INDEX [IX_suscripcion_renovacion] ON [suscripcion]([renovacion_automatica]);

CREATE INDEX [IX_pago_suscripcion] ON [pago]([id_suscripcion]);
CREATE INDEX [IX_pago_fecha] ON [pago]([fecha_pago]);
CREATE INDEX [IX_pago_estado] ON [pago]([estado]);

CREATE INDEX [IX_check_in_usuario] ON [check_in]([id_usuario]);
CREATE INDEX [IX_check_in_entrada] ON [check_in]([fecha_hora_entrada]);
CREATE INDEX [IX_check_in_salida] ON [check_in]([fecha_hora_salida]);

CREATE INDEX [IX_clase_zona_dia_hora] ON [clase]([id_zona], [dia_semana], [hora_inicio]);
CREATE INDEX [IX_clase_entrenador] ON [clase]([id_entrenador]);
CREATE INDEX [IX_clase_activa] ON [clase]([activa]);

CREATE INDEX [IX_reserva_clase_clase_fecha] ON [reserva_clase]([id_clase], [fecha]);
CREATE INDEX [IX_reserva_clase_usuario] ON [reserva_clase]([id_usuario]);
CREATE INDEX [IX_reserva_clase_estado] ON [reserva_clase]([estado]);

CREATE INDEX [IX_turno_cola_usuario] ON [turno_cola]([id_usuario]);
CREATE INDEX [IX_turno_cola_estado] ON [turno_cola]([estado]);
CREATE INDEX [IX_turno_cola_posicion] ON [turno_cola]([posicion]);

CREATE INDEX [IX_notificacion_usuario] ON [notificacion]([id_usuario]);
CREATE INDEX [IX_notificacion_fecha] ON [notificacion]([fecha_envio]);

CREATE INDEX [IX_registro_aforo_fecha_hora] ON [registro_aforo]([fecha], [hora]);

/* ========================================================================
   3. VISTAS
   ======================================================================== */

-- Vista: aforo actual (usuarios dentro del gimnasio ahora mismo)
IF OBJECT_ID('[vw_aforo_actual]', 'V') IS NOT NULL DROP VIEW [vw_aforo_actual];
GO
CREATE VIEW [vw_aforo_actual] AS
SELECT COUNT(*) AS personas_dentro
FROM [check_in]
WHERE [fecha_hora_salida] IS NULL;
GO

-- Vista: afluencia por día y hora (alimenta el mapa de calor)
IF OBJECT_ID('[vw_afluencia_por_hora]', 'V') IS NOT NULL DROP VIEW [vw_afluencia_por_hora];
GO
CREATE VIEW [vw_afluencia_por_hora] AS
SELECT
    DATEDIFF(DAY, CONVERT(DATE, '19000101'), [fecha]) % 7 AS [dia_semana],
    [hora],
    AVG(CAST([ocupacion_promedio] AS DECIMAL(10,2))) AS [ocupacion_promedio],
    MAX([ocupacion_maxima]) AS [ocupacion_maxima]
FROM [registro_aforo]
GROUP BY DATEDIFF(DAY, CONVERT(DATE, '19000101'), [fecha]) % 7, [hora]
GO

/* ========================================================================
   4. DATOS INICIALES
   ======================================================================== */

-- --- Planes de membresía ---
INSERT INTO [plan] ([nombre], [descripcion], [precio], [duracion_dias], [activo]) VALUES
('Básico', 'Acceso a salas de pesas y cardio.', 70000.00, 30, 1),
('Estándar', 'Acceso completo + 2 clases grupales por semana.', 90000.00, 30, 1),
('Premium', 'Acceso ilimitado + todas las clases grupales + asesoría.', 130000.00, 30, 1),
('Anual Básico', 'Acceso a pesas y cardio por un año.', 700000.00, 365, 1);

-- --- Zonas del gimnasio ---
INSERT INTO [zona] ([nombre], [tipo], [capacidad_maxima], [activa]) VALUES
('Sala de Pesas', 'pesas', 40, 1),
('Sala de Cardio', 'cardio', 30, 1),
('Sala Grupal', 'sala_grupal', 25, 1),
('Zona Funcional', 'funcional', 20, 1);

-- --- Configuración del sistema (fila única) ---
INSERT INTO [configuracion]
    ([capacidad_maxima], [umbral_medio], [umbral_alto],
     [ventana_turno_min], [tiempo_rotacion_min], [reintentos_cobro],
     [horario_apertura], [horario_cierre])
VALUES
    (100, 60, 85, 10, 15, 3, '06:00', '22:00');

-- --- Administrador inicial ---
-- No se siembra ninguna cuenta. El registro publico (POST /api/auth/registro) acepta cualquier rol:
--   * rol='cliente'                  -> nace con estado='activo'.
--   * rol de personal, si NO hay ningún administrador 'activo'
--                                     -> nace 'activo' (modo arranque: no hay quien revise la solicitud).
--   * rol de personal, si YA hay un administrador 'activo'
--                                     -> nace 'pendiente'; un administrador la aprueba o rechaza
--                                        (PUT /api/usuarios/{id}/aprobar y /rechazar, CU-26).
-- GET /api/auth/arranque informa qué modo aplicará el registro.

/***********************************************************************************************
 *  Fin del script DDL
 ***********************************************************************************************/
