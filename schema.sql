DROP TABLE IF EXISTS clientes;

CREATE TABLE clientes (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    razon_social VARCHAR(150) NOT NULL,
    cuit VARCHAR(20) NOT NULL UNIQUE,
    fecha_nacimiento DATE NOT NULL,
    telefono_celular VARCHAR(30) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- STORED PROCEDURE: Search clients by name (case-insensitive)
CREATE OR REPLACE FUNCTION search_clients_by_name(p_name VARCHAR)
RETURNS SETOF clientes AS $$
BEGIN
    RETURN QUERY
    SELECT *
    FROM clientes
    WHERE nombre ILIKE '%' || p_name || '%'
       OR apellido ILIKE '%' || p_name || '%';
END;
$$ LANGUAGE plpgsql;

-- SEED DATA
INSERT INTO clientes (
    nombre, apellido, razon_social, cuit, fecha_nacimiento,
    telefono_celular, email
) VALUES
('Juan', 'Perez', 'JP Servicios SRL', '20-12345678-9', '1985-06-15', '1165874210', 'juan.perez@example.com'),
('Maria', 'Gomez', 'MG Soluciones', '27-23456789-0', '1990-09-21', '1165874221', 'maria.gomez@example.com'),
('Carlos', 'Lopez', 'CL Construcciones', '23-34567890-1', '1978-01-10', '1165874332', 'carlos.lopez@example.com'),
('Lucia', 'Martinez', 'LM Consultora', '27-45678901-2', '1992-03-05', '1165874443', 'lucia.martinez@example.com'),
('Diego', 'Fernandez', 'DF Diseno', '20-56789012-3', '1988-11-22', '1165874554', 'diego.fernandez@example.com');
