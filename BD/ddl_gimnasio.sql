/* =====================================================================
   SISTEMA DE GESTIÓN DE GIMNASIO — Creación de base de datos
   Motor: Microsoft SQL Server (2019 o superior)
   Basado en el modelo ER: docs/modelo_bd/modelo_er.puml
   Ejecutar en SSMS o con:  sqlcmd -S localhost -i ddl_gimnasio.sql

   Convenciones:
     - Enumeraciones: NVARCHAR + CHECK constraint; se almacena el nombre
       de la constante del enum Java (siempre en MAYÚSCULAS), p. ej.
       'ACTIVA', 'EN_ESPERA', 'TARJETA_CREDITO'
     - Booleanos: BIT (0/1)   ·   Fechas: DATE / TIME / DATETIME2
     - Los comentarios referencian los casos de uso (CU-XX),
       requisitos funcionales (RF) y reglas de negocio (RN)
   ===================================================================== */

IF DB_ID(N'GimnasioDB') IS NULL
    CREATE DATABASE GimnasioDB;
GO

USE GimnasioDB;
GO

-- Los índices filtrados exigen estas opciones (SSMS las activa por defecto;
-- sqlcmd usa QUOTED_IDENTIFIER OFF, por eso se fijan explícitamente aquí).
SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
GO

/* =====================================================================
   1. usuario  (clientes y personal del gimnasio)
   ===================================================================== */
CREATE TABLE usuario (
    id_usuario        INT IDENTITY(1,1) NOT NULL,
    nombre            NVARCHAR(80)  NOT NULL,
    apellidos         NVARCHAR(80)  NOT NULL,
    email             NVARCHAR(120) NOT NULL,
    password_hash     VARCHAR(255)  NOT NULL,          -- hash bcrypt/argon2 (RNF-01)
    telefono          NVARCHAR(20)  NULL,
    fecha_nacimiento  DATE          NULL,
    rol               NVARCHAR(20)  NOT NULL,
    estado            NVARCHAR(20)  NOT NULL CONSTRAINT df_usuario_estado   DEFAULT N'ACTIVO',
    fecha_registro    DATETIME2(0)  NOT NULL CONSTRAINT df_usuario_registro DEFAULT SYSDATETIME(),
    CONSTRAINT pk_usuario        PRIMARY KEY (id_usuario),
    CONSTRAINT uq_usuario_email  UNIQUE (email),
    CONSTRAINT ck_usuario_rol    CHECK (rol IN (N'CLIENTE', N'RECEPCIONISTA', N'ENTRENADOR', N'ADMINISTRADOR')),
    CONSTRAINT ck_usuario_estado CHECK (estado IN (N'ACTIVO', N'SUSPENDIDO'))
);
GO

/* =====================================================================
   2. plan  (catálogo de planes del gimnasio — CU-05, CU-27)
   NOTA: "plan" es palabra reservada de T-SQL; la tabla se referencia
   siempre entre corchetes: [plan]
   ===================================================================== */
CREATE TABLE [plan] (
    id_plan        INT IDENTITY(1,1) NOT NULL,
    nombre         NVARCHAR(60)  NOT NULL,
    descripcion    NVARCHAR(255) NULL,
    precio         DECIMAL(10,2) NOT NULL,
    duracion_dias  INT           NOT NULL,
    activo         BIT           NOT NULL CONSTRAINT df_plan_activo DEFAULT 1,
    CONSTRAINT pk_plan          PRIMARY KEY (id_plan),
    CONSTRAINT ck_plan_precio   CHECK (precio > 0),
    CONSTRAINT ck_plan_duracion CHECK (duracion_dias > 0)
);
GO

/* =====================================================================
   3. suscripcion  (membresía contratada — CU-06, CU-07, CU-08, CU-09)
   ===================================================================== */
CREATE TABLE suscripcion (
    id_suscripcion        INT IDENTITY(1,1) NOT NULL,
    id_usuario            INT           NOT NULL,
    id_plan               INT           NOT NULL,
    fecha_inicio          DATE          NOT NULL,
    fecha_fin             DATE          NOT NULL,
    estado                NVARCHAR(20)  NOT NULL,
    renovacion_automatica BIT           NOT NULL CONSTRAINT df_suscripcion_renovacion DEFAULT 0,  -- RN-01: por defecto desactivada (opt-in)
    intentos_cobro        INT           NOT NULL CONSTRAINT df_suscripcion_intentos  DEFAULT 0,   -- RN-04: reintentos de renovación usados
    fecha_cancelacion     DATETIME2(0)  NULL,
    motivo_cancelacion    NVARCHAR(255) NULL,
    CONSTRAINT pk_suscripcion           PRIMARY KEY (id_suscripcion),
    CONSTRAINT fk_suscripcion_usuario   FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT fk_suscripcion_plan      FOREIGN KEY (id_plan)    REFERENCES [plan] (id_plan),
    CONSTRAINT ck_suscripcion_estado    CHECK (estado IN (N'ACTIVA', N'CANCELADA', N'VENCIDA', N'SUSPENDIDA')),
    CONSTRAINT ck_suscripcion_fechas    CHECK (fecha_fin >= fecha_inicio)
);
GO

