import { createContext } from "react";

/**
 * Contexto de autenticación. Forma del valor:
 *   {
 *     usuario: { nombre, iniciales, correo, rol } | null,
 *     cargando: boolean,          // true mientras se resuelve la sesión inicial
 *     iniciarSesion(credenciales): Promise<usuario>,
 *     cerrarSesion(): Promise<void>,
 *     estaAutenticado: boolean
 *   }
 */
export const AuthContext = createContext(null);
