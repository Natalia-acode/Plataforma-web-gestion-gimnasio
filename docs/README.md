# Documentación — Sistema de Gestión de Gimnasio

Documentación de análisis y diseño del sistema, en formato PlantUML + Markdown.
**Stack:** Spring Boot (Java) · Microsoft SQL Server.

## Problemas que resuelve el sistema

1. **Cobros automáticos sin control del usuario**: en plataformas como las de SmartFit el cobro es automático y el usuario no puede cancelar ni gestionar su plan desde la web. Este sistema permite cancelar la suscripción, activar/desactivar la renovación automática y ver el historial de pagos en línea, con recordatorio previo a cada cobro.
2. **Saturación horaria**: el sistema mide el aforo en tiempo real (a partir de los check-in) y publica un mapa de calor de afluencia por día y hora, para que el cliente elija las franjas menos concurridas.
3. **Colas y esperas**: cola virtual que permite reservar turno de ingreso cuando el aforo está lleno, ver la posición y estimación de espera, y recibir aviso cuando sea el turno.

## Índice

| Documento | Contenido |
|---|---|
| [01_requisitos.md](01_requisitos.md) | Problemática, objetivos, alcance, actores, requisitos funcionales (RF) y no funcionales (RNF), reglas de negocio, tecnologías |
| [casos_de_uso/descripcion_casos_de_uso.md](casos_de_uso/descripcion_casos_de_uso.md) | Descripción detallada de los 30 casos de uso (CU-01 a CU-30) |
| [casos_de_uso/diagramas/](casos_de_uso/diagramas/) | 6 diagramas de casos de uso, uno por módulo |
| [modelo_bd/modelo_er.puml](modelo_bd/modelo_er.puml) | Modelo Entidad-Relación de la base de datos (13 entidades, tipos SQL Server) |
| [../BD/ddl_gimnasio.sql](../BD/ddl_gimnasio.sql) | Script DDL completo para SQL Server (tablas, constraints, índices, vistas y datos iniciales) |
| [arquitectura/arquitectura_sistema.puml](arquitectura/arquitectura_sistema.puml) | Arquitectura del sistema: API REST Spring Boot → SQL Server |
| [clases/diagrama_clases.puml](clases/diagrama_clases.puml) | Diagrama de clases conceptual (modelo de dominio, fase de análisis) |
| [clases/diagrama_clases_diseno.puml](clases/diagrama_clases_diseno.puml) | Diagrama de clases de diseño con las capas Spring Boot (controller/service/repository/entity) |
| [actividades/](actividades/) | 8 diagramas de actividad de los procesos clave |
| [imagenes/](imagenes/) | Las 18 imágenes PNG ya renderizadas, listas para pegar en el informe |


## Convenciones

- **CU-XX**: caso de uso (ver catálogo completo en el documento de descripción).
- **RF-XX / RNF-XX / RN-XX**: requisito funcional / no funcional / regla de negocio.
- **ACT-XX**: diagrama de actividad.
- Los diagramas de casos de uso están agrupados por módulo (6 módulos).