-- RN-10: un cliente no puede tener más de una suscripción activa a la vez
CREATE UNIQUE INDEX uq_suscripcion_activa
    ON suscripcion (id_usuario)
    WHERE estado = N'ACTIVA';
GO

-- Proceso diario de renovaciones (CU-12): buscar suscripciones por vencer
CREATE INDEX ix_suscripcion_vencimiento
    ON suscripcion (estado, fecha_fin)
    INCLUDE (id_usuario, id_plan, renovacion_automatica);
GO

/* =====================================================================
   4. metodo_pago  (tarjetas tokenizadas — CU-10 · RNF-02: PCI delegado)
   ===================================================================== */
CREATE TABLE metodo_pago (
    id_metodo      INT IDENTITY(1,1) NOT NULL,
    id_usuario     INT           NOT NULL,
    tipo           NVARCHAR(20)  NOT NULL,
    ultimos_cuatro NCHAR(4)      NOT NULL,
    titular        NVARCHAR(120) NULL,
    token_pasarela VARCHAR(255)  NOT NULL,   -- token de la pasarela; NUNCA datos completos de tarjeta
    predeterminado BIT           NOT NULL CONSTRAINT df_metodo_predeterminado DEFAULT 0,
    activo         BIT           NOT NULL CONSTRAINT df_metodo_activo         DEFAULT 1,
    CONSTRAINT pk_metodo_pago        PRIMARY KEY (id_metodo),
    CONSTRAINT fk_metodo_pago_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT ck_metodo_pago_tipo    CHECK (tipo IN (N'TARJETA_CREDITO', N'TARJETA_DEBITO'))
);
GO

/* =====================================================================
   5. pago  (cobros: alta, renovación, ajuste por cambio de plan — CU-06/CU-11/CU-12)
   ===================================================================== */
CREATE TABLE pago (
    id_pago                INT IDENTITY(1,1) NOT NULL,
    id_suscripcion         INT           NOT NULL,
    id_metodo              INT           NULL,      -- NULL cuando el pago fue en efectivo (CU-30)
    monto                  DECIMAL(10,2) NOT NULL,
    fecha_pago             DATETIME2(0)  NOT NULL CONSTRAINT df_pago_fecha DEFAULT SYSDATETIME(),
    tipo                   NVARCHAR(20)  NOT NULL,
    estado                 NVARCHAR(20)  NOT NULL,
    numero_factura         NVARCHAR(30)  NULL,
    id_transaccion_pasarela VARCHAR(100) NULL,
    CONSTRAINT pk_pago             PRIMARY KEY (id_pago),
    CONSTRAINT fk_pago_suscripcion FOREIGN KEY (id_suscripcion) REFERENCES suscripcion (id_suscripcion),
    CONSTRAINT fk_pago_metodo      FOREIGN KEY (id_metodo)      REFERENCES metodo_pago (id_metodo),
    CONSTRAINT ck_pago_tipo        CHECK (tipo   IN (N'ALTA', N'RENOVACION', N'AJUSTE_CAMBIO_PLAN')),
    CONSTRAINT ck_pago_estado      CHECK (estado IN (N'PENDIENTE', N'APROBADO', N'RECHAZADO', N'REEMBOLSADO')),
    CONSTRAINT ck_pago_monto       CHECK (monto > 0)
);
GO

-- Número de factura único entre los pagos que la tienen (los pendientes/rechazados van NULL)
CREATE UNIQUE INDEX uq_pago_factura
    ON pago (numero_factura)
    WHERE numero_factura IS NOT NULL;
GO

CREATE INDEX ix_pago_suscripcion ON pago (id_suscripcion, fecha_pago DESC);
GO

/* =====================================================================
   6. check_in  (entradas y salidas — CU-13, CU-14 · base del aforo RN-11)
   ===================================================================== */
