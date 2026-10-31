INSERT INTO [plan] ([nombre], [descripcion], [precio], [duracion_dias], [activo]) VALUES
('Básico', 'Acceso a salas de pesas y cardio.', 70000.00, 30, 1),
('Estándar', 'Acceso completo + 2 clases grupales por semana.', 90000.00, 30, 1),
('Premium', 'Acceso ilimitado + todas las clases grupales + asesoría.', 130000.00, 30, 1),
('Anual Básico', 'Acceso a pesas y cardio por un año.', 700000.00, 365, 1),
('Anual Premium', 'Acceso ilimitado y clases por un año.', 1560000.00, 365, 1);

INSERT INTO [zona] ([nombre], [tipo], [capacidad_maxima], [activa]) VALUES
('Sala de Pesas', 'pesas', 40, 1),
('Sala de Cardio', 'cardio', 30, 1),
('Sala Grupal', 'sala_grupal', 25, 1),
('Zona Funcional', 'funcional', 20, 1);

INSERT INTO [configuracion]
    ([capacidad_maxima], [umbral_medio], [umbral_alto],
     [ventana_turno_min], [tiempo_rotacion_min], [reintentos_cobro],
     [horario_apertura], [horario_cierre])
VALUES
    (100, 60, 85, 10, 15, 3, '06:00', '22:00');
