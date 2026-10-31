USE GimnasioDB;
GO

SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @Catalogo TABLE (
        nombre NVARCHAR(60) PRIMARY KEY,
        descripcion NVARCHAR(255),
        precio DECIMAL(10, 2),
        duracion_dias INT
    );

    INSERT INTO @Catalogo (nombre, descripcion, precio, duracion_dias) VALUES
        (N'B' + NCHAR(225) + N'sico', N'Acceso a salas de pesas y cardio.', 70000.00, 30),
        (N'Est' + NCHAR(225) + N'ndar', N'Acceso completo + 2 clases grupales por semana.', 90000.00, 30),
        (N'Premium', N'Acceso ilimitado + todas las clases grupales + asesor' + NCHAR(237) + N'a.', 130000.00, 30),
        (N'Anual B' + NCHAR(225) + N'sico', N'Acceso a pesas y cardio por un a' + NCHAR(241) + N'o.', 700000.00, 365);

    DECLARE @Alias TABLE (
        nombre NVARCHAR(60),
        nombreCanonico NVARCHAR(60)
    );

    INSERT INTO @Alias (nombre, nombreCanonico) VALUES
        (N'B' + NCHAR(225) + N'sico', N'B' + NCHAR(225) + N'sico'),
        (N'B' + NCHAR(195) + NCHAR(161) + N'sico', N'B' + NCHAR(225) + N'sico'),
        (N'Basico', N'B' + NCHAR(225) + N'sico'),
        (N'Est' + NCHAR(225) + N'ndar', N'Est' + NCHAR(225) + N'ndar'),
        (N'Est' + NCHAR(195) + NCHAR(161) + N'ndar', N'Est' + NCHAR(225) + N'ndar'),
        (N'Estandar', N'Est' + NCHAR(225) + N'ndar'),
        (N'Premium', N'Premium'),
        (N'Anual B' + NCHAR(225) + N'sico', N'Anual B' + NCHAR(225) + N'sico'),
        (N'Anual B' + NCHAR(195) + NCHAR(161) + N'sico', N'Anual B' + NCHAR(225) + N'sico'),
        (N'Anual Basico', N'Anual B' + NCHAR(225) + N'sico');

    INSERT INTO [plan] ([nombre], [descripcion], [precio], [duracion_dias], [activo])
    SELECT c.nombre, c.descripcion, c.precio, c.duracion_dias, 1
    FROM @Catalogo c
    WHERE NOT EXISTS (
        SELECT 1
        FROM [plan] p
        JOIN @Alias a
          ON LTRIM(RTRIM(p.[nombre])) COLLATE Latin1_General_BIN2
           = a.nombre COLLATE Latin1_General_BIN2
        WHERE a.nombreCanonico = c.nombre
    );

    ;WITH Clasificados AS (
        SELECT
            p.[id_plan],
            a.nombreCanonico,
            ROW_NUMBER() OVER (
                PARTITION BY a.nombreCanonico
                ORDER BY
                    CASE WHEN EXISTS (
                        SELECT 1 FROM [suscripcion] s WHERE s.[id_plan] = p.[id_plan]
                    ) THEN 0 ELSE 1 END,
                    p.[id_plan]
            ) AS numero
        FROM [plan] p
        JOIN @Alias a
          ON LTRIM(RTRIM(p.[nombre])) COLLATE Latin1_General_BIN2
           = a.nombre COLLATE Latin1_General_BIN2
    )
    SELECT id_plan, nombreCanonico, numero
    INTO #PlanesClasificados
    FROM Clasificados;

    UPDATE p
    SET p.[nombre] = c.nombre,
        p.[descripcion] = c.descripcion,
        p.[precio] = c.precio,
        p.[duracion_dias] = c.duracion_dias,
        p.[activo] = 1
    FROM [plan] p
    JOIN #PlanesClasificados pc ON pc.id_plan = p.[id_plan] AND pc.numero = 1
    JOIN @Catalogo c ON c.nombre = pc.nombreCanonico;

    UPDATE p
    SET p.[activo] = 0
    FROM [plan] p
    JOIN #PlanesClasificados pc ON pc.id_plan = p.[id_plan]
    WHERE pc.numero > 1;

    DELETE p
    FROM [plan] p
    JOIN #PlanesClasificados pc ON pc.id_plan = p.[id_plan]
    WHERE pc.numero > 1
      AND NOT EXISTS (
          SELECT 1 FROM [suscripcion] s WHERE s.[id_plan] = p.[id_plan]
      );

    UPDATE p
    SET p.[activo] = 0
    FROM [plan] p
    WHERE NOT EXISTS (
        SELECT 1
        FROM @Alias a
        WHERE LTRIM(RTRIM(p.[nombre])) COLLATE Latin1_General_BIN2
            = a.nombre COLLATE Latin1_General_BIN2
    )
      AND EXISTS (
          SELECT 1 FROM [suscripcion] s WHERE s.[id_plan] = p.[id_plan]
      );

    DELETE p
    FROM [plan] p
    WHERE NOT EXISTS (
        SELECT 1
        FROM @Alias a
        WHERE LTRIM(RTRIM(p.[nombre])) COLLATE Latin1_General_BIN2
            = a.nombre COLLATE Latin1_General_BIN2
    )
      AND NOT EXISTS (
          SELECT 1 FROM [suscripcion] s WHERE s.[id_plan] = p.[id_plan]
      );

    DROP TABLE #PlanesClasificados;
    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF OBJECT_ID('tempdb..#PlanesClasificados') IS NOT NULL
        DROP TABLE #PlanesClasificados;
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;

SELECT [nombre], [precio], [duracion_dias], [activo]
FROM [plan]
ORDER BY [activo] DESC, [duracion_dias], [precio], [nombre];