CREATE TABLE check_in (
    id_checkin         INT IDENTITY(1,1) NOT NULL,
    id_usuario         INT           NOT NULL,
    id_recepcionista   INT           NULL,      -- NULL cuando el ingreso fue con QR
    fecha_hora_entrada DATETIME2(0)  NOT NULL,
    fecha_hora_salida  DATETIME2(0)  NULL,      -- NULL = la persona sigue dentro
    metodo             NVARCHAR(10)  NOT NULL,
    CONSTRAINT pk_check_in              PRIMARY KEY (id_checkin),
    CONSTRAINT fk_check_in_usuario      FOREIGN KEY (id_usuario)       REFERENCES usuario (id_usuario),
    CONSTRAINT fk_check_in_recepcionista FOREIGN KEY (id_recepcionista) REFERENCES usuario (id_usuario),
    CONSTRAINT ck_check_in_metodo CHECK (metodo IN (N'QR', N'MANUAL')),
    CONSTRAINT ck_check_in_salida CHECK (fecha_hora_salida IS NULL OR fecha_hora_salida >= fecha_hora_entrada)
);
GO

-- Consultas de ocupación (CU-15): personas dentro = salida NULL
CREATE INDEX ix_check_in_salida  ON check_in (fecha_hora_salida);
CREATE INDEX ix_check_in_entrada ON check_in (fecha_hora_entrada);
GO

/* =====================================================================
   7. zona  (áreas del gimnasio — CU-28)
   ===================================================================== */
CREATE TABLE zona (
    id_zona          INT IDENTITY(1,1) NOT NULL,
    nombre           NVARCHAR(60) NOT NULL,
    tipo             NVARCHAR(20) NOT NULL,
    capacidad_maxima INT          NOT NULL,
    activa           BIT          NOT NULL CONSTRAINT df_zona_activa DEFAULT 1,
    CONSTRAINT pk_zona          PRIMARY KEY (id_zona),
    CONSTRAINT ck_zona_tipo     CHECK (tipo IN (N'PESAS', N'CARDIO', N'SALA_GRUPAL', N'FUNCIONAL')),
    CONSTRAINT ck_zona_capacidad CHECK (capacidad_maxima > 0)
);
GO

/* =====================================================================
   8. clase  (clases grupales programadas — CU-21)
   ===================================================================== */
CREATE TABLE clase (
    id_clase      INT IDENTITY(1,1) NOT NULL,
    id_entrenador INT          NOT NULL,
    id_zona       INT          NOT NULL,
    nombre        NVARCHAR(80) NOT NULL,
    dia_semana    NVARCHAR(10) NOT NULL,
    hora_inicio   TIME(0)      NOT NULL,
    hora_fin      TIME(0)      NOT NULL,
    cupo_maximo   INT          NOT NULL,
    activa        BIT          NOT NULL CONSTRAINT df_clase_activa DEFAULT 1,
    CONSTRAINT pk_clase              PRIMARY KEY (id_clase),
    CONSTRAINT fk_clase_entrenador   FOREIGN KEY (id_entrenador) REFERENCES usuario (id_usuario),
    CONSTRAINT fk_clase_zona         FOREIGN KEY (id_zona)       REFERENCES zona (id_zona),
    CONSTRAINT ck_clase_dia_semana   CHECK (dia_semana IN (N'LUNES', N'MARTES', N'MIERCOLES', N'JUEVES', N'VIERNES', N'SABADO', N'DOMINGO')),
    CONSTRAINT ck_clase_horario      CHECK (hora_fin > hora_inicio),
    CONSTRAINT ck_clase_cupo         CHECK (cupo_maximo > 0)
);
GO

/* =====================================================================
   9. reserva_clase  (reservas y lista de espera — CU-18, CU-19, CU-20)
   ===================================================================== */
CREATE TABLE reserva_clase (
    id_reserva      INT IDENTITY(1,1) NOT NULL,
    id_clase        INT           NOT NULL,
    id_usuario      INT           NOT NULL,
    fecha           DATE          NOT NULL,      -- fecha de la sesión reservada
    estado          NVARCHAR(20)  NOT NULL,
    posicion_espera SMALLINT      NULL,          -- posición cuando estado = EN_ESPERA (RN-09)
    fecha_reserva   DATETIME2(0)  NOT NULL CONSTRAINT df_reserva_fecha DEFAULT SYSDATETIME(),
    CONSTRAINT pk_reserva_clase       PRIMARY KEY (id_reserva),
    CONSTRAINT fk_reserva_clase       FOREIGN KEY (id_clase)   REFERENCES clase (id_clase),
    CONSTRAINT fk_reserva_usuario     FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT ck_reserva_estado      CHECK (estado IN (N'CONFIRMADA', N'EN_ESPERA', N'CANCELADA', N'ASISTIO', N'NO_ASISTIO')),
    CONSTRAINT ck_reserva_espera      CHECK ((estado = N'EN_ESPERA' AND posicion_espera IS NOT NULL)
                                             OR (estado <> N'EN_ESPERA' AND posicion_espera IS NULL))
);
GO

