/***********************************************************************************************
 *  Script: crear_login.sql
 *  Crea el login de SQL Server que usa el backend (gym_app / GymApp123!)
 *
 *  Ejecutar este script en SQL Server (desde VS Code o SSMS) con una cuenta
 *  que tenga permisos de administrador (por ejemplo, tu usuario de Windows).
 ***********************************************************************************************/

USE master;
GO

-- Si el login ya existe, lo eliminamos para recrearlo con la contraseña correcta
IF EXISTS (SELECT * FROM sys.server_principals WHERE name = 'gym_app')
    DROP LOGIN [gym_app];
GO

-- Crear login de SQL Server
CREATE LOGIN [gym_app] WITH PASSWORD = 'GymApp123!';
GO

-- Conceder permisos en la base de datos GimnasioDB
USE GimnasioDB;
GO

IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'gym_app')
    CREATE USER [gym_app] FOR LOGIN [gym_app];
GO

ALTER ROLE db_datareader ADD MEMBER [gym_app];
ALTER ROLE db_datawriter ADD MEMBER [gym_app];
ALTER ROLE db_ddladmin ADD MEMBER [gym_app];
GO

PRINT 'Login gym_app creado con éxito en GimnasioDB.';