-- Un cliente no puede tener dos reservas vigentes para la misma sesión (EX-2 de CU-18)
CREATE UNIQUE INDEX uq_reserva_vigente
    ON reserva_clase (id_clase, id_usuario, fecha)
    WHERE estado IN (N'CONFIRMADA', N'EN_ESPERA');
GO

CREATE INDEX ix_reserva_sesion ON reserva_clase (id_clase, fecha, estado);
GO

/* =====================================================================
   10. turno_cola  (cola virtual — CU-22 a CU-25)
   ===================================================================== */
CREATE TABLE turno_cola (
    id_turno        INT IDENTITY(1,1) NOT NULL,
    id_usuario      INT           NOT NULL,
    tipo            NVARCHAR(10)  NOT NULL,
    fecha_solicitud DATETIME2(0)  NOT NULL CONSTRAINT df_turno_fecha DEFAULT SYSDATETIME(),
    posicion        INT           NOT NULL,
    estado          NVARCHAR(20)  NOT NULL,
    hora_llamado    DATETIME2(0)  NULL,
    hora_atencion   DATETIME2(0)  NULL,
    CONSTRAINT pk_turno_cola       PRIMARY KEY (id_turno),
    CONSTRAINT fk_turno_usuario    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT ck_turno_tipo       CHECK (tipo   IN (N'INGRESO', N'CLASE')),
    CONSTRAINT ck_turno_estado     CHECK (estado IN (N'ESPERANDO', N'LLAMADO', N'ATENDIDO', N'CANCELADO', N'EXPIRADO')),
    CONSTRAINT ck_turno_posicion   CHECK (posicion >= 1)
);
GO

-- Un cliente no puede tener más de un turno activo a la vez (EX de CU-22)
CREATE UNIQUE INDEX uq_turno_activo
    ON turno_cola (id_usuario)
    WHERE estado IN (N'ESPERANDO', N'LLAMADO');
GO

CREATE INDEX ix_turno_cola_espera ON turno_cola (estado, fecha_solicitud);
GO

/* =====================================================================
   11. notificacion  (avisos al usuario — CU-12, CU-18, CU-22, CU-25)
   ===================================================================== */
CREATE TABLE notificacion (
    id_notificacion INT IDENTITY(1,1) NOT NULL,
    id_usuario      INT           NOT NULL,
    tipo            NVARCHAR(30)  NOT NULL,
    mensaje         NVARCHAR(255) NOT NULL,
    fecha_envio     DATETIME2(0)  NOT NULL CONSTRAINT df_notificacion_fecha DEFAULT SYSDATETIME(),
    leida           BIT           NOT NULL CONSTRAINT df_notificacion_leida DEFAULT 0,
    CONSTRAINT pk_notificacion         PRIMARY KEY (id_notificacion),
    CONSTRAINT fk_notificacion_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT ck_notificacion_tipo    CHECK (tipo IN (N'RECORDATORIO_COBRO', N'RESULTADO_COBRO', N'CONFIRMACION_RESERVA',
                                                       N'TURNO_PROXIMO', N'CANCELACION', N'AFORO', N'CUENTA'))
);
GO

CREATE INDEX ix_notificacion_usuario ON notificacion (id_usuario, fecha_envio DESC);
GO

/* =====================================================================
   12. registro_aforo  (analítica horaria — alimenta el mapa de calor CU-16)
   ===================================================================== */
CREATE TABLE registro_aforo (
    id_registro        INT IDENTITY(1,1) NOT NULL,
    fecha              DATE    NOT NULL,
    hora               TINYINT NOT NULL,     -- 0 a 23
    ocupacion_promedio INT     NOT NULL,
    ocupacion_maxima   INT     NOT NULL,
    total_ingresos     INT     NOT NULL,
    CONSTRAINT pk_registro_aforo   PRIMARY KEY (id_registro),
    CONSTRAINT uq_registro_aforo   UNIQUE (fecha, hora),
    CONSTRAINT ck_registro_hora    CHECK (hora BETWEEN 0 AND 23),
    CONSTRAINT ck_registro_valores CHECK (ocupacion_maxima >= ocupacion_promedio AND ocupacion_promedio >= 0)
);
GO

/* =====================================================================
   13. configuracion  (parámetros del sistema — CU-17 · fila única)
   ===================================================================== */
CREATE TABLE configuracion (
    id_config           INT IDENTITY(1,1) NOT NULL,
    capacidad_maxima    INT     NOT NULL,
    umbral_medio        TINYINT NOT NULL,    -- % de ocupación para semáforo amarillo
    umbral_alto         TINYINT NOT NULL,    -- % de ocupación para semáforo rojo
    ventana_turno_min   INT     NOT NULL,    -- minutos para presentarse al turno (RN-08)
    tiempo_rotacion_min INT     NOT NULL,    -- estimación de espera por persona
    reintentos_cobro    INT     NOT NULL,    -- RN-04
    horario_apertura    TIME(0) NOT NULL,
    horario_cierre      TIME(0) NOT NULL,
    CONSTRAINT pk_configuracion    PRIMARY KEY (id_config),
    CONSTRAINT ck_config_capacidad CHECK (capacidad_maxima > 0),
    CONSTRAINT ck_config_umbrales  CHECK (umbral_medio BETWEEN 0 AND 100
                                          AND umbral_alto BETWEEN umbral_medio AND 100),
    CONSTRAINT ck_config_tiempos   CHECK (ventana_turno_min > 0 AND tiempo_rotacion_min > 0 AND reintentos_cobro >= 0),
    CONSTRAINT ck_config_horario   CHECK (horario_cierre > horario_apertura)
);
GO

/* =====================================================================
   VISTAS DE APOYO
   ===================================================================== */

-- Ocupación en tiempo real (CU-15): personas dentro = entradas sin salida
CREATE VIEW vw_ocupacion_actual AS
SELECT COUNT(*) AS personas_dentro
FROM check_in
WHERE fecha_hora_salida IS NULL;
GO

-- Promedios de afluencia por día de la semana y hora (CU-16)
-- dia_semana: 0 = lunes ... 6 = domingo (calculado de forma determinista)
CREATE VIEW vw_afluencia_por_hora AS
SELECT
    (DATEDIFF(DAY, '19000101', fecha) % 7) AS dia_semana,
    hora,
    AVG(CAST(ocupacion_promedio AS DECIMAL(6,2))) AS ocupacion_promedio,
    MAX(ocupacion_maxima) AS ocupacion_maxima
FROM registro_aforo
GROUP BY (DATEDIFF(DAY, '19000101', fecha) % 7), hora;
GO

/* =====================================================================
   DATOS INICIALES
   ===================================================================== */

INSERT INTO configuracion
    (capacidad_maxima, umbral_medio, umbral_alto, ventana_turno_min,
     tiempo_rotacion_min, reintentos_cobro, horario_apertura, horario_cierre)
VALUES
    (150, 50, 80, 10, 60, 3, '05:00', '23:00');
GO

INSERT INTO [plan] (nombre, descripcion, precio, duracion_dias) VALUES
    (N'Básico Mensual', N'Acceso a zona de pesas y cardio en todo el horario',            29.90,  30),
    (N'Full Mensual',   N'Acceso total + clases grupales ilimitadas',                     39.90,  30),
    (N'Full Anual',     N'Acceso total + clases grupales, 12 meses con descuento',       399.00, 365);
GO

INSERT INTO zona (nombre, tipo, capacidad_maxima) VALUES
    (N'Zona de pesas',   N'PESAS',       60),
    (N'Zona cardio',     N'CARDIO',      40),
    (N'Sala grupal 1',   N'SALA_GRUPAL', 25),
    (N'Sala funcional',  N'FUNCIONAL',   25);
GO

-- Usuario administrador inicial:
--   correo: admin@gimnasio.local   ·   contraseña: Admin123!
-- El hash bcrypt corresponde a esa contraseña; cambiarla en producción.
INSERT INTO usuario (nombre, apellidos, email, password_hash, rol) VALUES
    (N'Admin', N'Gimnasio', N'admin@gimnasio.local',
     '$2a$10$UF.vRNvZzu0GZqm1YEsEyuGAoiMzc90jAD5yB.7muooRLTGnV7n1y', N'ADMINISTRADOR');
GO

PRINT N'Base de datos GimnasioDB creada correctamente (13 tablas, 2 vistas, datos iniciales).';
GO
